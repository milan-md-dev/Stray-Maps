package com.miles.straymaps.ui.screens.nearby_clinics

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.maps.extension.compose.MapEffect
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import com.mapbox.maps.extension.compose.annotation.generated.PointAnnotationGroup
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import com.mapbox.maps.extension.style.expressions.dsl.generated.literal
import com.mapbox.maps.extension.style.expressions.generated.Expression
import com.mapbox.maps.plugin.PuckBearing
import com.mapbox.maps.plugin.annotation.AnnotationConfig
import com.mapbox.maps.plugin.annotation.AnnotationSourceOptions
import com.mapbox.maps.plugin.annotation.ClusterOptions
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.locationcomponent.createDefault2DPuck
import com.mapbox.maps.plugin.locationcomponent.location
import com.miles.straymaps.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayMapsMap(
    onBackClick: () -> Unit,
    viewModel: NearbyClinicsViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val mapViewportState = rememberMapViewportState()
    val vetClinics by viewModel.vetClinics.collectAsState()

    val mapMarker = rememberIconImage(
        key = R.drawable.veterinary,
        painter = painterResource(R.drawable.veterinary)
    )

    val permissions = mutableListOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    )

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        permissions.add(Manifest.permission.FOREGROUND_SERVICE_LOCATION)
    }

    var isLocationPermissionGranted by remember {
        mutableStateOf(
            PermissionsManager.areLocationPermissionsGranted(
                context
            )
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permission ->
        val granted = permission[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permission[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        isLocationPermissionGranted = granted

        if (granted) {
            Toast.makeText(context, "Location permission granted!", Toast.LENGTH_SHORT).show()
            // enable location tracking
        } else {
            Toast.makeText(context, "Location permission denied!", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.getUserLocationAndVetClinicLocations()
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(
                        text = stringResource(R.string.map),
                        maxLines = 1
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = { onBackClick() }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.arrow_back)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isLocationPermissionGranted) {
                MapboxMap(
                    modifier = Modifier
                        .fillMaxSize(),
                    mapViewportState = mapViewportState
                ) {
                    MapEffect(Unit) { mapView ->
                        mapView.location.updateSettings {
                            locationPuck = createDefault2DPuck(withBearing = true)
                            enabled = true
                            puckBearing = PuckBearing.COURSE
                            puckBearingEnabled = true
                        }
                        mapViewportState.transitionToFollowPuckState()
                    }

                    vetClinics?.let { it ->
                        PointAnnotationGroup(
                            annotations = it.map {
                                PointAnnotationOptions()
                                    .withPoint(it)
                            },
                            annotationConfig = AnnotationConfig(
                                annotationSourceOptions = AnnotationSourceOptions(
                                    clusterOptions = ClusterOptions(
                                        textColorExpression = Expression.color(Color.YELLOW),
                                        textColor = Color.BLACK,
                                        textSize = 20.0,
                                        circleRadiusExpression = literal(25.0)
                                    )
                                )
                            ),
                        ) {
                            iconImage = mapMarker
                        }
                    }
                }
            } else {
                Text(
                    text = stringResource(R.string.feature_requires_location),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                )

                Spacer(modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium)))

                Button(
                    onClick = {
                        locationPermissionLauncher.launch(
                            permissions.toTypedArray()
                        )
                    }
                ) {
                    Text(stringResource(R.string.allow_location))
                }
            }

        }
    }
}
