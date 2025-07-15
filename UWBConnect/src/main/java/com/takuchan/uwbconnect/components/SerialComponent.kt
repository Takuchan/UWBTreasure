package com.takuchan.uwbconnect.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takuchan.uwbconnect.data.SerialDevice
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme

@Composable
fun SerialItem(item: SerialDevice, onItemClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onItemClick)
    ){
        Row(
          modifier = Modifier
              .padding(16.dp)
              .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ){
            Text("🦴",style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.width(16.dp))
            Column{
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = item.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSerialItem(){
    UWBviaSerialTheme{
        SerialItem(
            item = SerialDevice("テストデバイス","テストアドレス"),
            onItemClick = {}
        )

    }
}