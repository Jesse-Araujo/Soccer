package com.example.soocer

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.example.soocer.auxiliary.Global
import com.example.soocer.ui.theme.SoocerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
            ), 0
        )
        setContent {
            SoocerTheme {
                val navController = rememberNavController()
                if (intent.action == "OPEN_APP_FROM_NOTIFICATION_ACTION") NavGraph(
                    navController = navController,
                    appContext = applicationContext,
                    startService = ::startService,
                    startDestination = Screens.Map.route
                )
                else NavGraph(navController, applicationContext, ::startService)
            }
        }
    }
}