package com.example.trainium2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

data class Plato(
    val id: Int,
    val autor: String, // Cambiado para que no falle si el JOIN es vacío
    val nombrePlato: String,
    val descripcion: String,
    val calorias: Int
)

@Composable
fun PlatosScreen(onBack: () -> Unit) {
    var platoDelDia by remember { mutableStateOf<Plato?>(null) }
    var cargando by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            val listaTotal = mutableListOf<Plato>()

            if (conn != null) {
                try {
                    val query = """
                         SELECT p.ID, IFNULL(u.NOMBRE, 'Usuario Anónimo') as AUTOR, 
                         p.NOMBRE as PLATONOMBRE, p.DESCRIPCION, p.CALORIAS 
                         FROM platos p 
                         LEFT JOIN usuario u ON p.ID_USUARIO = u.ID
                         """.trimIndent()

                    val stmt = conn.prepareStatement(query)
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        listaTotal.add(
                            Plato(
                                rs.getInt("ID"),
                                rs.getString("AUTOR"),
                                rs.getString("PLATONOMBRE"),
                                rs.getString("DESCRIPCION"),
                                rs.getInt("CALORIAS")
                            )
                        )
                    }
                    conn.close()
                } catch (e: Exception) {
                    errorMsg = "Error en consulta: ${e.message}"
                }
            } else {
                errorMsg = "Sin conexión al servidor"
            }

            withContext(Dispatchers.Main) {
                cargando = false
                if (listaTotal.isNotEmpty()) {
                    val diaDelAño = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
                    val indice = diaDelAño % listaTotal.size
                    platoDelDia = listaTotal[indice]
                } else if (errorMsg.isEmpty()) {
                    errorMsg = "No se encontraron platos en la tabla"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Plato del Día", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(30.dp))

        if (cargando) {
            CircularProgressIndicator()
        } else if (errorMsg.isNotEmpty()) {
            Text(text = errorMsg, color = Color.Red, fontWeight = FontWeight.Bold)
        } else if (platoDelDia != null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = platoDelDia!!.nombrePlato,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Sugerido por: ${platoDelDia!!.autor}",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    Text(text = platoDelDia!!.descripcion, fontSize = 16.sp)

                    Spacer(modifier = Modifier.height(15.dp))

                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${platoDelDia!!.calorias} Calorías",
                            modifier = Modifier.padding(8.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}