package com.takuchan.uwbviaserial.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Button
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takuchan.uwbviaserial.MainActivityUiState
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

@Composable
fun GameControlPanel(
    uiState: MainActivityUiState,
    onTimeSettingClick: () -> Unit,
    onStartGame: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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
            remainingTime <= 10 && isRunning -> MaterialTheme.colorScheme.error
            isRunning -> ComponentsColor.Success
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


@Preview(showBackground = true)
@Composable
private fun PreviewGameControlPanel(){
    UWBviaSerialTheme {
        GameControlPanel(
            uiState = MainActivityUiState(),
            onTimeSettingClick = {},
            onStartGame = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewTimerDisplay(){
    UWBviaSerialTheme {
        TimerDisplay(
            remainingTime = 10,
            isRunning = true
        )

    }
}