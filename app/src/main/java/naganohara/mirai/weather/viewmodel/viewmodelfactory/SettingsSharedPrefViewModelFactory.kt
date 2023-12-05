package naganohara.mirai.weather.viewmodel.viewmodelfactory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import naganohara.mirai.weather.data.repository.SettingsSharedPrefRepository
import naganohara.mirai.weather.viewmodel.SettingsSharedPrefViewModel

class SettingsSharedPrefViewModelFactory(private val repository: SettingsSharedPrefRepository): ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return SettingsSharedPrefViewModel(repository) as T
    }

}