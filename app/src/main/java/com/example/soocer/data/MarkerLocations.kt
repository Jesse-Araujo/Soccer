package com.example.soocer.data

import com.google.android.gms.maps.model.LatLng

enum class Type() {
    STADIUM,
    PAVILION
}

class MarkerLocations(val location: String, val latLng: LatLng, val type: Type) {


    companion object {
        val markers = mutableListOf(
            MarkerLocations("Estádio da Luz", LatLng(38.752663, -9.184720), Type.STADIUM),
            MarkerLocations("Pavilhão nº1 da Luz", LatLng(38.75131600625119, -9.183512266644923), Type.PAVILION),
            MarkerLocations("Pavilhão nº2 da Luz", LatLng(38.75170132955841, -9.18334996464678), Type.PAVILION),
            MarkerLocations("Estádio do Restelo", LatLng(38.702351, -9.207772), Type.STADIUM),
            MarkerLocations("Estádio José Alvalade", LatLng(38.761158, -9.160905), Type.STADIUM),
            MarkerLocations("Pavilhão João Rocha", LatLng(38.76348, -9.15846), Type.PAVILION)
        )
    }
}
