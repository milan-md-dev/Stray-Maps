package com.miles.straymaps.ui.screens.existing_reports.stray_animals

import android.os.Build
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.miles.straymaps.R
import com.miles.straymaps.data.stray_animal.StrayAnimal
import com.miles.straymaps.data.toLocalDateTime


enum class SortCriteria {
    TYPE,
    COLOUR,
    SEX,
    DATE
}

// Composable for seeing already existing stray animal reports
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayAnimalsExistingReportsScreen(
    restartApp: (String) -> Unit,
    onBackClick: () -> Unit,
    viewModel: StrayAnimalsExistingReportsViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val allStrayAnimalReports by viewModel.allStrayAnimalFiledReportState.collectAsState()
    var sortByMenu by remember { mutableStateOf(false) }
    val searchAlertDialog = remember { mutableStateOf(false) }


    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp)
    }


    if (searchAlertDialog.value) {
        SearchAlertDialog(
            userInput = viewModel.userInputMicrochipID,
            onValueChange = viewModel::updateUserInputMicrochipId,
            onDismissRequest = { searchAlertDialog.value = false },
            confirmButton = {
                if (viewModel.userInputMicrochipID != "") {
                    viewModel.findStrayAnimalReportByMicrochipId(viewModel.userInputMicrochipID)
                    searchAlertDialog.value = false
                } else if (viewModel.userInputMicrochipID == "") {
                    Toast.makeText(context, "Please enter proper microchip id.", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        )
    }

    val microchipIdReportFound by viewModel.microchipIdReportFound.collectAsState()

    LaunchedEffect(microchipIdReportFound) {
        when (microchipIdReportFound) {
            true -> Toast.makeText(context, "Report found!", Toast.LENGTH_SHORT).show()
            false -> Toast.makeText(context, "Report not found!", Toast.LENGTH_SHORT).show()
            null -> null
        }
    }


    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text(
                        text = stringResource(id = R.string.filed_reports_screen_top_app_bar),
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
                actions = {
                    IconButton(
                        onClick = { viewModel.reloadReports() }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = stringResource(R.string.refresh_reports)
                        )
                    }
                    IconButton(
                        onClick = { searchAlertDialog.value = true }
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = stringResource(id = R.string.search_icon)
                        )
                    }
                    IconButton(
                        onClick = { sortByMenu = !sortByMenu }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(id = R.string.sort_by)
                        )
                    }
                    DropdownMenu(
                        expanded = sortByMenu,
                        onDismissRequest = { sortByMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "Sort by:"
                                )
                            },
                            onClick = {},
                            modifier = Modifier
                                .clickable(enabled = false) {},
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = SortCriteria.TYPE.name
                                )
                            },
                            onClick = {
                                viewModel.getAllStrayReportsByType()
                                sortByMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = SortCriteria.COLOUR.name
                                )
                            },
                            onClick = {
                                viewModel.getAllStrayReportsByColour()
                                sortByMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = SortCriteria.SEX.name
                                )
                            },
                            onClick = {
                                viewModel.getAllStrayReportsBySex()
                                sortByMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = SortCriteria.DATE.name
                                )
                            },
                            onClick = {
                                viewModel.getAllStrayReportsByDate()
                                sortByMenu = false
                            }
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        ReportsScreen(
            reportList = allStrayAnimalReports,
            viewModel = viewModel,
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxWidth()
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ReportsScreen(
    reportList: List<StrayAnimal>,
    viewModel: StrayAnimalsExistingReportsViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        if (reportList.isEmpty()) {
            Text(
                text = stringResource(id = R.string.empty_list),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        } else {
            ListOfReports(
                reportList = reportList,
                viewModel = viewModel,
                modifier = modifier
                    .padding(dimensionResource(id = R.dimen.padding_small))
            )
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ListOfReports(
    reportList: List<StrayAnimal>,
    viewModel: StrayAnimalsExistingReportsViewModel,
    modifier: Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_medium))
    ) {
        items(items = reportList) { strayAnimal ->
            StrayAnimalFiledReportCard(animalReportCard = strayAnimal, viewModel = viewModel)
        }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StrayAnimalFiledReportCard(
    animalReportCard: StrayAnimal,
    viewModel: StrayAnimalsExistingReportsViewModel
) {
    var showDialog by remember { mutableStateOf(false) }
    var imageUri by remember { mutableStateOf("") }

    Card(
        modifier = Modifier
            .padding(dimensionResource(id = R.dimen.padding_medium))
            .fillMaxWidth(),
        shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_small)),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_small))
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
                    AsyncImage(
                        model =
                        if (animalReportCard.strayAnimalPhotoPath == stringResource(R.string.no_image_path)) {
                            ImageRequest.Builder(LocalContext.current)
                                .data(R.drawable.noimageavailable)
                                .crossfade(true)
                                .build()
                        } else {
                            ImageRequest.Builder(LocalContext.current)
                                .data(animalReportCard.strayAnimalPhotoPath)
                                .crossfade(true)
                                .build()
                        },
                        placeholder = painterResource(id = R.drawable.noimageavailable),
                        contentDescription = "Photo of the animal in question.",
                        modifier = Modifier
                            .size(128.dp)
                            .clickable(
                                onClick = {
                                    imageUri = animalReportCard.strayAnimalPhotoPath.toString()
                                    showDialog = true
                                }
                            )
                    )
                }
                Spacer(
                    modifier = Modifier.weight(0.3f)
                )
            }
            Text(
                text = "Type of animal: ${animalReportCard.strayAnimalType}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Colour of the animal: ${animalReportCard.strayAnimalColour}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalSex == "") {
                    "Sex of the animal: Not available"
                } else {
                    "Sex of the animal: ${animalReportCard.strayAnimalSex}"
                },
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Description: ${animalReportCard.strayAnimalAppearanceDescription}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Last known location: ${animalReportCard.strayAnimalLocationDescription}",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalMicrochipID == "") {
                    "Microchip ID: Not available"
                } else {
                    "Microchip ID: ${animalReportCard.strayAnimalMicrochipID}"
                },
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalContactInformation == "") {
                    "Animal contact person: Not available"
                } else {
                    "Animal contact person: ${animalReportCard.strayAnimalContactInformation}"
                },
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = if (animalReportCard.strayAnimalAdditionalInformation == "") {
                    "Additional information: Not available"
                } else {
                    "Additional information: ${animalReportCard.strayAnimalAdditionalInformation}"
                },
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = "Report created: ${
                    animalReportCard.strayAnimalReportDateAndTime?.let {
                        viewModel.formatLocalDateTime(
                            it.toLocalDateTime()
                        )
                    }
                }",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

    if (showDialog && imageUri.isNotEmpty()) {
        Dialog(onDismissRequest = { showDialog = false }) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(id = R.string.expanded_image),
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun SearchAlertDialog(
    userInput: String,
    onValueChange: (String) -> Unit,
    onDismissRequest: () -> Unit,
    confirmButton: (String) -> Unit,
) {
    AlertDialog(
        icon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = null
            )
        },
        text = {
            TextField(
                value = userInput,
                onValueChange = onValueChange,
                label = {
                    Text(
                        text = stringResource(id = R.string.microchip_id_text_field),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )
        },
        onDismissRequest = { onDismissRequest() },
        confirmButton = {
            TextButton(
                onClick = { confirmButton(userInput) }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismissRequest() }) {
                Text("Cancel")
            }
        }
    )
}
