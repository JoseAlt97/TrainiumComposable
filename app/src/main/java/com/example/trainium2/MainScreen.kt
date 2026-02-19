package com.example.trainium2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MainScreen(onNavigateToLogin: () -> Unit) {
    // Mantenemos el morado para el botón
    val moradoPrincipal = Color(0xFF6200EE)

    // Cambiamos el color de la Surface a Negro puro
    Surface(
        modifier = Modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 1. Logo principal ampliado
            Image(
                painter = painterResource(id = R.drawable.logo_trainium),
                contentDescription = "Logo Trainium",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(30.dp))

            // 2. Botón de acción principal
            Button(
                onClick = onNavigateToLogin,
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(65.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = moradoPrincipal, // El botón destaca sobre el negro
                    contentColor = Color.White    // Texto del botón en blanco
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp)
            ) {
                Text(
                    text = "COMENZAR AHORA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}