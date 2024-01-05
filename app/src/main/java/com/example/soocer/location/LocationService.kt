package com.example.soocer.location

import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.location.Location
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.soocer.R
import com.example.soocer.auxiliary.getDistanceBetweenTwoPoints
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.events.Events
import com.example.soocer.listeners.OnLocationChangedListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private  lateinit var locationClient : OnLocationChangedListener
    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        locationClient = DefaultLocationClient(
            applicationContext,
            LocationServices.getFusedLocationProviderClient(applicationContext)
        )

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val events = mutableListOf<Events>()
        FirebaseFunctions.getDataFromFirebase(mutableStateOf(false)){
            events.addAll(it)
        }
        val notification = NotificationCompat.Builder(this,"location")
            .setContentTitle("Tracking location")
            .setContentText("Gps disabled or permission not granted!")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val eventsCloseBy = hashSetOf<String>()

        locationClient
            .getLocationUpdates(1000L)
            .catch { it.printStackTrace() }
            .onEach { location->
                val lat = location.latitude.toString()
                val long = location.longitude.toString()
                val latLng = LatLng(location.latitude,location.longitude)
                events.forEach { if(getDistanceBetweenTwoPoints(it.markerLocations.latLng, latLng) <= 1.0) {
                    //eventsCloseBy.add("${it}")
                }
                }
                val updateNotification = notification.setContentText(
                    "Location: ($lat, $long)"
                )
                notificationManager.notify(1, updateNotification.build())
            }.launchIn(serviceScope)

        startForeground(1,notification.build())
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    companion object{
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}