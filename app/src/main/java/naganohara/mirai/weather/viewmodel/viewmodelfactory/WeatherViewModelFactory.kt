package naganohara.mirai.weather.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naganohara.mirai.weather.data.repository.WeatherRepository
import naganohara.mirai.weather.viewmodel.WeatherViewModel

class WeatherViewModelFactory(private val repository: WeatherRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return WeatherViewModel(repository) as T
    }

}