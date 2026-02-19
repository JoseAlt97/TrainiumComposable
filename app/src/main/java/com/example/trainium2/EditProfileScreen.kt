package com.example.trainium2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun EditProfileScreen(
    idUsuario: Int,
    onBack: () -> Unit,
    onNavigateToHistorial: (Int) -> Unit,
    onNavigateToPremium: () -> Unit // Parámetro necesario para la navegación al proceso de pago
) {
    // Estados para los campos editables del perfil
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // Estado para verificar si el usuario ya es premium y controlar la carga
    var isPremium by remember { mutableStateOf(false) }
    var cargando by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Carga inicial de los datos del usuario desde la base de datos
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val stmt = conn.prepareStatement("SELECT NOMBRE, EMAIL, TELEFONO, PREMIUM FROM usuario WHERE ID = ?")
                    stmt.setInt(1, idUsuario)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        nombre = rs.getString("NOMBRE") ?: ""
                        email = rs.getString("EMAIL") ?: ""
                        telefono = rs.getString("TELEFONO") ?: ""
                        isPremium = rs.getInt("PREMIUM") == 1
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    conn.close()
                }
            }
            withContext(Dispatchers.Main) { cargando = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Cabecera de la pantalla
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Ajustes de Perfil", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Spacer(modifier = Modifier.height(20.dp))

            // Campos de texto para editar la información
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre Completo") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = telefono,
                onValueChange = { telefono = it },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva Contraseña (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón para actualizar los datos en la base de datos
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        if (conn != null) {
                            try {
                                // Se decide si actualizar la contraseña basándose en si el campo está vacío
                                val sql = if (password.isEmpty()) {
                                    "UPDATE usuario SET NOMBRE = ?, EMAIL = ?, TELEFONO = ? WHERE ID = ?"
                                } else {
                                    "UPDATE usuario SET NOMBRE = ?, EMAIL = ?, TELEFONO = ?, contraseña_hash = ? WHERE ID = ?"
                                }

                                val pstmt = conn.prepareStatement(sql)
                                pstmt.setString(1, nombre)
                                pstmt.setString(2, email)
                                pstmt.setString(3, telefono)

                                if (password.isEmpty()) {
                                    pstmt.setInt(4, idUsuario)
                                } else {
                                    pstmt.setString(4, password)
                                    pstmt.setInt(5, idUsuario)
                                }

                                pstmt.executeUpdate()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Perfil actualizado", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                conn.close()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            Spacer(modifier = Modifier.height(30.dp))

            // Tarjeta de información de suscripción
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Estado de cuenta", fontSize = 12.sp)
                        if (isPremium) {
                            Text("USUARIO PREMIUM", color = Color(0xFFD4AF37), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Usuario Estándar", fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botón para ir a la pantalla de compra si no es premium
                    if (!isPremium) {
                        Button(
                            onClick = onNavigateToPremium,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Hacerme Premium", fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), thickness = 1.dp, color = Color.LightGray)

            // Botón para acceder al historial de pagos
            OutlinedButton(
                onClick = { onNavigateToHistorial(idUsuario) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ver historial de pagos")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}