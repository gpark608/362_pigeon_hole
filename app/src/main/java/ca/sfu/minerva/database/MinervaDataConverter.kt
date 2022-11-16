package ca.sfu.minerva.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng

class MinervaDataConverter {
    @TypeConverter
    fun toBikeLocation(latLng: String): LatLng{
        val latLng = latLng.split("/")
        return LatLng(latLng[0].toDouble(),latLng[1].toDouble())
    }

    @TypeConverter
    fun fromBikeLocation(latLng: LatLng): String {
        return "${latLng.latitude}/${latLng.longitude}"
    }
}