package ca.sfu.minerva.util

import com.google.android.gms.maps.model.LatLng
import com.google.common.reflect.TypeToken
import com.google.gson.Gson
import java.lang.reflect.Type
import java.util.ArrayList

object Helper {
    private fun getTimeInSeconds(start: Long, end: Long): Double {
        return ((end - start) / 1000).toDouble()
    }

    fun getTimeInMinutes(start: Long, end: Long): Double {
        return getTimeInSeconds(start, end) / 60
    }

    // Might delete later
    fun getTimeInHours(start: Long, end: Long): Double {
        return getTimeInMinutes(start, end) / 60
    }

    fun metersToMiles(meters: Float): Float {
        return (meters / 1609.344).toFloat()
    }

    fun jsonToLatLng(json: String): ArrayList<LatLng> {
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<LatLng>>() {}.type
        return gson.fromJson(json, listType)
    }

    fun latLngToString(list: ArrayList<LatLng>): String {
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<LatLng>>() {}.type
        return gson.toJson(list, listType)
    }
}