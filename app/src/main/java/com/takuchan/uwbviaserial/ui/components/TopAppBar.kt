package com.takuchan.uwbviaserial.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasureHuntTopBar(
    onSettingsClick: () -> Unit,
    onRoomSettingsClick: () -> Unit,
    onStatusButtonClick: () -> Unit,
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🏴‍☠️ 宝探しゲーム",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        actions = {
            OutlinedButton(
                onClick = onStatusButtonClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("PAUSE")
            }
            IconButton(onClick = onRoomSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "部屋設定",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "設定",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

@Preview(showBackground = true, widthDp = 412)
@Composable
fun TreasureHuntTopBarPreview() {

    UWBviaSerialTheme {
        TreasureHuntTopBar(
            onSettingsClick = {},
            onRoomSettingsClick = {},
            onStatusButtonClick = {}
        )
    }

}