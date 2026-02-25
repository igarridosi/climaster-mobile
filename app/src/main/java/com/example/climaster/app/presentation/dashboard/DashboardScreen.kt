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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Air
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
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
import com.climaster.domain.model.AiInsight
import com.climaster.domain.model.ThermalSensation
import com.climaster.domain.model.Weather
import com.climaster.domain.model.WeatherCondition
import com.example.climaster.app.presentation.components.AnimatedWeatherBackground
import com.example.climaster.app.presentation.components.LocationSearchOverlay
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import kotlinx.coroutines.launch
import com.example.climaster.app.presentation.components.WeatherLoadingAnimation
import com.example.climaster.app.presentation.components.shimmerEffect

// ---------------------------------------------------------
// KOLORE DINAMIKOEN FUNTZIO MAGIKOA (EGURALDIA + ORDUA)
// ---------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
fun getDynamicWeatherColors(condition: WeatherCondition, time: LocalDateTime): List<Color> {
    val hour = time.hour
    val isMorning = hour in 6..16
    val isAfternoon = hour in 17..19

    return when (condition) {
        WeatherCondition.EGUZKITSUA -> {
            when {
                isMorning -> listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)) // Goiza: Urdin argi distiratsua
                isAfternoon -> listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)) // Ilunabarra: Laranja epela
                else -> listOf(Color(0xFF141E30), Color(0xFF243B55)) // Gaua: Urdin espaziala
            }
        }
        WeatherCondition.HODEITSUA -> {
            when {
                isMorning || isAfternoon -> listOf(Color(0xFFbdc3c7), Color(0xFF2c3e50)) // Eguna: Gris urdinxka
                else -> listOf(Color(0xFF0f2027), Color(0xFF203a43)) // Gaua: Gris oso iluna
            }
        }
        WeatherCondition.EURITSUA, WeatherCondition.EKAITSUA -> {
            listOf(Color(0xFF4b6cb7), Color(0xFF182848)) // Euria: Urdin goibela eta iluna
        }
        WeatherCondition.ELURTSUA -> {
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
    val recommendationState by viewModel.recommendationState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()

    // ---------------------------------------------------------
    // KOREOGRAFIA ANIMAZIOA (Staggered Animation States)
    // ---------------------------------------------------------
    var showLocationSearch by remember { mutableStateOf(false) } // Logika orokorra
    var isBlurred by remember { mutableStateOf(false) }          // Blur-aren egoera
    var isOverlayVisible by remember { mutableStateOf(false) }   // Txartelaren egoera

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // Hemen gertatzen da magia: Denborak banatzen ditugu.
    LaunchedEffect(showLocationSearch) {
        if (showLocationSearch) {
            isBlurred = true           // 1. Hasi lausotzen atzeko planoa
            delay(200)                 // 2. Itxaron 200 milisegundo Blur-a finkatu arte
            isOverlayVisible = true    // 3. Atera txartela garbi-garbi
        } else {
            isOverlayVisible = false   // 1. Ezkutatu txartela lehenik
            delay(350)                 // 2. Itxaron txartelaren animazioa (350ms) amaitu arte
            isBlurred = false          // 3. Argi ezazu atzeko planoa
        }
    }

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

    val chatAnswerState by viewModel.chatAnswerState.collectAsState()
    var showChatOverlay by remember { mutableStateOf(false) }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        floatingActionButton = {
            // MAGIC FAB BOTOIA
            FloatingActionButton(
                onClick = { showChatOverlay = true },
                containerColor = Color(0xFF4FC3F7), // Zeru urdina
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = "Galdetu Agenteari")
            }
        }
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
                        WeatherLoadingAnimation(modifier = Modifier.align(Alignment.Center))
                    }
                    is Resource.Error -> { /* Errorea */ }
                    is Resource.Success -> {
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedWeatherBackground(condition = state.data.condition)
                            WeatherDashboardContent(
                                weather = state.data,
                                recommendationState = recommendationState,
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
                visible = isOverlayVisible,
                // ENTER: Behetik pixka bat igo, handitu eta agertu modu leunean
                enter = slideInVertically(
                    initialOffsetY = { it / 8 }, // Pantailaren %12tik gora hasten da
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                ) + fadeIn(
                    animationSpec = tween(durationMillis = 200)
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
            // 4. TXAT OVERLAY (Ask the Agent)
            AnimatedVisibility(
                visible = showChatOverlay,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
            ) {
                AskAgentOverlay(
                    answerState = chatAnswerState,
                    onAsk = { question -> viewModel.askAgent(question) },
                    onDismiss = {
                        showChatOverlay = false
                        viewModel.clearChat()
                    }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDashboardContent(
    weather: Weather,
    recommendationState: Resource<AiInsight>,
    onFeedback: (ThermalSensation) -> Unit,
    onLocationClick: () -> Unit
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
        // --- GOMENDIOA ETA BIZIMODUA (IA MOTORRA) ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .glassmorphic(cornerRadius = 24.dp, alpha = 0.2f)
                .padding(20.dp)
        ) {
            when (recommendationState) {
                is Resource.Loading -> {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(16.dp))
                        Row {
                            Box(modifier = Modifier.width(80.dp).height(24.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(modifier = Modifier.width(100.dp).height(24.dp).clip(RoundedCornerShape(12.dp)).shimmerEffect())
                        }
                    }
                }
                is Resource.Error -> {
                    Text("⚠️ AI Laguntzailea ez dago erabilgarri.", color = Color(0xFFFFB74D), fontSize = 14.sp)
                }
                is Resource.Success -> {
                    val insight = recommendationState.data
                    Column {
                        // Briefing nagusia
                        Text(
                            text = "💡 ${insight.briefing}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Action Chips (Arropa eta Ekintza)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Arropa Chip-a
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = insight.clothingIcon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = insight.clothing, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }

                            // Ekintza Chip-a
                            Row(
                                modifier = Modifier
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = insight.activityIcon, fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = insight.activity, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${weather.temperature.toInt()}°", style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp, fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = weather.condition.name, style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha=0.9f))
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

@Composable
fun AskAgentOverlay(
    answerState: Resource<String>?,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E3C72).copy(alpha=0.95f), Color(0xFF2A5298).copy(alpha=0.95f))))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(24.dp)
                .navigationBarsPadding() // Teklatua errespetatzeko
        ) {
            Text("Galdetu Agenteari ✨", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))

            // Erantzunaren kaxa
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 80.dp)
                    .glassmorphic(cornerRadius = 16.dp, alpha = 0.1f)
                    .padding(16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                when (answerState) {
                    null -> Text("Adibidez: Gaur arratsaldean hondartzara joan naiteke?", color = Color.White.copy(0.6f))
                    is Resource.Loading -> CircularProgressIndicator(color = Color(0xFF4FC3F7), modifier = Modifier.size(24.dp))
                    is Resource.Error -> Text("Errorea: ${answerState.message}", color = Color.Red)
                    is Resource.Success -> Text(answerState.data, color = Color.White, fontSize = 15.sp, lineHeight = 22.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Input eremua
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Idatzi hemen...", color = Color.White.copy(0.5f)) },
                trailingIcon = {
                    IconButton(onClick = {
                        if(text.isNotBlank()) {
                            onAsk(text)
                            text = ""
                        }
                    }) {
                        Icon(Icons.Rounded.Send, contentDescription = "Bidali", tint = Color(0xFF4FC3F7))
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4FC3F7),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF4FC3F7),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}