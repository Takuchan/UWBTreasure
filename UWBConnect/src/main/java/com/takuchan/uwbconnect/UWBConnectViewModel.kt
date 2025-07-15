package com.takuchan.uwbconnect

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takuchan.uwbconnect.data.ConnectionStatus
import com.takuchan.uwbconnect.data.TrilaterationResult
import com.takuchan.uwbconnect.repository.ExchangeUWBDataParser
import com.takuchan.uwbconnect.repository.SerialConnectRepository
import com.takuchan.uwbconnect.repository.UwbDataRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// UIの状態を表すデータクラス (RepositoryとViewModelで共有)
data class UWBConnectUiState(
    val connected: Boolean = false,
    val statusMessage: String = "Please connect a device.",
    val availableDevices: List<UsbDevice> = emptyList(),
    val totalDataCount: Int = 0,
    val trilaterationResult: TrilaterationResult? = null
)

@HiltViewModel
class UsbSerialViewModel @Inject constructor(
    private val repository: SerialConnectRepository,
    private val uwbDataRepository: UwbDataRepository,
    private val application: Application
) : ViewModel() {

    // ViewModelがUIの状態(UiState)の管理に責任を持つ
    private val _uiState = MutableStateFlow(UWBConnectUiState())
    val uiState = _uiState.asStateFlow()

    private val usbManager = application.getSystemService(Context.USB_SERVICE) as UsbManager
    private val INTENT_ACTION_GRANT_USB = "com.takuchan.uwbconnect.GRANT_USB"

    private var dataListenJob: Job? = null

    private val uwbParser = ExchangeUWBDataParser()


    init {
        // Repositoryからの状態変化を監視
        observeConnectionStatus()
        observeStatusMessage()
        refreshDeviceList() // 初期化時にデバイスリストを更新


    }

    private fun observeConnectionStatus() {
        repository.connectionStatus
            .onEach { status ->
                _uiState.update { it.copy(connected = status == ConnectionStatus.CONNECTED) }
                // 接続されたらデータ受信を開始し、そうでなければ停止する
                if (status == ConnectionStatus.CONNECTED) {
                    startListeningData()
                } else {
                    stopListeningData()
                }
            }
            .launchIn(viewModelScope)
    }

    private fun observeStatusMessage() {
        repository.statusMessage
            .onEach { message -> _uiState.update { it.copy(statusMessage = message) } }
            .launchIn(viewModelScope)
    }

    private fun startListeningData() {
        // すでに実行中なら何もしない
        if (dataListenJob?.isActive == true) return

        dataListenJob = repository.listenToSerialData()
            .onEach { newData ->
                val ansiRegex = Regex("""\u001B\[[0-9;]*m""")
                val cleanedData = newData.replace(ansiRegex, "")

                uwbParser.parseLine(cleanedData)
                val readyDataList = uwbParser.getTrilaterationData() // デフォルトで[0, 1, 2]を要求

                if (readyDataList != null) {
                    // 3つのアンカーデータを格納した結果オブジェクトを作成
                    val result = TrilaterationResult(
                        anchor0 = readyDataList.first { it.id == 0 },
                        anchor1 = readyDataList.first { it.id == 1 },
                        anchor2 = readyDataList.first { it.id == 2 }
                    )

                    Log.d("result11", result.toString())
                    viewModelScope.launch {
                        uwbDataRepository.updateResult(result)
                    }
                }

            }
            .catch { e ->
                // Flowがエラーで終了した場合の処理
                _uiState.update { it.copy(statusMessage = "Data listening error: ${e.message}") }
            }
            .launchIn(viewModelScope)
    }

    private fun stopListeningData() {
        dataListenJob?.cancel()
        dataListenJob = null
    }

    //接続しているシリアル通信のデバイスを検知
    fun refreshDeviceList() {
        viewModelScope.launch {
            val devices = repository.refreshDeviceList()
            _uiState.update { it.copy(availableDevices = devices) }
        }
    }

    fun connect(device: UsbDevice) {
        if (usbManager.hasPermission(device)) {
            viewModelScope.launch {
                repository.connect(device)
            }
        } else {
            requestUsbPermission(device)
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            repository.disconnect()
        }
    }

    fun sendData(data: String) {
        viewModelScope.launch {
            repository.sendData(data)
        }
    }


    // --- パーミッション関連 ---
    private val permissionReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (INTENT_ACTION_GRANT_USB == intent.action) {
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    val device: UsbDevice? = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
                    device?.let { connect(it) } // 許可されたら接続
                } else {
                    _uiState.update { it.copy(statusMessage = "Permission denied.") }
                }
            }
        }
    }

    private fun requestUsbPermission(device: UsbDevice) {
        val intent = Intent(INTENT_ACTION_GRANT_USB)

        // Android S (API 31) 以降では FLAG_IMMUTABLE を使用する
        // 古いバージョンとの互換性を保ちつつ、必要に応じて FLAG_UPDATE_CURRENT も追加
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // M (API 23) 以降でフラグの組み合わせが重要になることが多い
            // FLAG_IMMUTABLE は API 23 (M) で追加されたため、それより古い場合は0
            // S (API 31) 以降では FLAG_IMMUTABLE が必須または推奨
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT // または他の適切なフラグ (例: FLAG_ONE_SHOT, FLAG_NO_CREATE)
            }
        } else {
            0 // M (API 23) より古いバージョンの場合
        }

        // よりシンプルで一般的な修正方法（推奨）:
        // Android S 以降は FLAG_IMMUTABLE を追加。それ以外は既存のフラグを維持。
        val updatedFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT // 必要に応じて他のフラグも追加
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT // S未満の場合は既存のフラグをそのまま使う（または0など適切なもの）
        }

        val pendingIntent = PendingIntent.getBroadcast(application, 0, intent, updatedFlags)
        usbManager.requestPermission(device, pendingIntent)
    }
    init {
        val filter = IntentFilter(INTENT_ACTION_GRANT_USB)
        ContextCompat.registerReceiver(application, permissionReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    override fun onCleared() {
        super.onCleared()
        application.unregisterReceiver(permissionReceiver)
        // ViewModelが破棄されるときに切断処理を確実に行う
        disconnect()
    }
}