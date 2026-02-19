package com.example.trainium2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Modelo de datos para la máquina
data class Maquina(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val estado: Int,
    val descripcion: String,
    val operativa: Int
)

@Composable
fun MaquinasScreen(isAdmin: Boolean, idUsuario: Int, onBack: () -> Unit) {
    var listaMaquinas by remember { mutableStateOf(listOf<Maquina>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    // Estados para la gestión de nuevas máquinas (Admin)
    var mostrarDialogoAdd by remember { mutableStateOf(false) }
    var nuevoNombre by remember { mutableStateOf("") }
    var nuevoTipo by remember { mutableStateOf("") }
    var nuevaDesc by remember { mutableStateOf("") }

    var maquinaParaReservar by remember { mutableStateOf<Maquina?>(null) }
    val calendar = Calendar.getInstance()

    // --- FUNCION: CARGAR DATOS ---
    fun cargarDatos() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection()
                val temporalList = mutableListOf<Maquina>()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT ID, NOMBRE, TIPO, ESTADO, DESCRIPCION, operativa FROM maquinas").executeQuery()
                    while (rs.next()) {
                        temporalList.add(Maquina(
                            rs.getInt("ID"),
                            rs.getString("NOMBRE") ?: "",
                            rs.getString("TIPO") ?: "",
                            rs.getInt("ESTADO"),
                            rs.getString("DESCRIPCION") ?: "",
                            rs.getInt("operativa")
                        ))
                    }
                    conn.close()
                }
                withContext(Dispatchers.Main) {
                    listaMaquinas = temporalList
                    cargando = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // --- FUNCION: CAMBIAR ESTADO OPERATIVO (ADMIN) ---
    fun alternarEstadoOperativo(maquina: Maquina) {
        scope.launch(Dispatchers.IO) {
            val nuevoEstado = if (maquina.operativa == 1) 0 else 1
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val sql = "UPDATE maquinas SET operativa = ? WHERE ID = ?"
                    val pstmt = conn.prepareStatement(sql)
                    pstmt.setInt(1, nuevoEstado)
                    pstmt.setInt(2, maquina.id)
                    pstmt.executeUpdate()
                    conn.close()

                    withContext(Dispatchers.Main) {
                        cargarDatos() // Refrescar la UI
                        Toast.makeText(context, "Estado actualizado", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // --- FUNCION: ELIMINAR MÁQUINA (ADMIN) ---
    fun eliminarMaquina(id: Int) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val sql = "DELETE FROM maquinas WHERE ID = ?"
                    val pstmt = conn.prepareStatement(sql)
                    pstmt.setInt(1, id)
                    pstmt.executeUpdate()
                    conn.close()

                    withContext(Dispatchers.Main) {
                        cargarDatos() // Refrescar la UI
                        Toast.makeText(context, "Máquina eliminada", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // Cargar datos al iniciar
    LaunchedEffect(Unit) { cargarDatos() }

    // --- LÓGICA DE RESERVA ---
    fun ejecutarReserva(maquina: Maquina, fecha: String, horaInicio: String) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateInicio = sdf.parse(horaInicio)
                    val calFin = Calendar.getInstance().apply {
                        time = dateInicio!!
                        add(Calendar.HOUR, 1)
                    }
                    val horaFin = sdf.format(calFin.time)

                    val sql = "INSERT INTO reservas (ID_USUARIO, ID_MAQUINA, FECHA, HORA_INICIO, HORA_FIN) VALUES (?, ?, ?, ?, ?)"
                    val pstmt = conn.prepareStatement(sql)
                    pstmt.setInt(1, idUsuario)
                    pstmt.setInt(2, maquina.id)
                    pstmt.setString(3, fecha)
                    pstmt.setString(4, "$horaInicio:00")
                    pstmt.setString(5, "$horaFin:00")
                    pstmt.executeUpdate()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Reserva: $horaInicio a $horaFin", Toast.LENGTH_LONG).show()
                    }
                    conn.close()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Horario no disponible", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Diálogos de Selección de Fecha y Hora
    val datePickerDialog = DatePickerDialog(
        context,
        { _, y, m, d ->
            val fechaSel = String.format("%d-%02d-%02d", y, m + 1, d)
            TimePickerDialog(context, { _, h, min ->
                if (h in 7..20) {
                    val horaSel = String.format("%02d:%02d", h, min)
                    maquinaParaReservar?.let { ejecutarReserva(it, fechaSel, horaSel) }
                } else {
                    Toast.makeText(context, "Horario permitido: 07:00 a 21:00", Toast.LENGTH_LONG).show()
                }
            }, 12, 0, true).show()
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
        datePicker.maxDate = System.currentTimeMillis() + (21L * 24 * 60 * 60 * 1000)
    }

    // --- DISEÑO DE LA INTERFAZ ---
    Scaffold(
        floatingActionButton = {
            if (isAdmin) {
                FloatingActionButton(onClick = { mostrarDialogoAdd = true }) {
                    Icon(Icons.Default.Add, "Añadir")
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver") }
                Text("Reservar Máquina", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listaMaquinas) { maquina ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    // Imagen dinámica
                                    val resId = context.resources.getIdentifier("maquina${maquina.id}", "drawable", context.packageName)
                                    Image(
                                        painter = if (resId != 0) painterResource(resId) else painterResource(R.drawable.logo_trainium),
                                        contentDescription = null,
                                        modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                        contentScale = ContentScale.Crop
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = maquina.nombre, fontWeight = FontWeight.Bold)
                                        if (maquina.operativa == 0) {
                                            Text("FUERA DE SERVICIO", color = Color.Red, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                        } else {
                                            Text(text = maquina.tipo, fontSize = 12.sp, color = Color.Gray)
                                        }
                                    }

                                    // Botones de acción solo para Admin
                                    if (isAdmin) {
                                        IconButton(onClick = { alternarEstadoOperativo(maquina) }) {
                                            Icon(
                                                imageVector = if (maquina.operativa == 1) Icons.Default.Build else Icons.Default.CheckCircle,
                                                contentDescription = "Mantenimiento",
                                                tint = if (maquina.operativa == 1) Color.Gray else Color(0xFF4CAF50)
                                            )
                                        }
                                        IconButton(onClick = { eliminarMaquina(maquina.id) }) {
                                            Icon(Icons.Default.Delete, null, tint = Color.Red)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = maquina.descripcion, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                Button(
                                    onClick = { maquinaParaReservar = maquina; datePickerDialog.show() },
                                    enabled = maquina.operativa == 1,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(if (maquina.operativa == 1) "Reservar" else "No disponible")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}