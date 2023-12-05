package naganohara.mirai.weather.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naganohara.mirai.weather.data.repository.LocationSharedPrefRepository
import naganohara.mirai.weather.viewmodel.LocationSharedPrefViewModel

class LocationSharedPrefViewModelFactory(private val repository: LocationSharedPrefRepository) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return LocationSharedPrefViewModel(repository) as T
    }

}