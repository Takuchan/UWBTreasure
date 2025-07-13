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
import kotlin.random.Random

data class MainActivityUiState(
    val isLoading: Boolean = false,
    val isConnected: Boolean = false,

    // 部屋の設定
    val roomWidth: Double = 10.0,
    val roomHeight: Double = 8.0,

    // アンカー位置
    val anchor0: UwbCoordinate = UwbCoordinate(0.0, 0.0),
    val anchor1: UwbCoordinate = UwbCoordinate(6.0, 0.0),
    val anchor2: UwbCoordinate = UwbCoordinate(3.0, 5.2),

    // プレイヤーの現在位置
    val tag: UwbCoordinate = UwbCoordinate(3.0, 2.6),

    // 隠されたUWB（宝物）の位置
    val hiddenTag: UwbCoordinate = UwbCoordinate(7.0, 6.0),

    // アンカー間の距離
    val distance01: Double = 6.0,
    val distance02: Double = 6.0,
    val distance12: Double = 6.0,

    // タイマー関連
    val remainingTime: Int = 0,
    val isTimerRunning: Boolean = false,
    val timerFinished: Boolean = false,
    val showTimerEndDialog: Boolean = false,
    val lastVibratedSecond: Int = -1,
    val initialCountDown: Int = 60,

    // ゲーム状態
    val gameState: GameState = GameState.SETUP,
    val score: Int = 0,
    val foundTreasure: Boolean = false,

    // エラー状態
    val errorMessage: String? = null,
    val showErrorDialog: Boolean = false
)

enum class GameState {
    SETUP,      // ゲーム準備中
    PLAYING,    // ゲーム中
    PAUSED,     // 一時停止
    FINISHED    // ゲーム終了
}

class MainActivityViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(MainActivityUiState())
    val uiState: StateFlow<MainActivityUiState> = _uiState.asStateFlow()

    private var countdownJob: Job? = null
    private var gameJob: Job? = null

    init {
        // 初期設定
        calculateAnchorPositions(
            _uiState.value.distance01,
            _uiState.value.distance02,
            _uiState.value.distance12
        )
        generateRandomTreasureLocation()
    }

    /**
     * 部屋のサイズを更新
     */
    fun updateRoomSize(width: Double, height: Double) {
        _uiState.value = _uiState.value.copy(
            roomWidth = width,
            roomHeight = height
        )

        // 現在のアンカー距離で位置を再計算
        val currentState = _uiState.value
        calculateAnchorPositions(
            currentState.distance01,
            currentState.distance02,
            currentState.distance12
        )

        generateRandomTreasureLocation()
    }

    /**
     * アンカー位置を手動で更新
     */
    fun updateAnchorPosition(anchorIndex: Int, x: Double, y: Double) {
        val currentState = _uiState.value
        // 部屋の境界内に制限
        val clampedX = x.coerceIn(0.0, currentState.roomWidth)
        val clampedY = y.coerceIn(0.0, currentState.roomHeight)

        val newCoordinate = UwbCoordinate(clampedX, clampedY)

        _uiState.value = when (anchorIndex) {
            0 -> currentState.copy(anchor0 = newCoordinate)
            1 -> currentState.copy(anchor1 = newCoordinate)
            2 -> currentState.copy(anchor2 = newCoordinate)
            else -> currentState
        }

        // 位置変更後に距離を再計算
        recalculateDistancesFromPositions()
    }

    /**
     * アンカー位置から距離を再計算
     */
    private fun recalculateDistancesFromPositions() {
        val currentState = _uiState.value
        val d01 = calculateDistance(currentState.anchor0, currentState.anchor1)
        val d02 = calculateDistance(currentState.anchor0, currentState.anchor2)
        val d12 = calculateDistance(currentState.anchor1, currentState.anchor2)

        _uiState.value = _uiState.value.copy(
            distance01 = d01,
            distance02 = d02,
            distance12 = d12
        )
    }

    /**
     * アンカー間の距離を手動で更新し、位置を再計算
     */
    fun updateAnchorDistances(dist01: Double, dist02: Double, dist12: Double) {
        // 三角形の不等式をチェック
        if (!isValidTriangle(dist01, dist02, dist12)) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "無効な距離設定です。\n三角形の不等式を満たしていません。\n" +
                        "各辺の長さは他の2辺の和より小さく、差より大きくなければなりません。",
                showErrorDialog = true
            )
            return
        }

        _uiState.value = _uiState.value.copy(
            distance01 = dist01,
            distance02 = dist02,
            distance12 = dist12
        )

        // 新しい距離に基づいてアンカー位置を計算
        calculateAnchorPositions(dist01, dist02, dist12)
    }

    /**
     * 三角形の不等式をチェック
     */
    private fun isValidTriangle(a: Double, b: Double, c: Double): Boolean {
        return (a + b > c) && (a + c > b) && (b + c > a) &&
                a > 0 && b > 0 && c > 0
    }

    /**
     * 距離からアンカー位置を計算（三辺測位の逆算）
     */
    private fun calculateAnchorPositions(d01: Double, d02: Double, d12: Double) {
        try {
            // Anchor0を原点に固定
            val anchor0 = UwbCoordinate(0.0, 0.0)

            // Anchor1をX軸上に配置
            val anchor1 = UwbCoordinate(d01, 0.0)

            // Anchor2の位置を三辺測位で計算
            // 公式: x = (d01² + d02² - d12²) / (2 * d01)
            val x2 = (d01 * d01 + d02 * d02 - d12 * d12) / (2 * d01)

            // 公式: y = √(d02² - x²)
            val y2Squared = d02 * d02 - x2 * x2

            if (y2Squared < 0) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "無効な距離設定です。\n指定された距離では三角形を作ることができません。",
                    showErrorDialog = true
                )
                return
            }

            val y2 = sqrt(y2Squared)
            val anchor2 = UwbCoordinate(x2, y2)

            // 計算されたアンカー位置が部屋の範囲内かチェック
            val currentState = _uiState.value
            val maxX = maxOf(anchor0.x, anchor1.x, anchor2.x)
            val maxY = maxOf(anchor0.y, anchor1.y, anchor2.y)
            val minX = minOf(anchor0.x, anchor1.x, anchor2.x)
            val minY = minOf(anchor0.y, anchor1.y, anchor2.y)

            val requiredWidth = maxX - minX
            val requiredHeight = maxY - minY

            if (requiredWidth > currentState.roomWidth || requiredHeight > currentState.roomHeight) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "アンカーの配置が部屋のサイズを超えています。\n" +
                            "必要なサイズ: ${requiredWidth.format(2)}m × ${requiredHeight.format(2)}m\n" +
                            "現在の部屋: ${currentState.roomWidth.format(2)}m × ${currentState.roomHeight.format(2)}m\n" +
                            "部屋のサイズを大きくするか、アンカー間の距離を短くしてください。",
                    showErrorDialog = true
                )
                return
            }

            // 部屋の中央に配置するためのオフセットを計算
            val offsetX = (currentState.roomWidth - requiredWidth) / 2 - minX
            val offsetY = (currentState.roomHeight - requiredHeight) / 2 - minY

            _uiState.value = _uiState.value.copy(
                anchor0 = UwbCoordinate(anchor0.x + offsetX, anchor0.y + offsetY),
                anchor1 = UwbCoordinate(anchor1.x + offsetX, anchor1.y + offsetY),
                anchor2 = UwbCoordinate(anchor2.x + offsetX, anchor2.y + offsetY),
                errorMessage = null,
                showErrorDialog = false
            )

        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "アンカー位置の計算でエラーが発生しました。\n距離設定を確認してください。",
                showErrorDialog = true
            )
        }
    }

    /**
     * 2点間の距離を計算
     */
    private fun calculateDistance(point1: UwbCoordinate, point2: UwbCoordinate): Double {
        val dx = point1.x - point2.x
        val dy = point1.y - point2.y
        return sqrt(dx * dx + dy * dy)
    }

    /**
     * ランダムな宝物の位置を生成
     */
    private fun generateRandomTreasureLocation() {
        val currentState = _uiState.value
        val margin = 1.0

        val randomX = Random.nextDouble(margin, currentState.roomWidth - margin)
        val randomY = Random.nextDouble(margin, currentState.roomHeight - margin)

        _uiState.value = _uiState.value.copy(
            hiddenTag = UwbCoordinate(randomX, randomY)
        )
    }

    /**
     * プレイヤーの位置を更新
     */
    fun updateTagPosition(newX: Double, newY: Double) {
        val currentState = _uiState.value
        val clampedX = newX.coerceIn(0.0, currentState.roomWidth)
        val clampedY = newY.coerceIn(0.0, currentState.roomHeight)

        _uiState.value = _uiState.value.copy(
            tag = UwbCoordinate(clampedX, clampedY)
        )

        // 宝物との距離をチェック
        checkTreasureFound()
    }

    /**
     * 宝物が見つかったかチェック
     */
    private fun checkTreasureFound() {
        val currentState = _uiState.value
        val distance = calculateDistance(currentState.tag, currentState.hiddenTag)

        if (distance <= 0.5 && !currentState.foundTreasure) { // 0.5m以内
            _uiState.value = _uiState.value.copy(
                foundTreasure = true,
                gameState = GameState.FINISHED
            )
            // タイマーを停止
            countdownJob?.cancel()
            showTreasureFoundDialog()
        }
    }

    /**
     * カウントダウンを開始
     */
    fun startCountdown(totalSeconds: Int) {
        countdownJob?.cancel()

        _uiState.value = _uiState.value.copy(
            remainingTime = totalSeconds,
            isTimerRunning = true,
            timerFinished = false,
            showTimerEndDialog = false,
            lastVibratedSecond = -1,
            gameState = GameState.PLAYING,
            foundTreasure = false
        )

        // 新しい宝物の位置を生成
        generateRandomTreasureLocation()

        countdownJob = viewModelScope.launch {
            for (i in totalSeconds downTo 0) {
                _uiState.value = _uiState.value.copy(remainingTime = i)
                if (i == 0) {
                    _uiState.value = _uiState.value.copy(
                        isTimerRunning = false,
                        timerFinished = true,
                        gameState = GameState.FINISHED
                    )
                    break
                }
                delay(1000L)
            }
        }
    }

    /**
     * 制限時間を設定
     */
    fun setCountDown(time: Int) {
        _uiState.value = _uiState.value.copy(
            initialCountDown = time
        )
    }

    /**
     * タイマー終了時のバイブレーション処理
     */
    fun onTimerVibrated() {
        _uiState.value = _uiState.value.copy(timerFinished = false)
    }

    /**
     * タイマー終了ダイアログを表示
     */
    fun showTimerFinishedDialog() {
        _uiState.value = _uiState.value.copy(showTimerEndDialog = true)
    }

    /**
     * タイマー終了ダイアログを非表示
     */
    fun hideTimerFinishedDialog() {
        _uiState.value = _uiState.value.copy(showTimerEndDialog = false)
    }

    /**
     * エラーダイアログを非表示
     */
    fun hideErrorDialog() {
        _uiState.value = _uiState.value.copy(
            showErrorDialog = false,
            errorMessage = null
        )
    }

    /**
     * 宝物発見ダイアログを表示
     */
    private fun showTreasureFoundDialog() {
        // 実装はUIで行う
    }

    /**
     * 最後にバイブレーションした秒数を設定
     */
    fun setLastVibratedSecond(second: Int) {
        _uiState.value = _uiState.value.copy(lastVibratedSecond = second)
    }

    /**
     * ゲームをリセット
     */
    fun resetGame() {
        countdownJob?.cancel()
        _uiState.value = _uiState.value.copy(
            remainingTime = 0,
            isTimerRunning = false,
            timerFinished = false,
            showTimerEndDialog = false,
            gameState = GameState.SETUP,
            foundTreasure = false,
            score = 0
        )
        generateRandomTreasureLocation()
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        gameJob?.cancel()
    }
}