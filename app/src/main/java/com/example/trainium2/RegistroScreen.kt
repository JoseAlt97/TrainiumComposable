package com.example.trainium2

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*

// Modelo para los registros de peso
data class RegistroPeso(
    val id: Int,
    val peso: Double,
    val fecha: String
)

@Composable
fun RegistroScreen(idUsuario: Int, onBack: () -> Unit) {
    var listaPesos by remember { mutableStateOf(listOf<RegistroPeso>()) }
    var cargando by remember { mutableStateOf(true) }

    // Estados para el diálogo de añadir/editar
    var mostrarDialogo by remember { mutableStateOf(false) }
    var nuevoPesoInput by remember { mutableStateOf("") }
    var idRegistroAEditar by remember { mutableStateOf<Int?>(null) }

    val scope = rememberCoroutineScope()
    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val fechaHoy = sdf.format(Date())

    // --- FUNCIÓN PARA CARGAR DATOS ---
    fun cargarDatos() {
        cargando = true
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            val tempLista = mutableListOf<RegistroPeso>()
            if (conn != null) {
                try {
                    val query = "SELECT ID, PESO, FECHA FROM peso_usuario WHERE ID_USUARIO = ? ORDER BY FECHA DESC"
                    val stmt = conn.prepareStatement(query)
                    stmt.setInt(1, idUsuario)
                    val rs = stmt.executeQuery()
                    while (rs.next()) {
                        tempLista.add(RegistroPeso(
                            rs.getInt("ID"),
                            rs.getDouble("PESO"),
                            rs.getString("FECHA")
                        ))
                    }
                    conn.close()
                } catch (e: Exception) { e.printStackTrace() }
            }
            withContext(Dispatchers.Main) {
                listaPesos = tempLista
                cargando = false
            }
        }
    }

    // --- FUNCIÓN PARA INSERTAR O ACTUALIZAR PESO ---
    fun guardarPeso(peso: Double) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    if (idRegistroAEditar == null) {
                        // INSERTAR NUEVO
                        val query = "INSERT INTO peso_usuario (ID_USUARIO, PESO, FECHA) VALUES (?, ?, ?)"
                        val stmt = conn.prepareStatement(query)
                        stmt.setInt(1, idUsuario)
                        stmt.setDouble(2, peso)
                        stmt.setString(3, fechaHoy)
                        stmt.executeUpdate()
                    } else {
                        // ACTUALIZAR EXISTENTE
                        val query = "UPDATE peso_usuario SET PESO = ? WHERE ID = ?"
                        val stmt = conn.prepareStatement(query)
                        stmt.setDouble(1, peso)
                        stmt.setInt(2, idRegistroAEditar!!)
                        stmt.executeUpdate()
                    }
                    conn.close()
                    cargarDatos()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    // --- FUNCIÓN PARA ELIMINAR PESO ---
    fun eliminarPeso(id: Int) {
        scope.launch(Dispatchers.IO) {
            val conn = DatabaseAdmin.connection()
            if (conn != null) {
                try {
                    val query = "DELETE FROM peso_usuario WHERE ID = ?"
                    val stmt = conn.prepareStatement(query)
                    stmt.setInt(1, id)
                    stmt.executeUpdate()
                    conn.close()
                    cargarDatos()
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    LaunchedEffect(Unit) { cargarDatos() }

    Scaffold(
        floatingActionButton = {
            // Solo mostramos el botón de añadir si NO hay ya un registro hoy
            val yaExisteHoy = listaPesos.any { it.fecha == fechaHoy }
            if (!yaExisteHoy) {
                FloatingActionButton(
                    onClick = {
                        idRegistroAEditar = null
                        nuevoPesoInput = ""
                        mostrarDialogo = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Añadir", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onBack) { Text("← Volver") }
                Spacer(modifier = Modifier.width(10.dp))
                Text("Mi Historial de Peso", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (cargando) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listaPesos) { registro ->
                        val esHoy = registro.fecha == fechaHoy

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(4.dp),
                            // Si es hoy, le damos un color sutilmente diferente para destacarlo
                            colors = if (esHoy) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                            else CardDefaults.cardColors()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Fecha: ${registro.fecha}", fontSize = 14.sp, color = Color.Gray)
                                    Text("${registro.peso} kg", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                                }

                                // BOTONES DE ACCIÓN (Solo visibles si el registro es de hoy)
                                if (esHoy) {
                                    Row {
                                        IconButton(onClick = {
                                            idRegistroAEditar = registro.id
                                            nuevoPesoInput = registro.peso.toString()
                                            mostrarDialogo = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.primary)
                                        }
                                        IconButton(onClick = { eliminarPeso(registro.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = Color.Red)
                                        }
                                    }
                                } else {
                                    Text("⚖️", fontSize = 28.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text(if (idRegistroAEditar == null) "Añadir peso" else "Modificar peso hoy") },
                text = {
                    OutlinedTextField(
                        value = nuevoPesoInput,
                        onValueChange = { nuevoPesoInput = it },
                        label = { Text("Peso (kg)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        val pesoVal = nuevoPesoInput.replace(',', '.').toDoubleOrNull()
                        if (pesoVal != null) {
                            guardarPeso(pesoVal)
                            mostrarDialogo = false
                        }
                    }) { Text("Guardar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarDialogo = false }) { Text("Cancelar") }
                }
            )
        }
    }
}