package ca.sfu.minerva.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MinervaDatabaseDao {
    @Insert
    suspend fun insertBikeRental(bikePlace: BikeRentalPlace)

    @Insert
    suspend fun insertBikeTrail(bikeT: BikeTrail)

    @Insert
    suspend fun insertEcoStore(eco: EcoStore)

    @Insert
    suspend fun insertRecyclingCenter(recyclePlace: RecyclingCenter)

    @Insert
    suspend fun insertBikeRepairShop(bikeR: BikeRepairShop)

    @Insert
    suspend fun insertBikeLocation(bikeLocation: BikeLocation)

    @Insert
    suspend fun insertFavouriteLocation(favLocation: FavouriteLocation)

//    --------

    @Query("SELECT * FROM bike_rental_place_table")
    fun getAllBikeRentalPlaces() : Flow<List<BikeRentalPlace>>

    @Query("SELECT * FROM bike_trail_table")
    fun getAllBikeTrails() : Flow<List<BikeTrail>>

    @Query("SELECT * FROM ecostore_table")
    fun getAllEcoStores() : Flow<List<EcoStore>>

    @Query("SELECT * FROM recycling_center_table")
    fun getAllRecyclingCenters() : Flow<List<RecyclingCenter>>

    @Query("SELECT * FROM bike_repair_place_table")
    fun getAllBikeRepairShops() : Flow<List<BikeRepairShop>>

    @Query("SELECT * FROM bike_location_table")
    fun getAllBikeLocation(): Flow<List<BikeLocation>>

    @Query("SELECT * FROM favourite_locations_table")
    fun getAllFavouriteLocation(): Flow<List<FavouriteLocation>>

//    -----
    @Query("SELECT * FROM bike_rental_place_table WHERE id = :key")
    suspend fun getBikeRentalFromKey(key: Long) : BikeRentalPlace

    @Query("SELECT * FROM bike_trail_table WHERE id = :key")
    suspend fun getBikeTrailFromKey(key: Long) : BikeTrail

    @Query("SELECT * FROM ecostore_table WHERE id = :key")
    suspend fun getEcoStoreFromKey(key: Long) : EcoStore

    @Query("SELECT * FROM recycling_center_table WHERE id = :key")
    suspend fun getRecyclingCenterFromKey(key: Long) : RecyclingCenter

    @Query("SELECT * FROM bike_repair_place_table WHERE id = :key")
    suspend fun getBikeRepairShopFromKey(key: Long) : BikeRepairShop

    @Query("SELECT * FROM bike_location_table WHERE id = :key")
    suspend fun getBikeLocationFromKey(key: Long) : BikeLocation
}
