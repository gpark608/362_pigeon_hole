package ca.sfu.minerva.util

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class TrackingViewModel: ViewModel(), ServiceConnection {
    private var mapDataHandler: MapDataHandler = MapDataHandler(Looper.getMainLooper())
    private val _mapBundle = MutableLiveData<Bundle>()

    val mapBundle: LiveData<Bundle>
        get() {
            return _mapBundle
        }

    override fun onServiceConnected(name: ComponentName?, iBinder: IBinder?) {
        val trackingServiceBinder = iBinder as TrackingService.MyBinder
        trackingServiceBinder.setMsgHandler(mapDataHandler)
    }

    override fun onServiceDisconnected(p0: ComponentName?) {
    }

    inner class MapDataHandler(looper: Looper): Handler(looper) {
        override fun handleMessage(msg: Message) {
            if (msg.what == TrackingService.LOCATION_MSG_INT) {
                _mapBundle.value = msg.data
            }
        }
    }
}