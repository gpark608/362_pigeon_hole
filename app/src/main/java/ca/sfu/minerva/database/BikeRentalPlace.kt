package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bike_rental_place_table")
data class BikeRentalPlace (
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "address_column")
    var address:String = "",

//    @ColumnInfo(name = "latlng_column")
//    var latLng:LatLng = LatLng(0,0),

    @ColumnInfo(name = "business_name_column")
    var businessName:String = ""
)