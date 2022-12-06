package ca.sfu.minerva.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [
    RecyclingCenter::class, EcoStore::class, BikeRentalPlace::class, BikeTrail::class,
    BikeRepairShop::class, BikeLocation::class, FavouriteLocation::class, BikeUsage::class
                     ], version = 8
)
@TypeConverters(MinervaDataConverter::class)
abstract class MinervaDatabase : RoomDatabase() {
    abstract val MinervaDatabaseDao : MinervaDatabaseDao

    companion object{
        @Volatile
        private var INSTANCE: MinervaDatabase? = null

        fun getInstance(context: Context): MinervaDatabase {
            synchronized(this){
                var instance = INSTANCE
                if(instance == null){
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        MinervaDatabase::class.java,
                        "minerva_DB"
                    ).fallbackToDestructiveMigration().build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}