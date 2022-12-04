package ca.sfu.minerva.database


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.flow.collect
import java.lang.IllegalArgumentException

class MinervaViewModel(private val repository: MinervaRepository): ViewModel(){
    val BikeRentalPlaceDataLive: LiveData<List<BikeRentalPlace>> = repository.BikeRentalPlaceData.asLiveData()
    val BikeRepairShopDataLive: LiveData<List<BikeRepairShop>> = repository.BikeRepairShopData.asLiveData()
    val BikeTrailDataLive: LiveData<List<BikeTrail>> = repository.BikeTrailData.asLiveData()
    val EcoStoreDataLive: LiveData<List<EcoStore>> = repository.EcoStoreData.asLiveData()
    val RecyclingCentreDataLive: LiveData<List<RecyclingCenter>> = repository.RecyclingCenterData.asLiveData()
    val BikeLocationDataLive: LiveData<List<BikeLocation>> = repository.BikeLocationData.asLiveData()
    val FavouriteLocationDataLive: LiveData<List<FavouriteLocation>> = repository.FavouriteLocationData.asLiveData()
    var favouritesActive: Boolean = false

    fun insertBikeRentalPlace(data: BikeRentalPlace){
        repository.insertBikeRentalPlace(data)
    }

    fun insertBikeRepairShop(data: BikeRepairShop){
        repository.insertBikeRepairShop(data)
    }

    fun insertBikeTrail(data: BikeTrail){
        repository.insertBikeTrail(data)
    }

    fun insertEcoStore(data: EcoStore){
        repository.insertEcoStore(data)
    }

    fun insertRecyclingCenter(data: RecyclingCenter){
        repository.insertRecyclingCenter(data)
    }

    fun insertBikeLocation(data: BikeLocation){
        repository.insertBikeLocation(data)
    }

    fun insertFavouriteLocation(data: FavouriteLocation){
        repository.insertFavouriteLocation(data)
    }

    fun deleteFavouriteLocation(data: FavouriteLocation){
        repository.deleteFavouriteLocation(data)
    }

}

class MinervaViewModelFactory (private val repository: MinervaRepository) : ViewModelProvider.Factory {
    override fun<T: ViewModel> create(modelClass: Class<T>) : T{ //create() creates a new instance of the modelClass, which is CommentViewModel in this case.
        if(modelClass.isAssignableFrom(MinervaViewModel::class.java))
            return MinervaViewModel(repository) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}