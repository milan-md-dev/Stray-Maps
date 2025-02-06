package com.miles.straymaps.ui.screens.sign_up

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import com.miles.straymaps.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreenWithTopAppBar(
    onBackClick: () -> Unit,
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsSignUpScreenViewModel = hiltViewModel()
    ){

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    val snackbarHostState = remember { SnackbarHostState() }

    val emailNotSupported by viewModel.emailFormatNotSupported.collectAsState()
    val passwordNotSupported by viewModel.passwordFormatNotSupported.collectAsState()
    val passwordsDoNotMatch by viewModel.passwordsDoNotMatch.collectAsState()

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                title = {
                    Text (
                        text = stringResource(id = R.string.sign_up),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {onBackClick()}) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(id = R.string.arrow_back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) {innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .wrapContentSize(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            SignUpScreen(
                openAndPopUp,
                viewModel,
            )
        }
    }

    LaunchedEffect(emailNotSupported, passwordNotSupported, passwordsDoNotMatch) {
        val message = when {
            emailNotSupported == true -> "Email format not valid!"
            passwordNotSupported == true -> "Password must contain at least 6 characters, and at least one number!"
            passwordsDoNotMatch == true -> "Passwords do not match!"
            else -> null
        }

        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearErrorStates()
        }
    }
}

// Composable function that allows the user to create a new account
@Composable
fun SignUpScreen(
    openAndPopUp: (String, String) -> Unit,
    viewModel: StrayMapsSignUpScreenViewModel = hiltViewModel(),
){

    val email = viewModel.signUpEmail.collectAsState()
    val password = viewModel.signUpPassword.collectAsState()
    val confirmPassword = viewModel.signUpConfirmPassword.collectAsState()

    Column(
        modifier = Modifier.padding(dimensionResource(id = R.dimen.padding_medium))) {
        OutlinedTextField(
            value = email.value,
            onValueChange = { email -> viewModel.updateEmail(email) },
            label = {
                Text(stringResource(id = R.string.email))
            }
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        OutlinedTextField(
            value = password.value,
            onValueChange = {password -> viewModel.updatePassword(password)},
            label = {
                Text(stringResource(id = R.string.password))
            }
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        OutlinedTextField(
            value = confirmPassword.value,
            onValueChange = {confirmPassword -> viewModel.updateConfirmPassword(confirmPassword)},
            label = {
                Text(stringResource(id = R.string.confirm_password))
            },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )
        Spacer(modifier = Modifier.height(dimensionResource(id = R.dimen.padding_medium)))
        OutlinedButton(
            onClick = { viewModel.onSignUpClick(openAndPopUp) },
            modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
            Text(
                text = "Sign up"
            )
        }
    }
}
