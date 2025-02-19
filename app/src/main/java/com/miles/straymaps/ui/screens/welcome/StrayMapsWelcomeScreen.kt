package com.miles.straymaps.ui.screens.welcome


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.miles.straymaps.R
import com.miles.straymaps.ui.screens.FakeAccountService
import com.miles.straymaps.ui.theme.AppTypography


// This is the first screen that the user sees after opening Stray Maps app
// Sign up leads to the screen for creation of a new account
// Sign in leads the user to a screen where they can enter their (already existing) account info and proceed to sign in
// Continue as guest allows the user to browse the app as a guest (with limited functionality)
// e.g. they can't see photos or locations of animals
@Composable
fun WelcomeScreen(
    openAndPopUp: (String, String) -> Unit,
    showSignUpPage: () -> Unit,
    showSignInPage: () -> Unit,
    viewModel: StrayMapsWelcomeScreenViewModel = hiltViewModel()
) {

    LaunchedEffect(Unit) {
        viewModel.onAppStart(openAndPopUp)
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .background(color = MaterialTheme.colorScheme.primary)
    ) {
        Box {
            Image(
                painter = painterResource(R.drawable.initial_screen_photo),
                contentDescription = stringResource(R.string.initial_photo_description),
                modifier = Modifier
                    .padding(24.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        Spacer(
            modifier = Modifier
                .height(dimensionResource(id = R.dimen.padding_medium))
        )
        Text(
            text = stringResource(id = R.string.welcome_sign),
            style = AppTypography.titleMedium,
            fontSize = 22.sp
        )
        Text(
            text = stringResource(id = R.string.app_name),
            style = AppTypography.titleLarge,
            fontWeight = FontWeight.SemiBold,
            fontSize = 30.sp
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        ElevatedButton(onClick = { showSignUpPage() }) {
            Text(
                text = stringResource(id = R.string.sign_up)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        ElevatedButton(onClick = { showSignInPage() }) {
            Text(
                text = stringResource(id = R.string.sign_in)
            )
        }
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        ElevatedButton(onClick = { viewModel.createAnonymousAccount(openAndPopUp) }) {
            Text(
                text = stringResource(id = R.string.continue_as_guest)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WelcomeScreenPreview() {
    WelcomeScreen(
        openAndPopUp = { _, _ -> },
        showSignUpPage = {},
        showSignInPage = {},
        viewModel = FakeWelcomeScreenViewModel()
    )
}

class FakeWelcomeScreenViewModel : StrayMapsWelcomeScreenViewModel(
    accountService = FakeAccountService()
)

