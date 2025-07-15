package com.takuchan.uwbconnect.repository

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.hoho.android.usbserial.driver.UsbSerialPort
import com.hoho.android.usbserial.driver.UsbSerialProber
import com.hoho.android.usbserial.util.SerialInputOutputManager
import com.takuchan.uwbconnect.data.ConnectionStatus
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SerialConnectRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    private var usbSerialPort: UsbSerialPort? = null
    private var usbIoManager: SerialInputOutputManager? = null

    private val BAUD_RATE = 3000000

    // 接続状態とエラーメッセージはFlowで通知
    private val _connectionStatus = MutableStateFlow(ConnectionStatus.DISCONNECTED)
    val connectionStatus: StateFlow<ConnectionStatus> = _connectionStatus.asStateFlow()

    private val _statusMessage = MutableStateFlow("Please connect a device.")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    /**
     * デバイスリストを取得する
     */
    suspend fun refreshDeviceList(): List<UsbDevice> = withContext(Dispatchers.IO) {
        val prober = UsbSerialProber.getDefaultProber()
        return@withContext prober.findAllDrivers(usbManager).map { it.device }
    }

    /**
     * データ受信を監視するFlowを開始する
     * このFlowをcollectするとデータ受信が始まり、collectを止めると停止する
     */
    fun listenToSerialData(): Flow<String> = callbackFlow {
        // リスナーを再定義
        val listener = object : SerialInputOutputManager.Listener {
            override fun onNewData(data: ByteArray) {
                trySend(String(data)) // 新しいデータをFlowに流す
            }

            override fun onRunError(e: Exception) {
                _statusMessage.value = "Connection Lost: ${e.message} 😵"
                _connectionStatus.value = ConnectionStatus.ERROR
                close(e) // Flowをエラーで閉じる
            }
        }
        usbIoManager = SerialInputOutputManager(usbSerialPort, listener)
        usbIoManager?.start()

        // Flowがキャンセル（coroutineが終了）されたら呼ばれる
        awaitClose {
            usbIoManager?.stop()
            usbIoManager = null
        }
    }

    /**
     * デバイスに接続する
     */
    suspend fun connect(device: UsbDevice) = withContext(Dispatchers.IO) {
        if (_connectionStatus.value == ConnectionStatus.CONNECTED) return@withContext

        if (!usbManager.hasPermission(device)) {
            _statusMessage.value = "Permission not granted."
            _connectionStatus.value = ConnectionStatus.ERROR
            return@withContext
        }

        _connectionStatus.value = ConnectionStatus.CONNECTING
        try {
            val connection = usbManager.openDevice(device)
                ?: throw IOException("Failed to open device.")

            val driver = UsbSerialProber.getDefaultProber().probeDevice(device)
                ?: throw IOException("No serial port driver found.")

            usbSerialPort = driver.ports[0]
            usbSerialPort?.open(connection)
            usbSerialPort?.setParameters(BAUD_RATE, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE)

            _connectionStatus.value = ConnectionStatus.CONNECTED
            _statusMessage.value = "Connected! 🎉"
        } catch (e: Exception) {
            _statusMessage.value = "Connection failed: ${e.message}"
            disconnect() // 失敗したら切断処理を呼ぶ
        }
    }

    /**
     * データを送信する
     */
    suspend fun sendData(data: String) = withContext(Dispatchers.IO) {
        if (_connectionStatus.value != ConnectionStatus.CONNECTED || usbSerialPort == null) return@withContext
        try {
            usbSerialPort?.write(data.toByteArray(), 2000)
        } catch (e: IOException) {
            _statusMessage.value = "Failed to send data: ${e.message}"
            _connectionStatus.value = ConnectionStatus.ERROR
        }
    }

    /**
     * 切断する
     */
    suspend fun disconnect() = withContext(Dispatchers.IO) {
        try {
            usbSerialPort?.close()
        } catch (ignored: IOException) {
        } finally {
            usbSerialPort = null
            _connectionStatus.value = ConnectionStatus.DISCONNECTED
            if(_statusMessage.value.contains("Lost")){ // エラーでなければ通常の切断メッセージ
                _statusMessage.value = "Disconnected. 👋"
            }
        }
    }
}