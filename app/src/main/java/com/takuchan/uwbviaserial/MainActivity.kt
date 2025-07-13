package com.takuchan.uwbviaserial

import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

// カラーパレット
object GameColors {
    val Primary = Color(0xFFFF6B35) // オレンジ
    val Secondary = Color(0xFF4ECDC4) // ターコイズ
    val Accent = Color(0xFFFFE66D) // イエロー
    val Success = Color(0xFF95E1A3) // グリーン
    val Warning = Color(0xFFFF8A80) // レッド
    val Background = Color(0xFFF7F9FC) // ライトグレー
    val Surface = Color.White

    val Anchor0 = Color(0xFFFF6B6B) // 明るい赤
    val Anchor1 = Color(0xFF4ECDC4) // ターコイズ
    val Anchor2 = Color(0xFF45B7D1) // 青
    val Tag = Color(0xFFFFD93D) // 明るい黄色
    val HiddenTag = Color(0xFFE74C3C) // 隠されたUWB
}

// MainActivity class部分の更新
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

                MainScreen(
                    uiState = uiState,
                    onSettingsClick = { showSettingsDialog = true },
                    onRoomSettingsClick = { showRoomSettingsDialog = true },
                    onStartCountdown = { viewModel.startCountdown(uiState.initialCountDown) },
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

// MainActivity.ktの一部分のみ更新（MainScreen関数）
@Composable
fun MainScreen(
    uiState: MainActivityUiState,
    onSettingsClick: () -> Unit,
    onRoomSettingsClick: () -> Unit,
    onStartCountdown: () -> Unit,
    onSetCountDownTime: (Int) -> Unit,
    onAnchorDistancesSave: (Double, Double, Double) -> Unit,
    onTimerFinishedDialogDismiss: () -> Unit,
    onRoomSettingsSave: (Double, Double) -> Unit,
    onAnchorPositionUpdate: (Int, Double, Double) -> Unit,
    onErrorDialogDismiss: () -> Unit, // 追加
    showSettingsDialog: Boolean,
    showRoomSettingsDialog: Boolean,
    onSettingsDialogDismiss: () -> Unit,
    onRoomSettingsDialogDismiss: () -> Unit
) {
    var showNumberPickerDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TreasureHuntTopBar(
                onSettingsClick = onSettingsClick,
                onRoomSettingsClick = onRoomSettingsClick
            )
        },
        containerColor = GameColors.Background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // ゲーム情報カード
            GameInfoCard(
                uiState = uiState,
                modifier = Modifier.padding(16.dp)
            )

            // メインゲーム画面
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                GameRoomView(
                    uiState = uiState,
                    onAnchorPositionUpdate = onAnchorPositionUpdate
                )
            }

            // コントロールパネル
            GameControlPanel(
                uiState = uiState,
                onTimeSettingClick = { showNumberPickerDialog = true },
                onStartGame = onStartCountdown,
                modifier = Modifier.padding(16.dp)
            )
        }

        // ダイアログ類
        if (showSettingsDialog) {
            AnchorSettingsDialog(
                initialDistance01 = uiState.distance01,
                initialDistance02 = uiState.distance02,
                initialDistance12 = uiState.distance12,
                onDismiss = onSettingsDialogDismiss,
                onSave = { dist01, dist02, dist12 ->
                    onAnchorDistancesSave(dist01, dist02, dist12)
                    onSettingsDialogDismiss()
                }
            )
        }

        if (showRoomSettingsDialog) {
            RoomSettingsDialog(
                initialWidth = uiState.roomWidth,
                initialHeight = uiState.roomHeight,
                onDismiss = onRoomSettingsDialogDismiss,
                onSave = { width, height ->
                    onRoomSettingsSave(width, height)
                    onRoomSettingsDialogDismiss()
                }
            )
        }

        if (showNumberPickerDialog) {
            TimePickerDialog(
                initialTime = uiState.initialCountDown,
                onDismiss = { showNumberPickerDialog = false },
                onConfirm = { time ->
                    onSetCountDownTime(time)
                    showNumberPickerDialog = false
                }
            )
        }

        if (uiState.showTimerEndDialog) {
            GameOverDialog(
                onDismiss = onTimerFinishedDialogDismiss
            )
        }

        // エラーダイアログを追加
        if (uiState.showErrorDialog) {
            ErrorDialog(
                message = uiState.errorMessage ?: "エラーが発生しました",
                onDismiss = onErrorDialogDismiss
            )
        }
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreasureHuntTopBar(
    onSettingsClick: () -> Unit,
    onRoomSettingsClick: () -> Unit
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
                onClick = {

                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.Background,
                    contentColor = GameColors.Primary
                )
            ) {
                Text("DISCONNECTED")
            }
            IconButton(onClick = onRoomSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.LocationOn,
                    contentDescription = "部屋設定",
                    tint = GameColors.Primary
                )
            }
            IconButton(onClick = onSettingsClick) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "設定",
                    tint = GameColors.Primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = GameColors.Surface
        )
    )
}

@Composable
fun GameInfoCard(
    uiState: MainActivityUiState,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GameColors.Surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "🎯 ミッション: 隠された宝物を見つけよう！",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(
                    title = "部屋サイズ",
                    value = "${uiState.roomWidth.format(1)}m × ${uiState.roomHeight.format(1)}m",
                    icon = "🏠"
                )
                InfoItem(
                    title = "現在地",
                    value = "(${uiState.tag.x.format(2)}, ${uiState.tag.y.format(2)})",
                    icon = "📍"
                )
            }
        }
    }
}

@Composable
fun InfoItem(
    title: String,
    value: String,
    icon: String
) {
    Column {
        Text(
            text = "$icon $title",
            fontSize = 12.sp,
            color = Color.Gray
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun GameRoomView(
    uiState: MainActivityUiState,
    onAnchorPositionUpdate: (Int, Double, Double) -> Unit
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = GameColors.Surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoom, _ ->
                        scale = (scale * zoom).coerceIn(0.5f, 3.0f)
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
                drawGameRoom(uiState)
            }

            // レジェンド
            GameLegend(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            )
        }
    }
}

fun DrawScope.drawGameRoom(uiState: MainActivityUiState) {
    val canvasWidth = size.width
    val canvasHeight = size.height
    val padding = 40.dp.toPx()

    // 描画エリアの計算
    val availableWidth = canvasWidth - 2 * padding
    val availableHeight = canvasHeight - 2 * padding

    // スケールの計算
    val scaleX = availableWidth / uiState.roomWidth.toFloat()
    val scaleY = availableHeight / uiState.roomHeight.toFloat()
    val scale = minOf(scaleX, scaleY)

    // 中央に配置するためのオフセット
    val roomPixelWidth = uiState.roomWidth.toFloat() * scale
    val roomPixelHeight = uiState.roomHeight.toFloat() * scale
    val startX = padding + (availableWidth - roomPixelWidth) / 2
    val startY = padding + (availableHeight - roomPixelHeight) / 2

    // 部屋の境界線を描画
    drawRect(
        color = GameColors.Primary,
        topLeft = Offset(startX, startY),
        size = androidx.compose.ui.geometry.Size(roomPixelWidth, roomPixelHeight),
        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 4.dp.toPx())
    )



    // 座標変換関数
    fun coordToPixel(x: Double, y: Double): Offset {
        return Offset(
            startX + (x * scale).toFloat(),
            startY + roomPixelHeight - (y * scale).toFloat() // Y軸を反転
        )
    }

    // アンカー間の線を描画
    drawAnchorConnections(uiState, ::coordToPixel)

    // アンカーを描画
    drawAnchor(uiState.anchor0, GameColors.Anchor0, "0", ::coordToPixel)
    drawAnchor(uiState.anchor1, GameColors.Anchor1, "1", ::coordToPixel)
    drawAnchor(uiState.anchor2, GameColors.Anchor2, "2", ::coordToPixel)

    // タグを描画
    drawTag(uiState.tag, ::coordToPixel)

    // 隠されたUWBを描画（点滅効果付き）
    if (uiState.isTimerRunning) {
        drawHiddenTreasure(uiState.hiddenTag, ::coordToPixel)
    }
}


fun DrawScope.drawAnchorConnections(
    uiState: MainActivityUiState,
    coordToPixel: (Double, Double) -> Offset
) {
    val connectionColor = Color.Gray.copy(alpha = 0.1f)
    val strokeWidth = 2.dp.toPx()

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor0.x, uiState.anchor0.y),
        end = coordToPixel(uiState.anchor1.x, uiState.anchor1.y),
        strokeWidth = strokeWidth
    )

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor0.x, uiState.anchor0.y),
        end = coordToPixel(uiState.anchor2.x, uiState.anchor2.y),
        strokeWidth = strokeWidth
    )

    drawLine(
        color = connectionColor,
        start = coordToPixel(uiState.anchor1.x, uiState.anchor1.y),
        end = coordToPixel(uiState.anchor2.x, uiState.anchor2.y),
        strokeWidth = strokeWidth
    )
}

fun DrawScope.drawAnchor(
    anchor: UwbCoordinate,
    color: Color,
    label: String,
    coordToPixel: (Double, Double) -> Offset
) {
    val center = coordToPixel(anchor.x, anchor.y)
    val radius = 16.dp.toPx()

    // 外側の円
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )

    // 内側の円
    drawCircle(
        color = Color.White,
        radius = radius * 0.6f,
        center = center
    )
}

fun DrawScope.drawTag(
    tag: UwbCoordinate,
    coordToPixel: (Double, Double) -> Offset
) {
    val center = coordToPixel(tag.x, tag.y)
    val radius = 12.dp.toPx()

    // 外側の円（グロー効果）
    drawCircle(
        color = GameColors.Tag.copy(alpha = 0.3f),
        radius = radius * 1.5f,
        center = center
    )

    // メインの円
    drawCircle(
        color = GameColors.Tag,
        radius = radius,
        center = center
    )

    // 内側の円
    drawCircle(
        color = Color.White,
        radius = radius * 0.5f,
        center = center
    )
}

fun DrawScope.drawHiddenTreasure(
    hiddenTag: UwbCoordinate,
    coordToPixel: (Double, Double) -> Offset
) {
    val center = coordToPixel(hiddenTag.x, hiddenTag.y)
    val radius = 20.dp.toPx()

    // 宝箱のような形で描画
    drawCircle(
        color = GameColors.HiddenTag,
        radius = radius,
        center = center
    )

    // 宝箱のハイライト
    drawCircle(
        color = Color.White.copy(alpha = 0.3f),
        radius = radius * 0.7f,
        center = center
    )
}

@Composable
fun GameLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = GameColors.Surface.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = "凡例",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            LegendItem(color = GameColors.Anchor0, text = "アンカー0")
            LegendItem(color = GameColors.Anchor1, text = "アンカー1")
            LegendItem(color = GameColors.Anchor2, text = "アンカー2")
            LegendItem(color = GameColors.Tag, text = "現在地")
            LegendItem(color = GameColors.HiddenTag, text = "隠された宝物")
        }
    }
}

@Composable
fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 10.sp,
            color = Color.Black
        )
    }
}

@Composable
fun GameControlPanel(
    uiState: MainActivityUiState,
    onTimeSettingClick: () -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GameColors.Surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // タイマー表示
            TimerDisplay(
                remainingTime = uiState.remainingTime,
                isRunning = uiState.isTimerRunning,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // コントロールボタン
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OutlinedButton(
                    onClick = onTimeSettingClick,
                    enabled = !uiState.isTimerRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GameColors.Primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("時間設定")
                }

                Spacer(modifier = Modifier.width(16.dp))

                Button(
                    onClick = onStartGame,
                    enabled = !uiState.isTimerRunning,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameColors.Primary
                    )
                ) {
                    Text(
                        text = if (uiState.isTimerRunning) "ゲーム中..." else "🎮 ゲーム開始",
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun TimerDisplay(
    remainingTime: Int,
    isRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val timerColor by animateColorAsState(
        targetValue = when {
            remainingTime <= 10 && isRunning -> GameColors.Warning
            isRunning -> GameColors.Success
            else -> Color.Gray
        },
        animationSpec = tween(300)
    )

    val scale by animateFloatAsState(
        targetValue = if (remainingTime <= 10 && isRunning) 1.1f else 1f,
        animationSpec = tween(300)
    )

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = timerColor.copy(alpha = 0.1f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(scale),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⏰ 残り時間",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "${remainingTime}秒",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = timerColor,
                textAlign = TextAlign.Center
            )
        }
    }
}
@Composable
fun RoomSettingsDialog(
    initialWidth: Double,
    initialHeight: Double,
    onDismiss: () -> Unit,
    onSave: (Double, Double) -> Unit
) {
    var width by remember { mutableStateOf(initialWidth.toString()) }
    var height by remember { mutableStateOf(initialHeight.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🏠 部屋の設定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary
            )
        },
        text = {
            Column {
                Text(
                    text = "部屋のサイズを設定してください",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = width,
                    onValueChange = { newValue ->
                        width = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("幅 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = height,
                    onValueChange = { newValue ->
                        height = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("高さ (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val w = width.toDoubleOrNull() ?: 10.0
                    val h = height.toDoubleOrNull() ?: 8.0
                    onSave(w, h)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Primary)
            ) {
                Text("保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = GameColors.Primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
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
        title = {
            Text(
                text = "⚓ アンカー設定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary
            )
        },
        text = {
            Column {
                Text(
                    text = "アンカー間の距離を設定してください",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = distance01,
                    onValueChange = { newValue ->
                        distance01 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("アンカー0 - アンカー1 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = distance02,
                    onValueChange = { newValue ->
                        distance02 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("アンカー0 - アンカー2 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = distance12,
                    onValueChange = { newValue ->
                        distance12 = newValue.filter { it.isDigit() || it == '.' }
                    },
                    label = { Text("アンカー1 - アンカー2 (m)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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
                    onSave(d01, d02, d12)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Primary)
            ) {
                Text("保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = GameColors.Primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
}

@Composable
fun TimePickerDialog(
    initialTime: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int) -> Unit
) {
    var currentValue by remember { mutableStateOf(initialTime) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⏰ 制限時間設定",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Primary
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "制限時間を設定してください",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = GameColors.Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${currentValue}秒",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = GameColors.Primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Button(
                        onClick = { currentValue = (currentValue - 10).coerceAtLeast(10) },
                        modifier = Modifier.size(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameColors.Secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("-10", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { currentValue = (currentValue - 1).coerceAtLeast(1) },
                        modifier = Modifier.size(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameColors.Secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("-1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { currentValue += 1 },
                        modifier = Modifier.size(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+1", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { currentValue += 10 },
                        modifier = Modifier.size(60.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GameColors.Primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("+10", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(currentValue) },
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Primary)
            ) {
                Text("決定", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = GameColors.Primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
}

@Composable
fun GameOverDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "⏰ 時間切れ！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Warning,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🏴‍☠️",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "残念！宝物を見つけることができませんでした。",
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "もう一度チャレンジしてみましょう！",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = GameColors.Primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎮 もう一度遊ぶ",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
}

@Composable
fun TreasureFoundDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "🎉 宝物発見！",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = GameColors.Success,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "💎",
                    fontSize = 64.sp,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "おめでとうございます！",
                    fontSize = 18.sp,
                    textAlign = TextAlign.Center,
                    color = GameColors.Success,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "隠された宝物を見つけることができました！",
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = GameColors.Success),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "🎮 次のゲーム",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
}

// 拡張関数
fun Double.format(digits: Int) = "%.${digits}f".format(this)

// GameDialogs.ktにエラーダイアログを追加

@Composable
fun ErrorDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "⚠️",
                    fontSize = 24.sp,
                    modifier = Modifier.padding(end = 8.dp)
                )
                Text(
                    text = "設定エラー",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = GameColors.Warning
                )
            }
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = Color.Black,
                lineHeight = 20.sp
            )
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    containerColor = GameColors.Warning
                )
            ) {
                Text(
                    text = "OK",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = GameColors.Surface
    )
}