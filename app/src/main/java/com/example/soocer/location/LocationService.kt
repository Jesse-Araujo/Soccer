package com.example.soocer.location

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.compose.runtime.mutableStateOf
import androidx.core.app.NotificationCompat
import com.example.soocer.MainActivity
import com.example.soocer.R
import com.example.soocer.auxiliary.getDistanceBetweenTwoPoints
import com.example.soocer.data.FirebaseFunctions
import com.example.soocer.data.Events
import com.example.soocer.listeners.OnLocationChangedListener
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class LocationService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var locationClient: OnLocationChangedListener
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
        when (intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return START_STICKY//super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        //TODO se for a 1ª cena a ser lida no dia ele n encontra eventos pq a DB n foi atualizada
        val events = mutableListOf<Events>()
        val favSports = hashSetOf<String>()
        FirebaseFunctions.getDataFromFirebase(mutableStateOf(false)) {
            events.addAll(it)
        }
        FirebaseFunctions.getUserFavSports {
            favSports.addAll(it)
        }

        val intent = Intent(this, MainActivity::class.java)
        intent.action = "OPEN_APP_FROM_NOTIFICATION_ACTION"
        val pendingIntent = PendingIntent.getActivity(
            this,
            69,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location")
            //.setContentText("Gps disabled or permission not granted!")
            .setContentText("Events nearby: 0")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentIntent(pendingIntent)
            .setOngoing(true)


        /*GPSChecker(baseContext) { gpsIsOnline, loc ->
            if(!gpsIsOnline) {

            }
        }*/

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val eventsCloseBy = hashSetOf<Events>()
        val eventsCheck = hashSetOf<Events>()

        locationClient
            .getLocationUpdates(1000L)
            .catch { it.printStackTrace() }
            .onEach { location ->
                val lat = location.latitude
                val long = location.longitude
                val latLng = LatLng(lat, long)
                events.forEach {
                    if (getDistanceBetweenTwoPoints(it.markerLocations.latLng, latLng) <= 1.0 && favSports.contains(it.eventType.type)) {
                        eventsCloseBy.add(it)
                    }
                }
                if(events.isNotEmpty() && eventsCloseBy.isNotEmpty() && !areSetsEqual(eventsCheck,eventsCloseBy)) {
                    val updateNotification = notification.setContentText(
                        "Events Nearby: ${eventsCloseBy.size}"
                    )
                    notificationManager.notify(1, updateNotification.build())
                }
                eventsCheck.clear()
                eventsCheck.addAll(eventsCloseBy)
                eventsCloseBy.clear()
                //notificationManager.notify(1, updateNotification.build())
            }.launchIn(serviceScope)

        startForeground(1, notification.build())
    }

    private fun areSetsEqual(set1:HashSet<Events>, set2:HashSet<Events>) :Boolean {
        if(set1.size != set2.size) return false
        set1.forEach { if(!set2.contains(it)) return false }
        return true
    }

    private fun stop() {
        stopForeground(true)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}