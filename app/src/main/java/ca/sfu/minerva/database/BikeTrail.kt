package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bike_trail_table")
data class BikeTrail (
    @PrimaryKey(autoGenerate = true)
    var id:Long = 0,

    @ColumnInfo(name = "trail_name_column")
    var trailName:String = "",

//    @ColumnInfo(name = "latlng_column")
//    var latLng:LatLng = LatLng(0,0),

    @ColumnInfo(name = "amenities_column")
    var amenities:String = "",

    @ColumnInfo(name = "address_column")
    var address:String = "",

    @ColumnInfo(name = "path_name_column")
    var pathName:String = "",

    @ColumnInfo(name = "trail_owner_column")
    var trailOwner:String = "",

    @ColumnInfo(name = "trail_website_column")
    var website:String = "",

    @ColumnInfo(name = "hours_column")
    var hoursOfOperation:String = ""
)