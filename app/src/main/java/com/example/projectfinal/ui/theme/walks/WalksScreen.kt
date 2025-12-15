package com.example.projectfinal.ui.theme.walks

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectfinal.data.model.Walk
import com.example.projectfinal.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalksScreen(navController: NavController, viewModel: WalksViewModel) {
    LaunchedEffect(Unit) {
        viewModel.fetchWalks()
    }

    val tabs = listOf("Paseos Actuales", "Historial")
    var selectedTab by remember { mutableIntStateOf(0) }
    val currentWalks = viewModel.walks.filter {
        it.status in listOf("pending", "accepted", "rejected", "in_progress", "walking")
    }

    val historyWalks = viewModel.walks.filter {
        it.status in listOf("finished", "completed")
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Mis Paseos") }) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate(Screen.MapSearch.route) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo Paseo"
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
                if (viewModel.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (viewModel.errorMessage != null) {
                    Text(
                        text = viewModel.errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    val walksToShow = when (selectedTab) {
                        0 -> currentWalks
                        else -> historyWalks
                    }

                    if (walksToShow.isEmpty()) {
                        Text(
                            text = "No hay paseos en esta sección",
                            modifier = Modifier.align(Alignment.Center),
                            color = Color.Gray
                        )
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(walksToShow) { walk ->
                                WalkItem(walk = walk, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WalkItem(walk: Walk, navController: NavController) {
    val baseImageUrl = "https://apimascotas.jmacboy.com/"

    val petPhotoUrl = remember(walk.petPhoto) {
        val photo = walk.petPhoto
        if (photo.isNullOrEmpty()) {
            null
        } else if (photo.startsWith("http")) {
            photo
        } else {
            val cleanPath = photo.removePrefix("/")
            "$baseImageUrl$cleanPath"
        }
    }

    val dateTimeText = remember(walk.scheduledAt) {
        walk.scheduledAt?.let { dateTime ->
            try {
                val parts = dateTime.split(" ")
                if (parts.size >= 2) {
                    "${parts[0]} ${parts[1]}"
                } else {
                    dateTime
                }
            } catch (e: Exception) {
                walk.scheduledAt
            }
        } ?: "Fecha no definida"
    }


    val statusColor = remember(walk.status) {
        when (walk.status?.lowercase()) {
            "pending" -> Color(0xFFFFA500)
            "accepted" -> Color(0xFF4CAF50)
            "rejected" -> Color(0xFFFF5722)
            "in_progress", "walking" -> Color(0xFF2196F3)
            "finished", "completed" -> Color(0xFF9C27B0)
            else -> Color.Gray
        }
    }

    Card(
        elevation = CardDefaults.cardElevation(6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                navController.navigate(Screen.Tracking.createRoute(walk.id))
            }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = petPhotoUrl ?: "https://via.placeholder.com/100",
                    contentDescription = "Foto de ${walk.petName}",
                    modifier = Modifier
                        .size(70.dp)
                        .clip(CircleShape)
                        .background(Color.LightGray),
                    contentScale = ContentScale.Crop
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Mascota: ${walk.petName ?: "Desconocida"}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Paseador: ${walk.walkerName ?: "No asignado"}",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color(0xFF1976D2)
                    )

                    Text(
                        text = dateTimeText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.DarkGray
                    )
                }

                Surface(
                    color = statusColor,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = when (walk.status?.lowercase()) {
                            "pending" -> "Pendiente"
                            "accepted" -> "Aceptado"
                            "rejected" -> "Rechazado"
                            "in_progress", "walking" -> "En Curso"
                            "finished", "completed" -> "Finalizado"
                            else -> walk.status ?: "Desconocido"
                        },
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            walk.durationMinutes?.let { duration ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Duración: $duration minutos",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }
    }
}