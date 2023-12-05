package naganohara.mirai.weather.ui.base

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import naganohara.mirai.weather.R
import naganohara.mirai.weather.adapter.SearchCitiesAdapter
import naganohara.mirai.weather.data.RetrofitHelper
import naganohara.mirai.weather.data.local.LocationSharedPrefService
import naganohara.mirai.weather.data.remote.GeoLocationService
import naganohara.mirai.weather.data.repository.GeoLocationRepository
import naganohara.mirai.weather.data.repository.LocationSharedPrefRepository
import naganohara.mirai.weather.databinding.ActivitySearchBinding
import naganohara.mirai.weather.model.CityLocationData
import naganohara.mirai.weather.util.AppConstants
import naganohara.mirai.weather.util.InternetConnection
import naganohara.mirai.weather.viewmodel.GeoLocationViewModel
import naganohara.mirai.weather.viewmodel.LocationSharedPrefViewModel
import naganohara.mirai.weather.viewmodel.viewmodelfactory.GeoLocationViewModelFactory
import naganohara.mirai.weather.viewmodel.viewmodelfactory.LocationSharedPrefViewModelFactory
import java.text.Normalizer

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchBinding

    private lateinit var geoLocationViewModel: GeoLocationViewModel

    private lateinit var citiesList: CityLocationData

    private lateinit var locationSharedPrefViewModel: LocationSharedPrefViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Changing status bar color to black
        setStatusBarColor()

        initLocationSharedPrefThing()
        initGeoLocationAPIThing()

        //Getting location data from sharedPref
        val locationSharedPrefData = locationSharedPrefViewModel.getData()

        //Observing LiveData from GeoLocationViewModel
        geoLocationViewModel.locationLiveData.observe(this@SearchActivity) {
            if(!it.isEmpty()) {
                citiesList = it

                for(city in citiesList) {
                    if(locationSharedPrefData != null &&
                        areEqualIgnoringDiacritics(city.name, locationSharedPrefData.city) &&
                        areEqualIgnoringDiacritics(city.country, locationSharedPrefData.country)
                    ) {
                        if(city.state.isNullOrEmpty() || areEqualIgnoringDiacritics(city.state, locationSharedPrefData.region)) {
                            city.alreadyExist = true
                            break
                        }
                    }
                }

                setSearchCitiesAdapter()
                binding.searchPlaceholderTV.visibility = View.GONE
                binding.citiesRecyclerView.visibility = View.VISIBLE
            } else {
                binding.searchPlaceholderTV.text = getString(R.string.no_results)
                binding.searchPlaceholderTV.visibility = View.VISIBLE
                binding.citiesRecyclerView.visibility = View.GONE
            }
        }

        //On Press Back Navigation Button
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        //On Press Back ImageButton
        binding.cancelSearch.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        //SearchView Text Listener
        binding.searchCity.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.citiesRecyclerView.visibility = View.GONE

                if(query != null) {
                    binding.searchPlaceholderTV.visibility = View.VISIBLE

                    if(query.trim().isEmpty()) {
                        binding.searchPlaceholderTV.visibility = View.GONE
                    } else if(query.trim().length <= 2) {
                        binding.searchPlaceholderTV.text = getString(R.string.search_your_city)
                    } else {
                        binding.searchPlaceholderTV.text = getString(R.string.searching)
                        if(InternetConnection.isNetworkAvailable(this@SearchActivity)) {
                            lifecycleScope.launch(Dispatchers.IO) {
                                geoLocationViewModel.getLocation(
                                    query,
                                    AppConstants.CITY_LIMITS,
                                    resources.getString(R.string.api_key)
                                )
                            }
                        } else {
                            val snackBar =
                                Snackbar.make(binding.root, "No internet connection.", Snackbar.LENGTH_INDEFINITE)
                            snackBar.setAction(R.string.try_again) {
                                if (InternetConnection.isNetworkAvailable(this@SearchActivity)) {
                                    lifecycleScope.launch {
                                        geoLocationViewModel.getLocation(
                                            query,
                                            AppConstants.CITY_LIMITS,
                                            resources.getString(R.string.api_key)
                                        )
                                    }
                                } else {
                                    Toast.makeText(
                                        this@SearchActivity,
                                        "No internet connection. Please try later.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }.show()
                        }

                    }
                } else {
                    binding.searchPlaceholderTV.visibility = View.GONE
                }

                return false
            }

            override fun onQueryTextChange(newCity: String?): Boolean {
                binding.citiesRecyclerView.visibility = View.GONE

                if(newCity != null) {
                    binding.searchPlaceholderTV.visibility = View.VISIBLE

                    if(newCity.trim().isEmpty()) {
                        binding.searchPlaceholderTV.visibility = View.GONE
                    } else if(newCity.trim().length <= 2) {
                        binding.searchPlaceholderTV.text = getString(R.string.search_your_city)
                    } else {
                        binding.searchPlaceholderTV.text = getString(R.string.click_search)
                    }
                } else {
                    binding.citiesRecyclerView.visibility = View.GONE
                }

                return false
            }
        })
    }

    private fun removeDiacritics(input: String): String {
        if(input.isEmpty()) return ""
        val normalizedString = Normalizer.normalize(input, Normalizer.Form.NFD)
        return "\\p{InCombiningDiacriticalMarks}+".toRegex().replace(normalizedString, "")
    }

    private fun areEqualIgnoringDiacritics(str1: String, str2: String): Boolean {
        val normalizedStr1 = removeDiacritics(str1)
        val normalizedStr2 = removeDiacritics(str2)
        return normalizedStr1.equals(normalizedStr2, ignoreCase = true)
    }

    private fun initLocationSharedPrefThing() {
        val locationSharedPrefService = LocationSharedPrefService(this)
        val locationSharedPrefRepository = LocationSharedPrefRepository(locationSharedPrefService)

        locationSharedPrefViewModel = ViewModelProvider(this@SearchActivity, LocationSharedPrefViewModelFactory(locationSharedPrefRepository))[LocationSharedPrefViewModel::class.java]
    }

    private fun initGeoLocationAPIThing() {
        //Initialization of GeoLocationRepository and GeoLocationServices
        val geoLocationService = RetrofitHelper.getInstance(AppConstants.OpenWeatherMap_API_BASE_URL).create(GeoLocationService::class.java)
        val geoLocationRepository = GeoLocationRepository(geoLocationService)

        //Initialization of GeoLocationViewModel
        geoLocationViewModel = ViewModelProvider(this@SearchActivity, GeoLocationViewModelFactory(geoLocationRepository))[GeoLocationViewModel::class.java]
    }

    private fun setStatusBarColor() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.black)
    }

    private fun setSearchCitiesAdapter() {
        binding.citiesRecyclerView.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)

        val adapter = SearchCitiesAdapter(citiesList)
        binding.citiesRecyclerView.adapter = adapter
    }

}