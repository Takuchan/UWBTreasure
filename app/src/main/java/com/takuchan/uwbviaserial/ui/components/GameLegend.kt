package com.takuchan.uwbviaserial.ui.components

import android.R
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.takuchan.uwbviaserial.ui.theme.ComponentsColor
import com.takuchan.uwbviaserial.ui.theme.UWBviaSerialTheme

@Composable
fun GameLegend(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)),
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
                color = ComponentsColor.Primary
            )
            Spacer(modifier = Modifier.height(10.dp))

            LegendItem(color = ComponentsColor.Anchor0, text = "アンカー0")
            LegendItem(color = ComponentsColor.Anchor1, text = "アンカー1")
            LegendItem(color = ComponentsColor.Anchor2, text = "アンカー2")
            LegendItem(color = ComponentsColor.Tag, text = "現在地")
            LegendItem(color = ComponentsColor.HiddenTag, text = "隠された宝物")
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



@Preview(showBackground = true)
@Composable
private fun PreviewGameLegend(){
    UWBviaSerialTheme {
        GameLegend()
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLegendItem(){
    LegendItem(Color.Red,"アンカーポイント")
}