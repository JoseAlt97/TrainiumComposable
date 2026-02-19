package com.example.trainium2

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
fun LoginScreen(
    onBack: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgot: () -> Unit,
    // onLoginSuccess ahora recibe: Nombre, isAdmin (0/1), idUsuario, isPremium (0/1)
    onLoginSuccess: (String, Int, Int, Int) -> Unit
) {
    var dni by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        TextButton(onClick = onBack, modifier = Modifier.align(Alignment.Start)) { Text("← Inicio") }
        Text("Login", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        OutlinedTextField(value = dni, onValueChange = { dni = it.uppercase() }, label = { Text("DNI") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(value = pass, onValueChange = { pass = it }, label = { Text("CONTRASEÑA") }, visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(20.dp))

        Button(onClick = {
            scope.launch(Dispatchers.IO) {
                val conn = DatabaseAdmin.connection()
                var nombre: String? = null
                var isAdmin = 0
                var idUsuario = 0
                var isPremium = 0

                if (conn != null) {
                    try {
                        // Consultamos todos los atributos necesarios del usuario
                        val stmt = conn.prepareStatement("SELECT ID, NOMBRE, ADMIN, PREMIUM FROM usuario WHERE DNI = ? AND contraseña_hash = ?")
                        stmt.setString(1, dni)
                        stmt.setString(2, pass)
                        val rs = stmt.executeQuery()
                        if (rs.next()) {
                            idUsuario = rs.getInt("ID")
                            nombre = rs.getString("NOMBRE")
                            isAdmin = rs.getInt("ADMIN")
                            isPremium = rs.getInt("PREMIUM")
                        }
                        conn.close()
                    } catch (e: Exception) { e.printStackTrace() }
                }

                withContext(Dispatchers.Main) {
                    if (nombre != null) {
                        onLoginSuccess(nombre!!, isAdmin, idUsuario, isPremium)
                    } else {
                        Toast.makeText(context, "Datos incorrectos", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }, modifier = Modifier.fillMaxWidth()) { Text("Entrar") }

        TextButton(onClick = onNavigateToForgot) { Text("Olvidé mi contraseña") }
        TextButton(onClick = onNavigateToRegister) { Text("Registrarme") }
    }
}