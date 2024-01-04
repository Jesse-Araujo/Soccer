package com.example.soocer

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController

import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.soocer.componets.HomeScreen
import com.example.soocer.componets.SignInScreen
import com.example.soocer.componets.SignViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    appContext: Context,
    startService: (Intent) -> ComponentName?
) {
    NavHost(
        navController = navController,
        startDestination = Screens.SignIn.route
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

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    HomeScreen(navController = navController, appContext)
                }
        }
    }
}
