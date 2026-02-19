package com.example.trainium2

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
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
    onLoginSuccess: (String, Int, Int, Int) -> Unit
) {
    var dni by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Definimos nuestra paleta de morados
    val moradoPrincipal = Color(0xFF6200EE) // Morado vibrante
    val moradoSuave = Color(0xFFBB86FC)    // Lavanda claro
    val moradoOscuro = Color(0xFF3700B3)   // Morado profundo

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Imagen de fondo
        Image(
            painter = painterResource(id = R.drawable.fondopantalla),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // 2. Capa de transparencia con un ligero tinte morado (70% blanco-lavanda)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.75f))
        )

        // 3. Contenido Principal
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón volver con estilo morado
            TextButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.Start),
                colors = ButtonDefaults.textButtonColors(contentColor = moradoPrincipal)
            ) { Text("← INICIO", fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(50.dp))

            // Título LOGIN en mayúsculas y morado
            Text(
                "LOGIN",
                fontSize = 40.sp,
                fontWeight = FontWeight.ExtraBold,
                color = moradoPrincipal,
                letterSpacing = 3.sp
            )

            Text(
                "Bienvenido a Trainium",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Inputs con bordes morados al enfocar
            val inputColors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = moradoPrincipal,
                focusedLabelColor = moradoPrincipal,
                cursorColor = moradoPrincipal
            )

            OutlinedTextField(
                value = dni,
                onValueChange = { dni = it.uppercase() },
                label = { Text("DNI") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = pass,
                onValueChange = { pass = it },
                label = { Text("CONTRASEÑA") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = inputColors
            )

            Spacer(modifier = Modifier.height(40.dp))

            // BOTÓN ENTRAR: Estilizado con morado y bordes redondeados
            Button(
                onClick = {
                    scope.launch(Dispatchers.IO) {
                        val conn = DatabaseAdmin.connection()
                        var nombre: String? = null
                        var isAdmin = 0
                        var idUsuario = 0
                        var isPremium = 0

                        if (conn != null) {
                            try {
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
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = moradoPrincipal)
            ) {
                Text("ENTRAR", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botones de acción secundaria en morado suave
            TextButton(onClick = onNavigateToForgot) {
                Text("¿OLVIDASTE TU CONTRASEÑA?", color = moradoOscuro, fontSize = 12.sp, fontWeight = FontWeight.Medium)
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onNavigateToRegister,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, moradoSuave)
            ) {
                Text("CREAR CUENTA NUEVA", color = moradoPrincipal, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}