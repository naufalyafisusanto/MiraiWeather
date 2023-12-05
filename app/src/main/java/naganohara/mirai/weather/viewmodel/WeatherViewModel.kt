package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.WeatherRepository
import naganohara.mirai.weather.model.weathermodel.WeatherData

class WeatherViewModel(private val weatherRepository: WeatherRepository): ViewModel() {

    suspend fun getWeather(lat: String, lon: String, appId: String) {
        weatherRepository.getWeather(lat, lon, appId)
    }

    val weatherLiveData: LiveData<WeatherData>
        get() = weatherRepository.weatherData

}