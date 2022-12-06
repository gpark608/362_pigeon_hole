package ca.sfu.minerva.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.Flow

class MinervaRepository(private val databaseDao : MinervaDatabaseDao) {
    val BikeRentalPlaceData: Flow<List<BikeRentalPlace>> = databaseDao.getAllBikeRentalPlaces()
    val BikeRepairShopData: Flow<List<BikeRepairShop>> = databaseDao.getAllBikeRepairShops()
    val BikeTrailData: Flow<List<BikeTrail>> = databaseDao.getAllBikeTrails()
    val EcoStoreData: Flow<List<EcoStore>> = databaseDao.getAllEcoStores()
    val RecyclingCenterData: Flow<List<RecyclingCenter>> = databaseDao.getAllRecyclingCenters()
    val BikeLocationData: Flow<List<BikeLocation>> = databaseDao.getAllBikeLocation()
    val FavouriteLocationData: Flow<List<FavouriteLocation>> = databaseDao.getAllFavouriteLocation()
    val BikeUsageData: Flow<List<BikeUsage>> = databaseDao.getAllBikeUsages()


    fun insertBikeRentalPlace(data: BikeRentalPlace){
        CoroutineScope(IO).launch {
            databaseDao.insertBikeRental(data)
        }
    }

    fun insertBikeRepairShop(data: BikeRepairShop){
        CoroutineScope(IO).launch {
            databaseDao.insertBikeRepairShop(data)
        }
    }

    fun insertBikeTrail(data: BikeTrail){
        CoroutineScope(IO).launch {
            databaseDao.insertBikeTrail(data)
        }
    }

    fun insertEcoStore(data: EcoStore){
        CoroutineScope(IO).launch {
            databaseDao.insertEcoStore(data)
        }
    }

    fun insertRecyclingCenter(data: RecyclingCenter){
        CoroutineScope(IO).launch {
            databaseDao.insertRecyclingCenter(data)
        }
    }

    fun insertBikeLocation(data: BikeLocation){
        CoroutineScope(IO).launch {
            databaseDao.insertBikeLocation(data)
        }
    }

    fun insertFavouriteLocation(data: FavouriteLocation){
        CoroutineScope(IO).launch {
            databaseDao.deleteFavouriteLocationFromName(data.name)
            databaseDao.insertFavouriteLocation(data)
        }
    }

    fun insertBikeUsage(data: BikeUsage){
        CoroutineScope(IO).launch {
            databaseDao.insertBikeUsage(data)
        }
    }

//    ----
    fun getBikeRentalPlace(id: Long): BikeRentalPlace{
        var result: BikeRentalPlace = BikeRentalPlace()
        CoroutineScope(IO).launch {
            result = databaseDao.getBikeRentalFromKey(id)
        }
        return result
    }

    fun getBikeRepairShop(id: Long): BikeRepairShop{
        var result: BikeRepairShop = BikeRepairShop()
        CoroutineScope(IO).launch {
            result = databaseDao.getBikeRepairShopFromKey(id)
        }
        return result
    }

    fun getBikeTrail(id: Long): BikeTrail{
        var result: BikeTrail = BikeTrail()
        CoroutineScope(IO).launch {
            result = databaseDao.getBikeTrailFromKey(id)
        }
        return result
    }

    fun getEcoStore(id: Long): EcoStore{
        var result: EcoStore = EcoStore()
        CoroutineScope(IO).launch {
            result = databaseDao.getEcoStoreFromKey(id)
        }
        return result
    }

    fun getRecyclingCenter(id: Long): RecyclingCenter{
        var result: RecyclingCenter = RecyclingCenter()
        CoroutineScope(IO).launch {
            result = databaseDao.getRecyclingCenterFromKey(id)
        }
        return result
    }

    fun getBikeLocation(id: Long): BikeLocation{
        var result: BikeLocation = BikeLocation()
        CoroutineScope(IO).launch {
            result = databaseDao.getBikeLocationFromKey(id)
        }
        return result
    }

    fun getBikeUsage(id: Long): BikeUsage{
        var result: BikeUsage = BikeUsage()
        CoroutineScope(IO).launch {
            result = databaseDao.getBikeUsageFromKey(id)
        }
        return result
    }

    //----------------
    fun deleteFavouriteLocation(data: FavouriteLocation){
        CoroutineScope(IO).launch {
            databaseDao.deleteFavouriteLocationFromName(data.name)
        }
    }

}