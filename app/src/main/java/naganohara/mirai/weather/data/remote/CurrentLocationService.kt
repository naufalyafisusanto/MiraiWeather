package naganohara.mirai.weather.data.remote

import naganohara.mirai.weather.model.CurrentLocationData
import retrofit2.Response
import retrofit2.http.GET

interface CurrentLocationService {

    @GET("/json")
    suspend fun getApproximateLocation(): Response<CurrentLocationData>

}