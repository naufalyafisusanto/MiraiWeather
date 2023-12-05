package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.CurrentLocationRepository
import naganohara.mirai.weather.model.CurrentLocationData

class CurrentLocationViewModel(private val currentLocationRepository: CurrentLocationRepository) : ViewModel() {
    suspend fun getLocation() {
        currentLocationRepository.getLocation()
    }

    val approximateLocationLiveData: LiveData<CurrentLocationData>
        get() = currentLocationRepository.locationLiveData
}