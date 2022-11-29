package ca.sfu.minerva.ui.home

import com.google.android.gms.maps.model.LatLng
import java.util.*

class Event(
    val id: String, val title: String, val description: String, val startTime: Date,
    val endTime: Date, val geopoint: LatLng,
    val location:String, val allDay: Boolean) {

}