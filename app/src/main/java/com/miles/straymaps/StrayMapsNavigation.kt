package com.miles.straymaps


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.miles.straymaps.ui.StrayMapsAppState
import com.miles.straymaps.ui.screens.existing_reports.lost_pets.LostPetExistingReportsScreen
import com.miles.straymaps.ui.screens.existing_reports.stray_animals.StrayAnimalsExistingReportsScreen
import com.miles.straymaps.ui.screens.feed_a_stray.FeedAStrayScreen
import com.miles.straymaps.ui.screens.home.StrayMapsHomeScreen
import com.miles.straymaps.ui.screens.nearby_clinics.StrayMapsMap
import com.miles.straymaps.ui.screens.new_reports.lost_pets.ReportALostPet
import com.miles.straymaps.ui.screens.new_reports.stray_animals.ReportAStrayAnimal
import com.miles.straymaps.ui.screens.sign_in.SignInScreenWithTopAppBar
import com.miles.straymaps.ui.screens.sign_up.SignUpScreenWithTopAppBar
import com.miles.straymaps.ui.screens.splash.SplashScreen
import com.miles.straymaps.ui.screens.welcome.WelcomeScreen


// Sealed class that will be used as Strings for routes in navigation composable
sealed class StrayMapsScreen(val route: String) {
    data object SplashScreen : StrayMapsScreen("splash")
    data object Welcome : StrayMapsScreen("welcome")
    data object Home : StrayMapsScreen("home")
    data object SignUp : StrayMapsScreen("signup")
    data object SignIn : StrayMapsScreen("signin")
    data object ReportAStrayAnimal : StrayMapsScreen("report_stray_animal")
    data object ReportALostPet : StrayMapsScreen("report_lost_pet")
    data object SeeStrayFiledReports : StrayMapsScreen("stray_filed_reports")
    data object SeePetFiledReports : StrayMapsScreen("pet_filed_reports")
    data object LookForNearbyAnimalSheltersAndVetClinics :
        StrayMapsScreen("nearby_shelters/vet_clinics")

    data object DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome : StrayMapsScreen("donate")
}


// Navigation composable function
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StrayMapsApp(
    paddingValues: PaddingValues
) {
    val appState = rememberAppState()

    Scaffold(
        modifier = Modifier
            .padding(paddingValues)
    ) {
        NavHost(
            navController = appState.navController,
            startDestination = StrayMapsScreen.SplashScreen.route,
            modifier = Modifier.padding(it)
        ) {

            strayMapsGraph(appState)
        }
    }
}


@Composable
fun rememberAppState(
    navController: NavHostController = rememberNavController()
) =
    remember(NavController) {
        StrayMapsAppState(navController)
    }


//Navigation graph builder
@RequiresApi(Build.VERSION_CODES.O)
fun NavGraphBuilder.strayMapsGraph(
    appState: StrayMapsAppState
) {
    composable(route = StrayMapsScreen.SplashScreen.route) {
        SplashScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
        )
    }

    composable(route = StrayMapsScreen.Welcome.route) {
        WelcomeScreen(
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) },
            showSignUpPage = { appState.navigate(StrayMapsScreen.SignUp.route) },
            showSignInPage = { appState.navigate(StrayMapsScreen.SignIn.route) },
        )
    }

    composable(route = StrayMapsScreen.SignUp.route) {
        SignUpScreenWithTopAppBar(
            onBackClick = { appState.popUp() },
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }

    composable(route = StrayMapsScreen.SignIn.route) {
        SignInScreenWithTopAppBar(
            onBackClick = { appState.popUp() },
            openAndPopUp = { route, popUp -> appState.navigateAndPopUp(route, popUp) }
        )
    }

    composable(route = StrayMapsScreen.Home.route) {
        StrayMapsHomeScreen(
            onStraySpotterButtonClicked = {
                appState.navigate(StrayMapsScreen.ReportAStrayAnimal.route)
            },
            onLostPetsButtonClicked = {
                appState.navigate(StrayMapsScreen.ReportALostPet.route)
            },
            onStraySelection = {
                appState.navigate(StrayMapsScreen.SeeStrayFiledReports.route)
            },
            onPetSelection = {
                appState.navigate(StrayMapsScreen.SeePetFiledReports.route)
            },
            onWhereToGoButtonClicked = {
                appState.navigate(StrayMapsScreen.LookForNearbyAnimalSheltersAndVetClinics.route)
            },
            onFeedAStrayButtonClicked = {
                appState.navigate(StrayMapsScreen.DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome.route)
            },
            restartApp = { route ->
                appState.clearAndNavigate(route)
            },
            onSignInClicked = {
                appState.navigate(StrayMapsScreen.SignIn.route)
            },
            onSignUpClicked = {
                appState.navigate(StrayMapsScreen.SignUp.route)
            }
        )
    }

    composable(route = StrayMapsScreen.ReportAStrayAnimal.route) {
        ReportAStrayAnimal(
            restartApp = { route -> appState.clearAndNavigate(route) },
            onBackClick = { appState.popUp() }
        )
    }

    composable(route = StrayMapsScreen.ReportALostPet.route) {
        ReportALostPet(
            restartApp = { route -> appState.clearAndNavigate(route) },
            onBackClick = { appState.popUp() }
        )
    }

    composable(route = StrayMapsScreen.SeeStrayFiledReports.route) {
        StrayAnimalsExistingReportsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
            onBackClick = { appState.popUp() }
        )
    }

    composable(route = StrayMapsScreen.SeePetFiledReports.route) {
        LostPetExistingReportsScreen(
            restartApp = { route -> appState.clearAndNavigate(route) },
            onBackClick = { appState.popUp() }
        )
    }

    composable(route = StrayMapsScreen.LookForNearbyAnimalSheltersAndVetClinics.route) {
        StrayMapsMap(
            onBackClick = { appState.popUp() }
        )
    }

    composable(route = StrayMapsScreen.DonateMoneyToHelpFeedAndHouseAnimalsWithoutAHome.route) {
        FeedAStrayScreen()
    }
}