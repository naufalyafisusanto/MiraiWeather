package naganohara.mirai.weather.model

data class SettingsData(var temperatureUnit: String = "°C", var windSpeedUnit: String = "m/s", var atmosphericPressureUnit: String = "atm", var weatherMusic: Boolean = false)
