package com.example.projectfinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.projectfinal.ui.theme.ProjectFinalTheme
import com.example.projectfinal.ui.navigation.Screen
import com.example.projectfinal.ui.theme.auth.AuthViewModel
import com.example.projectfinal.ui.theme.auth.LoginScreen
import com.example.projectfinal.ui.theme.auth.RegisterScreen
import com.example.projectfinal.ui.theme.home.HomeScreen
import com.example.projectfinal.ui.theme.home.HomeViewModel
import com.example.projectfinal.ui.theme.pets.PetsViewModel
import com.example.projectfinal.ui.theme.pets.PetListScreen
import com.example.projectfinal.ui.theme.pets.PetFormScreen
import com.example.projectfinal.ui.theme.walks.WalksViewModel
import com.example.projectfinal.ui.theme.walks.WalksScreen
import com.example.projectfinal.ui.theme.booking.BookingViewModel
import com.example.projectfinal.ui.theme.booking.MapSearchScreen
import com.example.projectfinal.ui.theme.booking.WalkerDetailScreen
import com.example.projectfinal.ui.theme.tracking.TrackingViewModel
import com.example.projectfinal.ui.theme.tracking.TrackingScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ProjectFinalTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val authViewModel: AuthViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val petsViewModel: PetsViewModel = viewModel()
    val walksViewModel: WalksViewModel = viewModel()
    val bookingViewModel: BookingViewModel = viewModel()
    val trackingViewModel: TrackingViewModel = viewModel()
    val startDestination = if (authViewModel.isUserLoggedIn()) {
        Screen.Home.route
    } else {
        Screen.Login.route
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.Register.route) {
            RegisterScreen(navController = navController, viewModel = authViewModel)
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController, authViewModel = authViewModel, homeViewModel = homeViewModel)
        }

        composable(Screen.PetList.route) {
            LaunchedEffect(Unit) { petsViewModel.fetchPets() }
            PetListScreen(navController = navController, viewModel = petsViewModel)
        }

        composable(
            route = Screen.PetForm.route,
            arguments = listOf(navArgument("petId") { type = NavType.IntType })
        ) { backStackEntry ->
            val petId = backStackEntry.arguments?.getInt("petId") ?: -1
            PetFormScreen(
                navController = navController,
                viewModel = petsViewModel,
                petId = petId
            )
        }

        composable(Screen.Walks.route) {
            LaunchedEffect(Unit) { walksViewModel.fetchWalks() }
            WalksScreen(navController = navController, viewModel = walksViewModel)
        }

        composable(Screen.MapSearch.route) {
            MapSearchScreen(navController, bookingViewModel)
        }

        composable(
            route = Screen.WalkerDetail.route,
            arguments = listOf(navArgument("walkerId") { type = NavType.IntType })
        ) { backStackEntry ->
            val walkerId = backStackEntry.arguments?.getInt("walkerId") ?: -1
            WalkerDetailScreen(navController, bookingViewModel, walkerId)
        }

            composable(
                route = Screen.Tracking.route,
                arguments = listOf(navArgument("walkId") { type = NavType.IntType })
            ) { backStackEntry ->
                val walkId = backStackEntry.arguments?.getInt("walkId") ?: -1
                TrackingScreen(navController, trackingViewModel, walkId)
            }
    }
}