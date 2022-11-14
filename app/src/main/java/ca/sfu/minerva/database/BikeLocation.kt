package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "bike_location_table")
data class BikeLocation (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "bikeLocation_column")
    var bikeLocation:LatLng = LatLng(0.0,0.0)


){
    fun getbikeLocation(): LatLng{
        return bikeLocation
    }

    fun setbikeLocation(bikeLocation: LatLng){
        this.bikeLocation=bikeLocation
    }
}