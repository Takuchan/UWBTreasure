package com.takuchan.uwbviaserial.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme


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
                color = ComponentsColor.Primary
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
                colors = ButtonDefaults.buttonColors(containerColor = ComponentsColor.Primary)
            ) {
                Text("保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = ComponentsColor.Primary)
            }
        },
        containerColor = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(16.dp),
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
                color = ComponentsColor.Primary
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
                colors = ButtonDefaults.buttonColors(containerColor = ComponentsColor.Primary)
            ) {
                Text("保存", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = ComponentsColor.Primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
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
                color = ComponentsColor.Primary
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
                        containerColor = ComponentsColor.Primary.copy(alpha = 0.1f)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "${currentValue}秒",
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = ComponentsColor.Primary,
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
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("-10", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = { currentValue += 10 },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("決定", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", color = MaterialTheme.colorScheme.primary)
            }
        },
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.surface
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
                color = MaterialTheme.colorScheme.error,
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
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
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
        containerColor = MaterialTheme.colorScheme.surface
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
                color = ComponentsColor.Success,
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
                    color = ComponentsColor.Success,
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
                colors = ButtonDefaults.buttonColors(containerColor = ComponentsColor.Success),
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
        containerColor = MaterialTheme.colorScheme.surface
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
                    color = MaterialTheme.colorScheme.error
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
                    containerColor = MaterialTheme.colorScheme.error
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
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Preview(showBackground = true, name = "Preview RoomSettingsDialog")
@Composable
fun PreviewRoomSettingsDialog() {
    UWBviaSerialTheme {
        RoomSettingsDialog(
            initialWidth = 10.5,
            initialHeight = 8.2,
            onDismiss = { /* do nothing */ },
            onSave = { width, height -> /* do nothing */ }
        )
    }
}

@Preview(showBackground = true, name = "Preview AnchorSettingsDialog")
@Composable
fun PreviewAnchorSettingsDialog() {
    UWBviaSerialTheme {
        AnchorSettingsDialog(
            initialDistance01 = 3.0,
            initialDistance02 = 4.5,
            initialDistance12 = 5.0,
            onDismiss = { /* do nothing */ },
            onSave = { d01, d02, d12 -> /* do nothing */ }
        )
    }
}

@Preview(showBackground = true, name = "Preview TimePickerDialog")
@Composable
fun PreviewTimePickerDialog() {
    UWBviaSerialTheme {
        TimePickerDialog(
            initialTime = 60,
            onDismiss = { /* do nothing */ },
            onConfirm = { time -> /* do nothing */ }
        )
    }
}

@Preview(showBackground = true, name = "Preview GameOverDialog")
@Composable
fun PreviewGameOverDialog() {
    UWBviaSerialTheme {
        GameOverDialog(onDismiss = { /* do nothing */ })
    }
}

@Preview(showBackground = true, name = "Preview TreasureFoundDialog")
@Composable
fun PreviewTreasureFoundDialog() {
    UWBviaSerialTheme {
        TreasureFoundDialog(onDismiss = { /* do nothing */ })
    }
}

@Preview(showBackground = true, name = "Preview ErrorDialog")
@Composable
fun PreviewErrorDialog() {
    UWBviaSerialTheme {
        ErrorDialog(
            message = "部屋の幅は0より大きく設定してください。",
            onDismiss = { /* do nothing */ }
        )
    }
}