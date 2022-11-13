package ca.sfu.minerva.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecyclingCenter::class, EcoStore::class, BikeRentalPlace::class, BikeTrail::class, BikeRepairShop::class], version = 1)
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
                    ).build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}