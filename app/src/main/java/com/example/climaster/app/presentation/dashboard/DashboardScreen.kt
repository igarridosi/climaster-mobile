package com.example.climaster.app.presentation.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.climaster.app.presentation.theme.DeepPurple
import com.climaster.app.presentation.theme.MidnightBlue
import com.climaster.app.presentation.theme.RoyalBlue
import com.climaster.app.presentation.theme.SkyBlue
import com.climaster.app.presentation.theme.TextWhite
import com.climaster.app.presentation.theme.glassmorphic
import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.example.climaster.app.presentation.components.AnimatedWeatherBackground
import com.example.climaster.app.presentation.components.LocationSearchDialog
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.weatherState.collectAsState()
    val recommendation by viewModel.recommendationText.collectAsState()

    // UX FEEDBACK-ARAKO ALDAGAIAK
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // KOKAPEN BILATZAILEAREN EGOERA (Berria)
    var showLocationSearch by remember { mutableStateOf(false) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(MidnightBlue, RoyalBlue, DeepPurple)
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent // Atzeko planoa mantentzeko
    ) { paddingValues ->
        // DIALOG-A ERAKUTSI BALDIN BADA EGIA (Berria)
        if (showLocationSearch) {
            LocationSearchDialog(
                onDismiss = { showLocationSearch = false },
                onCitySelected = { cityName ->
                    viewModel.searchCity(cityName)
                }
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundBrush)
                .padding(paddingValues)
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
                    Box(modifier = Modifier.fillMaxSize()) {
                        // 1. ANIMAZIOA
                        AnimatedWeatherBackground(condition = state.data.condition)

                        // 2. EDUKIA
                        WeatherDashboardContent(
                            weather = state.data,
                            recommendation = recommendation,
                            onFeedback = { sensation ->
                                // 1. ViewModel-ari abisatu
                                viewModel.submitThermalFeedback(sensation)

                                // 2. UX FEEDBACK: Erabiltzaileari mezua erakutsi
                                coroutineScope.launch {
                                    val text = if(sensation.name == "COLD") "Uff, hotza! Gorde da." else "Beroa erregistratu da."
                                    // Aurreko mezua ezkutatu berrira pasatzeko
                                    snackbarHostState.currentSnackbarData?.dismiss()
                                    snackbarHostState.showSnackbar(
                                        message = text,
                                        duration = SnackbarDuration.Short
                                    )
                                }
                            },
                            onLocationClick = { showLocationSearch = true }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDashboardContent(
    weather: Weather,
    recommendation: String,
    onFeedback: (com.climaster.domain.model.ThermalSensation) -> Unit,
    onLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- 1. GOIBURUA ---
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .glassmorphic(cornerRadius = 50.dp, alpha = 0.1f)
                .clickable { onLocationClick() }
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = SkyBlue)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = weather.cityName,
                style = MaterialTheme.typography.titleMedium,
                color = TextWhite,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- GOMENDIOA (IA MOTORRA) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(cornerRadius = 16.dp, alpha = 0.2f)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "💡 $recommendation",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. HERO SECTION ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

        // --- 3. DETAILS GRID ---
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

        // --- 4. INTERAKZIOA ---
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