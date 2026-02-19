package com.example.trainium2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    // Estado para controlar si ya validamos al usuario
    var usuarioVerificado by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Text("Recuperar Contraseña", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        if (!usuarioVerificado) {
            // PASO 1: Solicitar DNI y Email para verificar en la BD
            OutlinedTextField(value = dni, onValueChange = { dni = it.uppercase() }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth())
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email de registro") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        var existe = false
                        if (conn != null) {
                            // Verificamos que ambos campos coincidan en la misma fila
                            val query = "SELECT * FROM usuario WHERE DNI = ? AND EMAIL = ?"
                            val stmt = conn.prepareStatement(query)
                            stmt.setString(1, dni)
                            stmt.setString(2, email)
                            val rs = stmt.executeQuery()
                            if (rs.next()) existe = true
                            conn.close()
                        }
                        withContext(Dispatchers.Main) {
                            if (existe) {
                                usuarioVerificado = true
                                Toast.makeText(context, "Identidad confirmada", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Los datos no coinciden con ningún usuario", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) { Text("Verificar Identidad") }
        } else {
            // PASO 2: Si es correcto, permitir escribir la nueva contraseña
            Text("Escribe tu nueva clave para el DNI: $dni", color = MaterialTheme.colorScheme.primary)
            OutlinedTextField(value = newPass, onValueChange = { newPass = it }, label = { Text("Nueva Contraseña") }, modifier = Modifier.fillMaxWidth())

            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        var exito = false
                        if (conn != null) {
                            val query = "UPDATE usuario SET contraseña_hash = ? WHERE DNI = ?"
                            val stmt = conn.prepareStatement(query)
                            stmt.setString(1, newPass)
                            stmt.setString(2, dni)
                            if (stmt.executeUpdate() > 0) exito = true
                            conn.close()
                        }
                        withContext(Dispatchers.Main) {
                            if (exito) {
                                Toast.makeText(context, "Contraseña actualizada correctamente", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp)
            ) { Text("Cambiar Contraseña") }
        }

        TextButton(onClick = onBack) { Text("Cancelar") }
    }
}