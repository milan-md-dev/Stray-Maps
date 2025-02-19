package com.miles.straymaps.ui.screens.nearby_clinics

import android.util.Log
import androidx.lifecycle.viewModelScope
import com.mapbox.common.location.AccuracyLevel
import com.mapbox.common.location.DeviceLocationProvider
import com.mapbox.common.location.IntervalSettings
import com.mapbox.common.location.LocationProviderRequest
import com.mapbox.common.location.LocationService
import com.mapbox.common.location.LocationServiceFactory
import com.mapbox.geojson.Point
import com.mapbox.search.discover.Discover
import com.mapbox.search.discover.DiscoverQuery
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NearbyClinicsViewModel @Inject constructor() : StrayMapsViewModel() {

    // Function for getting the user's location and then using that to
    // search for nearby vet clinics
    fun getUserLocationAndVetClinicLocations() {
        viewModelScope.launch {
            val userLocationSuccess = getUserLocation()
            if (userLocationSuccess != null) {
                loadData()
            } else {
                Log.e(TAG, "Failed to get user location.")
            }
        }
    }

    private val TAG = "NearbyClinicsViewModel"

    private val discover = Discover.create()

    private val locationService: LocationService = LocationServiceFactory.getOrCreate()

    private var locationProvider: DeviceLocationProvider? = null

    private var userLocation: Point? = null

    val query = DiscoverQuery.Category.create("VETERINARY")

    private val _vetClinics = MutableStateFlow<List<Point>?>(null)
    val vetClinics = _vetClinics.asStateFlow()

    private fun getUserLocation() {
        val request = LocationProviderRequest.Builder()
            .interval(
                IntervalSettings.Builder().interval(0L).minimumInterval(0L).maximumInterval(0L)
                    .build()
            )
            .displacement(0F)
            .accuracy(AccuracyLevel.HIGHEST)
            .build()

        val result = locationService.getDeviceLocationProvider(request)
        if (result.isValue) {
            locationProvider = result.value!!
            locationProvider!!.getLastLocation { location ->
                if (location != null) {
                    userLocation = Point.fromLngLat(location.longitude, location.latitude)
                } else {
                    Log.e(TAG, "Failed to get location.")
                }
            }
        } else {
            Log.e(TAG, "Failed to get device location provider.")
        }
    }

    private fun loadData() {
        viewModelScope.launch(Dispatchers.IO) {
            val response = userLocation?.let { discover.search(query, it) }
            val isResponseValid = response?.isValue
            if (isResponseValid == true) {
                _vetClinics.value =
                    response.value?.mapNotNull { result ->
                        result.coordinate.let { coord ->
                            Point.fromLngLat(coord.longitude(), coord.latitude())
                        }
                    }
            }
        }
    }
}