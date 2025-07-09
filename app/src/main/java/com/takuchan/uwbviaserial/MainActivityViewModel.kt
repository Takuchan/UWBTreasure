package com.takuchan.uwbviaserial

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

// MainActivityViewModel.kt
data class MainActivityUiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,
    val anchor0: UwbCoordinate = UwbCoordinate(0.0, 0.0), // Anchor 0 is at the origin
    val anchor1: UwbCoordinate = UwbCoordinate(1.0, 0.0), // Anchor 1 on x-axis
    val anchor2: UwbCoordinate = UwbCoordinate(0.0, 1.0), // Anchor 2 on y-axis initially
    val tag: UwbCoordinate = UwbCoordinate(0.5, 0.5), // Example tag position
    val distance01: Double = 6.0, // Initial distance between Anchor 0 and 1
    val distance02: Double = 6.0,  // Initial distance between Anchor 0 and 2
    val distance12: Double = 6.0,   // NEW: Initial distance between Anchor 1 and 2

    // 制限時間関連のプロパティ
    val remainingTime: Int = 0,
    val isTimerRunning: Boolean = false,
    val timerFinished: Boolean = false, // タイマーが終了したことを示すフラグ
    val showTimerEndDialog: Boolean = false, // タイマー終了ダイアログの表示フラグ
    val lastVibratedSecond: Int = -1 // 最後にバイブレーションした秒数を記録
)

class MainActivityViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null // カウントダウンコルーチンを管理するためのJob

    init {
        // Initial calculation of anchor positions based on default distances
        calculateAnchorPositions(
            _uiState.value.distance01,
            _uiState.value.distance02,
            _uiState.value.distance12
        )
    }

    /**
     * Updates the distances between anchors and recalculates their positions.
     * @param dist01 Distance between Anchor 0 and Anchor 1.
     * @param dist02 Distance between Anchor 0 and Anchor 2.
     * @param dist12 Distance between Anchor 1 and Anchor 2.
     */
    fun updateAnchorDistances(dist01: Double, dist02: Double, dist12: Double) {
        _uiState.value = _uiState.value.copy(
            distance01 = dist01,
            distance02 = dist02,
            distance12 = dist12
        )
        calculateAnchorPositions(dist01, dist02, dist12)
    }

    /**
     * Calculates the positions of Anchor 1 and Anchor 2 based on the distances between anchors.
     * Anchor 0 is fixed at (0,0,0).
     * Anchor 1 is fixed at (distance01, 0, 0).
     * Anchor 2's position is calculated using distances from Anchor 0 and Anchor 1 (trilateration for 2D).
     *
     * @param d01 Distance between Anchor 0 and Anchor 1.
     * @param d02 Distance between Anchor 0 and Anchor 2.
     * @param d12 Distance between Anchor 1 and Anchor 2.
     */
    private fun calculateAnchorPositions(d01: Double, d02: Double, d12: Double) {
        val newAnchor0 = UwbCoordinate(0.0, 0.0)
        val newAnchor1 = UwbCoordinate(d01, 0.0)

        // Calculate Anchor 2 (x2, y2) using distances to Anchor 0 and Anchor 1
        // Using the formula for 2D trilateration:
        // A2_x = (d02^2 - d12^2 + d01^2) / (2 * d01)
        val x2 = if (d01 != 0.0) (d02 * d02 - d12 * d12 + d01 * d01) / (2 * d01) else 0.0

        // A2_y = sqrt(d02^2 - A2_x^2)
        // We need to handle potential floating point inaccuracies or impossible triangles.
        // If (d02^2 - x2^2) is negative due to invalid distances (e.g., triangle inequality violation),
        // y2 would be NaN. In such cases, we might want to default to 0 or log an error.
        val y2Squared = d02 * d02 - x2 * x2
        val y2 = if (y2Squared >= 0) sqrt(y2Squared) else 0.0 // Ensure non-negative for sqrt

        // We choose the positive y-coordinate for Anchor 2 for a consistent layout.
        val newAnchor2 = UwbCoordinate(x2, y2)

        val currentTag = _uiState.value.tag

        _uiState.value = _uiState.value.copy(
            anchor0 = newAnchor0,
            anchor1 = newAnchor1,
            anchor2 = newAnchor2,
            tag = currentTag // Tag position is static for now
        )
    }

    // Placeholder for simulating tag movement (you'd replace this with actual UWB data)
    fun updateTagPosition(newX: Double, newY: Double) {
        _uiState.value = _uiState.value.copy(
            tag = UwbCoordinate(newX, newY)
        )
    }

    /**
     * 指定された時間でカウントダウンを開始します。
     * @param totalSeconds カウントダウンする秒数。
     */
    fun startCountdown(totalSeconds: Int) {
        // 既存のカウントダウンがあればキャンセル
        countdownJob?.cancel()

        // 状態を初期化してカウントダウンを開始
        _uiState.value = _uiState.value.copy(
            remainingTime = totalSeconds,
            isTimerRunning = true,
            timerFinished = false,
            showTimerEndDialog = false,
            lastVibratedSecond = -1
        )

        countdownJob = viewModelScope.launch {
            for (i in totalSeconds downTo 0) {
                _uiState.value = _uiState.value.copy(remainingTime = i)
                if (i == 0) {
                    _uiState.value = _uiState.value.copy(
                        isTimerRunning = false,
                        timerFinished = true // タイマー終了フラグを立てる
                    )
                }
                delay(1000L) // 1秒待機
            }
        }
    }

    /**
     * タイマー終了時のバイブレーションが発動したことをViewModelに通知します。
     * これにより、`timerFinished`フラグがリセットされ、バイブレーションが一度だけ発動するようになります。
     */
    fun onTimerVibrated() {
        _uiState.value = _uiState.value.copy(timerFinished = false)
    }

    /**
     * タイマー終了ダイアログを表示します。
     */
    fun showTimerFinishedDialog() {
        _uiState.value = _uiState.value.copy(showTimerEndDialog = true)
    }

    /**
     * タイマー終了ダイアログを非表示にします。
     */
    fun hideTimerFinishedDialog() {
        _uiState.value = _uiState.value.copy(showTimerEndDialog = false)
    }

    /**
     * 最後にバイブレーションした秒数を設定します。
     * これにより、同じ秒数で複数回バイブレーションが発動するのを防ぎます。
     */
    fun setLastVibratedSecond(second: Int) {
        _uiState.value = _uiState.value.copy(lastVibratedSecond = second)
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel() // ViewModelが破棄されるときにコルーチンをキャンセル
    }
}