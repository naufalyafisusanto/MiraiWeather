package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.HourlyWeatherRepository
import naganohara.mirai.weather.model.nextweathermodel.hourlyweathers.HourlyWeatherModel

class HourlyWeatherViewModel(private val repository: HourlyWeatherRepository) : ViewModel() {

    suspend fun getWeather(lat: String, lon: String, hourly: String, weatherCode: String, forecastDays: Int, timezone: String) {
        repository.getWeather(lat, lon ,hourly, weatherCode, forecastDays, timezone)
    }

    val weatherLiveData: LiveData<HourlyWeatherModel>
        get() = repository.weatherLiveData

}