package com.climaster.app.presentation.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Extension function bat sortzen dugu Modifier-entzat
fun Modifier.glassmorphic(
    cornerRadius: Dp = 16.dp,
    alpha: Float = 0.2f
): Modifier = this
    .clip(RoundedCornerShape(cornerRadius))
    .background(
        brush = Brush.verticalGradient(
            colors = listOf(
                Color.White.copy(alpha = alpha),      // Goian argiagoa
                Color.White.copy(alpha = alpha / 2)   // Behean ilunagoa
            )
        )
    )
    .border(
        width = 1.dp,
        brush = Brush.linearGradient(
            colors = listOf(
                Color.White.copy(alpha = 0.5f), // Ertz argia (argi islada)
                Color.Transparent,
                Color.White.copy(alpha = 0.2f)
            )
        ),
        shape = RoundedCornerShape(cornerRadius)
    )