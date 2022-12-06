package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.google.android.gms.maps.model.LatLng

//@TypeConverters(LocationConverter::class)
@Entity(tableName = "bike_usage_table")
data class BikeUsage (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "avg_speed_column")
    var avgSpeed: Double = 0.0,

    @ColumnInfo(name = "altitude_column")
    var climb: Double = 0.0,

    @ColumnInfo(name = "distance_column")
    var distance:Double = 0.0,

    @ColumnInfo(name = "duration_column")
    var duration:String = "",

    @ColumnInfo(name = "time_column")
    var time:String = "",

    @ColumnInfo(name = "date_column")
    var date:String = "",

    @ColumnInfo(name = "speed_column")
    var speed:Double = 0.0,

//    @ColumnInfo(name = "location_list_column")
//    var locationList: ArrayList<LatLng> = ArrayList()
)//: java.io.Serializable