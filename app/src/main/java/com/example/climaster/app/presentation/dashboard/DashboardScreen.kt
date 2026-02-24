package com.example.climaster.app.presentation.dashboard

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.climaster.app.presentation.components.ThermalFeedbackButtons
import com.climaster.app.presentation.theme.glassmorphic
import com.climaster.core.util.Resource
import com.climaster.domain.model.Weather
import com.climaster.domain.model.WeatherCondition
import com.example.climaster.app.presentation.components.AnimatedWeatherBackground
import com.example.climaster.app.presentation.components.LocationSearchOverlay
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.launch

// ---------------------------------------------------------
// KOLORE DINAMIKOEN FUNTZIO MAGIKOA (EGURALDIA + ORDUA)
// ---------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
fun getDynamicWeatherColors(condition: WeatherCondition, time: LocalDateTime): List<Color> {
    val hour = time.hour
    val isMorning = hour in 6..12
    val isAfternoon = hour in 13..19

    return when (condition) {
        WeatherCondition.SUNNY -> {
            when {
                isMorning -> listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)) // Goiza: Urdin argi distiratsua
                isAfternoon -> listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)) // Ilunabarra: Laranja epela
                else -> listOf(Color(0xFF141E30), Color(0xFF243B55)) // Gaua: Urdin espaziala
            }
        }
        WeatherCondition.CLOUDY -> {
            when {
                isMorning || isAfternoon -> listOf(Color(0xFFbdc3c7), Color(0xFF2c3e50)) // Eguna: Gris urdinxka
                else -> listOf(Color(0xFF0f2027), Color(0xFF203a43)) // Gaua: Gris oso iluna
            }
        }
        WeatherCondition.RAINY, WeatherCondition.STORMY -> {
            listOf(Color(0xFF4b6cb7), Color(0xFF182848)) // Euria: Urdin goibela eta iluna
        }
        WeatherCondition.SNOWY -> {
            when {
                isMorning || isAfternoon -> listOf(Color(0xFFC3DAFD), Color(0xFFA3B9D2)) // Eguna: Zuri/Urdin izoztua
                else -> listOf(Color(0xFF37474F), Color(0xFF000000)) // Gaua: Elur iluna
            }
        }
        else -> listOf(Color(0xFF1A237E), Color(0xFF311B92)) // Lehenetsia
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.weatherState.collectAsState()
    val recommendation by viewModel.recommendationText.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    var showLocationSearch by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 1. ANIMATE BACKGROUND COLORS (Klima eta orduaren arabera)
    val targetColors = if (uiState is Resource.Success) {
        val weather = (uiState as Resource.Success).data
        getDynamicWeatherColors(weather.condition, weather.lastUpdated)
    } else {
        listOf(Color(0xFF141E30), Color(0xFF243B55)) // Defektuz gaua kargatzean
    }

    // Trantsizio leuna kolore batetik bestera (Adibidez Bilbotik Tokyora pasatzean)
    val colorTop by animateColorAsState(targetValue = targetColors[0], animationSpec = tween(1500), label = "topColor")
    val colorBottom by animateColorAsState(targetValue = targetColors[1], animationSpec = tween(1500), label = "bottomColor")
    val backgroundBrush = Brush.verticalGradient(colors = listOf(colorTop, colorBottom))

    // 2. BLUR ANIMAZIOA
    val blurRadius by animateDpAsState(
        targetValue = if (showLocationSearch) 20.dp else 0.dp,
        animationSpec = tween(durationMillis = 300),
        label = "blur_anim"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            // ATZEKO PLANOA (Kolore animatuak eta Blur-a)
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .blur(radius = blurRadius)
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is Resource.Loading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    }
                    is Resource.Error -> { /* Errorea */ }
                    is Resource.Success -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedWeatherBackground(condition = state.data.condition)
                            WeatherDashboardContent(
                                weather = state.data,
                                recommendation = recommendation,
                                onFeedback = { sensation ->
                                    viewModel.submitThermalFeedback(sensation)
                                    coroutineScope.launch {
                                        snackbarHostState.currentSnackbarData?.dismiss()
                                        snackbarHostState.showSnackbar("Gordeta!")
                                    }
                                },
                                onLocationClick = { showLocationSearch = true }
                            )
                        }
                    }
                }
            }

            // 3. BILATZAILEA ANIMAZIO "SMOOTH COOL" BATEKIN SARTZEN DA
            AnimatedVisibility(
                visible = showLocationSearch,
                // ENTER: Behetik pixka bat igo, handitu eta agertu modu leunean
                enter = slideInVertically(
                    initialOffsetY = { it / 8 }, // Pantailaren %12tik gora hasten da
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 400)
                ) + scaleIn(
                    initialScale = 0.9f,
                    animationSpec = tween(durationMillis = 500, easing = FastOutSlowInEasing)
                ),
                // EXIT: Behera jaitsi pixka bat, txikitu eta desagertu modu azkarragoan
                exit = slideOutVertically(
                    targetOffsetY = { it / 8 },
                    animationSpec = tween(durationMillis = 350, easing = FastOutLinearInEasing)
                ) + fadeOut(
                    animationSpec = tween(durationMillis = 250)
                ) + scaleOut(
                    targetScale = 0.9f,
                    animationSpec = tween(durationMillis = 350)
                )
            ) {
                LocationSearchOverlay(
                    searchQuery = searchQuery,
                    searchResults = searchResults,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onDismiss = { showLocationSearch = false },
                    onLocationSelected = { name, lat, lon ->
                        viewModel.selectLocation(name, lat, lon)
                        showLocationSearch = false
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDashboardContent(
    weather: Weather, recommendation: String, onFeedback: (com.climaster.domain.model.ThermalSensation) -> Unit, onLocationClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.glassmorphic(cornerRadius = 50.dp, alpha = 0.1f).clickable { onLocationClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = weather.cityName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Box(
            modifier = Modifier.fillMaxWidth().glassmorphic(cornerRadius = 16.dp, alpha = 0.2f).padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "💡 $recommendation", style = MaterialTheme.typography.bodyLarge, color = Color.White, fontWeight = FontWeight.Medium)
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${weather.temperature.toInt()}°", style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp, fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = weather.condition.name, style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha=0.9f))
            Text(text = "Eguneratua: ${weather.lastUpdated.format(DateTimeFormatter.ofPattern("HH:mm"))}", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            DetailItem(icon = Icons.Rounded.WaterDrop, label = "Hezetasuna", value = "${weather.humidity}%")
            DetailItem(icon = Icons.Rounded.Air, label = "Haizea", value = "${weather.windSpeed} km/h")
        }
        Spacer(modifier = Modifier.height(48.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Sentsazio Termikoa doitu", style = MaterialTheme.typography.labelLarge, color = Color.White.copy(alpha = 0.8f))
            Spacer(modifier = Modifier.height(16.dp))
            ThermalFeedbackButtons(onFeedbackSelected = onFeedback)
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(110.dp).glassmorphic(cornerRadius = 20.dp, alpha = 0.15f).padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f))
    }
}