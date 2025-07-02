package com.example.shopapp.ui.user.profile

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.shopapp.R
import com.example.shopapp.data.dao.FirebaseUserDao
import com.example.shopapp.data.model.User
import com.example.shopapp.data.repository.UserKRepository
import com.example.shopapp.localization.Language
import com.example.shopapp.localization.LanguageManager
import com.example.shopapp.navigation.Screen
import com.example.shopapp.viewmodel.AuthViewModel
import com.example.shopapp.viewmodel.ProfileViewModel
import com.example.shopapp.viewmodel.ProfileViewModelFactory
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    authViewModel: AuthViewModel,
    rootNavController: NavController
) {
    // Initialize repositories and view models
    val userRepository = UserKRepository(FirebaseUserDao())
    val profileViewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(userRepository, FirebaseAuth.getInstance(), authViewModel)
    )

    val userState by profileViewModel.userState.collectAsState()
    val verificationState by profileViewModel.verificationState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for change language
    var showLanguageDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Get string from LanguageManager in context @Composable
    val verificationEmailSentMessage = LanguageManager.getString(R.string.verification_email_sent)

    // Handle errors
    LaunchedEffect(userState) {
        if (userState is ProfileViewModel.UserState.Error) {
            snackbarHostState.showSnackbar((userState as ProfileViewModel.UserState.Error).errorMessage)
        }
    }

    LaunchedEffect(verificationState) {
        when (verificationState) {
            is ProfileViewModel.VerificationState.Error -> {
                snackbarHostState.showSnackbar(
                    (verificationState as ProfileViewModel.VerificationState.Error).errorMessage
                )
            }
            is ProfileViewModel.VerificationState.VerificationSent -> {
                // use string from LanguageManager
                snackbarHostState.showSnackbar(verificationEmailSentMessage)
                profileViewModel.resetVerificationState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = LanguageManager.getString(R.string.my_profile),
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        when (userState) {
            is ProfileViewModel.UserState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is ProfileViewModel.UserState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = LanguageManager.getString(R.string.could_not_load_profile_data),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { profileViewModel.loadUserData() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(LanguageManager.getString(R.string.retry))
                        }
                    }
                }
            }

            is ProfileViewModel.UserState.Success -> {
                val user = (userState as ProfileViewModel.UserState.Success).user
                ProfileContent(
                    user = user,
                    verificationState = verificationState,
                    navController = navController,
                    modifier = Modifier.padding(padding),
                    onLogout = { profileViewModel.signOut(rootNavController) },
                    onSendVerification = { profileViewModel.sendVerificationEmail() },
                    onRefreshVerification = { profileViewModel.checkEmailVerificationStatus() },
                    onChangeLanguage = { showLanguageDialog = true }
                )
            }
        }
    }

    // Dialog language
    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            title = { Text(LanguageManager.getString(R.string.select_language)) },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(onClick = {
                        LanguageManager.setLanguage(Language.ENGLISH, context)
                        showLanguageDialog = false
                    }) {
                        Text(LanguageManager.getString(R.string.english))
                    }
                    TextButton(onClick = {
                        LanguageManager.setLanguage(Language.VIETNAMESE, context)
                        showLanguageDialog = false
                    }) {
                        Text(LanguageManager.getString(R.string.vietnamese))
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text(LanguageManager.getString(R.string.no))
                }
            }
        )
    }
}

@Composable
fun ProfileContent(
    user: User,
    verificationState: ProfileViewModel.VerificationState,
    navController: NavController,
    modifier: Modifier = Modifier,
    onLogout: () -> Unit,
    onSendVerification: () -> Unit,
    onRefreshVerification: () -> Unit,
    onChangeLanguage: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = user.role.replaceFirstChar { it.uppercase() },
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }

        // Email Verification Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LanguageManager.getString(R.string.email_verification),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                when (verificationState) {
                    is ProfileViewModel.VerificationState.Loading -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp),color = MaterialTheme.colorScheme.primary)
                            Text(LanguageManager.getString(R.string.checking_verification_status))
                        }
                    }
                    is ProfileViewModel.VerificationState.Verified -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = LanguageManager.getString(R.string.email_verified),
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    is ProfileViewModel.VerificationState.Unverified -> {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                                Text(
                                    text = LanguageManager.getString(R.string.email_not_verified),
                                    color = MaterialTheme.colorScheme.error,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = onSendVerification,
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )

                            ) {
                                Text(LanguageManager.getString(R.string.send_verification_email))
                            }
                        }
                    }
                    is ProfileViewModel.VerificationState.SendingVerification -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp),color = MaterialTheme.colorScheme.primary)
                            Text(LanguageManager.getString(R.string.sending_verification_email))
                        }
                    }
                    else -> {
                        Text(LanguageManager.getString(R.string.verification_status_unknown))
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onRefreshVerification,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(LanguageManager.getString(R.string.refresh_verification_status))
                }
            }
        }

        // User Details Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LanguageManager.getString(R.string.account_information),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                ListItem(
                    headlineContent = { Text(LanguageManager.getString(R.string.email)) },
                    supportingContent = { Text(user.email) },
                    leadingContent = { Icon(Icons.Default.Email, contentDescription = null) }
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                ListItem(
                    headlineContent = { Text(LanguageManager.getString(R.string.phone)) },
                    supportingContent = { Text(user.phone) },
                    leadingContent = { Icon(Icons.Default.Phone, contentDescription = null) }
                )

                Divider(color = MaterialTheme.colorScheme.outlineVariant)

                ListItem(
                    headlineContent = { Text(LanguageManager.getString(R.string.address)) },
                    supportingContent = { Text(user.address) },
                    leadingContent = { Icon(Icons.Default.LocationOn, contentDescription = null) }
                )
            }
        }

        // Settings & Options
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = LanguageManager.getString(R.string.settings),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.navigate(Screen.EditProfile.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = true
                ) {
                    Text(LanguageManager.getString(R.string.edit_profile))
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Language Button
                Button(
                    onClick = onChangeLanguage,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = true
                ) {
                    Text(LanguageManager.getString(R.string.change_language))
                }

                Spacer(modifier = Modifier.weight(1f))

                // Logout Button
                Button(
                    onClick = onLogout,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(LanguageManager.getString(R.string.log_out))
                }
            }
        }
    }
}