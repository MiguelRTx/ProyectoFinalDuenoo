package com.example.projectfinal.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
    object Home : Screen("home")
    object PetList : Screen("pets")

    object PetForm : Screen("pets/form/{petId}") {
        fun createRoute(petId: Int) = "pets/form/$petId"
    }
    object Walks : Screen("walks")
    object MapSearch : Screen("map_search")

    object WalkerDetail : Screen("walker_detail/{walkerId}") {
        fun createRoute(walkerId: Int) = "walker_detail/$walkerId"
    }

    object Tracking : Screen("tracking/{walkId}") {
        fun createRoute(walkId: Int) = "tracking/$walkId"
    }
}