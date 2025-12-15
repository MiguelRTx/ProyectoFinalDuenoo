package com.example.projectfinal.ui.theme.booking

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectfinal.ui.navigation.Screen

@Composable
fun WalkerDetailScreen(navController: NavController, viewModel: BookingViewModel, walkerId: Int) {

    val petsViewModel: com.example.projectfinal.ui.theme.pets.PetsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()

    LaunchedEffect(walkerId) {
        viewModel.selectWalker(walkerId)
        viewModel.resetBookingState()
        petsViewModel.fetchPets()
    }

    val walker = viewModel.selectedWalker

    LaunchedEffect(viewModel.bookingSuccess) {
        if (viewModel.bookingSuccess) {
            navController.navigate(Screen.Walks.route) {
                popUpTo(Screen.Home.route)
            }
        }
    }

    Scaffold { padding ->
        if (walker == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                AsyncImage(
                    model = walker.photo ?: "https://via.placeholder.com/150",
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(walker.name, style = MaterialTheme.typography.headlineMedium)
                Text("Calificación: ${walker.rating} ★", style = MaterialTheme.typography.bodyLarge)
                Text("Costo por hora: ${walker.priceHour}", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.primary)

                if (viewModel.hasSelectedRoute()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E8)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                "RUTA SELECCIONADA",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFF2E7D32),
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Punto A: ${viewModel.selectedRoutePointA?.let { "${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}" } ?: "No definido"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                "Punto B: ${viewModel.selectedRoutePointB?.let { "${String.format("%.4f", it.latitude)}, ${String.format("%.4f", it.longitude)}" } ?: "No definido"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Solicitar Paseo", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))

                var selectedPetId by remember { mutableStateOf<Int?>(null) }
                var date by remember { mutableStateOf("2025-12-01 10:00") }
                var duration by remember { mutableStateOf("60") }
                var notes by remember { mutableStateOf("") }

                var expandedPet by remember { mutableStateOf(false) }
                val pets = petsViewModel.pets

                if (pets.isNotEmpty() && selectedPetId == null) {
                    selectedPetId = pets.first().id
                }

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = pets.find { it.id == selectedPetId }?.name ?: "Seleccionar mascota",
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Mascota") },
                        trailingIcon = {
                            IconButton(onClick = { expandedPet = !expandedPet }) {
                                Icon(
                                    imageVector = if (expandedPet)
                                        Icons.Default.ExpandLess
                                    else
                                        Icons.Default.ExpandMore,
                                    contentDescription = null
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    DropdownMenu(
                        expanded = expandedPet,
                        onDismissRequest = { expandedPet = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        pets.forEach { pet ->
                            DropdownMenuItem(
                                text = { Text("${pet.name ?: "Sin nombre"} (${pet.type ?: "Tipo desconocido"})") },
                                onClick = {
                                    selectedPetId = pet.id
                                    expandedPet = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("Fecha (YYYY-MM-DD HH:mm)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = duration,
                    onValueChange = { duration = it },
                    label = { Text("Duración (minutos)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notas para el paseador") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(24.dp))

                if (viewModel.bookingMessage != null) {
                    Text(viewModel.bookingMessage!!, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        if (selectedPetId != null) {
                            viewModel.createWalk(selectedPetId!!, date, duration, notes)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !viewModel.isBookingLoading && selectedPetId != null
                ) {
                    if (viewModel.isBookingLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Confirmar Solicitud")
                    }
                }
            }
        }
    }
}