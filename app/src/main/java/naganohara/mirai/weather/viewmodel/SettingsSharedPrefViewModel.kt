package naganohara.mirai.weather.viewmodel

import androidx.lifecycle.ViewModel
import naganohara.mirai.weather.data.repository.SettingsSharedPrefRepository
import naganohara.mirai.weather.model.SettingsData

class SettingsSharedPrefViewModel(private val settingsRepository: SettingsSharedPrefRepository): ViewModel() {

    fun getData(): SettingsData? {
        return settingsRepository.getData()
    }

    fun sendData(settingsData: SettingsData) {
        settingsRepository.sendData(settingsData)
    }
}