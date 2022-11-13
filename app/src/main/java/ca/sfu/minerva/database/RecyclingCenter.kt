package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recycling_center_table")
data class RecyclingCenter(
    @PrimaryKey(autoGenerate = true)
    var id: Long = 0,

    @ColumnInfo(name = "website_column")
    var website:String = "",

    @ColumnInfo(name = "address_column")
    var address:String = "",

    @ColumnInfo(name = "phone_column")
    var phone:String = "",

    @ColumnInfo(name = "email_column")
    var email:String = "",

//    @ColumnInfo(name = "latlng_column")
//    var latLng:LatLng = LatLng(0,0),

    @ColumnInfo(name = "store_desciption_column")
    var description:String = "",

    @ColumnInfo(name = "business_name_column")
    var businessName:String = "",

    @ColumnInfo(name = "business_hours_column")
    var businessHours:String = "",

    @ColumnInfo(name = "is_bike_friendly_column")
    var isBikeFriendly:Boolean = true,

    @ColumnInfo(name = "recyclable_items_column")
    var recyclableItems:String = ""
)