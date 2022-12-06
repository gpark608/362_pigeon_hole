package ca.sfu.minerva.util

import android.annotation.SuppressLint
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.Criteria
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import androidx.core.app.NotificationCompat
import ca.sfu.minerva.CoreActivity
import ca.sfu.minerva.R
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.util.*
import kotlin.collections.ArrayList

class TrackingService: Service(), LocationListener {
    private lateinit var locationManager: LocationManager
    private var msgHandler: Handler? = null

    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID =  "channel id for Bike Usage Tracking"
    private val NOTIFICATION_ID = 57
    private val REQUEST_CODE = 11
    private lateinit var mapBinder: Binder
    private lateinit var customBroadcastReceiver: BroadcastReceiver
    companion object{
        val STOP_ACTION = "stop service"
        const val LOCATION_MSG_INT = 1
    }

    private lateinit var _date: LocalDate
    private lateinit var _activityTimeStart: LocalTime
    private var _mapStartMilliSec: Long = 0L
    private var _elapsedDistance: Float = 0F
    private var _initialAltitude: Double = 0.0
    private var _currentAltitude: Double = 0.0
    private var _speed: Float = 0F
    private var _lastLocationArray: ArrayList<Location> = ArrayList()
    private var _lastActiveLatLng: LatLng? = null
    private var _avgSpeed: Float = 0F
    private var _elapsedTime: Float = 0F

    override fun onCreate() {
        super.onCreate()

        notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        showNotification()

        customBroadcastReceiver = CustomBrodcastReceiver()
        val intentFilter = IntentFilter()
        intentFilter.addAction(STOP_ACTION)
        registerReceiver(customBroadcastReceiver, intentFilter)

        initLocationManager()

        val cal = Calendar.getInstance()
        val current = LocalDateTime.of(
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH),
            cal.get(Calendar.HOUR_OF_DAY),
            cal.get(Calendar.MINUTE),
            cal.get(Calendar.SECOND)
        )

        _activityTimeStart = LocalTime.now()

        _date = LocalDate.now()

        mapBinder = MyBinder()
        msgHandler = null
        _mapStartMilliSec = System.currentTimeMillis()

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return mapBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        return true
    }

    private fun sendMessage(){
        CoroutineScope(IO).launch {
            if (msgHandler != null) {
                val message: Message = msgHandler!!.obtainMessage()
                if (message != null) {
                    message.data = makeBundle()
                    message.what = LOCATION_MSG_INT
                    msgHandler!!.sendMessage(message)
                }
            }
        }
    }

    fun makeBundle(): Bundle {
        val bundle = Bundle()

        bundle.putString("date", _date.toString())
        bundle.putString("startTime", _activityTimeStart.toString())
        bundle.putParcelableArrayList("locations", _lastLocationArray)
        bundle.putParcelable("lastLocation", _lastActiveLatLng)
        bundle.putLong("startMilli", _mapStartMilliSec)
        bundle.putFloat("speed", _speed)
        bundle.putDouble("currentAltitude", _currentAltitude)
        bundle.putFloat("elapsedDistance", _elapsedDistance)
        bundle.putFloat("elapsedTime", _elapsedTime)
        bundle.putFloat("avgSpeed", _avgSpeed)

        return bundle
    }

    override fun onLocationChanged(location: Location) {
        if(!_lastLocationArray.isEmpty())
            _elapsedDistance += location.distanceTo(_lastLocationArray.last())*0.001F
        else
            _initialAltitude = location.altitude

        _elapsedTime = (System.currentTimeMillis() - _mapStartMilliSec)/ 3600000F
        _speed = Math.max((location.speed + location.speedAccuracyMetersPerSecond) * 3.6F, _speed)
        _currentAltitude = (_initialAltitude - location.altitude)*0.001
        _lastActiveLatLng = LatLng(location.latitude, location.longitude)
        _lastLocationArray.add(location)
        _avgSpeed = _elapsedDistance/_elapsedTime + Float.MIN_VALUE
        if(_avgSpeed < 0F || !_avgSpeed.toDouble().isFinite())
            _avgSpeed = 0.0F

        sendMessage()
    }

    private fun showNotification(){
        val temp = this

        CoroutineScope(IO).launch {
            val intent: Intent = Intent(temp, CoreActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            intent.putExtra("activityFromActiveService", true)

//            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(temp).run {
//                addNextIntentWithParentStack(intent)
//                getPendingIntent(REQUEST_CODE, PendingIntent.FLAG_UPDATE_CURRENT and PendingIntent.FLAG_IMMUTABLE)
//            }

            val notificationBuilder : NotificationCompat.Builder = NotificationCompat.Builder(
                temp, CHANNEL_ID
            )
            notificationBuilder.setContentTitle("Minerva")
            notificationBuilder.setContentText("Tracking current bike ride")
            notificationBuilder.setSmallIcon(R.drawable.splash_screen)
//            notificationBuilder.setContentIntent(resultPendingIntent)
            notificationBuilder.setOngoing(true)
            val notification = notificationBuilder.build()


            if(Build.VERSION.SDK_INT > 26){
                val channel = NotificationChannel(CHANNEL_ID, "myruns channel", NotificationManager.IMPORTANCE_HIGH)
                notificationManager.createNotificationChannel(channel)
            }

            notificationManager.notify(NOTIFICATION_ID, notification)
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    @SuppressLint("MissingPermission")
    fun  initLocationManager() {
        try {
            locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
            val criteria = Criteria()
            criteria.accuracy = Criteria.ACCURACY_FINE
            val provider : String? = locationManager.getBestProvider(criteria, true)

            val location = locationManager.getLastKnownLocation(provider!!)

            if (location != null)
                onLocationChanged(location)

            locationManager.requestLocationUpdates(provider, 0, 0F, this)
        } catch (e: SecurityException) {
        }
    }

    override fun onProviderDisabled(provider: String) {}

    override fun onProviderEnabled(provider: String) {}

    override fun onTaskRemoved(rootIntent: Intent?){
        super.onTaskRemoved(rootIntent)
        serviceTearDown()
        stopSelf()
    }

    override fun onDestroy() {
        unregisterReceiver(customBroadcastReceiver)
        super.onDestroy()
        serviceTearDown()
    }

    fun serviceTearDown(){
        if(locationManager != null){
            locationManager.removeUpdates(this)
        }
        msgHandler = null
        notificationManager.cancel(NOTIFICATION_ID)

        _date = LocalDate.MIN
        _activityTimeStart = LocalTime.MIN
        _mapStartMilliSec = 0L
        _elapsedDistance = 0F
        _initialAltitude = 0.0
        _currentAltitude = 0.0
        _speed = 0F
        _lastLocationArray = ArrayList()
        _lastActiveLatLng = null
        _avgSpeed = 0F
        _elapsedTime = 0F

        stopForeground(true)
        stopSelf()
    }

    inner class MyBinder: Binder(){
        fun setMsgHandler(handler: Handler){
            msgHandler = handler
        }

        fun getNotificationId(): Int{
            return NOTIFICATION_ID
        }
    }


    inner class CustomBrodcastReceiver: BroadcastReceiver(){
        override fun onReceive(context: Context?, intent: Intent?) {
            unregisterReceiver(customBroadcastReceiver)
            notificationManager.cancel(NOTIFICATION_ID)
            serviceTearDown()
        }
    }

}