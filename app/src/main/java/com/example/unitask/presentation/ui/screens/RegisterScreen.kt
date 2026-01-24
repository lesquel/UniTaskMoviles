package com.example.unitask.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.unitask.R
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.AuthEvent
import com.example.unitask.presentation.viewmodel.AuthViewModel

@Composable
fun RegisterRoute(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: AuthViewModel = viewModel(factory = AppModule.authViewModelFactory())
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is AuthEvent.RegisterSuccess -> onRegisterSuccess()
                is AuthEvent.LoginSuccess -> { /* Not used here */ }
                is AuthEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }
    
    RegisterScreen(
        username = state.username,
        email = state.email,
        password = state.password,
        confirmPassword = state.confirmPassword,
        isLoading = state.isLoading,
        snackbarHostState = snackbarHostState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onConfirmPasswordChanged = viewModel::onConfirmPasswordChanged,
        onRegister = viewModel::register,
        onNavigateToLogin = onNavigateToLogin
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    username: String,
    email: String,
    password: String,
    confirmPassword: String,
    isLoading: Boolean,
    snackbarHostState: SnackbarHostState,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onConfirmPasswordChanged: (String) -> Unit,
    onRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    
    val isFormValid = username.isNotBlank() && 
            email.isNotBlank() && 
            password.length >= 6 && 
            password == confirmPassword
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.register_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLogin) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_username)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Next
                )
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_email)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            
            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password_label)) },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next
                ),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                supportingText = {
                    if (password.isNotEmpty() && password.length < 6) {
                        Text(
                            text = stringResource(R.string.password_error_short),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.password_confirm_label)) },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                },
                supportingText = {
                    if (confirmPassword.isNotEmpty() && password != confirmPassword) {
                        Text(
                            text = stringResource(R.string.password_error_mismatch),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Button(
                onClick = onRegister,
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && isFormValid
            ) {
                Text(
                    text = if (isLoading) stringResource(R.string.saving) else stringResource(R.string.register_button)
                )
            }
            
            TextButton(onClick = onNavigateToLogin) {
                Text(
                    text = stringResource(R.string.register_have_account) + " " + stringResource(R.string.register_login)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
