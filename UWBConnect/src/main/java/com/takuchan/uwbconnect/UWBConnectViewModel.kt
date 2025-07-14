package com.takuchan.opencampusuwb // パッケージ名は元のものに合わせてください

import android.app.Application
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.takuchan.uwbconnect.data.ConnectionStatus
import com.takuchan.uwbconnect.repository.SerialConnectRepository
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


data class DataEntry(
    val id: Int,
    val data: String,
    val timestamp: Long = System.currentTimeMillis()
)
// UIの状態を表すデータクラス (RepositoryとViewModelで共有)
data class UWBConnectUiState(
    val connected: Boolean = false,
    val dataList: List<DataEntry> = emptyList(),
    val statusMessage: String = "Please connect a device.",
    val availableDevices: List<UsbDevice> = emptyList(),
    val totalDataCount: Int = 0
)

@HiltViewModel
class UsbSerialViewModel @Inject constructor(
    private val repository: SerialConnectRepository,
    private val application: Application
) : ViewModel() {

    // ViewModelがUIの状態(UiState)の管理に責任を持つ
    private val _uiState = MutableStateFlow(UWBConnectUiState())
    val uiState = _uiState.asStateFlow()

    private val usbManager = application.getSystemService(Context.USB_SERVICE) as UsbManager
    private val INTENT_ACTION_GRANT_USB = "com.takuchan.uwbconnect.GRANT_USB"

    private var dataListenJob: Job? = null

    // データ管理
    private val dataList = mutableListOf<DataEntry>()
    private val MAX_DATA_COUNT = 100
    private var dataIdCounter = 0


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
                synchronized(dataList) {
                    dataList.add(
                        DataEntry(
                            id = ++dataIdCounter,
                            data = newData,
                            timestamp = System.currentTimeMillis()
                        )
                    )
                    while (dataList.size > MAX_DATA_COUNT) {
                        dataList.removeAt(0)
                    }
                    _uiState.update {
                        it.copy(
                            dataList = dataList.toList(),
                            totalDataCount = dataIdCounter
                        )
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

    fun clearData() {
        synchronized(dataList) {
            dataList.clear()
        }
        _uiState.update { it.copy(dataList = emptyList()) }
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
        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        val pendingIntent = PendingIntent.getBroadcast(application, 0, intent, flags)
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