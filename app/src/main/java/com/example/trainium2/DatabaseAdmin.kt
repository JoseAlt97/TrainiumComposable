package com.example.trainium2

import java.sql.Connection
import java.sql.DriverManager

object DatabaseAdmin {
    // Usamos la IP 10.0.2.2 para el emulador.
    // Añadimos parámetros para evitar errores de SSL y zona horaria.
    private const val url = "jdbc:mysql://10.0.2.2:3306/bbddgimnasio?useSSL=false&serverTimezone=UTC"
    private const val user = "admin_tren"
    private const val pass = "admin123"

    fun connection(): Connection? {
        return try {
            // Usamos el driver estable para Android
            Class.forName("com.mysql.jdbc.Driver")
            DriverManager.getConnection(url, user, pass)
        } catch (e: Exception) {
            // Esto imprimirá el error real en tu Logcat si la conexión falla
            e.printStackTrace()
            null
        }
    }
}