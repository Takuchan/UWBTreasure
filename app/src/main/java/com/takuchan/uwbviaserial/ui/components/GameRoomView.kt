package com.takuchan.uwbviaserial.ui.components

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.takuchan.uwbconnect.ui.theme.UWBviaSerialTheme
import com.takuchan.uwbviaserial.MainActivityUiState
import com.takuchan.uwbviaserial.UwbCoordinate
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor


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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

            if(uiState.proximityVibrationAnchorId == 2){
                Text(
                    text = "😯",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                )
            }else if(uiState.proximityVibrationAnchorId == 3){
                Text(
                    text = "おぉっ",
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.displayLarge
                )
            }
        }
    }
}

private fun DrawScope.drawGameRoom(uiState: MainActivityUiState) {
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
        color = ComponentsColor.Primary,
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
    drawAnchor(uiState.anchor0, ComponentsColor.Anchor0, "0", ::coordToPixel)
    drawAnchor(uiState.anchor1, ComponentsColor.Anchor1, "1", ::coordToPixel)
    drawAnchor(uiState.anchor2, ComponentsColor.Anchor2, "2", ::coordToPixel)

    // タグを描画
    drawTag(uiState.tag, ::coordToPixel)

}


private fun DrawScope.drawAnchorConnections(
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
    Log.d("tagLoc","$tag")
    val center = coordToPixel(tag.x, tag.y)
    val radius = 12.dp.toPx()

    // 外側の円（グロー効果）
    drawCircle(
        color = ComponentsColor.Tag.copy(alpha = 0.3f),
        radius = radius * 1.5f,
        center = center
    )

    // メインの円
    drawCircle(
        color = ComponentsColor.Tag,
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


@Preview(showBackground = true)
@Composable
private fun PreviewGameRoomView(){
    UWBviaSerialTheme {
        GameRoomView(
            uiState = MainActivityUiState(),
            onAnchorPositionUpdate = {it,it2,it3->

            }
        )
    }

}