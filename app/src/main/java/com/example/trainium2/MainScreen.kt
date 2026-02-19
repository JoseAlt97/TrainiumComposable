package com.example.trainium2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp // Importante para definir el tamaño de la letra

@Composable
fun MainScreen(onNavigateToLogin: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Aumentamos el tamaño de la imagen de 200.dp a 300.dp (o el valor que prefieras)
        Image(
            painter = painterResource(id = R.drawable.logo_trainium),
            contentDescription = "Logo",
            modifier = Modifier.size(300.dp)
        )

        // Espaciado entre la imagen y el botón
        Spacer(modifier = Modifier.height(30.dp))

        // Para agrandar el botón usamos fillMaxWidth con un porcentaje (0.7f es el 70% del ancho)
        // y le damos una altura fija con .height()
        Button(
            onClick = onNavigateToLogin,
            modifier = Modifier
                .fillMaxWidth(0.7f) // El botón ocupará el 70% de la pantalla horizontalmente
                .height(60.dp)      // Hacemos el botón más alto
        ) {
            // Agrandamos también el texto dentro del botón con fontSize
            Text(
                text = "Iniciar Aplicación",
                fontSize = 18.sp
            )
        }
    }
}