package com.example.projectfinal.ui.theme.pets // <--- SOLO HASTA .pets

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.projectfinal.utils.FileUtils

@Composable
fun PetFormScreen(
    navController: NavController,
    viewModel: PetsViewModel,
    petId: Int
) {
    val petToEdit = remember { if (petId != -1) viewModel.getPetById(petId) else null }

    var name by remember { mutableStateOf(petToEdit?.name ?: "") }
    var type by remember { mutableStateOf(petToEdit?.type ?: "Perro") } // Default a "Perro"
    var notes by remember { mutableStateOf(petToEdit?.notes ?: "") }

    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }


    val baseUrl = "https://apimascotas.jmacboy.com/"
    val existingPhotoUrl = remember(petToEdit?.photo) {
        if (petToEdit?.photo.isNullOrEmpty()) {
            null
        } else if (petToEdit?.photo?.startsWith("http") == true) {
            petToEdit.photo
        } else {
            val cleanPath = petToEdit?.photo?.removePrefix("/") ?: ""
            "$baseUrl$cleanPath"
        }
    }

    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        selectedImageUri = uri
    }

    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(
            text = if (petId == -1) "Nueva Mascota" else "Editar Mascota",
            style = MaterialTheme.typography.headlineMedium
        )
        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.LightGray)
                .clickable { launcher.launch("image/*") },
            contentAlignment = Alignment.Center
        ) {
            if (selectedImageUri != null) {
                AsyncImage(
                    model = selectedImageUri,
                    contentDescription = "Foto de mascota seleccionada",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else if (!existingPhotoUrl.isNullOrEmpty()) {
                AsyncImage(
                    model = existingPhotoUrl,
                    contentDescription = "Foto actual de la mascota",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = "Seleccionar foto",
                        tint = Color.White,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Toca para agregar foto",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = name,
            onValueChange = {
                name = it
                viewModel.clearError()
            },
            label = { Text("Nombre") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        var expanded by remember { mutableStateOf(false) }
        val petTypes = listOf("Perro", "Gato", "Ave", "Pez", "Hamster", "Conejo", "Otro")

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedTextField(
                value = type,
                onValueChange = { },
                readOnly = true,
                label = { Text("Tipo") },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = if (expanded)
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
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                petTypes.forEach { petType ->
                    DropdownMenuItem(
                        text = { Text(petType) },
                        onClick = {
                            type = petType
                            expanded = false
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = {
                notes = it
                viewModel.clearError()
            },
            label = { Text("Notas") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        if (viewModel.errorMessage != null) {
            Text(
                text = viewModel.errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Button(
            onClick = {
                if (name.isBlank()) {
                    return@Button
                }
                if (type.isBlank()) {
                    return@Button
                }

                val photoFile = selectedImageUri?.let { FileUtils.getFileFromUri(context, it) }
                if (petId == -1) {
                    viewModel.addPet(name, type, notes, photoFile) { navController.popBackStack() }
                } else {
                    viewModel.updatePet(petId, name, type, notes, photoFile) { navController.popBackStack() }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !viewModel.isLoading && name.isNotBlank() && type.isNotBlank()
        ) {
            if (viewModel.isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            else Text("Guardar")
        }
    }
}