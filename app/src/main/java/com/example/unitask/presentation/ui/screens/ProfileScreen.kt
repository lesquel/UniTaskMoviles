package com.example.unitask.presentation.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import com.example.unitask.R
import com.example.unitask.di.AppModule
import com.example.unitask.presentation.viewmodel.ProfileViewModel
import kotlinx.coroutines.launch
import java.io.File

/**
 * Ruta para la pantalla de perfil.
 */
@Composable
fun ProfileRoute(
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val viewModel: ProfileViewModel = viewModel(factory = AppModule.profileViewModelFactory())
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    
    ProfileScreen(
        username = state.username,
        email = state.email,
        profileImagePath = state.profileImagePath,
        totalXp = state.totalXp,
        level = state.level,
        tasksCompleted = state.tasksCompleted,
        currentStreak = state.currentStreak,
        snackbarHostState = snackbarHostState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onEmailChanged = viewModel::onEmailChanged,
        onImageSelected = { uri ->
            viewModel.onImageSelected(context, uri)
        },
        onSave = {
            scope.launch {
                val success = viewModel.saveProfile()
                if (success) {
                    snackbarHostState.showSnackbar(
                        context.getString(R.string.profile_save)
                    )
                }
            }
        },
        onLogout = onLogout
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    username: String,
    email: String,
    profileImagePath: String?,
    totalXp: Int,
    level: Int,
    tasksCompleted: Int,
    currentStreak: Int,
    snackbarHostState: SnackbarHostState,
    onUsernameChanged: (String) -> Unit,
    onEmailChanged: (String) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onSave: () -> Unit,
    onLogout: () -> Unit
) {
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageSelected(it) }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.profile_title)) }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Foto de perfil
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .clickable { imagePickerLauncher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (profileImagePath != null && File(profileImagePath).exists()) {
                    Image(
                        painter = rememberAsyncImagePainter(File(profileImagePath)),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(60.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                // Overlay de cámara
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.BottomEnd
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = stringResource(R.string.profile_change_photo),
                        modifier = Modifier
                            .padding(8.dp)
                            .size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            
            Text(
                text = stringResource(R.string.profile_change_photo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Campos de edición
            OutlinedTextField(
                value = username,
                onValueChange = onUsernameChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_username)) },
                singleLine = true
            )
            
            OutlinedTextField(
                value = email,
                onValueChange = onEmailChanged,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(stringResource(R.string.profile_email)) },
                singleLine = true
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Estadísticas
            Text(
                text = stringResource(R.string.profile_stats),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.fillMaxWidth()
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatRow(
                        label = stringResource(R.string.rewards_level_label, level),
                        value = stringResource(R.string.rewards_xp_label, totalXp)
                    )
                    StatRow(
                        label = stringResource(R.string.profile_tasks_completed),
                        value = tasksCompleted.toString()
                    )
                    StatRow(
                        label = stringResource(R.string.profile_current_streak),
                        value = "$currentStreak días"
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Botones
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_save))
            }
            
            OutlinedButton(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.profile_logout))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun StatRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
