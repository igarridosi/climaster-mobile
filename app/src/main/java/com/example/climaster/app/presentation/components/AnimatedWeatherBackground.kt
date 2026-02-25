package com.example.climaster.app.presentation.components

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
fun AnimatedWeatherBackground(condition: WeatherCondition, modifier: Modifier = Modifier) {
    when (condition) {
        WeatherCondition.EGUZKITSUA -> SunnyScene(modifier)
        WeatherCondition.HODEITARTEAK -> PartlyCloudyScene(modifier) // BERRIA
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

// BERRIA: Eguzkia azpian + Hodeiak goian

@Composable
private fun PartlyCloudyScene(modifier: Modifier = Modifier) {
    SunnyScene(modifier) // Eguzkia kargatu
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
        initialValue = 0.8f, targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = tween(8000, easing = FastOutSlowInEasing), repeatMode = RepeatMode.Reverse), label = "pulse"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        drawCircle(color = Color.White.copy(alpha = 0.1f), radius = size.width * 1.5f * pulse, center = Offset(size.width * 0.2f, size.height * 0.8f))
        drawCircle(color = Color.White.copy(alpha = 0.15f), radius = size.width * pulse, center = Offset(size.width * 0.8f, size.height * 0.3f))
    }
}

// Laguntzaile datu egitura
private data class Particle(val x: Float, val yOffset: Float, val speed: Float, val size: Float)