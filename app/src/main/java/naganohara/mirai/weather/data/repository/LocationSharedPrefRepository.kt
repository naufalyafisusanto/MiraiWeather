package naganohara.mirai.weather.data.repository

import naganohara.mirai.weather.data.local.LocationSharedPrefService
import naganohara.mirai.weather.model.CurrentLocationData

class LocationSharedPrefRepository(private val locationSharedPrefService: LocationSharedPrefService) {

    fun getData(): CurrentLocationData? {
        return locationSharedPrefService.getData()
    }

    fun sendData(locationData: CurrentLocationData) {
        locationSharedPrefService.sendData(locationData)
    }

}