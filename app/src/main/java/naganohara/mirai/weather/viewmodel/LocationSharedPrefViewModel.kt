package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.LocationSharedPrefRepository
import naganohara.mirai.weather.model.CurrentLocationData

class LocationSharedPrefViewModel(private val locationSharedPrefRepository: LocationSharedPrefRepository): ViewModel() {

    fun getData(): CurrentLocationData? {
        return locationSharedPrefRepository.getData()
    }

    fun sendData(locationData: CurrentLocationData) {
        locationSharedPrefRepository.sendData(locationData)
    }

}