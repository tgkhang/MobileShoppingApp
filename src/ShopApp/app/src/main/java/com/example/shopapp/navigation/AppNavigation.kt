package com.example.shopapp.navigation

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.shopapp.ui.auth.IntroScreen
import com.example.shopapp.ui.auth.LoginScreen
import com.example.shopapp.ui.auth.ResetPassword
import com.example.shopapp.ui.auth.SignupScreen
import com.example.shopapp.viewmodel.AuthViewModel


@Composable
fun AppNavigation(authViewModel: AuthViewModel, intent: Intent?) {
    val navController = rememberNavController()
    val loginState by authViewModel.loginState.collectAsState()

    NavHost(navController = navController, startDestination = Screen.Intro.route) {
        composable(Screen.Intro.route) { IntroScreen(navController) }
        composable(Screen.Auth.route) { LoginScreen(authViewModel, navController) }
        composable (Screen.ResetPassword.route){
            ResetPassword(authViewModel,navController)
        }
        composable(Screen.AdminHome.route) { AdminNavigation(authViewModel, navController) }
        composable(Screen.Signup.route){ SignupScreen(authViewModel,navController) }
        composable(Screen.UserHome.route) {
            UserNavigation(authViewModel, navController, intent)
        }
    }

    LaunchedEffect(loginState) {
        if (loginState is AuthViewModel.LoginState.Success) {
            val role = (loginState as AuthViewModel.LoginState.Success).role
            val destination = if (role == "admin") Screen.AdminHome.route else Screen.UserHome.route
            navController.navigate(destination) {
                popUpTo(Screen.Auth.route) { inclusive = true }
            }
            authViewModel.resetLoginState()
        }
    }
}

