// FILE: app/src/main/java/com/madaba/supervisorapp/ui/screens/LoginScreen.kt
package com.madaba.supervisorapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.error
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.madaba.supervisorapp.ui.viewmodels.LoginUiState
import com.madaba.supervisorapp.ui.viewmodels.LoginViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {
        when (uiState) {
            is LoginUiState.Success -> {
                onLoginSuccess()
                viewModel.resetState() // Reset state after navigation
            }
            else -> {
                // Handle other states if needed
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        androidx.compose.material3.Text("Supervisor Login", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        androidx.compose.material3.OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { androidx.compose.material3.Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = uiState is LoginUiState.Error
        )
        Spacer(Modifier.height(8.dp))

        androidx.compose.material3.OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { androidx.compose.material3.Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState is LoginUiState.Error
        )
        Spacer(Modifier.height(16.dp))

        when (val state = uiState) {
            is LoginUiState.Loading -> {
                androidx.compose.material3.CircularProgressIndicator()
            }
            is LoginUiState.Error -> {
                androidx.compose.material3.Text(state.message, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Text("Login")
                }
            }
            else -> {
                androidx.compose.material3.Button(
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    androidx.compose.material3.Text("Login")
                }
            }
        }
    }
}