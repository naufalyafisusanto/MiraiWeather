package naganohara.mirai.weather.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naganohara.mirai.weather.data.remote.WeatherService
import naganohara.mirai.weather.model.weathermodel.WeatherData

class WeatherRepository(private val weatherService: WeatherService) {

    private val weatherLiveData = MutableLiveData<WeatherData>()

    val weatherData: LiveData<WeatherData>
        get() = weatherLiveData

    suspend fun getWeather(lat: String, lon: String, appId: String) {
        val result = weatherService.getWeather(lat, lon, appId)

        if(result.body() != null) {
            weatherLiveData.postValue(result.body())
        }
    }

}