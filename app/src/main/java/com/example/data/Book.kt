package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Entidad que representa un Libro en la base de datos local SQLite (Room).
 *
 * PUNTOS DONDE ALGUIEN PUEDE EQUIVOCARSE:
 * 1. Omitir el índice único en 'code': Permitiría duplicar libros con el mismo código de inventario
 *    de aula, lo que causaría descontrol físico de los ejemplares.
 * 2. Iniciar el 'id' en un valor distinto de 0: Room solo autogenera la clave primaria si el id es 0.
 * 3. Omitir 'loanCount': Este contador incremental alimenta las estadísticas del Dashboard Analítico
 *    para identificar el libro más popular y solicitado por los alumnos de la sección.
 */
@Entity(
    tableName = "books",
    indices = [Index(value = ["code"], unique = true)]
)
data class Book(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Función 1: Datos de registro del libro
    val title: String,
    val author: String,
    val code: String,

    // Funciones 2 y 3: Estado de disponibilidad y préstamo
    val isLoaned: Boolean = false,
    val loanedToStudent: String? = null,
    val loanDate: Long? = null,
    val dueDate: Long? = null,

    // Dashboard Analítico: Métrica de popularidad acumulada
    val loanCount: Int = 0,

    val createdAt: Long = System.currentTimeMillis()
)
