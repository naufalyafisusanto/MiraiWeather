package naganohara.mirai.weather.model
data class WeatherHourlyCardData(
    val time: String,
    val weatherIcon: Int,
    val weatherValue: String,
    val isActive: Boolean)
