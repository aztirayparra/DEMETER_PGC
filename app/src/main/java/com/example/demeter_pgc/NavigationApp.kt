package com.example.demeter_pgc

import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.composable
import androidx.navigation.compose.NavHost

@Composable
fun NavigationApp() {
    val myNavController = rememberNavController()
    val myStartDestination = "login"

    NavHost(
        navController = myNavController,
        startDestination = myStartDestination,
    ) {
        composable("forgot_password") {
            ForgotPasswordScreen(onClickBack = {
                myNavController.popBackStack()
            })
        }
        composable("login") {
            LoginScreen(
                onClickRegister = {
                    myNavController.navigate("register")
                },
                onSuccessfulLogin = {
                    myNavController.navigate("home") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onClickForgotPassword = {
                    myNavController.navigate("forgot_password")
                }
            )
        }
        composable("register") {
            RegisterScreen(
                onClickBack = {
                    myNavController.popBackStack()
                }, onSuccessfullRegister = {
                    myNavController.navigate("home") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("home") {
            HomeScreen(
                onNavigateToCamera = {
                    myNavController.navigate("camera")
                },
                onNavigateToHistory = {
                    myNavController.navigate("history")
                },
                onNavigateToCameraManagement = {
                    myNavController.navigate("cameras")
                },
                onLogout = {
                    myNavController.navigate("login") {
                        popUpTo(0)
                    }
                }
            )
        }
        composable("camera") {
            CameraScreen(onBack = { myNavController.popBackStack() })
        }
        composable("history") {
            HistoryScreen(onBack = { myNavController.popBackStack() })
        }
        composable("cameras") {
            CamerasScreen(onBack = { myNavController.popBackStack() })
        }
    }
}