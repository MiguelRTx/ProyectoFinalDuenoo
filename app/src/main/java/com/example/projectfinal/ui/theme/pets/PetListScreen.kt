package com.example.projectfinal.ui.theme.pets

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectfinal.data.model.Pet
import com.example.projectfinal.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetListScreen(navController: NavController, viewModel: PetsViewModel) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mis Mascotas") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    navController.navigate(Screen.PetForm.createRoute(-1))
                },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Mascota", tint = Color.White)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF0F0F0))
        ) {
            if (viewModel.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (viewModel.errorMessage != null) {
                Text(
                    text = viewModel.errorMessage ?: "Error desconocido",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                if (viewModel.pets.isEmpty()) {
                    Text(
                        text = "No tienes mascotas registradas",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.Gray
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(viewModel.pets) { pet ->
                            PetItem(
                                pet = pet,
                                onEditClick = {
                                    navController.navigate(Screen.PetForm.createRoute(pet.id))
                                },
                                onDeleteClick = {
                                    viewModel.deletePet(pet.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PetItem(pet: Pet, onEditClick: () -> Unit, onDeleteClick: () -> Unit) {

    val baseUrl = "https://apimascotas.jmacboy.com/"

    val fullImageUrl = remember(pet.photo) {
        if (pet.photo.isNullOrEmpty()) {
            null
        } else if (pet.photo.startsWith("http")) {
            pet.photo
        } else {
            val cleanPath = pet.photo.removePrefix("/")
            "$baseUrl$cleanPath"
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = fullImageUrl ?: "https://via.placeholder.com/150",
                contentDescription = "Foto de ${pet.name}",
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name ?: "Sin nombre",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pet.type ?: "Tipo desconocido",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray
                )
                val notes = pet.notes
                if (!notes.isNullOrEmpty()) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        maxLines = 2
                    )
                }
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar", tint = Color.Blue)
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                }
            }
        }
    }
}