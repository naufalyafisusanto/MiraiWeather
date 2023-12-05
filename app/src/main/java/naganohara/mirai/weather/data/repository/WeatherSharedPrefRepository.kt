package naganohara.mirai.weather.data.repository

import naganohara.mirai.weather.data.local.WeatherSharedPrefService
import naganohara.mirai.weather.model.weathermodel.WeatherData

class WeatherSharedPrefRepository(private val weatherService: WeatherSharedPrefService) {

    fun getData(): WeatherData? {
        return weatherService.getData()
    }

    fun sendData(weatherData: WeatherData) {
        weatherService.sendData(weatherData)
    }

}