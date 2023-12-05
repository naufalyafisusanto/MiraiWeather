package naganohara.mirai.weather.data.repository

import naganohara.mirai.weather.data.local.SettingsSharedPrefService
import naganohara.mirai.weather.model.SettingsData

class SettingsSharedPrefRepository(private val settingsService: SettingsSharedPrefService) {

    fun getData(): SettingsData? {
        return settingsService.getData()
    }

    fun sendData(settingsData: SettingsData) {
        settingsService.setData(settingsData)
    }
}