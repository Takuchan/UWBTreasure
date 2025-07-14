package com.takuchan.uwbconnect

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.takuchan.uwbconnect.screens.SerialSelectScreen
import com.takuchan.uwbconnect.screens.UWBConnectScreen
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // Hiltのエントリーポイントとして指定
class UWBConnectActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UWBviaSerialTheme {
                UWBConnectScreen(
                    onFinish = {finish()}
                )
            }
        }
    }
}






@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    UWBviaSerialTheme {
    }
}