package naganohara.mirai.weather.ui.liveDate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naganohara.mirai.weather.model.CityLocationDataItem

object SearchCitiesLiveData {
    private val citiesLiveData = MutableLiveData<CityLocationDataItem>()

    fun getCitiesLiveData(): LiveData<CityLocationDataItem> = citiesLiveData

    fun updateCitiesLiveData(newCitiesData: CityLocationDataItem) {
        citiesLiveData.value = newCitiesData
    }
}