package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "recycling_center_table")
data class RecyclingCenter(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,

    @ColumnInfo(name = "latlng_column")
    var latlng:LatLng = LatLng(0.0, 0.0),

    @ColumnInfo(name = "name_column")
    var name:String = "",

    @ColumnInfo(name = "phone_column")
    var phone:String = "",



)