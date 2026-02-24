package com.climaster.app.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource // Beharbada ikono propioak beharko dituzu
// Edo Material Icons erabiliz:
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AcUnit
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.unit.dp
import com.climaster.app.presentation.theme.glassmorphic
import com.climaster.domain.model.ThermalSensation

@Composable
fun ThermalFeedbackButtons(
    onFeedbackSelected: (ThermalSensation) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        // Hotz Botoia (Iozten)
        GlassButton(
            text = "Hotz nago",
            icon = Icons.Default.AcUnit,
            color = Color(0xFF81D4FA), // Urdin argia
            onClick = { onFeedbackSelected(ThermalSensation.COLD) }
        )

        // Bero Botoia (Kixkaltzen)
        GlassButton(
            text = "Bero nago",
            icon = Icons.Default.WbSunny,
            color = Color(0xFFFFCC80), // Laranja argia
            onClick = { onFeedbackSelected(ThermalSensation.WARM) }
        )
    }
}

@Composable
fun GlassButton(
    text: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .glassmorphic(cornerRadius = 12.dp, alpha = 0.15f)
            .clickable { onClick() }
            .padding(16.dp)
            .width(100.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = color,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White
        )
    }
}