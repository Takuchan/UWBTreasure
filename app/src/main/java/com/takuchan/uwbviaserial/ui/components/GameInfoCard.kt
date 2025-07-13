package com.takuchan.uwbviaserial.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takuchan.uwbviaserial.MainActivityUiState
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

@Composable
fun GameInfoCard(
    uiState: MainActivityUiState,
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
            Text(
                text = "🎯 ミッション: 隠された宝物を見つけよう！",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
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


@Preview(showBackground = true)
@Composable
private fun PreviewGameInfoCard(){
    UWBviaSerialTheme {
        GameInfoCard(uiState = MainActivityUiState(), modifier = Modifier)

    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewInfoItem(){
    UWBviaSerialTheme {
        InfoItem(title = "部屋サイズ", value = "10m × 10m", icon = "🏠")
    }
}