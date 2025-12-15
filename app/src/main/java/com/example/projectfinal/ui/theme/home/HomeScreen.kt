package com.example.projectfinal.ui.theme.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectfinal.ui.navigation.Screen
import com.example.projectfinal.ui.theme.auth.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, authViewModel: AuthViewModel? = null, homeViewModel: HomeViewModel = viewModel()) {

    LaunchedEffect(Unit) {
        println("DEBUG HomeScreen - Refrescando datos del usuario...")
        homeViewModel.refreshUser()
    }

    val baseUrl = "https://apimascotas.jmacboy.com/"

    val userPhotoUrl = remember(homeViewModel.currentUser?.photo) {
        val photo = homeViewModel.currentUser?.photo
        println("DEBUG HomeScreen - Foto del usuario: '$photo'")

        val finalUrl = if (photo.isNullOrEmpty()) {
            println("DEBUG HomeScreen - No hay foto, usando placeholder")
            null
        } else if (photo.startsWith("http")) {
            println("DEBUG HomeScreen - URL completa: $photo")
            photo
        } else {
            val cleanPath = photo.removePrefix("/")
            val fullUrl = "$baseUrl$cleanPath"
            println("DEBUG HomeScreen - URL construida: $fullUrl")
            fullUrl
        }

        println("DEBUG HomeScreen - URL final para AsyncImage: $finalUrl")
        finalUrl
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = userPhotoUrl ?: "https://via.placeholder.com/40",
                            contentDescription = "Foto de perfil",
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Color.LightGray),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        Text("Inicio - Dueño")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        authViewModel?.logout()
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.Default.ExitToApp, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("¿Qué deseas hacer hoy?", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(32.dp))

            SimpleMenuButton(
                text = "Mis Mascotas",
                onClick = { navController.navigate(Screen.PetList.route) }
            )

            Spacer(modifier = Modifier.height(16.dp))
            SimpleMenuButton(
                text = "Paseos",
                onClick = { navController.navigate(Screen.Walks.route) }
            )
        }
    }
}

@Composable
fun SimpleMenuButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(60.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, style = MaterialTheme.typography.titleMedium)
    }
}