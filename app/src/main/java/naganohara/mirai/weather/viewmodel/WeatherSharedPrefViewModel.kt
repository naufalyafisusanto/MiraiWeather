package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.WeatherSharedPrefRepository
import naganohara.mirai.weather.model.weathermodel.WeatherData

class WeatherSharedPrefViewModel(private val weatherRepository: WeatherSharedPrefRepository): ViewModel() {

    fun getData(): WeatherData? {
        return weatherRepository.getData()
    }

    fun sendData(weatherData: WeatherData) {
        weatherRepository.sendData(weatherData)
    }

}