package com.example.climaster.app.presentation.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.LocationCity
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.climaster.core.util.Resource
import com.example.climaster.domain.model.LocationResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchOverlay(
    searchQuery: String,
    searchResults: Resource<List<LocationResult>>,
    onQueryChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onLocationSelected: (String, Double, Double) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { onDismiss() }
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 180.dp, start = 16.dp, end = 16.dp, bottom = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .clickable(interactionSource = remember { MutableInteractionSource() }, indication = null) { }
                .padding(24.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                Text("Aukeratu kokapena", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF1E3C72), modifier = Modifier.align(Alignment.Center))
            }
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                placeholder = { Text("Adib: Oslo...", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Rounded.Search, tint = Color.DarkGray, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onQueryChange("") }) { Icon(Icons.Rounded.Close, tint = Color.Gray, contentDescription = "Garbitu") }
                    }
                },
                singleLine = true,
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color(0xFF4FC3F7),
                    unfocusedIndicatorColor = Color.White.copy(alpha = 0.3f),
                    cursorColor = Color(0xFF4FC3F7),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(modifier = Modifier.fillMaxHeight(0.6f)) {
                if (searchQuery.isEmpty()) {
                    Column {
                        Text("Hiri ezagunak", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(modifier = Modifier.height(16.dp))
                        listOf(
                            LocationResult("Madrid, España", 40.4168, -3.7038),
                            LocationResult("Bilbao, Euskadi", 43.2630, -2.9350),
                            LocationResult("London, UK", 51.5074, -0.1278),
                            LocationResult("London, UK", 51.5074, -0.1278)
                        ).forEach { city -> LocationListItem(city) { onLocationSelected(city.name, city.lat, city.lon) } }
                    }
                } else {
                    when (searchResults) {
                        is Resource.Loading -> {
                            // UX HOBETUA: SKELETON / SHIMMER LOADING EFEKTUA
                            LazyColumn {
                                items(5) { // 5 elementu faltsu erakutsi
                                    ShimmerLocationItem()
                                }
                            }
                        }
                        is Resource.Error -> {
                            Text("Ez da ezer aurkitu.", color = Color.Red, modifier = Modifier.align(Alignment.Center))
                        }
                        is Resource.Success -> {
                            val locations = searchResults.data
                            if (locations.isEmpty() && searchQuery.length >= 3) {
                                Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Rounded.Search, contentDescription = null, tint = Color.LightGray, modifier = Modifier.size(48.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Ez dugu aurkitu '$searchQuery'", color = Color.Gray, fontSize = 15.sp)
                                }
                            } else {
                                LazyColumn {
                                    items(locations) { location ->
                                        LocationListItem(location) { onLocationSelected(location.name, location.lat, location.lon) }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// OSAGAI BERRIA: Shimmer Item (2. Irudiko efektua)
@Composable
fun ShimmerLocationItem() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Borobila
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .shimmerEffect()
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            // Izenburua (Luzeagoa)
            Box(
                modifier = Modifier
                    .height(16.dp)
                    .fillMaxWidth(0.6f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
            Spacer(modifier = Modifier.height(8.dp))
            // Azpititulua (Motzagoa)
            Box(
                modifier = Modifier
                    .height(12.dp)
                    .fillMaxWidth(0.4f)
                    .clip(RoundedCornerShape(4.dp))
                    .shimmerEffect()
            )
        }
    }
}

// MODIFIER BERRIA: Zilar koloreko uhin animazioa
fun Modifier.shimmerEffect(): Modifier = composed {
    // 1. Corregido: Import añadido arriba y tipo explícito para evitar dudas
    var size by remember { mutableStateOf(IntSize.Zero) }

    val transition = rememberInfiniteTransition(label = "shimmer")
    val startOffsetX by transition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_anim"
    )

    this
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color(0xFFE0E0E0),
                    Color(0xFFF5F5F5),
                    Color(0xFFE0E0E0)
                ),
                start = Offset(startOffsetX, 0f),
                end = Offset(startOffsetX + 400f, 400f) // Diagonal
            )
        )
        // 2. Útil si en el futuro quieres que el gradiente dependa del tamaño real
        .onGloballyPositioned {
            size = it.size
        }
}

@Composable
fun LocationListItem(location: LocationResult, onClick: () -> Unit) {
    // ... (Funtzio hau berdina da, mantendu ondo zegoen bezala) ...
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).clickable { onClick() }.padding(vertical = 12.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(Color(0xFFE8F0FE)), contentAlignment = Alignment.Center) {
            Icon(Icons.Rounded.LocationCity, contentDescription = null, tint = Color(0xFF1E3C72))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            val parts = location.name.split(",")
            Text(text = parts.firstOrNull() ?: "", fontWeight = FontWeight.Bold, color = Color(0xFF1E3C72), fontSize = 16.sp)
            val subtitle = parts.drop(1).joinToString(",").trim()
            if (subtitle.isNotEmpty()) Text(text = subtitle, color = Color.Gray, fontSize = 13.sp, maxLines = 1)
        }
    }
}