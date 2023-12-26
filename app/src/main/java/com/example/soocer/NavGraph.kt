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
        //if(Global.open != route) Global.open = route.toString() else return@NavHost
        //Log.d("nav akinav ", navController.currentDestination.toString())
        composable(route = Screens.SignIn.route) {
            //if (Global.open != it.destination.route.toString()) Global.open = it.destination.route.toString()
            //val signViewModel: SignViewModel = viewModel()
            SignInScreen(
                navController = navController,
                appContext,
                startService
            )
        }
        composable(route = Screens.Register.route) {
            //if (Global.open != it.destination.route.toString()) Global.open = it.destination.route.toString()
        }
        composable(route = Screens.Home.route) {

            Log.d("nav aki", it.destination.route.toString())
            //if (Global.open != it.destination.route.toString()) {
                //Global.open = it.destination.route.toString()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.d("abri", "")
                    HomeScreen(navController = navController, appContext)
                }
        }
    }
}
