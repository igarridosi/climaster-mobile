package com.example.climaster.app.presentation.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.climaster.app.presentation.components.ThermalFeedbackButtons
import com.climaster.app.presentation.theme.* // Ziurtatu GlassmorphismModifier hemen dagoela
import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.example.climaster.app.presentation.dashboard.DashboardViewModel
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.weatherState.collectAsState()

    // Atzeko plano gradientea (Goiz/Gau arabera aldatu daiteke etorkizunean)
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(MidnightBlue, RoyalBlue, DeepPurple)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .statusBarsPadding() // Goiko barra errespetatzeko
    ) {
        when (val state = uiState) {
            is Resource.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = SkyBlue
                )
            }
            is Resource.Error -> {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Errorea datuak kargatzean", color = Color.Red)
                    Text(text = state.message, color = TextWhite, style = MaterialTheme.typography.bodySmall)
                    Button(onClick = { viewModel.loadWeather(43.31, -1.98) }) {
                        Text("Saiatu berriro")
                    }
                }
            }
            is Resource.Success -> {
                WeatherDashboardContent(
                    weather = state.data,
                    onFeedback = { viewModel.submitThermalFeedback(it) }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDashboardContent(
    weather: Weather,
    onFeedback: (com.climaster.domain.model.ThermalSensation) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()), // Pantaila txikietan scroll egiteko
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. GOIBURUA (KOKAPENA) ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .glassmorphic(cornerRadius = 50.dp, alpha = 0.1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = SkyBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = weather.cityName, // "Europe/Madrid" bada ere, hemen agertuko da
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. HERO SECTION (TENPERATURA) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // Egunen batean hemen Lottie animazio bat jarriko dugu
            Text(
                text = "${weather.temperature.toInt()}°",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 100.sp,
                    fontWeight = FontWeight.Bold
                ),
                color = TextWhite
            )
            Text(
                text = weather.condition.name,
                style = MaterialTheme.typography.headlineMedium,
                color = SkyBlue
            )
            Text(
                text = "Eguneratua: ${weather.lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm"))}",
                style = MaterialTheme.typography.bodySmall,
                color = TextWhite.copy(alpha = 0.6f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. DETAILS GRID (GLASS CARDS) ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            DetailItem(
                icon = Icons.Rounded.WaterDrop,
                label = "Hezetasuna",
                value = "${weather.humidity}%"
            )
            DetailItem(
                icon = Icons.Rounded.Air,
                label = "Haizea",
                value = "${weather.windSpeed} km/h"
            )
        }

        Spacer(modifier = Modifier.height(48.dp))

        // --- 4. INTERAKZIOA (FEEDBACK) ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Sentsazio Termikoa doitu",
                style = MaterialTheme.typography.labelLarge,
                color = TextWhite.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            ThermalFeedbackButtons(onFeedbackSelected = onFeedback)
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(110.dp)
            .glassmorphic(cornerRadius = 20.dp, alpha = 0.15f)
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = SkyBlue, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = TextWhite, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextWhite.copy(alpha = 0.7f))
    }
}