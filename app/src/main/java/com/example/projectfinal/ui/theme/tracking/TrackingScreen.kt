package com.example.projectfinal.ui.theme.tracking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import androidx.compose.ui.text.font.FontWeight
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(navController: NavController, viewModel: TrackingViewModel, walkId: Int) {

    LaunchedEffect(walkId) {
        viewModel.startTracking(walkId)
    }

    val walk = viewModel.walk
    val baseUrl = "https://apimascotas.jmacboy.com/"

    Scaffold(
        topBar = {
            val title = when(walk?.status?.lowercase()) {
                "pending" -> "Paseo Pendiente"
                "accepted" -> "Paseo Aceptado"
                "rejected" -> "Paseo Rechazado"
                "in_progress", "walking" -> "Paseo en Curso"
                "finished", "completed" -> "Paseo Finalizado"
                else -> "Detalle del Paseo"
            }
            TopAppBar(title = { Text(title) })
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (walk?.status?.lowercase() == "rejected") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "Paseo Rechazado",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color(0xFFD32F2F),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "El paseador ha rechazado tu solicitud. Puedes seleccionar otro paseador y solicitar el paseo nuevamente.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                        Button(
                            onClick = { navController.navigate("map_search") },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                        ) {
                            Text("Buscar Otro Paseador")
                        }
                    }
                }
                return@Column
            }

            walk?.let {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3E5F5))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("Mascota: ${it.petName ?: "No definida"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Paseador: ${it.walkerName ?: "No asignado"}", style = MaterialTheme.typography.bodyMedium)
                        Text("Fecha: ${it.scheduledAt ?: "No definida"}", style = MaterialTheme.typography.bodySmall)
                        it.durationMinutes?.let { duration ->
                            Text("Duración: $duration minutos", style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxWidth().height(300.dp).background(Color.LightGray)) {
                if (viewModel.walkerLat != null && viewModel.walkerLng != null) {
                    val walkerPos = LatLng(viewModel.walkerLat!!, viewModel.walkerLng!!)
                    val cameraState = rememberCameraPositionState {
                        position = CameraPosition.fromLatLngZoom(walkerPos, 15f)
                    }

                    LaunchedEffect(walkerPos) {
                        cameraState.position = CameraPosition.fromLatLngZoom(walkerPos, 15f)
                    }

                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState
                    ) {
                        Marker(
                            state = MarkerState(position = walkerPos),
                            title = "Paseador",
                            snippet = walk?.walkerName ?: "Paseador"
                        )
                    }
                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        if (walk?.status == "finished") {
                            Text("El paseo ha finalizado.")
                        } else {
                            Text("Esperando ubicación del paseador...")
                        }
                    }
                }
            }

            Text("Fotos del paseo", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(16.dp))

            if (viewModel.photos.isEmpty()) {
                Text("No hay fotos disponibles.", modifier = Modifier.padding(horizontal = 16.dp), color = Color.Gray)
            } else {
                LazyRow(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(viewModel.photos) { photo ->

                        val fullUrl = if (photo.url.startsWith("http")) {
                            photo.url
                        } else {
                            "$baseUrl${photo.url.removePrefix("/")}"
                        }

                        AsyncImage(
                            model = fullUrl,
                            contentDescription = null,
                            modifier = Modifier.size(100.dp).padding(end = 8.dp),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (walk?.status == "finished") {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {

                        if (viewModel.reviewSuccess) {
                            Text("¡Gracias por tu calificación!", style = MaterialTheme.typography.titleMedium, color = Color.DarkGray)
                            Button(onClick = { navController.popBackStack() }, modifier = Modifier.fillMaxWidth()) {
                                Text("Volver")
                            }
                        } else {
                            Text("Califica al paseador", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(8.dp))

                            var rating by remember { mutableIntStateOf(5) }
                            var comment by remember { mutableStateOf("") }
                            Row {
                                for (i in 1..5) {
                                    IconButton(onClick = { rating = i }) {
                                        Icon(
                                            imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                                            contentDescription = null,
                                            tint = Color(0xFFFFC107)
                                        )
                                    }
                                }
                            }

                            OutlinedTextField(
                                value = comment,
                                onValueChange = { comment = it },
                                label = { Text("Comentario") },
                                modifier = Modifier.fillMaxWidth().background(Color.White)
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { viewModel.sendReview(walkId, rating, comment) },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = !viewModel.isSendingReview
                            ) {
                                if (viewModel.isSendingReview) {
                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                                } else {
                                    Text("Enviar Calificación")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}