package ca.sfu.minerva.database

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MinervaRepository(private val databaseDao : MinervaDatabaseDao) {
    val BikeRentalPlaceData = databaseDao.getAllBikeRentalPlaces()
    val BikeRepairShopData = databaseDao.getAllBikeRepairShops()
    val BikeTrailData = databaseDao.getAllBikeTrails()
    val EcoStoreData = databaseDao.getAllEcoStores()
    val RecyclingCenterData = databaseDao.getAllRecyclingCenters()


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

}