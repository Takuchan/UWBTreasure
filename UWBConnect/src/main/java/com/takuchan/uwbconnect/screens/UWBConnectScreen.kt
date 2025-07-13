package com.takuchan.uwbconnect.screens

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UWBConnectScreen(onFinish:() -> Unit){
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("🔨UWBの接続設定") },
                navigationIcon = {
                    IconButton(onClick = onFinish) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "戻る"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {},
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Text("再検出")
            }
        },
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surface
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Text(
                text="スマホ本体にType2BP/Type2DKのデバイスを接続し、該当のデバイス名を選択してください。表示されない場合は「再検出」ボタンを押してください。",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
            SerialSelectScreen()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewUWBConnectScreen(){
    UWBviaSerialTheme {
        UWBConnectScreen(
            onFinish = {}
        )
    }
}