package naganohara.mirai.weather.ui.fragments

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import naganohara.mirai.weather.R
import naganohara.mirai.weather.adapter.HorizontalWeatherAdapter
import naganohara.mirai.weather.data.RetrofitHelper
import naganohara.mirai.weather.data.local.LocationSharedPrefService
import naganohara.mirai.weather.data.local.SettingsSharedPrefService
import naganohara.mirai.weather.data.local.WeatherSharedPrefService
import naganohara.mirai.weather.data.remote.CurrentLocationService
import naganohara.mirai.weather.data.remote.HourlyWeatherService
import naganohara.mirai.weather.data.remote.WeatherService
import naganohara.mirai.weather.data.repository.CurrentLocationRepository
import naganohara.mirai.weather.data.repository.HourlyWeatherRepository
import naganohara.mirai.weather.data.repository.LocationSharedPrefRepository
import naganohara.mirai.weather.data.repository.SettingsSharedPrefRepository
import naganohara.mirai.weather.data.repository.WeatherRepository
import naganohara.mirai.weather.data.repository.WeatherSharedPrefRepository
import naganohara.mirai.weather.databinding.FragmentHomeBinding
import naganohara.mirai.weather.model.CityLocationDataItem
import naganohara.mirai.weather.model.CurrentLocationData
import naganohara.mirai.weather.model.SettingsData
import naganohara.mirai.weather.model.WeatherHourlyCardData
import naganohara.mirai.weather.model.nextweathermodel.hourlyweathers.HourlyWeatherModel
import naganohara.mirai.weather.model.weathermodel.WeatherData
import naganohara.mirai.weather.model.weathermodel.WeatherType
import naganohara.mirai.weather.ui.base.SearchActivity
import naganohara.mirai.weather.ui.liveDate.SearchCitiesLiveData
import naganohara.mirai.weather.ui.liveDate.SettingsLiveData
import naganohara.mirai.weather.util.AppConstants
import naganohara.mirai.weather.util.CountryNameByCode
import naganohara.mirai.weather.util.InternetConnection
import naganohara.mirai.weather.util.TimeUtil
import naganohara.mirai.weather.util.WeatherCodeToIcon
import naganohara.mirai.weather.viewmodel.CurrentLocationViewModel
import naganohara.mirai.weather.viewmodel.HourlyWeatherViewModel
import naganohara.mirai.weather.viewmodel.LocationSharedPrefViewModel
import naganohara.mirai.weather.viewmodel.SettingsSharedPrefViewModel
import naganohara.mirai.weather.viewmodel.WeatherSharedPrefViewModel
import naganohara.mirai.weather.viewmodel.WeatherViewModel
import naganohara.mirai.weather.viewmodel.viewmodelfactory.CurrentLocationViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.HourlyWeatherViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.LocationSharedPrefViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.SettingsSharedPrefViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.WeatherSharedPrefViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.WeatherViewModelFactory
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding

    private lateinit var weatherCardData: ArrayList<WeatherHourlyCardData>

    private lateinit var settingsData: SettingsData
    private lateinit var hourlyWeatherData: HourlyWeatherModel

    private lateinit var settingsDataObserver: Observer<SettingsData>
    private lateinit var citiesDataObserver: Observer<CityLocationDataItem>

    private lateinit var weatherViewModel: WeatherViewModel
    private lateinit var hourlyWeatherViewModel: HourlyWeatherViewModel
    private lateinit var currentLocationViewModel: CurrentLocationViewModel
    private lateinit var locationSharedPrefViewModel: LocationSharedPrefViewModel
    private lateinit var settingSharedPrefViewModel: SettingsSharedPrefViewModel
    private lateinit var weatherSharedPrefViewModel: WeatherSharedPrefViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Setting data in UI
        val currentDate = Date()
        val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
        binding.date.text = dateFormat.format(currentDate)

        binding.hourlyShimmerLayout.startShimmer()

        //First get location then set the location data in local and in UI after that get weather data from API
        initCurrentLocationThing()
        initLocationSharedPrefThing()
        initWeatherSharedPrefThing()
        initSettingsSharedPrefThing()
        initWeatherAPIThing()
        initHourlyWeatherAPIThing()

        //Getting and setting data from LocationSharedPrefViewModel to UI
        var locationSharedPrefData = locationSharedPrefViewModel.getData()
        locationSharedPrefData?.let { setLocationDataToUI(it) }

        //Getting Data from Settings SharedPref
        settingsData = settingSharedPrefViewModel.getData() ?: SettingsData()
        settingsData.weatherMusic = false
        settingsData.temperatureUnit = getString(R.string.celsius)
        settingsData.windSpeedUnit = getString(R.string.meters_per_second)
        settingsData.atmosphericPressureUnit = getString(R.string.atm)
        setSettingDataToUI(settingsData)

        //Observing the livedata from WeatherAPI
        weatherViewModel.weatherLiveData.observe(requireActivity()) {
            if(it != null) {
                setWeatherDataToUI(it)
                weatherSharedPrefViewModel.sendData(it)
            }
        }

        //Getting and setting data from WeatherSharedPref to UI
        val weatherData = weatherSharedPrefViewModel.getData()
        weatherData?.let { setWeatherDataToUI(it) }

        //Observing LiveData from CurrentLocationViewModel
        currentLocationViewModel.approximateLocationLiveData.observe(requireActivity()) {
            if(it != null) {
                if(locationSharedPrefData == null || locationSharedPrefData!!.loc != it.loc && locationSharedPrefData!!.ip.isNotEmpty()) {
                    locationSharedPrefViewModel.sendData(it)
                    locationSharedPrefData = it
                    setLocationDataToUI(it)
                    callingWeatherAPI(it)
                    callingHourlyWeatherAPI(it)
                }
            }
        }

        citiesDataObserver = Observer {
            val state = it.state
            val data = CurrentLocationData(it.name, it.country, "", "${it.lat},${it.lon}", "", state, "")
            locationSharedPrefViewModel.sendData(data)
            locationSharedPrefData = locationSharedPrefViewModel.getData()
            setLocationDataToUI(data)
            callingWeatherAPI(data)
            callingHourlyWeatherAPI(data)
        }
        SearchCitiesLiveData.getCitiesLiveData().observe(requireActivity(), citiesDataObserver)

        //Observing the livedata from HourlyWeatherAPI
        hourlyWeatherViewModel.weatherLiveData.observe(requireActivity()) {
            if(it != null) {
                hourlyWeatherData = it
                changeWeatherToToday()
            }
        }

        callingWeatherAPI(locationSharedPrefData)
        callingHourlyWeatherAPI(locationSharedPrefData)

        binding.searchPage.setOnClickListener {moveToSearch()}

        binding.today.setOnClickListener { changeWeatherToToday() }
    }

    private fun callingHourlyWeatherAPI(locationData: CurrentLocationData?) {
        val loc = locationData?.loc?.split(",")
        if(loc != null) {
            val lat = loc[0]
            val lon = loc[1]
            if(InternetConnection.isNetworkAvailable(requireActivity())) {
                lifecycleScope.launch {
                    hourlyWeatherViewModel.getWeather(lat, lon, AppConstants.HOURLY_WEATHER_QUERY, AppConstants.HOURLY_WEATHER_CODE, AppConstants.HOURLY_WEATHER_DAYS_LIMIT, TimeZone.getDefault().id)
                }
            } else {
                val snackBar = Snackbar.make(requireView(), "No internet connection.", Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction(R.string.try_again) {
                    if(InternetConnection.isNetworkAvailable(requireActivity())) {
                        lifecycleScope.launch {
                            hourlyWeatherViewModel.getWeather(lat, lon, AppConstants.HOURLY_WEATHER_QUERY, AppConstants.HOURLY_WEATHER_CODE, AppConstants.HOURLY_WEATHER_DAYS_LIMIT, TimeZone.getDefault().id)
                        }
                    } else {
                        Toast.makeText(requireActivity(), "No internet connection. Please try later.", Toast.LENGTH_SHORT).show()
                    }
                }.show()
            }
        }
    }

    private fun callingWeatherAPI(locationData: CurrentLocationData?) {
        val loc = locationData?.loc?.split(",")
        if(loc != null) {
            val lat = loc[0]
            val lon = loc[1]
            if(InternetConnection.isNetworkAvailable(requireActivity())) {
                lifecycleScope.launch {
                    weatherViewModel.getWeather(lat, lon, resources.getString(R.string.api_key))
                }
            } else {
                val snackBar = Snackbar.make(requireView(), "No internet connection.", Snackbar.LENGTH_INDEFINITE)
                snackBar.setAction(R.string.try_again) {
                    if(InternetConnection.isNetworkAvailable(requireActivity())) {
                        lifecycleScope.launch {
                            weatherViewModel.getWeather(lat, lon, resources.getString(R.string.api_key))
                        }
                    } else {
                        Toast.makeText(requireActivity(), "No internet connection. Please try later.", Toast.LENGTH_SHORT).show()
                    }
                }.show()
            }
        }
    }

    private fun setLocationDataToUI(currentLocation: CurrentLocationData) {
        binding.cityName.text = currentLocation.city
        binding.countryName.text = CountryNameByCode.getCountryNameByCode(requireActivity(), currentLocation.country)
    }

    private fun setWeatherDataToUI(weatherData: WeatherData) {
        var temp = weatherData.main.temp

        if(settingsData.temperatureUnit == resources.getString(R.string.fahrenheit)) {
            temp = (temp-273.15)*1.8+32
        } else {
            temp -= 273.15
        }

        if(settingsData.atmosphericPressureUnit == resources.getString(R.string.atm)) {
            val pressure = weatherData.main.pressure.toDouble() / 1013.25
            binding.atmosphericPressureValue.text = String.format("%.4f", pressure)
        } else {
            binding.atmosphericPressureValue.text = String.format("%.0f", weatherData.main.pressure.toDouble())
        }

        if(settingsData.windSpeedUnit == resources.getString(R.string.miles_per_hour)) {
            val speed = weatherData.wind.speed*2.237
            binding.windValue.text = String.format("%.2f", speed)
        } else if(settingsData.windSpeedUnit == resources.getString(R.string.kilometers_per_hour)) {
            val speed = weatherData.wind.speed*3.6
            binding.windValue.text = String.format("%.2f", speed)
        } else {
            binding.windValue.text = weatherData.wind.speed.toString()
        }

        setWeatherIcon(weatherData.weather.first())

        binding.weatherNumericValue.text = String.format("%.0f", temp)
        binding.humidityValue.text = weatherData.main.humidity.toString()
        binding.weatherType.text = weatherData.weather.first().main
    }

    private fun setHourlyWeatherDataToUI(hourlyWeatherData: HourlyWeatherModel, isToday: Boolean): Int {
        val hourlyWeather = hourlyWeatherData.hourly

        val time = hourlyWeather.time
        val weather = hourlyWeather.temperature_2m
        val weatherCode = hourlyWeather.weathercode

        weatherCardData = ArrayList()

        var position = 0

        if(isToday) {
            for (i in 0 .. 23) {
                var currTime = TimeUtil.extractTimeFromString(time[i])
                val currWeather = weather[i]
                val weatherIcon = WeatherCodeToIcon.getWeatherIcon(weatherCode[i])
                val isCurrentTime = isCurrentLocalTime(currTime)

                if (isCurrentTime) {
                    currTime = "now"
                    position = i
                }

                val currTimeData = WeatherHourlyCardData(
                    currTime,
                    weatherIcon,
                    String.format("%.0f", currWeather),
                    isCurrentTime
                )

                weatherCardData.add(currTimeData)
            }

            return position
        } else {
            for (i in 24 .. 47) {
                val currTime = TimeUtil.extractTimeFromString(time[i])
                val currWeather = weather[i]
                val weatherIcon = WeatherCodeToIcon.getWeatherIcon(weatherCode[i])

                val currTimeData = WeatherHourlyCardData(
                    currTime,
                    weatherIcon,
                    String.format("%.0f", currWeather),
                    false
                )

                weatherCardData.add(currTimeData)
            }

            return 0
        }
    }

    private fun setSettingDataToUI(settingsData: SettingsData) {
        var temp = binding.weatherNumericValue.text.toString().toDouble()

        if(binding.weatherUnit.text.toString() != settingsData.temperatureUnit ) {
            if (settingsData.temperatureUnit == resources.getString(R.string.celsius)) {
                binding.weatherUnit.text = resources.getString(R.string.celsius)
                temp = (temp - 32) * 5 / 9
            } else {
                binding.weatherUnit.text = resources.getString(R.string.fahrenheit)
                temp = (temp * 9 / 5) + 32
            }
            binding.weatherNumericValue.text = String.format("%.0f", temp)
        }

        if(binding.atmosphericPressureUnit.text.toString() != settingsData.atmosphericPressureUnit) {
            if (settingsData.atmosphericPressureUnit == resources.getString(R.string.atm)) {
                val pressure = binding.atmosphericPressureValue.text.toString().toDouble() / 1013.25
                binding.atmosphericPressureValue.text = String.format("%.4f", pressure)
            } else {
                val pressure = binding.atmosphericPressureValue.text.toString().toDouble() * 1013.25
                binding.atmosphericPressureValue.text = String.format("%.0f", pressure)
            }
            binding.atmosphericPressureUnit.text = settingsData.atmosphericPressureUnit
        }

        if(binding.windUnit.text.toString() != settingsData.windSpeedUnit) {
            val prevWindUnit = binding.windUnit.text.toString()
            val prevWindSpeed = binding.windValue.text.toString().toDouble()
            val newWindSpeed: Double
            if(settingsData.windSpeedUnit == resources.getString(R.string.miles_per_hour)) {
                newWindSpeed = if(prevWindUnit == resources.getString(R.string.kilometers_per_hour)) {
                    prevWindSpeed * 0.621371
                } else {
                    prevWindSpeed * 2.23694
                }
            } else if(settingsData.windSpeedUnit == resources.getString(R.string.kilometers_per_hour)) {
                newWindSpeed = if(prevWindUnit == resources.getString(R.string.miles_per_hour)) {
                    prevWindSpeed * 1.60934
                } else {
                    prevWindSpeed * 3.6
                }
            } else {
                newWindSpeed = if(prevWindUnit == resources.getString(R.string.kilometers_per_hour)) {
                    prevWindSpeed / 3.6
                } else {
                    prevWindSpeed / 2.23694
                }
            }

            binding.windValue.text = String.format("%.2f", newWindSpeed)
        }

        binding.windUnit.text = settingsData.windSpeedUnit
    }

    private fun setWeatherIcon(weatherType: WeatherType) {
        if(weatherType.id in 200..232) { //Thunderstorm
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_thunderstorm_cloud)
        } else if(weatherType.id in 300..321) { //Drizzle
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_rain_cloud)
        } else if(weatherType.id in 500..531) { //Rain
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_sun_rain_cloud)
        } else if(weatherType.id in 701..781) { //Atmosphere
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_cloud_sun)
        } else if(weatherType.id in 600..622) { //Snow
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_snow_cloud)
        } else if(weatherType.id == 800) { //Clear
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_sun)
        } else if(weatherType.id in 801..804) { //Cloud
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_cloud)
        } else {
            binding.weatherIcon.setImageResource(R.drawable.icon_weather_sun)
        }
    }

    private fun isCurrentLocalTime(timeString: String): Boolean {
        val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
        val currentTime = Calendar.getInstance()
        val parsedTime = sdf.parse(timeString)

        if (parsedTime != null) {
            val parsedCalendar = Calendar.getInstance()
            parsedCalendar.time = parsedTime

            return currentTime.get(Calendar.HOUR_OF_DAY) == parsedCalendar.get(Calendar.HOUR_OF_DAY)
        }

        return false
    }

    private fun changeWeatherToToday() {
        val todayTypeface: Typeface = Typeface.createFromAsset(requireActivity().assets, "fonts/Inter-Bold.ttf")

        binding.today.typeface = todayTypeface

        binding.today.setTextColor(requireActivity().getColor(R.color.textColor))

        //Adding Today's Weather Data in array and adapter
        val position = setHourlyWeatherDataToUI(hourlyWeatherData, true)
        setHorizontalWeatherViewAdapter()

        binding.hourlyShimmerLayoutContainer.visibility = View.GONE
        binding.hourlyShimmerLayout.stopShimmer()
        binding.smallWeatherCardView.visibility = View.VISIBLE

        binding.smallWeatherCardView.layoutManager?.scrollToPosition(position)
    }

    private fun setHorizontalWeatherViewAdapter() {
        binding.smallWeatherCardView.layoutManager = LinearLayoutManager(requireActivity(), RecyclerView.HORIZONTAL, false)

        val adapter = HorizontalWeatherAdapter(weatherCardData)
        binding.smallWeatherCardView.adapter = adapter
    }

    private fun moveToSearch() {
        val intent = Intent(requireActivity(), SearchActivity::class.java)
        startActivity(intent)
    }

    private fun initWeatherAPIThing() {
        val weatherService = RetrofitHelper.getInstance(AppConstants.OpenWeatherMap_API_BASE_URL).create(
            WeatherService::class.java)
        val weatherRepository = WeatherRepository(weatherService)

        weatherViewModel = ViewModelProvider(requireActivity(), WeatherViewModelFactory(weatherRepository))[WeatherViewModel::class.java]
    }

    private fun initHourlyWeatherAPIThing() {
        val hourlyWeatherService = RetrofitHelper.getInstance(AppConstants.OpenMeteo_API_BASE_URL).create(
            HourlyWeatherService::class.java
        )
        val hourlyWeatherRepository = HourlyWeatherRepository(hourlyWeatherService)

        hourlyWeatherViewModel = ViewModelProvider(requireActivity(), HourlyWeatherViewModelFactory(hourlyWeatherRepository))[HourlyWeatherViewModel::class.java]
    }

    private fun initCurrentLocationThing() {
        //Initialization of CurrentLocationRepository and CurrentLocationServices
        val currentLocationService = RetrofitHelper.getInstance(AppConstants.CURRENT_LOCATION_API_BASE_URL).create(CurrentLocationService::class.java)
        val currentLocationRepository = CurrentLocationRepository(currentLocationService)

        //Initialization of CurrentLocationViewModel
        currentLocationViewModel = ViewModelProvider(requireActivity(), CurrentLocationViewModelFactory(currentLocationRepository))[CurrentLocationViewModel::class.java]

        //Calling API to get current location
        if(InternetConnection.isNetworkAvailable(requireActivity())) {
            lifecycleScope.launch {
                currentLocationViewModel.getLocation()
            }
        } else {
            val snackBar =
                Snackbar.make(requireView(), "No internet connection.", Snackbar.LENGTH_INDEFINITE)
            snackBar.setAction(R.string.try_again) {
                if (InternetConnection.isNetworkAvailable(requireActivity())) {
                    lifecycleScope.launch {
                        currentLocationViewModel.getLocation()
                    }
                } else {
                    Toast.makeText(
                        requireActivity(),
                        "No internet connection. Please try later.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.show()
        }
    }

    private fun initLocationSharedPrefThing() {
        val locationSharedPrefService = LocationSharedPrefService(requireActivity())
        val locationSharedPrefRepository = LocationSharedPrefRepository(locationSharedPrefService)

        locationSharedPrefViewModel = ViewModelProvider(requireActivity(), LocationSharedPrefViewModelFactory(locationSharedPrefRepository))[LocationSharedPrefViewModel::class.java]
    }

    private fun initSettingsSharedPrefThing() {
        val settingsSharedPrefService = SettingsSharedPrefService(requireActivity())
        val settingsSharedPrefRepository = SettingsSharedPrefRepository(settingsSharedPrefService)

        settingSharedPrefViewModel = ViewModelProvider(requireActivity(), SettingsSharedPrefViewModelFactory(settingsSharedPrefRepository))[SettingsSharedPrefViewModel::class.java]
    }

    private fun initWeatherSharedPrefThing() {
        val weatherSharedPrefService = WeatherSharedPrefService(requireActivity())
        val weatherSharedPrefRepository = WeatherSharedPrefRepository(weatherSharedPrefService)

        weatherSharedPrefViewModel = ViewModelProvider(requireActivity(), WeatherSharedPrefViewModelFactory(weatherSharedPrefRepository))[WeatherSharedPrefViewModel::class.java]
    }

    override fun onDestroy() {
        super.onDestroy()

        SettingsLiveData.getSettingsLiveData().removeObserver(settingsDataObserver)
        SearchCitiesLiveData.getCitiesLiveData().removeObserver(citiesDataObserver)
    }

}