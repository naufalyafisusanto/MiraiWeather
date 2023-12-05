package naganohara.mirai.weather.model

data class CurrentLocationData(
    val city: String,
    val country: String,
    val ip: String,
    val loc: String,
    val postal: String,
    val region: String,
    val timezone: String
)