package com.example.sports

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.app.ActivityCompat
import androidx.navigation.compose.rememberNavController
import com.example.sports.auxiliary.requireLogin
import com.example.sports.ui.theme.SportsTheme

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
        val bol = requireLogin(applicationContext)
        setContent {
            SportsTheme {
                val navController = rememberNavController()
                if (intent.action == "OPEN_APP_FROM_NOTIFICATION_ACTION") NavGraph(
                    navController = navController,
                    appContext = applicationContext,
                    startService = ::startService,
                    startDestination = Screens.Map.route
                )
                else if(!bol) NavGraph(navController, applicationContext, startDestination = Screens.Home.route,startService =::startService)
                else NavGraph(navController, applicationContext, ::startService)
            }
        }
    }
}