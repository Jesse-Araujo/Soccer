package com.example.sports

sealed class Screens (val route: String) {
    object SignIn: Screens("sign_in")

    object Home : Screens("home")
    object Map : Screens("map/{sport}")
    object Review : Screens("review/{markerName}")
}
