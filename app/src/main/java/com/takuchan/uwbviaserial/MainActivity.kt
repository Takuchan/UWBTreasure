package com.takuchan.uwbviaserial

import android.R
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// MainActivity.kt
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            UWBviaSerialTheme {
                val viewModel: MainActivityViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                var showSettingsDialog by remember { mutableStateOf(false) }


                //バイブレーションサービスを取得
                val context = this
                val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    vibratorManager.defaultVibrator
                } else {
                    @Suppress("DEPRECATION")
                    context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                }

                if (uiState.timerFinished){
                    vibrator.vibrate(VibrationEffect.createOneShot(3000, VibrationEffect.DEFAULT_AMPLITUDE))
                    viewModel.onTimerVibrated() // バイブレーション発動済みフラグをリセット
                    viewModel.showTimerFinishedDialog() // アラートダイアログを表示

                } else if (uiState.remainingTime <= 10 && uiState.remainingTime > 0 && uiState.isTimerRunning) {
                    // 終了10秒前から徐々に強くバイブレーション
                    val vibrationDuration = 200L // バイブレーションの持続時間
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
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        UwbTopAppBar(onSettingsClick = { showSettingsDialog = true })
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ){
                        Box(modifier = Modifier
                            .weight(1f)
                        ) {
                            RoomView(
                                u0 = uiState.anchor0,
                                u1 = uiState.anchor1,
                                u2 = uiState.anchor2,
                                tag = uiState.tag
                            )
                        }
                        // 制限時間開始ボタンとタイマー表示を画面下部に配置
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.startCountdown(13) }, // 60秒のカウントダウンを開始
                                enabled = !uiState.isTimerRunning // タイマーが実行中でない場合のみ有効
                            ) {
                                Text("制限時間を開始")
                            }
                            Text(
                                text = "残り時間: ${uiState.remainingTime}秒",
                                fontSize = 24.sp,
                                color = if (uiState.remainingTime <= 10 && uiState.isTimerRunning) Color.Red else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }


                    if (showSettingsDialog) {
                        AnchorSettingsDialog(
                            initialDistance01 = uiState.distance01,
                            initialDistance02 = uiState.distance02,
                            initialDistance12 = uiState.distance12,
                            onDismiss = { showSettingsDialog = false },
                            onSave = { dist01, dist02,dist12 ->
                                viewModel.updateAnchorDistances(dist01, dist02,dist12)
                                showSettingsDialog = false
                            }
                        )
                    }
                    if (uiState.showTimerEndDialog) {
                        AlertDialog(
                            onDismissRequest = { viewModel.hideTimerFinishedDialog() },
                            title = { Text("時間切れ！") },
                            text = { Text("制限時間が終了しました！") },
                            confirmButton = {
                                TextButton(onClick = { viewModel.hideTimerFinishedDialog() }) {
                                    Text("OK")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UwbTopAppBar(onSettingsClick: () -> Unit) {
    TopAppBar(
        title = { Text("UWBゲーム💛") },
        actions = {
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "設定"
                )
            }
        }
    )
}

@Composable
fun RoomView(u0: UwbCoordinate, u1: UwbCoordinate, u2: UwbCoordinate, tag: UwbCoordinate) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    val anchorRadius = 8.dp
    val tagRadius = 6.dp

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,){
            Column(
                modifier = Modifier.weight(1f)
            ){
                AnchorDetail(
                    color = Color.Red,
                    text = "Anchor 0",
                    value = "(${u0.x.format(2)}, ${u0.y.format(2)})"

                )
                AnchorDetail(
                    color = Color.Green,
                    text = "Anchor 1",
                    value = "(${u1.x.format(2)}, ${u1.y.format(2)})"
                )
                AnchorDetail(
                    color = Color.Blue,
                    text = "Anchor 2",
                    value = "(${u2.x.format(2)}, ${u2.y.format(2)})"
                )
            }
            Text(text = "Location: (${tag.x.format(2)}, ${tag.y.format(2)})",
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Spacer(modifier = Modifier.height(8.dp)) // 情報表示とCanvasの間にスペース


        Box(
            modifier = Modifier
                .size(500.dp) // Fixed size for the drawing area
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale *= zoom
                        offsetX += pan.x
                        offsetY += pan.y
                    }
                }
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            ) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                val metersToPixels = 50.0f
                val centerX = canvasWidth / 2f
                val centerY = canvasHeight / 2f

                // Function to convert UwbCoordinate to Canvas Offset
                fun UwbCoordinate.toOffset(): Offset {
                    return Offset(
                        x = centerX + (this.x * metersToPixels).toFloat(),
                        y = centerY - (this.y * metersToPixels).toFloat() // Y-axis in UWB is usually up, in canvas it's down.
                    )
                }

                // Draw Anchors
                drawCircle(
                    color = Color.Red,
                    radius = anchorRadius.toPx(),
                    center = u0.toOffset()
                )
                drawCircle(
                    color = Color.Green,
                    radius = anchorRadius.toPx(),
                    center = u1.toOffset()
                )
                drawCircle(
                    color = Color.Blue,
                    radius = anchorRadius.toPx(),
                    center = u2.toOffset()
                )

                // Draw Lines between anchors (optional, but good for visualization)
                drawLine(
                    color = Color.DarkGray,
                    start = u0.toOffset(),
                    end = u1.toOffset(),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.DarkGray,
                    start = u0.toOffset(),
                    end = u2.toOffset(),
                    strokeWidth = 2f
                )
                drawLine(
                    color = Color.DarkGray,
                    start = u1.toOffset(),
                    end = u2.toOffset(),
                    strokeWidth = 2f
                )

                // Draw Tag
                drawCircle(
                    color = Color.Red,
                    radius = tagRadius.toPx(),
                    center = tag.toOffset()
                )
            }
        }
    }
}

@Composable
fun AnchorSettingsDialog(
    initialDistance01: Double,
    initialDistance02: Double,
    initialDistance12: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double, Double) -> Unit
) {
    var distance01 by remember { mutableStateOf(initialDistance01.toString()) }
    var distance02 by remember { mutableStateOf(initialDistance02.toString()) }
    var distance12 by remember { mutableStateOf(initialDistance12.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Set Anchor Distances") },
        text = {
            Column {
                TextField(
                    value = distance01,
                    onValueChange = { newValue ->
                        distance01 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("0から1のAnchor距離 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = distance02,
                    onValueChange = { newValue ->
                        distance02 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("0から2のAnchor距離(m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextField(
                    value = distance12,
                    onValueChange = { newValue ->
                        distance12 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("1から2のAnchor距離(m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val d01 = distance01.toDoubleOrNull() ?: 0.0
                    val d02 = distance02.toDoubleOrNull() ?: 0.0
                    val d12 = distance12.toDoubleOrNull() ?: 0.0
                    onSave(d01, d02,d12)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}



fun Double.format(digits: Int) = "%.${digits}f".format(this)



@Composable
fun AnchorDetail(color: Color,text: String,value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .size(16.dp)
                .background(color)
            
        ){

        }
        Spacer(modifier = Modifier.padding(4.dp))
        Column(){
            Text(text = text, style = MaterialTheme.typography.titleMedium)
            Text("座標 $value" , style = MaterialTheme.typography.bodyMedium)
        }

    }
}

@Preview(showBackground = true)
@Composable
fun AnchorSettingsDialogPreview() {
    AnchorDetail(Color.Red, "Anchor 0", "0.0")
}


// Preview functions (optional, but good practice)
@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    UWBviaSerialTheme {
        val viewModel: MainActivityViewModel = viewModel()
        val uiState by viewModel.uiState.collectAsState()
        Scaffold(
            topBar = { UwbTopAppBar(onSettingsClick = {}) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    RoomView(
                        u0 = uiState.anchor0,
                        u1 = uiState.anchor1,
                        u2 = uiState.anchor2,
                        tag = uiState.tag,
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(onClick = { /* ViewModelのスタート関数を呼び出す */ }) {
                        Text("制限時間を開始")
                    }
                    Text(
                        text = "残り時間: ${uiState.remainingTime}秒",
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }
    }
}