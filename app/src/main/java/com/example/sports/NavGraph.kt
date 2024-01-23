package com.example.sports

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.sports.components.HomeScreen
import com.example.sports.components.MapScreen
import com.example.sports.components.ReviewMarkerWindow
import com.example.sports.components.SignInScreen

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
        composable(route = Screens.Home.route) {
            HomeScreen(
                navController = navController,
                appContext = appContext,
                startService = startService
            )
        }
        composable(route = Screens.Map.route + "?sport={sport}") {navBackStack ->
            val sport = navBackStack.arguments?.getString("sport")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if(sport == null || sport == "{sport}") MapScreen(navController, appContext)
                else MapScreen(navController, appContext, hashSetOf(sport).toHashSet())
            }
        }
        composable(route = Screens.Review.route+ "?markerName={markerName}") {navBackStack ->
            val markerName = navBackStack.arguments?.getString("markerName") ?: ""
            ReviewMarkerWindow(navController,appContext,markerName)
        }
    }
}
