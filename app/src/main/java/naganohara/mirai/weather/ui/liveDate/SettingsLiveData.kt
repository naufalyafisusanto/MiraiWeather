package naganohara.mirai.weather.ui.liveDate

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import naganohara.mirai.weather.model.SettingsData

object SettingsLiveData {
    private val settingsLiveData = MutableLiveData<SettingsData>()

    fun getSettingsLiveData(): LiveData<SettingsData> = settingsLiveData

    fun updateSettingsData (newSettingsData: SettingsData) {
        settingsLiveData.value = newSettingsData
    }
}
