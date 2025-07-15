package com.takuchan.uwbviaserial

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takuchan.uwbconnect.UWBConnectActivity
import com.takuchan.uwbviaserial.ui.components.GameLegend
import com.takuchan.uwbviaserial.ui.screens.MainScreen
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint // ★このアノテーションを追加
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UWBviaSerialTheme {
                val viewModel: MainActivityViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                var showSettingsDialog by remember { mutableStateOf(false) }
                var showRoomSettingsDialog by remember { mutableStateOf(false) }

                // バイブレーションサービスを取得
                val context = this
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(VIBRATOR_SERVICE) as Vibrator
                }

                if (uiState.timerFinished){
                    vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
                    viewModel.onTimerVibrated()
                    viewModel.showTimerFinishedDialog()
                } else if (uiState.remainingTime <= 10 && uiState.remainingTime > 0 && uiState.isTimerRunning) {
                    val vibrationDuration = 200L
                    val maxAmplitude = VibrationEffect.DEFAULT_AMPLITUDE
                    val amplitude = ((10 - uiState.remainingTime) / 10.0 * maxAmplitude).toInt().coerceAtLeast(1)

                    if (uiState.lastVibratedSecond != uiState.remainingTime) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            vibrator.vibrate(VibrationEffect.createOneShot(vibrationDuration, amplitude))
                        } else {
                            @Suppress("DEPRECATION")
                            vibrator.vibrate(vibrationDuration)
                        }
                        viewModel.setLastVibratedSecond(uiState.remainingTime)
                    }
                }

                val lifecycleOwner = LocalLifecycleOwner.current

                DisposableEffect(lifecycleOwner) {
                    val observer = object : DefaultLifecycleObserver {
                        override fun onResume(owner: LifecycleOwner) {
                            super.onResume(owner)
                            Log.d("MyScreen", "Screen resumed, calling ViewModel method")
                            // ここでViewModelのメソッドを呼び出す
                            viewModel.observeConnectionStatus() // 例: データの再取得など
                        }
                    }
                    lifecycleOwner.lifecycle.addObserver(observer)

                    onDispose {
                        lifecycleOwner.lifecycle.removeObserver(observer)
                    }
                }

                MainScreen(
                    uiState = uiState,
                    onSettingsClick = { showSettingsDialog = true },
                    onRoomSettingsClick = { showRoomSettingsDialog = true },
                    onStartCountdown = { viewModel.startCountdown(uiState.initialCountDown) },
                    onStatusButtonClick = {
                        val intent = Intent(this, UWBConnectActivity::class.java)
                        startActivity(intent)
                    },
                    onSetCountDownTime = { viewModel.setCountDown(it) },
                    onAnchorDistancesSave = { d01, d02, d12 -> viewModel.updateAnchorDistances(d01, d02, d12) },
                    onTimerFinishedDialogDismiss = { viewModel.hideTimerFinishedDialog() },
                    onRoomSettingsSave = { width, height -> viewModel.updateRoomSize(width, height) },
                    onAnchorPositionUpdate = { anchorIndex, x, y -> viewModel.updateAnchorPosition(anchorIndex, x, y) },
                    onErrorDialogDismiss = { viewModel.hideErrorDialog() }, // 追加
                    showSettingsDialog = showSettingsDialog,
                    showRoomSettingsDialog = showRoomSettingsDialog,
                    onSettingsDialogDismiss = { showSettingsDialog = false },
                    onRoomSettingsDialogDismiss = { showRoomSettingsDialog = false }
                )
            }
        }
    }
}




