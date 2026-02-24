package com.example.climaster

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.material3.MaterialTheme
import com.example.climaster.app.presentation.dashboard.DashboardScreen
import com.example.climaster.ui.theme.CliMasterTheme // Zure Theme originala (ui.theme barruan dagoena)
import dagger.hilt.android.AndroidEntryPoint
import androidx.activity.SystemBarStyle

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            ),
            navigationBarStyle = SystemBarStyle.dark(
                android.graphics.Color.TRANSPARENT
            )
        )
        setContent {
            // Irudian ikusten da 'ui.theme' paketea daukazula, beraz
            // ziurrenik CliMasterTheme hor barruan dago.
            CliMasterTheme {
                // DashboardScreen deitzen dugu
                DashboardScreen()
            }
        }
    }
}