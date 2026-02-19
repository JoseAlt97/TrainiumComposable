package com.example.trainium2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Usamos Double para el monto por si hay decimales, y String para la fecha
data class PagoInfo(val id: Int, val monto: Double, val fecha: String, val tipo: String, val metodo: String)

@Composable
fun HistorialScreen(idUsuario: Int, onBack: () -> Unit) {
    var listaPagos by remember { mutableStateOf(listOf<PagoInfo>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            val temp = mutableListOf<PagoInfo>()
            if (conn != null) {
                try {
                    // Importante: nombres de columnas exactamente igual a como se definieron en el INSERT
                    val query = "SELECT id, monto, fecha_pago, tipo, metodo_pago FROM pagos WHERE id_usuario = ? ORDER BY fecha_pago DESC"
                    val pstmt = conn.prepareStatement(query)
                    pstmt.setInt(1, idUsuario)
                    val rs = pstmt.executeQuery()

                    while (rs.next()) {
                        temp.add(PagoInfo(
                            id = rs.getInt("id"),
                            monto = rs.getDouble("monto"), // Usar getDouble para evitar errores de casteo
                            fecha = rs.getString("fecha_pago") ?: "",
                            tipo = rs.getString("tipo") ?: "Plan Premium",
                            metodo = rs.getString("metodo_pago") ?: "Desconocido"
                        ))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } finally {
                    conn.close() // Cerramos siempre en el finally para evitar fugas de memoria
                }
            }
            withContext(Dispatchers.Main) {
                listaPagos = temp
                cargando = false
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Mis Pagos", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (listaPagos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay registros de pago.", color = Color.Gray)
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listaPagos) { pago ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(pago.tipo, fontWeight = FontWeight.Bold)
                                Text("${pago.monto} €", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Text("Fecha: ${pago.fecha}", fontSize = 12.sp)
                            Text("Método: ${pago.metodo}", fontSize = 12.sp, color = Color.Gray)
                        }
                    }
                }
            }
        }
    }
}