package com.miles.straymaps.ui.screens.new_reports.stray_animals


import android.content.ActivityNotFoundException
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.PermissionState
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.miles.straymaps.R
import kotlinx.coroutines.launch


// Composable function that shows a screen users can enter information in for reporting stray animals
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ReportAStrayAnimal(
    onBackClick: () -> Unit,
    restartApp: (String) -> Unit,
    viewModel: StrayAnimalReportScreenViewModel = hiltViewModel()
) {
    val openAlertDialog = remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    val snackbarHostState = remember { SnackbarHostState() }

    val strayReportUpsertEvent by viewModel.reportUploadSnackbarState.collectAsState()

    val cameraPermissionState = rememberPermissionState(android.Manifest.permission.CAMERA)

    val mediaPermissionState =
        rememberPermissionState(android.Manifest.permission.READ_MEDIA_IMAGES)

    val resultBitmap by viewModel.imagePath.collectAsState()

    var sizeOfImageShown by remember { mutableStateOf(IntSize.Zero) }


    // Launcher for gallery
    val launcherForPickingImageFromGallery = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.getMetaDataThenResizeAndSave(
                uri,
                sizeOfImageShown.width,
                sizeOfImageShown.height
            )
        }
        openAlertDialog.value = false
    }


    var cameraLaunchedSuccessfully by remember { mutableStateOf(false) }


    //Launcher for camera
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            viewModel.capturedImagePath.value?.let { uri ->
                viewModel.getMetaDataThenResizeAndSave(
                    uri,
                    sizeOfImageShown.width,
                    sizeOfImageShown.height
                )
            }
            cameraLaunchedSuccessfully = true
        }
        openAlertDialog.value = false
    }

    LaunchedEffect(viewModel.capturedImagePath) {
        viewModel.capturedImagePath.collect { capturedPictureUri ->
            capturedPictureUri?.let {
                Log.d("CameraLaunch", "Launching camera with URI: $it")
                try {
                    takePictureLauncher.launch(it)
                } catch (e: ActivityNotFoundException) {
                    Log.e("StartingCamera", "Camera app not found", e)
                }
            }
        }
    }


    // Opens AlertDialog for the user to choose whether they want to choose a photo from their device's
    // gallery, or take a photo with their camera, while also checking for camera permission
    if (openAlertDialog.value) {
        PhotoAlertDialog(
            cameraPermissionState = cameraPermissionState,
            mediaPermissionState = mediaPermissionState,
            onDismissRequest = { openAlertDialog.value = false },
            onPickFromGallery = {
                launcherForPickingImageFromGallery.launch("image/*")
            },
            onTakeAPhoto = {
                viewModel.imageProcessing()
            }
        )
    }

    // Launched effect that checks if the user is logged in
    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp)
    }

    // Launched effect that checks if the user successfully saved a report
    LaunchedEffect(strayReportUpsertEvent) {
        if (strayReportUpsertEvent == true) {
            snackbarHostState.showSnackbar("Report filed successfully!")
            viewModel.clearErrorState()
            viewModel.resetStrayAnimalReportFields(true)
        } else if (strayReportUpsertEvent == false) {
            snackbarHostState.showSnackbar("Error filing the report!")
            viewModel.clearErrorState()
        }
    }
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.file_a_stray_animal_report_top_app_bar_text),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onBackClick() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.arrow_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },

        snackbarHost = {
            SnackbarHost(
                snackbarHostState
            )
        },

        ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(dimensionResource(id = R.dimen.padding_large))
                .fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(
                    modifier = Modifier.weight(0.3f)
                )
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(2f)
                ) {
                    resultBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    openAlertDialog.value = true
                                }
                                .onSizeChanged { newSize ->
                                    sizeOfImageShown = newSize
                                }

                        )
                    }
                    Text(
                        text = stringResource(id = R.string.please_upload_a_photo),
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(
                    modifier = Modifier.weight(0.3f)
                )
            }
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            LazyColumn(
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth(),
                contentPadding = PaddingValues(dimensionResource(id = R.dimen.padding_medium))
            ) {
                item {
                    Text(
                        text = stringResource(id = R.string.required_fields_stray),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalType,
                        onValueChange = { type -> viewModel.updateStrayAnimalReportType(type) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.type_of_animal),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = viewModel.strayAnimalReport.strayAnimalType.isEmpty()
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalColour,
                        onValueChange = { colour -> viewModel.updateStrayAnimalReportColour(colour) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_colour),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = viewModel.strayAnimalReport.strayAnimalColour.isEmpty()
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalSex,
                        onValueChange = { sex -> viewModel.updateStrayAnimalReportSex(sex) },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_sex),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalAppearanceDescription,
                        onValueChange = { appearance ->
                            viewModel.updateStrayAnimalReportAppearanceDescription(
                                appearance
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_appearance),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = viewModel.strayAnimalReport.strayAnimalAppearanceDescription.isEmpty()
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalLocationDescription,
                        onValueChange = { location ->
                            viewModel.updateStrayAnimalReportLocation(
                                location
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_location),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isError = viewModel.strayAnimalReport.strayAnimalLocationDescription.isEmpty()
                    )
                }
                item {
                    viewModel.strayAnimalReport.strayAnimalMicrochipID?.let {
                        OutlinedTextField(
                            value = it,
                            onValueChange = { id -> viewModel.updateStrayAnimalReportMicrochipId(id) },
                            label = {
                                Text(
                                    text = stringResource(id = R.string.animal_microchip),
                                    style = MaterialTheme.typography.bodySmall
                                )
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalContactInformation,
                        onValueChange = { info ->
                            viewModel.updateStrayAnimalReportContactInformation(
                                info
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_contact_information),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        value = viewModel.strayAnimalReport.strayAnimalAdditionalInformation,
                        onValueChange = { info ->
                            viewModel.updateStrayAnimalReportAdditionalInformation(
                                info
                            )
                        },
                        label = {
                            Text(
                                text = stringResource(id = R.string.animal_additional_information),
                                style = MaterialTheme.typography.bodySmall
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_large)))
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    viewModel.saveStrayAnimalReport()
                                }
                            }
                        ) {
                            Text("Finish")
                        }
                    }
                }
            }
        }
    }
}

//Composable function that displays an Alert Dialog where the user can pick the source of the photo
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PhotoAlertDialog(
    cameraPermissionState: PermissionState,
    mediaPermissionState: PermissionState,
    onDismissRequest: () -> Unit,
    onTakeAPhoto: () -> Unit,
    onPickFromGallery: () -> Unit,
) {
    if (cameraPermissionState.status.isGranted) {
        Text(stringResource(id = R.string.camera_permission_granted))
    } else {
        Column {
            val textToShow = if (cameraPermissionState.status.shouldShowRationale) {
                stringResource(id = R.string.Please_grant_camera_permission)
            } else {
                stringResource(id = R.string.Camera_required_for_this_feature)
            }

            Text(textToShow)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Button(
                onClick = {
                    cameraPermissionState.launchPermissionRequest()
                }) {
                Text("Request camera permission.")
            }
        }
    }

    if (mediaPermissionState.status.isGranted) {
        Text(stringResource(id = R.string.media_permission_granted))
    } else {
        Column {
            val textToShow = if (mediaPermissionState.status.shouldShowRationale) {
                stringResource(id = R.string.Please_grant_media_permission)
            } else {
                stringResource(id = R.string.Media_required_for_this_feature)
            }

            Text(textToShow)
            Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
            Button(
                onClick = {
                    mediaPermissionState.launchPermissionRequest()
                }) {
                Text("Request media permission.")
            }
        }
    }

    AlertDialog(
        icon = {
            Icon(
                Icons.Filled.Photo,
                contentDescription = stringResource(id = R.string.photo_icon)
            )
        },
        title = {
            Text(
                text = stringResource(id = R.string.photo_alert_dialog_title)
            )
        },
        text = {
            Text(
                text = stringResource(id = R.string.photo_alert_dialog_body)
            )
        },
        onDismissRequest = {
            onDismissRequest()
        },
        confirmButton = {
            OutlinedButton(
                onClick = {
                    onTakeAPhoto()
                }
            ) {
                Text(stringResource(id = R.string.photo_alert_dialog_camera))
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = {
                    onPickFromGallery()
                }
            ) {
                Text(stringResource(id = R.string.photo_alert_dialog_gallery))
            }
        }
    )
}