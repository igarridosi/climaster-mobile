package com.example.climaster.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun WeatherLoadingAnimation(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "loading_anim")

    // 1. Eguzkiaren taupadak (handitu eta txikitu)
    val sunScale by transition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sun_scale"
    )

    // 2. Hodeiaren mugimendua (ezkerraldetik eskuinaldera poliki)
    val cloudOffset by transition.animateFloat(
        initialValue = -20f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_move"
    )

    // 3. Testuaren opakotasuna (Arnasketa efektua)
    val textAlpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "text_alpha"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Canvas(modifier = Modifier.size(120.dp)) {
            val width = size.width
            val height = size.height

            // --- MARRAZTU EGUZKIA ---
            val sunCenter = Offset(width * 0.6f, height * 0.4f)
            drawCircle(
                color = Color(0xFFFFC107).copy(alpha = 0.2f), // Distira
                radius = 35.dp.toPx() * sunScale,
                center = sunCenter
            )
            drawCircle(
                color = Color(0xFFFFB300), // Eguzki solidoa
                radius = 25.dp.toPx() * sunScale,
                center = sunCenter
            )

            // --- MARRAZTU HODEIA ---
            val cloudColor = Color.White
            val cloudBaseX = width * 0.3f + cloudOffset
            val cloudBaseY = height * 0.6f

            // Hodeiaren zatiak (3 zirkulu eta laukizuzen bat oinarrian)
            drawCircle(color = cloudColor, radius = 20.dp.toPx(), center = Offset(cloudBaseX, cloudBaseY))
            drawCircle(color = cloudColor, radius = 30.dp.toPx(), center = Offset(cloudBaseX + 25.dp.toPx(), cloudBaseY - 10.dp.toPx()))
            drawCircle(color = cloudColor, radius = 22.dp.toPx(), center = Offset(cloudBaseX + 55.dp.toPx(), cloudBaseY))

            // Hodeiaren oinarria laua izateko
            drawRoundRect(
                color = cloudColor,
                topLeft = Offset(cloudBaseX, cloudBaseY - 5.dp.toPx()),
                size = Size(55.dp.toPx(), 27.dp.toPx()),
                cornerRadius = CornerRadius(15.dp.toPx(), 15.dp.toPx())
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Klimaren datuak jasotzen...",
            color = Color.White.copy(alpha = textAlpha),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium
        )
    }
}