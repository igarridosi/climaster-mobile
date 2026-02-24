package com.example.climaster.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import com.climaster.domain.model.WeatherCondition
import kotlin.random.Random

/**
 * Klima bakoitzerako eszena pertsonalizatua bideratzen duen osagai nagusia.
 */
@Composable
fun AnimatedWeatherBackground(condition: WeatherCondition, modifier: Modifier = Modifier) {
    when (condition) {
        WeatherCondition.EGUZKITSUA -> SunnyScene(modifier)
        WeatherCondition.HODEITSUA -> CloudyScene(modifier)
        WeatherCondition.EURITSUA, WeatherCondition.EKAITSUA -> RainyScene(modifier)
        WeatherCondition.ELURTSUA -> SnowyScene(modifier)
        else -> { /* Ezer ez (garbia) */ }
    }
}

/**
 * ☀️ EGUZKITSUA: Diseinu minimalista. Eguzki handi bat behealdean poliki distiratzen.
 */
@Composable
private fun SunnyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_pulse")
    // Pultsu efektua: 0.95 eta 1.05 artean tamaina aldatzen
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val sunRadius = width * 0.6f // Eguzki oso handia
        val sunCenter = Offset(width / 6f, height) // Pantailaren beheko erdialdean

        // 1. Eguzkiaren Koroa (Distira gardena)
        drawCircle(
            color = Color(0xFFFFC107).copy(alpha = 0.15f),
            radius = sunRadius * pulse * 1.2f,
            center = sunCenter
        )

        // 2. Eguzki Nagusia (Solidoagoa)
        drawCircle(
            color = Color(0xFFFFB300).copy(alpha = 0.8f),
            radius = sunRadius * pulse,
            center = sunCenter
        )
    }
}

/**
 * ☁️ LAINOTUA: Zirkulu erdi-garden handiak (hodeiak bezala) ezkerraldetik eskuinaldera mugitzen.
 */
@Composable
private fun CloudyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "clouds")
    val move by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing), // Oso motel
            repeatMode = RepeatMode.Restart
        ),
        label = "move"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // Hodei 1 (Goian)
        drawCircle(
            color = Color.White.copy(alpha = 0.1f),
            radius = width * 0.4f,
            center = Offset((move * width * 2) - width * 0.5f, height * 0.2f)
        )
        // Hodei 2 (Erdian)
        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = width * 0.3f,
            center = Offset(((move + 0.5f) % 1f * width * 2) - width * 0.5f, height * 0.5f)
        )
    }
}

/**
 * 🌧️ EURIA: Euri tantak goitik behera abiadura ezberdinetan.
 */
@Composable
private fun RainyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "rain")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember { List(70) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 1f + 0.5f, Random.nextFloat() * 3f + 3f) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = ((p.yOffset + (phase * p.speed)) % 1f) * size.height
            val x = p.x * size.width
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = Offset(x, y),
                end = Offset(x, y + p.size * 8f),
                strokeWidth = p.size / 2f,
                cap = StrokeCap.Round
            )
        }
    }
}

/**
 * ❄️ ELURRA: Elur malutak poliki eta sigi-saga erortzen.
 */
@Composable
private fun SnowyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "snow")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "phase"
    )

    val particles = remember { List(50) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 0.3f + 0.2f, Random.nextFloat() * 4f + 2f) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val y = ((p.yOffset + (phase * p.speed)) % 1f) * size.height
            val x = (p.x * size.width) + (Math.sin(phase * Math.PI * 4 + p.yOffset * 10) * 30f).toFloat()
            drawCircle(
                color = Color.White.copy(alpha = 0.6f),
                radius = p.size,
                center = Offset(x, y)
            )
        }
    }
}

// Laguntzaile datu egitura
private data class Particle(val x: Float, val yOffset: Float, val speed: Float, val size: Float)