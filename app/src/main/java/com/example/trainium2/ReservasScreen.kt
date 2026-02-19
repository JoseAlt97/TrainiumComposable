package com.example.trainium2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
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
import java.text.SimpleDateFormat
import java.util.*

data class ReservaInfo(
    val idReserva: Int,
    val idMaquina: Int,
    val nombreMaquina: String,
    val fecha: String,
    val horaInicio: String,
    val horaFin: String
)

@Composable
fun ReservasScreen(idUsuario: Int, onBack: () -> Unit) {
    var listaReservas by remember { mutableStateOf(listOf<ReservaInfo>()) }
    var cargando by remember { mutableStateOf(true) }
    var reservaParaCancelar by remember { mutableStateOf<ReservaInfo?>(null) }
    val scope = rememberCoroutineScope()

    // Depuración automática de registros caducados
    suspend fun depurarBaseDeDatos() {
        val conn = DatabaseAdmin.connection()
        if (conn != null) {
            try {
                val ahora = Calendar.getInstance().time
                val hoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(ahora)
                val horaActual = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(ahora)

                val sqlBuscar = "SELECT ID, ID_MAQUINA FROM reservas WHERE FECHA < ? OR (FECHA = ? AND HORA_FIN < ?)"
                val ps = conn.prepareStatement(sqlBuscar)
                ps.setString(1, hoy); ps.setString(2, hoy); ps.setString(3, horaActual)
                val rs = ps.executeQuery()

                while (rs.next()) {
                    val idRes = rs.getInt("ID")
                    val idMaq = rs.getInt("ID_MAQUINA")
                    conn.prepareStatement("UPDATE maquinas SET ESTADO = 0 WHERE ID = $idMaq").executeUpdate()
                    conn.prepareStatement("DELETE FROM reservas WHERE ID = $idRes").executeUpdate()
                }
                conn.close()
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    fun cargarDatos() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            depurarBaseDeDatos()
            val conn = DatabaseAdmin.connection()
            val temporalList = mutableListOf<ReservaInfo>()
            if (conn != null) {
                try {
                    val query = "SELECT r.ID, r.ID_MAQUINA, m.NOMBRE, r.FECHA, r.HORA_INICIO, r.HORA_FIN FROM reservas r INNER JOIN maquinas m ON r.ID_MAQUINA = m.ID WHERE r.ID_USUARIO = ? ORDER BY r.FECHA DESC"
                    val pstmt = conn.prepareStatement(query)
                    pstmt.setInt(1, idUsuario)
                    val rs = pstmt.executeQuery()
                    while (rs.next()) {
                        temporalList.add(ReservaInfo(rs.getInt("ID"), rs.getInt("ID_MAQUINA"), rs.getString("NOMBRE"), rs.getString("FECHA"), rs.getString("HORA_INICIO"), rs.getString("HORA_FIN")))
                    }
                    conn.close()
                } catch (e: Exception) { e.printStackTrace() }
            }
            withContext(Dispatchers.Main) {
                listaReservas = temporalList
                cargando = false
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Mis Reservas", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(16.dp))

        if (cargando) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (listaReservas.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("No hay reservas activas.", color = Color.Gray) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(listaReservas) { reserva ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(reserva.nombreMaquina, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("📅 ${reserva.fecha}", fontSize = 13.sp)
                                Text("⏰ ${reserva.horaInicio} - ${reserva.horaFin}", fontSize = 13.sp)
                            }
                            IconButton(onClick = { reservaParaCancelar = reserva }) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN CORREGIDO
    if (reservaParaCancelar != null) {
        AlertDialog(
            onDismissRequest = { reservaParaCancelar = null },
            title = { Text("¿Cancelar reserva?") },
            text = { Text("Se liberará la máquina ${reservaParaCancelar?.nombreMaquina} para otros usuarios.") },
            confirmButton = {
                Button(
                    onClick = {
                        val idRes = reservaParaCancelar?.idReserva
                        val idMaq = reservaParaCancelar?.idMaquina

                        // Lanzamos el borrado en segundo plano
                        scope.launch(Dispatchers.IO) {
                            val conn = DatabaseAdmin.connection()
                            if (conn != null && idRes != null) {
                                try {
                                    // 1. Borramos la reserva
                                    val psDel = conn.prepareStatement("DELETE FROM reservas WHERE ID = ?")
                                    psDel.setInt(1, idRes)
                                    psDel.executeUpdate()

                                    // 2. Liberamos la máquina
                                    val psUpd = conn.prepareStatement("UPDATE maquinas SET ESTADO = 0 WHERE ID = ?")
                                    psUpd.setInt(1, idMaq!!)
                                    psUpd.executeUpdate()

                                    conn.close()

                                    // RECARGAR DATOS TRAS EL BORRADO
                                    withContext(Dispatchers.Main) {
                                        reservaParaCancelar = null
                                        cargarDatos()
                                    }
                                } catch (e: Exception) { e.printStackTrace() }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Confirmar", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { reservaParaCancelar = null }) { Text("Volver") }
            }
        )
    }
}