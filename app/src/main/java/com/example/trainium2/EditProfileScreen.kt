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
    onNavigateToHistorial: (Int) -> Unit // Añadimos la navegación al historial
) {
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(true) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cargar datos actuales del usuario al iniciar
    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val stmt = conn.prepareStatement("SELECT NOMBRE, EMAIL, TELEFONO FROM usuario WHERE ID = ?")
                    stmt.setInt(1, idUsuario)
                    val rs = stmt.executeQuery()
                    if (rs.next()) {
                        nombre = rs.getString("NOMBRE") ?: ""
                        email = rs.getString("EMAIL") ?: ""
                        telefono = rs.getString("TELEFONO") ?: ""
                    }
                    conn.close()
                } catch (e: Exception) { e.printStackTrace() }
            }
            withContext(Dispatchers.Main) { cargando = false }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        // Cabecera
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

            // Formulario de edición
            OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(value = telefono, onValueChange = { telefono = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Nueva Contraseña (opcional)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón principal de guardado
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        if (conn != null) {
                            try {
                                val sql = if (password.isEmpty()) {
                                    "UPDATE usuario SET NOMBRE = ?, EMAIL = ?, TELEFONO = ? WHERE ID = ?"
                                } else {
                                    "UPDATE usuario SET NOMBRE = ?, EMAIL = ?, TELEFONO = ?, contraseña_hash = ? WHERE ID = ?"
                                }
                                val pstmt = conn.prepareStatement(sql)
                                pstmt.setString(1, nombre); pstmt.setString(2, email); pstmt.setString(3, telefono)
                                if (password.isEmpty()) {
                                    pstmt.setInt(4, idUsuario)
                                } else {
                                    pstmt.setString(4, password); pstmt.setInt(5, idUsuario)
                                }
                                pstmt.executeUpdate()
                                conn.close()
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Cambios guardados", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar cambios")
            }

            // SECCIÓN INFERIOR: HISTORIAL DE PAGOS
            // Usamos un Spacer con weight(1f) para empujar el contenido al final
            Spacer(modifier = Modifier.weight(1f))

            Divider(color = Color.LightGray, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))

            OutlinedButton(
                onClick = { onNavigateToHistorial(idUsuario) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
            ) {
                Text("Ver historial de pagos")
            }
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}