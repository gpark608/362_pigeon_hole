package ca.sfu.minerva.util

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Binder
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TrackingService: Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private lateinit var startLocation: Location

    private lateinit var trackingServiceBinder: TrackingServiceBinder
    private var mapDataHandler: Handler? = null

    private lateinit var locationList: ArrayList<LatLng>
    private var distance = 0F
    private var speed = 0F
    private var startTime = 0L
    private var altitude = 0.0
    private var totalTime = 0.0

    companion object {
        const val TRACKING_MESSAGE_ID = 0

        const val SPEED = "SPEED"
        const val DISTANCE = "DISTANCE"
        const val TOTAL_TIME = "TOTAL_TIME"
        const val ALTITUDE = "ALTITUDE"
        const val LOCATIONS = "LOCATIONS"
    }

    override fun onCreate() {
        super.onCreate()
        locationList = ArrayList()
        startTime = System.currentTimeMillis()

        trackingServiceBinder = TrackingServiceBinder()
        initLocationManager()
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanupTasks()
        stopSelf()
    }

    private fun cleanupTasks() {
        mapDataHandler = null
        locationManager.removeUpdates(this)
        locationList.clear()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return trackingServiceBinder
    }

    inner class TrackingServiceBinder: Binder() {
        fun setMapDataHandler(msgHandler: Handler) {
            this@TrackingService.mapDataHandler = msgHandler
        }
    }

    override fun onLocationChanged(location: Location) {
        println("DEBUG: Location Changed")
        // add locations
        val lat = location.latitude
        val lng = location.longitude
        val latLng = LatLng(lat, lng)
        locationList.add(latLng)

        if (::startLocation.isInitialized) {
            distance += Helper.metersToMiles(startLocation.distanceTo(location))
            speed = location.speed
            altitude = Helper.metersToMiles(location.altitude.toFloat()).toDouble()
        } else {
            // initialize the start location
            startLocation = location
        }

        // send data to handler
        sendMapData()
    }

    private fun sendMapData() {
        println("DEBUG: Send Map Data")
        try {
            if (mapDataHandler != null) {
                val bundle = Bundle()
                bundle.putFloat(SPEED, speed)
                bundle.putFloat(DISTANCE, distance)
                bundle.putDouble(TOTAL_TIME, totalTime)
                bundle.putDouble(ALTITUDE, altitude)
                bundle.putString(LOCATIONS, Helper.latLngToString(locationList))

                val mapMessage = mapDataHandler!!.obtainMessage()
                mapMessage.data = bundle
                mapMessage.what = TRACKING_MESSAGE_ID
                mapDataHandler!!.sendMessage(mapMessage)
            }
        } catch (t: Throwable) {
            Log.d("DEBUG: ", t.toString())
        }
    }

    @SuppressLint("MissingPermission")
    private fun initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)
            if (provider != null) {
                val location = locationManager.getLastKnownLocation(provider)
                if (location != null)
                    onLocationChanged(location)
                locationManager.requestLocationUpdates(provider, 0, 0f, this)
            }
        } catch (e: SecurityException) {
            println("DEBUG: $e")
        }
    }

    // this function will continuously calculate the time
    private suspend fun calculateTime() {
        TODO("Not yet implemented")
    }
}
