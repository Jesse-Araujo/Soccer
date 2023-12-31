package com.example.soocer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.navigation.NavDestination
import androidx.navigation.NavHostController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.soocer.components.HomeScreen
import com.example.soocer.components.MapScreen
import com.example.soocer.components.SignInScreen

@Composable
fun NavGraph(
    navController: NavHostController,
    appContext: Context,
    startService: (Intent) -> ComponentName?,
    startDestination: String = Screens.SignIn.route,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
    )
    {
        composable(route = Screens.SignIn.route) {
            SignInScreen(
                navController = navController,
                appContext,
                startService
            )
        }
        composable(route = Screens.Register.route) {
        }
        composable(route = Screens.Home.route) {
            HomeScreen(
                navController = navController,
                appContext = appContext,
                startService = startService
            )
        }
        composable(route = Screens.Map.route) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                MapScreen(navController = navController, appContext)
            }
        }
    }
}
