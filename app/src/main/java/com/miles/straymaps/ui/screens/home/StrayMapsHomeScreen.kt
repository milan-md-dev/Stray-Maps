package com.miles.straymaps.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.miles.straymaps.R
import com.miles.straymaps.ui.screens.FakeAccountService


// Composable function representing the Home screen with navigation options
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StrayMapsHomeScreen(
    onStraySpotterButtonClicked: () -> Unit,
    onLostPetsButtonClicked: () -> Unit,
    onStraySelection: () -> Unit,
    onPetSelection: () -> Unit,
    onWhereToGoButtonClicked: () -> Unit,
    onFeedAStrayButtonClicked: () -> Unit,
    restartApp: (String) -> Unit,
    onSignInClicked: () -> Unit,
    onSignUpClicked: () -> Unit,
    viewModel: StrayMapsHomeScreenViewModel = hiltViewModel()
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var showUserInfo by remember { mutableStateOf(false) }
    var showExitAppDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showFiledReportsSelectionDialog by remember { mutableStateOf(false) }

    val isCurrentUserAnonymous by viewModel.isCurrentUserAnonymous.collectAsState()
    val currentUserProfile by viewModel.currentUserProfile.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.initialize(restartApp)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.Black
                ),
                title = {
                    Text(
                        text = stringResource(R.string.app_name),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = { showUserInfo = true }) {
                        Icon(Icons.Filled.Person, "User info", tint = Color.Black)
                    }
                    IconButton(onClick = { showExitAppDialog = true }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, "Exit app", tint = Color.Black)
                    }
                    IconButton(onClick = { showDeleteAccountDialog = true }) {
                        Icon(Icons.Filled.Delete, "Delete account", tint = Color.Black)
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { innerPadding ->
        Row {
            Spacer(modifier = Modifier.weight(0.5f))
            LazyColumn(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(dimensionResource(id = R.dimen.padding_large)),
                modifier = Modifier
                    .padding(innerPadding)
                    .padding(top = dimensionResource(id = R.dimen.padding_medium))
                    .weight(2f)
            ) {
                item {
                    StraySpotter(
                        onClick = { onStraySpotterButtonClicked() },
                        modifier = Modifier
                    )
                }
                item {
                    LostPets(
                        onClick = { onLostPetsButtonClicked() },
                        modifier = Modifier
                    )
                }
                item {
                    SeeFiledReports(
                        onClick = {
                            showFiledReportsSelectionDialog = true
                        },
                        modifier = Modifier
                    )
                }
                item {
                    WhereToGo(
                        onClick = { onWhereToGoButtonClicked() },
                        modifier = Modifier
                    )
                }
                item {
                    FeedAStray(
                        onClick = { onFeedAStrayButtonClicked() },
                        modifier = Modifier
                    )
                }
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
    }

    if (showFiledReportsSelectionDialog) {
        FiledReportSelection(
            onDismissRequest = { showFiledReportsSelectionDialog = false },
            onStraySelection = { onStraySelection() },
            onPetSelection = { onPetSelection() },
            painter = painterResource(id = R.drawable.bebe)
        )
    }

    // Displays a dialog showing the currently signed-in user's information
    if (showUserInfo) {
        Dialog(
            onDismissRequest = { showUserInfo = false }) {
            Card(
                modifier = Modifier
                    .height(300.dp)
                    .fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    if (isCurrentUserAnonymous != true) {
                        item {
                            Text(
                                text = "User information: \n"
                            )
                        }
                        item {
                            currentUserProfile?.let {
                                Text(
                                    text = "Email: ${it.email}",
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(dimensionResource(R.dimen.padding_medium))
                                )
                            }
                        }
                    } else {
                        item {
                            Text(
                                text = stringResource(R.string.anonymous_account_message),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))
                            )
                        }
                        item {
                            ElevatedButton(
                                onClick = { onSignInClicked() },
                                modifier = Modifier
                                    .padding(dimensionResource(id = R.dimen.padding_small))
                            ) {
                                Text(
                                    text = stringResource(id = R.string.sign_in)
                                )
                            }
                        }
                        item {
                            ElevatedButton(
                                onClick = { onSignUpClicked() },
                                modifier = Modifier
                                    .padding(dimensionResource(id = R.dimen.padding_small))
                            ) {
                                Text(
                                    text = stringResource(id = R.string.sign_up)
                                )
                            }
                        }

                    }
                }
            }
        }
    }

    if (showExitAppDialog) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.sign_out)) },
            text = { Text(stringResource(id = R.string.sign_out_question)) },
            dismissButton = {
                Button(onClick = {
                    showExitAppDialog = false
                }) {
                    Text(
                        text = stringResource(id = R.string.no)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onSignOutClick()
                    showExitAppDialog = false
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            onDismissRequest = { showExitAppDialog = false }
        )
    }

    if (showDeleteAccountDialog) {
        AlertDialog(
            title = { Text(stringResource(id = R.string.delete_account)) },
            text = { Text(stringResource(id = R.string.delete_account_question)) },
            dismissButton = {
                Button(onClick = {
                    showDeleteAccountDialog = false
                }) {
                    Text(
                        text = stringResource(id = R.string.no)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.onDeleteAccountClick()
                    showDeleteAccountDialog = false
                }) {
                    Text(text = stringResource(id = R.string.yes))
                }
            },
            onDismissRequest = { showDeleteAccountDialog = false }
        )
    }
}

// Displays an interactive button for users to report stray animal sightings
@Composable
fun StraySpotter(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.strayspotter),
            contentDescription = stringResource(id = R.string.stray_spotter),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Text(
            text = stringResource(id = R.string.stray_spotter),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// Displays an interactive button for users to report their lost pets
@Composable
fun LostPets(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.lostandfound),
            contentDescription = stringResource(id = R.string.lost_and_found),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Text(
            text = stringResource(id = R.string.lost_and_found),
            style = MaterialTheme.typography.titleMedium

        )
    }
}

// Displays an interactive button that allows users to see already filed reports of either stray animals or lost pets
@Composable
fun SeeFiledReports(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.filedreports),
            contentDescription = stringResource(id = R.string.already_filed_reports),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height((dimensionResource(id = R.dimen.padding_small))))
        Text(
            text = stringResource(id = R.string.already_filed_reports),
            style = MaterialTheme.typography.titleMedium
        )
    }
}

// Displays a map with nearby veterinary clinics and animal shelters using Mapbox
@Composable
fun WhereToGo(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.wheretogo),
            contentDescription = stringResource(id = R.string.where_to_go),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Text(
            text = stringResource(id = R.string.where_to_go),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

// Displays an interactive button that leads users to a screen where they can donate money to animal shelters
@Composable
fun FeedAStray(
    onClick: () -> Unit,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
    ) {
        Image(
            painter = painterResource(id = R.drawable.feedastray),
            contentDescription = stringResource(id = R.string.feed_a_stray),
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(128.dp)
                .clip(CircleShape)
                .clickable(onClick = { onClick() })
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_small)))
        Text(
            text = stringResource(id = R.string.feed_a_stray),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
    }
}

// Dialog that lets the users choose whether they want to see filed reports on stray animals or lost pets
@Composable
fun FiledReportSelection(
    onDismissRequest: () -> Unit,
    onStraySelection: () -> Unit,
    onPetSelection: () -> Unit,
    painter: Painter
) {
    Dialog(
        onDismissRequest = { onDismissRequest() }
    ) {
        Card(
            modifier = Modifier
                .padding(dimensionResource(id = R.dimen.padding_large)),
            shape = RoundedCornerShape(dimensionResource(id = R.dimen.padding_medium))
        ) {
            Column(
                modifier = Modifier
                    .padding(dimensionResource(id = R.dimen.padding_medium)),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painter,
                    contentDescription = null,
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = stringResource(id = R.string.filed_report_selection),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ElevatedButton(
                        onClick = { onStraySelection() },
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        Text("Stray animals", maxLines = 2)
                    }
                    ElevatedButton(
                        onClick = { onPetSelection() },
                        modifier = Modifier
                            .padding(dimensionResource(id = R.dimen.padding_small))
                    ) {
                        Text("Lost pets")
                    }

                }

            }
        }
    }
}


@Preview
@Composable
fun StrayMapsHomeScreenPreview() {
    StrayMapsHomeScreen(
        onStraySpotterButtonClicked = {},
        onLostPetsButtonClicked = {},
        onStraySelection = {},
        onPetSelection = {},
        onWhereToGoButtonClicked = {},
        onFeedAStrayButtonClicked = {},
        restartApp = {},
        onSignInClicked = {},
        onSignUpClicked = {},
        viewModel = FakeHomeScreenViewModel()
    )
}

class FakeHomeScreenViewModel : StrayMapsHomeScreenViewModel(
    accountService = FakeAccountService()
)

