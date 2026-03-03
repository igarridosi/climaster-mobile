package com.example.climaster.app.presentation.dashboard

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.padding
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
import java.time.format.DateTimeFormatter
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.rounded.QrCodeScanner
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning

// ---------------------------------------------------------
// KOLORE DINAMIKOEN FUNTZIO MAGIKOA (EGURALDIA + ORDUA)
// ---------------------------------------------------------
@RequiresApi(Build.VERSION_CODES.O)
fun getDynamicWeatherColors(condition: WeatherCondition, time: LocalDateTime): List<Color> {
    val hour = time.hour
    val isMorning = hour in 6..16
    val isAfternoon = hour in 17..19

    return when (condition) {
        WeatherCondition.OSKARBIA -> {
            when {
                isMorning -> listOf(Color(0xFF56CCF2), Color(0xFF2F80ED)) // Goiza: Urdin argi distiratsua
                isAfternoon -> listOf(Color(0xFFFF7E5F), Color(0xFFFEB47B)) // Ilunabarra: Laranja epela
                else -> listOf(Color(0xFF141E30), Color(0xFF243B55)) // Gaua: Urdin espaziala
            }
        }
        WeatherCondition.HODEITARTEAK -> { // <--- BERRIA: Eguzkia + Hodeiak
            when {
                isMorning -> listOf(Color(0xFF8CA6DB), Color(0xFFB9935A)) // Urdin epela eta urre kolorea
                isAfternoon -> listOf(Color(0xFFD38312), Color(0xFFA83279)) // Arratsalde more-laranja
                else -> listOf(Color(0xFF2C3E50), Color(0xFF3498DB)) // Gau lasaia
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
        WeatherCondition.HAIZETSUA -> { // <--- BERRIA: Haizea
            listOf(Color(0xFFBBD2C5), Color(0xFF536976)) // Gris berdexka hegalaria
        }
        WeatherCondition.LAINOTSUA -> { // <--- BERRIA: Behe-lainoa
            listOf(Color(0xFF757F9A), Color(0xFFD7DDE8)) // Gris zilarra
        }
        else -> listOf(Color(0xFF1A237E), Color(0xFF311B92)) // Lehenetsia
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getLocalTimeInLocation(timezoneId: String): LocalDateTime {
    return try {
        val zoneId = java.time.ZoneId.of(timezoneId)
        java.time.ZonedDateTime.now(zoneId).toLocalDateTime()
    } catch (e: Exception) {
        LocalDateTime.now() // Fallback
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DashboardScreen(viewModel: DashboardViewModel = hiltViewModel()) {
    val uiState by viewModel.weatherState.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val chatAnswerState by viewModel.chatAnswerState.collectAsState()

    var showLocationSearch by remember { mutableStateOf(false) }
    var isBlurred by remember { mutableStateOf(false) }
    var isOverlayVisible by remember { mutableStateOf(false) }
    var showChatOverlay by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // BAIMEN KUDEATZAILEA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (fineLocationGranted || coarseLocationGranted) {
            // Baimena dugu! GPSa kargatu
            viewModel.loadWeatherForCurrentLocation()
        } else {
            // Baimenik ez: Defektuzko hiria kargatu
            viewModel.loadWeather(43.31, -1.98, "Donostia")
        }
    }

    // --- GOIBURUA ---
    val context = LocalContext.current
    val scanner = remember {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options)
    }

    // Aplikazioa irekitzean exekutatzen da
    LaunchedEffect(Unit) {
        permissionLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        )
    }

    // MEZUEN ENTZULE BERRIA: ViewModel-ak zerbait esatean, Snackbar-a erakutsi
    LaunchedEffect(Unit) {
        viewModel.widgetMessage.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss() // Aurrekoa kendu
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(showLocationSearch) {
        if (showLocationSearch) {
            isBlurred = true; delay(200); isOverlayVisible = true
        } else {
            isOverlayVisible = false; delay(350); isBlurred = false
        }
    }

    val targetColors = if (uiState is Resource.Success) {
        val weather = (uiState as Resource.Success).data

        // ORDU LOKALA KALKULATU
        val localTime = getLocalTimeInLocation(weather.timezone)

        // LOG bat jarri dezakezu hemen ziurtatzeko: println("Ordu Lokala: $localTime")

        getDynamicWeatherColors(weather.condition, localTime) // <--- ziurtatu localTime pasatzen duzula
    } else {
        listOf(Color(0xFF141E30), Color(0xFF243B55))
    }

    val colorTop by animateColorAsState(targetValue = targetColors[0], animationSpec = tween(1500), label = "topColor")
    val colorBottom by animateColorAsState(targetValue = targetColors[1], animationSpec = tween(1500), label = "bottomColor")
    val backgroundBrush = Brush.verticalGradient(colors = listOf(colorTop, colorBottom))

    val blurRadius by animateDpAsState(
        targetValue = if (isBlurred || showChatOverlay) 25.dp else 0.dp,
        animationSpec = tween(durationMillis = 400), label = "blur_anim"
    )

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = Color.Transparent,
        floatingActionButtonPosition = FabPosition.Center,

        floatingActionButton = {
            // BOTOI BIKOITZA (QR Ezkerrean - AI Eskubian)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 12.dp), // Tarteak ertzetatik
                horizontalArrangement = Arrangement.SpaceBetween, // Muturretara banandu
                verticalAlignment = Alignment.Bottom
            ) {
                AnimatedVisibility(
                    visible = !showChatOverlay,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    // EZKERRA: QR ESKANERRA (Urdin Iluna)
                    FloatingActionButton(
                        onClick = {
                            scanner.startScan()
                                .addOnSuccessListener { barcode ->
                                    barcode.rawValue?.let { json ->
                                        viewModel.handleScannedQr(json)
                                    }
                                }
                        },
                        containerColor = Color(0xFF1E3C72), // Urdin ilun dotorea
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Rounded.QrCodeScanner, contentDescription = "Eskaneatu")
                    }
                }

                // ESKUBIA: AI AGENTEA (Urdin Argia - Lehen bezala)
                // AnimatedVisibility mantentzen dugu txata irekitzean desagertu dadin
                AnimatedVisibility(
                    visible = !showChatOverlay,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    FloatingActionButton(
                        onClick = { showChatOverlay = true },
                        containerColor = Color(0xFF4FC3F7),
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Rounded.AutoAwesome, contentDescription = "Galdetu")
                    }
                }
            }
        }

    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .blur(radius = blurRadius)
                    .padding(paddingValues)
            ) {
                when (val state = uiState) {
                    is Resource.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = Color.White)
                    is Resource.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Arazo bat egon da 😢",
                                    color = Color.Red,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message ?: "Errore ezezaguna",
                                    color = Color.White,
                                    fontSize = 12.sp
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.loadWeather(43.31, -1.98, "Donostia") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4FC3F7))
                                ) {
                                    Text("Saiatu Berriro")
                                }
                            }
                        }
                    }
                    is Resource.Success -> {
                        val localTime = getLocalTimeInLocation(state.data.timezone)
                        val isNight = localTime.hour < 6 || localTime.hour >= 19
                        Box(modifier = Modifier.fillMaxSize()) {
                            AnimatedWeatherBackground(condition = state.data.condition, isNight = isNight)
                            WeatherDashboardContent(
                                weather = state.data,
                                localTimeStr = localTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                recommendationState = recommendationState,
                                onLocationClick = { showLocationSearch = true },
                                onQrScanned = { jsonString ->
                                    viewModel.handleScannedQr(jsonString)
                                }
                            )
                        }
                    }
                }
            }

            // Bilatzailea
            AnimatedVisibility(
                visible = isOverlayVisible,
                enter = slideInVertically(initialOffsetY = { it / 8 }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(animationSpec = tween(400)) + scaleIn(initialScale = 0.9f, animationSpec = tween(400, easing = FastOutSlowInEasing)),
                exit = slideOutVertically(targetOffsetY = { it / 8 }, animationSpec = tween(350, easing = FastOutLinearInEasing)) + fadeOut(animationSpec = tween(250)) + scaleOut(targetScale = 0.9f, animationSpec = tween(350))
            ) {
                LocationSearchOverlay(
                    searchQuery = searchQuery, searchResults = searchResults,
                    onQueryChange = { viewModel.onSearchQueryChanged(it) },
                    onDismiss = { showLocationSearch = false },
                    onLocationSelected = { name, lat, lon -> viewModel.selectLocation(name, lat, lon); showLocationSearch = false }
                )
            }


            // Txata
            AnimatedVisibility(
                visible = showChatOverlay,
                enter = slideInVertically(initialOffsetY = { it }, animationSpec = tween(400, easing = FastOutSlowInEasing)) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it }, animationSpec = tween(300)) + fadeOut()
            ) {
                val cityName = (uiState as? Resource.Success)?.data?.cityName ?: "Ezezaguna"
                AskAgentOverlay(
                    cityName = cityName, // HOBEKUNTZA 2: Hiria pasatu
                    answerState = chatAnswerState,
                    onAsk = { question -> viewModel.askAgent(question) },
                    onDismiss = { showChatOverlay = false; viewModel.clearChat() }
                )
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun WeatherDashboardContent(
    weather: Weather,
    localTimeStr: String,
    recommendationState: Resource<com.climaster.domain.model.AiInsight>,
    onLocationClick: () -> Unit,
    onQrScanned: (String) -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Goiburua eta Badge-a
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.glassmorphic(cornerRadius = 50.dp, alpha = 0.1f).clickable { onLocationClick() }.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = weather.cityName, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(6.dp))
            // Badge Dotorea Orduarekin
            Text(
                text = "Bertako ordua: $localTimeStr",
                style = MaterialTheme.typography.labelMedium,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // AI GOMENDIOA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(animationSpec = tween(400, easing = FastOutSlowInEasing)) // <--- ANIMAZIO AUTOMATIKOA
                .glassmorphic(cornerRadius = if (isExpanded) 32.dp else 24.dp, alpha = 0.2f)
                .clickable { isExpanded = !isExpanded } // KLIK EGITEAN EGOERA ALDATU
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            when (recommendationState) {
                is Resource.Loading -> {
                    Column {
                        Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEffect())
                    }
                }
                is Resource.Error -> Text(
                    text = "⚠️ ${recommendationState.message ?: "Errore ezezaguna"}",
                    color = Color(0xFFFFB74D),
                    fontSize = 12.sp,
                    lineHeight = 14.sp
                )
                is Resource.Success -> {
                    val insight = recommendationState.data
                    Column(horizontalAlignment = Alignment.Start) {
                        // BRIEFING (Beti ikusgai)
                        Text(
                            text = "💡 ${insight.briefing}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                            lineHeight = 22.sp,
                            maxLines = if (isExpanded) Int.MAX_VALUE else 3 // Zabaltzean dena erakutsi
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // CHIPS (Beti ikusgai, baina zabaltzean xehetasun gehiago izan litzakete)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Arropa Chip
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = insight.clothingIcon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = insight.clothing, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp)
                            }

                            // Ekintza Chip
                            Row(
                                modifier = Modifier
                                    .weight(1f)
                                    .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = insight.activityIcon, fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(text = insight.activity, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, lineHeight = 14.sp)
                            }
                        }

                        // --- EDUKI GEHIGARRIA (ZABALTZEAN BAKARRIK AGERTZEN DA) ---
                        if (isExpanded) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Divider(color = Color.White.copy(alpha = 0.2f))
                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Analisi Xehea:",
                                style = MaterialTheme.typography.labelLarge,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            // ORAIN AI-AREN TESTUA ERABILTZEN DUGU:
                            Text(
                                text = insight.detailedAnalysis, // <--- DINAMIKOA
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.95f),
                                lineHeight = 20.sp // Irakurgarritasuna hobetzeko
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "${weather.temperature.toInt()}°", style = MaterialTheme.typography.displayLarge.copy(fontSize = 100.sp, fontWeight = FontWeight.Bold), color = Color.White)
            Text(text = weather.condition.name, style = MaterialTheme.typography.headlineMedium, color = Color.White.copy(alpha=0.9f))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // UI Malgua (Grid-a)
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp), // Padding txiki bat kanpoan
            horizontalArrangement = Arrangement.spacedBy(16.dp) // Txartelen arteko tartea
        ) {
            DetailItem(icon = Icons.Rounded.WaterDrop, label = "Hezetasuna", value = "${weather.humidity}%", modifier = Modifier.weight(1f))
            DetailItem(icon = Icons.Rounded.Air, label = "Haizea", value = "${weather.windSpeed} km/h", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // HOBEKUNTZA 4: Orduko Iragarpena
        if (weather.hourlyForecast.isNotEmpty()) {
            Text("Gaurko Orduak", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.align(Alignment.Start))
            Spacer(modifier = Modifier.height(12.dp))
            HourlyForecastUI(hourlyList = weather.hourlyForecast)
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text("Hurrengo Egunak", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.9f), modifier = Modifier.align(Alignment.Start))
        Spacer(modifier = Modifier.height(8.dp))
        FiveDayForecastUI(forecastList = weather.forecast)
        Spacer(modifier = Modifier.height(46.dp))
    }
}

// ------------------------------------------------------------------
// OSAGAI BERRIA: 5 Eguneko Iragarpena
// ------------------------------------------------------------------
@Composable
fun FiveDayForecastUI(forecastList: List<com.climaster.domain.model.DailyForecast>) {
    // Lista hutsa bada (kargatzen ari bada oraindik)
    if (forecastList.isEmpty()) {
        CircularProgressIndicator(color = Color.White)
        return
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .glassmorphic(cornerRadius = 24.dp, alpha = 0.15f)
            .padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        forecastList.forEachIndexed { index, day ->
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = day.emoji, fontSize = 28.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = day.dayName, color = Color.White.copy(alpha=0.7f), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Text(text = "${day.maxTemp}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${day.minTemp}°", color = Color.White.copy(alpha=0.5f), fontSize = 15.sp)
                }
            }

            if (index < forecastList.size - 1) {
                Box(
                    modifier = Modifier
                        .height(40.dp)
                        .width(1.dp)
                        .background(Color.White.copy(alpha = 0.2f))
                )
            }
        }
    }
}

// OSAGAI BERRIA: Orduko Iragarpena (Scroll Horizontala)
@Composable
fun HourlyForecastUI(hourlyList: List<com.climaster.domain.model.HourlyForecast>) {
    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(hourlyList) { hour ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .glassmorphic(cornerRadius = 16.dp, alpha = 0.15f)
                    .padding(vertical = 12.dp, horizontal = 16.dp)
            ) {
                Text(text = hour.time, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = hour.emoji, fontSize = 22.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "${hour.temp}°", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

@Composable
fun DetailItem(icon: ImageVector, label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .glassmorphic(cornerRadius = 20.dp, alpha = 0.15f)
            .padding(16.dp)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.7f), maxLines = 1)
    }
}

// ------------------------------------------------------------------
// TXAT OSAGAIA: Orain Kokapen Etiketa barne du
// ------------------------------------------------------------------
@Composable
fun AskAgentOverlay(
    cityName: String, // HOBEKUNTZA 2
    answerState: Resource<String>?,
    onAsk: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }

    Box(
        modifier = Modifier.fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() },
        contentAlignment = Alignment.BottomCenter
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                .background(Brush.verticalGradient(listOf(Color(0xFF1E3C72).copy(alpha=0.95f), Color(0xFF2A5298).copy(alpha=0.95f))))
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) {}
                .padding(24.dp)
                .navigationBarsPadding()
        ) {
            // Izenburua eta Kokapen Etiketa
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Galdetu Agenteari ✨", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

                // Kokapen Etiketa (Badge)
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Rounded.LocationOn, contentDescription = null, tint = Color(0xFF4FC3F7), modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(cityName, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier.fillMaxWidth().heightIn(min = 80.dp).glassmorphic(cornerRadius = 16.dp, alpha = 0.1f).padding(16.dp),
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

            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                placeholder = { Text("Idatzi hemen...", color = Color.White.copy(0.5f)) },
                trailingIcon = {
                    IconButton(onClick = { if(text.isNotBlank()) { onAsk(text); text = "" } }) {
                        Icon(Icons.Rounded.Send, contentDescription = "Bidali", tint = Color(0xFF4FC3F7))
                    }
                },
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4FC3F7),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF4FC3F7),
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black
                ),
                shape = CircleShape,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}