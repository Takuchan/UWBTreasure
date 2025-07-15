package com.takuchan.uwbconnect.screens

import android.hardware.usb.UsbDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.takuchan.uwbconnect.UWBConnectUiState
import com.takuchan.uwbconnect.UsbSerialViewModel
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme

@Composable
fun SerialSelectScreen(
    // ViewModelをHiltから注入
    viewModel: UsbSerialViewModel = hiltViewModel()
) {
    // ViewModelのUiStateをライフサイクルを考慮して安全に収集
    // このためには build.gradle に implementation "androidx.lifecycle:lifecycle-runtime-compose:2.6.1" などが必要です
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SerialSelectContent(
        uiState = uiState,
        onDeviceSelected = { device ->
            viewModel.connect(device)
        },
        onDisconnectClicked = {
            viewModel.disconnect()
        }
    )
}

@Composable
private fun SerialSelectContent(
    uiState: UWBConnectUiState,
    onDeviceSelected: (UsbDevice) -> Unit,
    onDisconnectClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = uiState.statusMessage,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
        )

        // 接続状態に応じてUIを切り替え
        if (uiState.connected) {
            ConnectedView(onDisconnectClicked = onDisconnectClicked)
        } else {
            DeviceListView(
                devices = uiState.availableDevices,
                onDeviceSelected = onDeviceSelected
            )
        }
    }
}

@Composable
private fun DeviceListView(
    devices: List<UsbDevice>,
    onDeviceSelected: (UsbDevice) -> Unit
) {
    if (devices.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No USB devices found.")
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(devices, key = { it.deviceId }) { device ->
                DeviceItem(device = device, onClick = { onDeviceSelected(device) })
            }
        }
    }
}

@Composable
private fun DeviceItem(
    device: UsbDevice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Build, contentDescription = "USB Device")
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    text = device.productName ?: "Unknown Device",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Vendor ID: ${device.vendorId} | Product ID: ${device.productId}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ConnectedView(
    onDisconnectClicked: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Successfully Connected!", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.padding(16.dp))
        OutlinedButton(onClick = onDisconnectClicked) {
            Text("Disconnect")
        }
    }
}


@Preview(showBackground = true)
@Composable
private fun PreviewSerialSelectScreen() {
    UWBviaSerialTheme {
        // プレビュー用にダミーのデータを渡す
        val previewState = UWBConnectUiState(
            connected = false,
            availableDevices = emptyList(), // ここを空にするか、モックオブジェクトを作る必要がある
            statusMessage = "Select a device from the list."
        )
        SerialSelectContent(
            uiState = previewState,
            onDeviceSelected = {},
            onDisconnectClicked = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewConnectedScreen() {
    UWBviaSerialTheme {
        val previewState = UWBConnectUiState(
            connected = true,
            statusMessage = "Connected! 🎉"
        )
        SerialSelectContent(
            uiState = previewState,
            onDeviceSelected = {},
            onDisconnectClicked = {}
        )
    }
}
