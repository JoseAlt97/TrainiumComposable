package com.example.trainium2

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun PremiumSelectionScreen(idUsuario: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Estados para la selección
    var planSeleccionado by remember { mutableStateOf("") }
    var precioSeleccionado by remember { mutableStateOf(0) }
    var metodoPago by remember { mutableStateOf("") }

    // Estados para la tarjeta
    var numeroTarjeta by remember { mutableStateOf("") }
    var cvv by remember { mutableStateOf("") }
    var fechaCaducidad by remember { mutableStateOf("Seleccionar") }

    val calendar = Calendar.getInstance()

    // --- FUNCIÓN PARA PROCESAR EL PAGO EN LA BBDD ---
    fun procesarCompra() {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    // Desactivamos autoCommit para asegurar que ambas operaciones ocurran juntas
                    conn.autoCommit = false

                    // 1. Insertar en la tabla 'pagos'
                    val sqlPago = "INSERT INTO pagos (id_usuario, monto, fecha_pago, tipo, metodo_pago) VALUES (?, ?, ?, ?, ?)"
                    val pstmtPago = conn.prepareStatement(sqlPago)

                    val fechaHoy = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                    pstmtPago.setInt(1, idUsuario)
                    pstmtPago.setInt(2, precioSeleccionado)
                    pstmtPago.setString(3, fechaHoy)
                    pstmtPago.setString(4, planSeleccionado)
                    pstmtPago.setString(5, metodoPago)
                    pstmtPago.executeUpdate()

                    // 2. Actualizar al usuario a estado PREMIUM = 1
                    val sqlUser = "UPDATE usuario SET PREMIUM = 1 WHERE ID = ?"
                    val pstmtUser = conn.prepareStatement(sqlUser)
                    pstmtUser.setInt(1, idUsuario)
                    pstmtUser.executeUpdate()

                    // Confirmamos la transacción
                    conn.commit()
                    conn.close()

                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "¡Bienvenido a Trainium Premium!", Toast.LENGTH_LONG).show()
                        onBack() // Volver al perfil tras la compra
                    }
                } catch (e: Exception) {
                    conn.rollback() // Si algo falla, deshacemos los cambios
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error en el proceso de pago", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Cabecera
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("← Volver") }
            Text("Mejorar a Premium", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 1. Selección de Plan
        Text("1. Selecciona tu plan:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(10.dp))

        val planes = listOf("Mensual Premium" to 10, "Semestral Premium" to 30, "Anual Premium" to 50)
        planes.forEach { (nombre, precio) ->
            OutlinedButton(
                onClick = { planSeleccionado = nombre; precioSeleccionado = precio },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                border = BorderStroke(2.dp, if (planSeleccionado == nombre) MaterialTheme.colorScheme.primary else Color.LightGray),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = if (planSeleccionado == nombre) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(nombre, color = Color.Black)
                    Text("${precio}€", fontWeight = FontWeight.Bold, color = Color.Black)
                }
            }
        }

        // 2. Método de Pago
        if (planSeleccionado.isNotEmpty()) {
            Spacer(modifier = Modifier.height(30.dp))
            Text("2. Método de pago:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = { metodoPago = "Tarjeta" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (metodoPago == "Tarjeta") MaterialTheme.colorScheme.secondary else Color.Gray)
                ) { Text("Tarjeta") }

                Button(
                    onClick = { metodoPago = "PayPal" },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = if (metodoPago == "PayPal") Color(0xFF003087) else Color.Gray)
                ) { Text("PayPal") }
            }
        }

        // 3. Formulario de Tarjeta
        if (metodoPago == "Tarjeta") {
            Spacer(modifier = Modifier.height(30.dp))
            Text("Datos de la Tarjeta", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = numeroTarjeta,
                onValueChange = { if (it.length <= 16) numeroTarjeta = it },
                label = { Text("Número de Tarjeta (16 dígitos)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(15.dp)) {
                OutlinedTextField(
                    value = cvv,
                    onValueChange = { if (it.length <= 3) cvv = it },
                    label = { Text("CVV") },
                    modifier = Modifier.weight(1f)
                )

                Column(modifier = Modifier.weight(1f)) {
                    Text("Fecha de caducidad", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, modifier = Modifier.padding(start = 4.dp))
                    OutlinedButton(
                        onClick = {
                            DatePickerDialog(context, { _, y, m, d ->
                                fechaCaducidad = String.format("%02d/%02d/%d", d, m + 1, y)
                            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).apply {
                                datePicker.minDate = System.currentTimeMillis() + 86400000
                            }.show()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text(fechaCaducidad, fontSize = 14.sp) }
                }
            }
        }

        // Botón de confirmación Final
        if (metodoPago.isNotEmpty()) {
            Spacer(modifier = Modifier.height(40.dp))
            Button(
                onClick = {
                    if (metodoPago == "Tarjeta" && (numeroTarjeta.length < 16 || cvv.length < 3 || fechaCaducidad == "Seleccionar")) {
                        Toast.makeText(context, "Completa los datos de la tarjeta", Toast.LENGTH_SHORT).show()
                    } else {
                        procesarCompra()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37))
            ) {
                Text("Confirmar Pago de ${precioSeleccionado}€", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}