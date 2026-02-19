package com.example.trainium2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun RegisterScreen(onBack: () -> Unit) {
    // Estados para capturar la entrada del usuario
    var nombre by remember { mutableStateOf("") }
    var dni by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var telf by remember { mutableStateOf("") }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Registro de Usuario", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(value = nombre, onValueChange = { nombre = it }, label = { Text("Nombre Completo") }, modifier = Modifier.fillMaxWidth())

        // El DNI se pasa a mayúsculas automáticamente para facilitar la validación
        OutlinedTextField(value = dni, onValueChange = { dni = it.uppercase() }, label = { Text("DNI (8 números y 1 letra)") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("Contraseña") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        OutlinedTextField(value = telf, onValueChange = { telf = it }, label = { Text("Teléfono") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // 1. Validar campos vacíos
                if (nombre.isEmpty() || dni.isEmpty() || email.isEmpty() || pass.isEmpty() || telf.isEmpty()) {
                    Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                // 2. Validar formato DNI (8 números + 1 letra)
                val dniRegex = Regex("^[0-9]{8}[A-Z]$")
                if (!dni.matches(dniRegex)) {
                    Toast.makeText(context, "Formato de DNI inválido (Ej: 12345678A)", Toast.LENGTH_SHORT).show()
                    return@Button
                }

                scope.launch(Dispatchers.IO) {
                    val conn = DatabaseAdmin.connection()
                    var errorMsg: String? = null
                    var exito = false

                    if (conn != null) {
                        try {
                            // 3. Verificar si el DNI ya existe en la base de datos
                            val checkQuery = "SELECT COUNT(*) FROM usuario WHERE DNI = ?"
                            val checkStmt = conn.prepareStatement(checkQuery)
                            checkStmt.setString(1, dni)
                            val rs = checkStmt.executeQuery()
                            rs.next()

                            if (rs.getInt(1) > 0) {
                                errorMsg = "El DNI ya está registrado"
                            } else {
                                // 4. Proceder al registro si el DNI es único
                                val insertQuery = """
                                    INSERT INTO usuario (NOMBRE, DNI, EMAIL, contraseña_hash, TELEFONO, PREMIUM, FECHA_INI_PREM, FECHA_FIN_PREM, FECHA_REG) 
                                    VALUES (?, ?, ?, ?, ?, 0, NULL, NULL, CURDATE())
                                """.trimIndent()

                                val insertStmt = conn.prepareStatement(insertQuery)
                                insertStmt.setString(1, nombre)
                                insertStmt.setString(2, dni)
                                insertStmt.setString(3, email)
                                insertStmt.setString(4, pass)
                                insertStmt.setString(5, telf)

                                if (insertStmt.executeUpdate() > 0) exito = true
                            }
                            conn.close()
                        } catch (e: Exception) {
                            errorMsg = "Error en la base de datos: ${e.message}"
                            e.printStackTrace()
                        }
                    } else {
                        errorMsg = "No se pudo conectar con el servidor"
                    }

                    // Volvemos al hilo principal para mostrar el resultado al usuario
                    withContext(Dispatchers.Main) {
                        if (exito) {
                            Toast.makeText(context, "Registro completado con éxito", Toast.LENGTH_LONG).show()
                            onBack() // Volver al Login
                        } else {
                            Toast.makeText(context, errorMsg ?: "Error desconocido", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            },
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Crear cuenta")
        }

        Spacer(modifier = Modifier.height(10.dp))

        TextButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("¿Ya tienes cuenta? Volver")
        }
    }
}