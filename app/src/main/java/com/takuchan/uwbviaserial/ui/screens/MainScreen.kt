package com.takuchan.uwbviaserial.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takuchan.uwbviaserial.GameState
import com.takuchan.uwbviaserial.MainActivityUiState
import com.takuchan.uwbviaserial.ui.components.AnchorSettingsDialog
import com.takuchan.uwbviaserial.ui.components.ErrorDialog
import com.takuchan.uwbviaserial.ui.components.GameControlPanel
import com.takuchan.uwbviaserial.ui.components.GameInfoCard
import com.takuchan.uwbviaserial.ui.components.GameOverDialog
import com.takuchan.uwbviaserial.ui.components.GameRoomView
import com.takuchan.uwbviaserial.ui.components.RoomSettingsDialog
import com.takuchan.uwbviaserial.ui.components.TimePickerDialog
import com.takuchan.uwbviaserial.ui.components.TreasureFoundDialog
import com.takuchan.uwbviaserial.ui.components.TreasureHuntTopBar
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

// MainActivity.ktの一部分のみ更新（MainScreen関数）
@Composable
fun MainScreen(
    uiState: MainActivityUiState,
    onSettingsClick: () -> Unit,
    onRoomSettingsClick: () -> Unit,
    onStatusButtonClick: () -> Unit,
    onStartCountdown: () -> Unit,
    onSetCountDownTime: (Int) -> Unit,
    onAnchorDistancesSave: (Double, Double, Double) -> Unit,
    onTimerFinishedDialogDismiss: () -> Unit,
    onFinishedGame: () -> Unit,
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
                onRoomSettingsClick = onRoomSettingsClick,
                onStatusButtonClick = onStatusButtonClick
            )
        },
        containerColor = MaterialTheme.colorScheme.background
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

        if (uiState.showTreasureFoundDialog) {
            TreasureFoundDialog(
                onDismiss = onFinishedGame
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

@Preview(showBackground = true)
@Composable
private fun PreviewMainScreen(){
    UWBviaSerialTheme {
        MainScreen(
            uiState = MainActivityUiState(),
            onSettingsClick = {},
            onRoomSettingsClick = {},
            onStartCountdown = {},
            onSetCountDownTime = {},
            onAnchorDistancesSave = { _, _, _ -> },
            onTimerFinishedDialogDismiss = {},
            onRoomSettingsSave = { _, _ -> },
            onAnchorPositionUpdate = { _, _, _ -> },
            onErrorDialogDismiss = {},
            showSettingsDialog = false,
            showRoomSettingsDialog = false,
            onSettingsDialogDismiss = {},
            onRoomSettingsDialogDismiss = {},
            onStatusButtonClick = {},
            onFinishedGame = {  }
        )
    }
}