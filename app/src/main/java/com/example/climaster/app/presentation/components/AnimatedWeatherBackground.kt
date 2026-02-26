package com.example.climaster.app.presentation.components

import android.R.attr.left
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
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
fun AnimatedWeatherBackground(condition: WeatherCondition, isNight: Boolean, modifier: Modifier = Modifier) {
    when (condition) {
        WeatherCondition.OSKARBIA -> ClearScene(modifier, isNight)
        WeatherCondition.HODEITARTEAK -> PartlyCloudyScene(modifier, isNight) // BERRIA
        WeatherCondition.HODEITSUA -> CloudyScene(modifier)
        WeatherCondition.EURITSUA -> RainyScene(modifier)
        WeatherCondition.EURIELURRA -> SleetScene(modifier) // BERRIA
        WeatherCondition.EKAITSUA -> StormyScene(modifier) // BERRIA
        WeatherCondition.ELURTSUA -> SnowyScene(modifier)
        WeatherCondition.HAIZETSUA -> WindyScene(modifier) // BERRIA
        WeatherCondition.LAINOTSUA -> FoggyScene(modifier)
        else -> { /* Ezer ez (garbia) */ }
    }
}

/**
 * ☀️ EGUZKITSUA: Diseinu minimalista. Eguzki handi bat behealdean poliki distiratzen.
 */
@Composable
private fun ClearScene(modifier: Modifier = Modifier, isNight: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f, targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = tween(4000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse"
    )

    val glowColor = if (isNight) Color(0xFFE0E0E0).copy(alpha = 0.15f) else Color(0xFFFFC107).copy(alpha = 0.15f)
    val coreColor = if (isNight) Color(0xFFF5F5F5).copy(alpha = 0.9f) else Color(0xFFFFB300).copy(alpha = 0.8f)

    Canvas(modifier = modifier.fillMaxSize()) {
        val radius = size.width * 0.35f // <--- TXIKIAGOA EGIN DUGU

        // <--- GOI-ESKUBIAN KOKATU DUGU (X=%85, Y=%15)
        val center = Offset(size.width * 0.95f, size.height * 0.05f)

        drawCircle(color = glowColor, radius = radius * pulse * 1.3f, center = center)
        drawCircle(color = coreColor, radius = radius * pulse, center = center)
    }
}

// BERRIA: Eguzkia azpian + Hodeiak goian

@Composable
private fun PartlyCloudyScene(modifier: Modifier = Modifier, isNight: Boolean) {
    ClearScene(modifier, isNight) // Eguzkia kargatu
    CloudyScene(modifier) // Hodeiak gainean kargatu
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

// BERRIA: Euria eta Elurra batera
@Composable
private fun SleetScene(modifier: Modifier = Modifier) {
    RainyScene(modifier) // Tanta gardenak
    SnowyScene(modifier) // Maluta zuriak
}

// BERRIA: Ekaitza (Euria + Tximista kolpeak)
@Composable
private fun StormyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "lightning")
    val flash by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 5000 // 5 segundoro tximista
                0.0f at 0
                0.0f at 4800
                0.8f at 4850 // Distira zuri indartsua
                0.0f at 4900
                0.4f at 4950 // Bigarren distira txikia
                0.0f at 5000
            },
            repeatMode = RepeatMode.Restart
        ), label = "flash"
    )

    Box(modifier = modifier.fillMaxSize()) {
        // Tximistaren argia atzeko planoan
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawRect(color = Color.White.copy(alpha = flash), size = size)
        }
        RainyScene() // Euri indartsua gainean
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

// BERRIA: Haizea (Marra horizontal azkarrak)
@Composable
private fun WindyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "wind")
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(1500, easing = LinearEasing), repeatMode = RepeatMode.Restart), label = "phase"
    )
    val particles = remember { List(30) { Particle(Random.nextFloat(), Random.nextFloat(), Random.nextFloat() * 2f + 1f, Random.nextFloat() * 20f + 10f) } }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { p ->
            val x = ((p.x + (phase * p.speed)) % 1f) * size.width
            val y = p.yOffset * size.height
            drawLine(
                color = Color.White.copy(alpha = 0.2f),
                start = Offset(x, y),
                end = Offset(x + p.size * 5f, y), // Marra luze horizontalak
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )
        }
    }
}

// BERRIA: Behe-lainoa (Zirkulu erraldoi opakuegiak geldirik ia)
@Composable
private fun FoggyScene(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "fog")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f, targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = tween(5000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(color = Color(0xFFCCCCCC).copy(alpha = 0.35f), radius = size.width * 1.5f * pulse, center = Offset(size.width * 0.2f, size.height * 0.8f))
        drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.width * pulse, center = Offset(size.width * 0.8f, size.height * 0.3f))
    }
}

// Laguntzaile datu egitura
private data class Particle(val x: Float, val yOffset: Float, val speed: Float, val size: Float)