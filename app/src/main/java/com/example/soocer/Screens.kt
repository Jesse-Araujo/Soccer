package com.example.soocer

sealed class Screens (val route: String) {
    object SignIn: Screens("sign_in")
    object Register : Screens("register")

    object Home : Screens("home")
    object Map : Screens("map/{sport}")
    object Review : Screens("review/{markerName}")
}
