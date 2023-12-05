package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.GeoLocationRepository
import naganohara.mirai.weather.model.CityLocationData

class GeoLocationViewModel(private val repository: GeoLocationRepository) : ViewModel() {

    suspend fun getLocation(cityName: String, limit: Int, appID: String) {
        repository.getLocation(cityName, limit, appID)
    }

    val locationLiveData: LiveData<CityLocationData>
        get() = repository.locationLiveData

}