package com.example.trainium2

import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ReservaInfo(
    val id: Int,
    val maquina: String,
    val usuario: String?,
    val fecha: String,
    val inicio: String,
    val fin: String
)

@Composable
fun ReservasScreen(isAdmin: Boolean, idUsuario: Int, onBack: () -> Unit) {
    var lista by remember { mutableStateOf(listOf<ReservaInfo>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Cargar datos: Todas si es Admin, solo propias si es Usuario
    fun cargar() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                val sql = if (isAdmin) {
                    "SELECT r.ID, m.NOMBRE, u.NOMBRE, r.FECHA, r.HORA_INICIO, r.HORA_FIN FROM reservas r JOIN maquinas m ON r.ID_MAQUINA = m.ID JOIN usuario u ON r.ID_USUARIO = u.ID"
                } else {
                    "SELECT r.ID, m.NOMBRE, u.NOMBRE, r.FECHA, r.HORA_INICIO, r.HORA_FIN FROM reservas r JOIN maquinas m ON r.ID_MAQUINA = m.ID JOIN usuario u ON r.ID_USUARIO = u.ID WHERE r.ID_USUARIO = ?"
                }
                val pstmt = conn.prepareStatement(sql)
                if (!isAdmin) pstmt.setInt(1, idUsuario)
                val rs = pstmt.executeQuery()
                val temp = mutableListOf<ReservaInfo>()
                while (rs.next()) {
                    temp.add(ReservaInfo(rs.getInt(1), rs.getString(2), rs.getString(3), rs.getString(4), rs.getString(5), rs.getString(6)))
                }
                withContext(Dispatchers.Main) { lista = temp; cargando = false }
                conn.close()
            }
        }
    }

    // Borrar reserva (Admin o Usuario)
    fun borrar(id: Int) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                val ps = conn.prepareStatement("DELETE FROM reservas WHERE ID = ?")
                ps.setInt(1, id)
                ps.executeUpdate()
                conn.close()
                withContext(Dispatchers.Main) {
                    cargar()
                    Toast.makeText(context, "Reserva eliminada", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    LaunchedEffect(Unit) { cargar() }

    Column(modifier = Modifier.fillMaxSize().padding(20.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text(if (isAdmin) "Control de Reservas" else "Mis Reservas", fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        if (cargando) CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(lista) { res ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(modifier = Modifier.padding(15.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(res.maquina, fontWeight = FontWeight.Bold)
                                if (isAdmin) Text("De: ${res.usuario}", color = Color.Blue, fontSize = 12.sp)
                                Text("${res.fecha} | ${res.inicio} - ${res.fin}", fontSize = 12.sp)
                            }
                            IconButton(onClick = { borrar(res.id) }) {
                                Icon(Icons.Default.Delete, null, tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}