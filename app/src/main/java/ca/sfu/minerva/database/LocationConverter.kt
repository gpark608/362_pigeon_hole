package ca.sfu.minerva.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type

class LocationConverter {
    @TypeConverter
    fun toArrayList(json : String ): ArrayList<LatLng>{
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<LatLng>>() {}.type
        val array: ArrayList<LatLng> = gson.fromJson(json, listType)
        return array
    }

    @TypeConverter
    fun fromArrayList(array : ArrayList<LatLng>): String{
        val gson = Gson()
        val listType: Type = object : TypeToken<ArrayList<LatLng>>() {}.type
        val json: String = gson.toJson(array, listType)
        return json
    }
}