package com.climaster.app.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel // Orain honek funtzionatuko du
import com.climaster.app.presentation.components.ThermalFeedbackButtons
import com.climaster.app.presentation.theme.glassmorphic
import com.climaster.core.util.Resource
import com.climaster.domain.model.ThermalSensation
import com.climaster.domain.model.Weather
import com.example.climaster.app.presentation.dashboard.DashboardViewModel

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel() // Injekzio automatikoa
) {
    val uiState by viewModel.weatherState.collectAsState()

    // Atzeko planoko kolorea (Gradientea)
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(Color(0xFF1E3C72), Color(0xFF2A5298))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundGradient)
            .padding(16.dp)
    ) {
        when (val state = uiState) {
            is Resource.Loading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.White
                )
            }
            is Resource.Error -> {
                Text(
                    text = state.message,
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            is Resource.Success -> {
                WeatherContent(
                    weather = state.data,
                    onFeedback = { sensation ->
                        // ViewModel-era bidali ekintza
                        viewModel.submitThermalFeedback(sensation)
                    }
                )
            }
        }
    }
}

@Composable
fun WeatherContent(
    weather: Weather,
    onFeedback: (ThermalSensation) -> Unit // <--- Callback berria
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .glassmorphic(cornerRadius = 24.dp, alpha = 0.2f) // Gure modifier berria!
                .padding(32.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${weather.temperature}°C",
                    style = MaterialTheme.typography.displayLarge,
                    color = Color.White
                )
                Text(
                    text = weather.cityName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
                Text(
                    text = "Haizea: ${weather.windSpeed} km/h • Hezetasuna: ${weather.humidity}%",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. SENTSAZIO TERMIKOA (FEEDBACK) ---
        Text(
            text = "Nola sentitzen zara?",
            style = MaterialTheme.typography.titleMedium,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Gure osagai berria deitzen dugu
        ThermalFeedbackButtons(
            onFeedbackSelected = onFeedback
        )
    }
}