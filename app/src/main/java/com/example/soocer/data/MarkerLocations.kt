package com.example.soocer.data

import com.google.android.gms.maps.model.LatLng

enum class Type() {
    STADIUM,
    PAVILION
}

class MarkerLocations(val title: String, val latLng: LatLng, val type: Type,val city: String) {


    companion object {
        val markers = mutableListOf(
            MarkerLocations("Estádio da Luz", LatLng(38.752663, -9.184720), Type.STADIUM,"Lisboa"),
            MarkerLocations("Pavilhão nº1 da Luz", LatLng(38.75131600625119, -9.183512266644923), Type.PAVILION,"Lisboa"),
            MarkerLocations("Pavilhão nº2 da Luz", LatLng(38.75170132955841, -9.18334996464678), Type.PAVILION,"Lisboa"),
            MarkerLocations("Estádio do Restelo", LatLng(38.702351, -9.207772), Type.STADIUM,"Lisboa"),
            MarkerLocations("Estádio José Alvalade", LatLng(38.761158, -9.160905), Type.STADIUM,"Lisboa"),
            MarkerLocations("Pavilhão João Rocha", LatLng(38.76348, -9.15846), Type.PAVILION,"Lisboa")
        )

        fun getClubStadium(club : String) : MarkerLocations {
            val c = club.lowercase()
            println(c)
            return when {
                c.contains("benfica") ->  MarkerLocations("Estádio da Luz", LatLng(38.752663, -9.184720), Type.STADIUM,"Lisboa")
                c.contains("braga") -> MarkerLocations("Estádio Municipal de Braga", LatLng(41.562544, -8.429873), Type.STADIUM,"Braga")
                c.contains("sporting") -> MarkerLocations("Estádio José Alvalade", LatLng(38.761158, -9.160905), Type.STADIUM,"Lisboa")
                else -> MarkerLocations("Estádio do Dragão", LatLng(41.161745739580674, -8.583816308722257), Type.STADIUM,"Porto")
            }
        }
    }
}
