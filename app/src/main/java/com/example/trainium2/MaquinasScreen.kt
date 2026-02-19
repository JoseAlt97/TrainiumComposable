package com.example.trainium2

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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

// Definición de la clase para evitar errores de inferencia de tipo
data class Maquina(
    val id: Int,
    val nombre: String,
    val tipo: String,
    val estado: Int,
    val descripcion: String
)

@Composable
fun MaquinasScreen(isAdmin: Boolean, idUsuario: Int, onBack: () -> Unit) {
    var listaMaquinas by remember { mutableStateOf(listOf<Maquina>()) }
    var cargando by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var maquinaParaReservar by remember { mutableStateOf<Maquina?>(null) }
    var fechaSeleccionada by remember { mutableStateOf("") }
    val calendar = Calendar.getInstance()

    // Carga inicial de las máquinas desde la base de datos
    fun cargarDatos() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            try {
                val conn = DatabaseAdmin.connection()
                val temporalList = mutableListOf<Maquina>()
                if (conn != null) {
                    val rs = conn.prepareStatement("SELECT ID, NOMBRE, TIPO, ESTADO, DESCRIPCION FROM maquinas").executeQuery()
                    while (rs.next()) {
                        temporalList.add(Maquina(
                            rs.getInt("ID"),
                            rs.getString("NOMBRE") ?: "Máquina",
                            rs.getString("TIPO") ?: "",
                            rs.getInt("ESTADO"),
                            rs.getString("DESCRIPCION") ?: ""
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
                withContext(Dispatchers.Main) { cargando = false }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    // Función para insertar la reserva con duración de 1 hora
    fun ejecutarReserva(maquina: Maquina, fecha: String, hora: String) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    // Calculamos la hora de fin sumando 1 hora a la de inicio
                    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
                    val dateInicio = sdf.parse(hora)
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
                    pstmt.setString(4, "$hora:00") // Formato SQL Time
                    pstmt.setString(5, "$horaFin:00")
                    pstmt.executeUpdate()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Reserva realizada: $hora a $horaFin", Toast.LENGTH_LONG).show()
                    }
                    conn.close()
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error: La máquina ya está reservada en ese horario", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Configuración del Selector de Fecha (Máximo 3 semanas)
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            fechaSeleccionada = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)

            // Al elegir fecha, abrimos el selector de hora (07:00 - 21:00)
            TimePickerDialog(context, { _, hour, minute ->
                if (hour in 7..20) {
                    val horaSel = String.format("%02d:%02d", hour, minute)
                    maquinaParaReservar?.let { ejecutarReserva(it, fechaSeleccionada, horaSel) }
                } else {
                    Toast.makeText(context, "Horario permitido: 07:00 a 21:00", Toast.LENGTH_LONG).show()
                }
            }, calendar.get(Calendar.HOUR_OF_DAY), 0, true).show()
        },
        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)
    ).apply {
        datePicker.minDate = System.currentTimeMillis()
        datePicker.maxDate = System.currentTimeMillis() + (21L * 24 * 60 * 60 * 1000) // 21 días
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Reservar Máquina", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

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
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Carga de imagen con fallback de seguridad
                            val resId = context.resources.getIdentifier("maquina${maquina.id}", "drawable", context.packageName)
                            Image(
                                painter = if (resId != 0) painterResource(resId) else painterResource(R.drawable.logo_trainium),
                                contentDescription = null,
                                modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(text = maquina.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text(text = maquina.descripcion, fontSize = 12.sp, color = Color.Gray)

                                Spacer(modifier = Modifier.height(8.dp))

                                Button(
                                    onClick = {
                                        maquinaParaReservar = maquina
                                        datePickerDialog.show()
                                    },
                                    modifier = Modifier.height(36.dp),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                                ) {
                                    Text("Reservar", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}