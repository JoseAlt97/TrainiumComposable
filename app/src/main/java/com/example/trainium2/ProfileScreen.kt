package com.example.trainium2

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ProfileScreen(
    nombre: String,
    isAdmin: Boolean,
    idUsuario: Int,
    isPremium: Boolean,
    onLogout: () -> Unit,
    onNavigateToMaquinas: (Boolean, Int) -> Unit,
    onNavigateToPlatos: () -> Unit,
    onNavigateToRegistro: (Int) -> Unit,
    onNavigateToReservas: (Int) -> Unit,
    onNavigateToEditProfile: (Int) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().padding(20.dp)) {

        // Icono de Rueda de Ajustes
        IconButton(
            onClick = { onNavigateToEditProfile(idUsuario) },
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = "Ajustes",
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Column(modifier = Modifier.align(Alignment.TopStart)) {
            Text(text = "Hola, $nombre", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)

            if (isPremium) {
                Text(text = "USUARIO PREMIUM", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFFD4AF37))
            } else {
                Column {
                    Text(text = "Usuario no premium", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Button(
                        onClick = { /* Compra */ },
                        modifier = Modifier.height(30.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Comprar", fontSize = 12.sp)
                    }
                }
            }
        }

        if (isAdmin) {
            Text(text = "Modo desarrollador", fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.TopEnd).padding(top = 45.dp))
        }

        Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Button(onClick = { onNavigateToMaquinas(isAdmin, idUsuario) }, modifier = Modifier.fillMaxWidth(0.8f).height(70.dp)) {
                Text("Visualizar máquinas", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onNavigateToReservas(idUsuario) }, modifier = Modifier.fillMaxWidth(0.8f).height(70.dp)) {
                Text("Máquinas reservadas", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onNavigateToPlatos() }, modifier = Modifier.fillMaxWidth(0.8f).height(70.dp)) {
                Text("Recomendación de platos diarias", fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = { onNavigateToRegistro(idUsuario) }, modifier = Modifier.fillMaxWidth(0.8f).height(70.dp)) {
                Text("Mi registro", fontSize = 18.sp)
            }
        }

        Button(onClick = onLogout, modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp)) {
            Text("Cerrar Sesión")
        }
    }
}