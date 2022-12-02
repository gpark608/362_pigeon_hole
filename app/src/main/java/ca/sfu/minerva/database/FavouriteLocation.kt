package ca.sfu.minerva.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.android.gms.maps.model.LatLng

@Entity(tableName = "favourite_table")
data class FavouriteLocation (
    @PrimaryKey(autoGenerate = false)
    var name: String = "",

    @ColumnInfo(name = "description_column")
    var description:String = "",

    @ColumnInfo(name = "favouriteLatLng_column")
    var favLatLng: String = "0.0,0.0"
)