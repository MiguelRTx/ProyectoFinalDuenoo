package com.example.projectfinal.ui.theme.booking


import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.example.projectfinal.ui.navigation.Screen
import androidx.compose.ui.unit.dp
import kotlin.random.Random

data class RandomWalker(
    val id: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val priceHour: String,
    val rating: Double
)

fun generateRandomWalkers(userLat: Double, userLng: Double): List<RandomWalker> {
    val names = listOf(
        "Juan Pérez", "María García", "Carlos López", "Ana Martínez", "Luis Rodríguez",
        "Sofia Hernández", "Diego Silva", "Valentina Torres", "Mateo Vargas", "Emma Castro"
    )

    val walkers = mutableListOf<RandomWalker>()

    repeat(5) { i ->
        val latOffset = (Random.nextDouble() - 0.5) * 0.02
        val lngOffset = (Random.nextDouble() - 0.5) * 0.02

        walkers.add(
            RandomWalker(
                id = 100 + i,
                name = names[Random.nextInt(names.size)],
                latitude = userLat + latOffset,
                longitude = userLng + lngOffset,
                priceHour = "${Random.nextInt(15, 45)} Bs/hora",
                rating = Random.nextDouble(3.5, 5.0)
            )
        )
    }

    return walkers
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapSearchScreen(navController: NavController, viewModel: BookingViewModel) {
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()
    var routePointA by remember { mutableStateOf<LatLng?>(null) }
    var routePointB by remember { mutableStateOf<LatLng?>(null) }
    var isMarkingRoute by remember { mutableStateOf(false) }
    var randomWalkers by remember { mutableStateOf<List<RandomWalker>>(emptyList()) }

    LaunchedEffect(viewModel.userLocation) {
        viewModel.userLocation?.let { location ->
            randomWalkers = generateRandomWalkers(location.latitude, location.longitude)
        }
    }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasLocationPermission = isGranted
            if (isGranted) viewModel.getCurrentLocation()
        }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            viewModel.getCurrentLocation()
        }
    }

    LaunchedEffect(viewModel.userLocation) {
        viewModel.userLocation?.let { loc ->
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(loc.latitude, loc.longitude), 15f)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buscar Paseadores") },
                actions = {
                    TextButton(
                        onClick = {
                            isMarkingRoute = !isMarkingRoute
                            if (!isMarkingRoute) {
                                routePointA = null
                                routePointB = null
                            }
                        }
                    ) {
                        Text(if (isMarkingRoute) "Cancelar Ruta" else "Marcar Ruta")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (isMarkingRoute) {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE3F2FD))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = when {
                                routePointA == null -> "Toca en el mapa para marcar el PUNTO A (inicio)"
                                routePointB == null -> "Toca en el mapa para marcar el PUNTO B (destino)"
                                else -> "Ruta marcada: A → B. Ahora selecciona un paseador"
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF1976D2)
                        )
                        if (routePointA != null && routePointB != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    routePointA = null
                                    routePointB = null
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                            ) {
                                Text("Nueva Ruta")
                            }
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                if (!hasLocationPermission) {
            Text("Se necesita permiso de ubicación para buscar paseadores.", modifier = Modifier.align(Alignment.Center))
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = true),
                onMapClick = { latLng ->
                    if (isMarkingRoute) {
                        when {
                            routePointA == null -> routePointA = latLng
                            routePointB == null -> routePointB = latLng
                            else -> {
                                // Reiniciar ruta
                                routePointA = latLng
                                routePointB = null
                            }
                        }
                    }
                }
            ) {
                routePointA?.let { point ->
                    Marker(
                        state = MarkerState(position = point),
                        title = "Punto A",
                        snippet = "Inicio del paseo"
                    )
                }

                routePointB?.let { point ->
                    Marker(
                        state = MarkerState(position = point),
                        title = "Punto B",
                        snippet = "Destino del paseo"
                    )
                }

                if (routePointA != null && routePointB != null) {
                    Polyline(
                        points = listOf(routePointA!!, routePointB!!),
                        color = Color.Blue,
                        width = 5f
                    )
                }

                randomWalkers.forEach { walker ->
                    val hasRoute = routePointA != null && routePointB != null

                    Marker(
                        state = MarkerState(position = LatLng(walker.latitude, walker.longitude)),
                        title = if (hasRoute) "${walker.name} ⚡ DISPONIBLE PARA RUTA" else walker.name,
                        snippet = if (hasRoute) {
                            "RUTA DEFINIDA - ${walker.priceHour} - ${"%.1f".format(walker.rating)} estrellas"
                        } else {
                            "${walker.priceHour} - ${"%.1f".format(walker.rating)} estrellas"
                        },
                        onClick = {
                            if (hasRoute) {
                                viewModel.setSelectedRoute(routePointA!!, routePointB!!)
                            }
                            navController.navigate(Screen.WalkerDetail.createRoute(walker.id))
                            true
                        }
                    )
                }
            }
                if (viewModel.userLocation == null) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
    }
}
