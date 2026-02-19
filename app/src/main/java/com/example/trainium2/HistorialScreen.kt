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
                    val rs = conn.prepareStatement("SELECT id, monto, fecha_pago, tipo, metodo_pago FROM pagos WHERE id_usuario = $idUsuario ORDER BY fecha_pago DESC").executeQuery()
                    while (rs.next()) {
                        temp.add(PagoInfo(rs.getInt("id"), rs.getDouble("monto"), rs.getString("fecha_pago"), rs.getString("tipo"), rs.getString("metodo_pago")))
                    }
                    conn.close()
                } catch (e: Exception) { e.printStackTrace() }
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
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (listaPagos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay registros de pago.", color = Color.Gray) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listaPagos) { pago ->
                    Card(modifier = Modifier.fillMaxWidth()) {
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