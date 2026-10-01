package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para la Biblioteca de Aula - Nivel Dashboard Ejecutivo.
 *
 * PUNTOS DONDE ALGUIEN PUEDE EQUIVOCARSE:
 * 1. No incrementar 'loanCount': Al prestar un libro, se incrementa atómicamente 'loanCount = loanCount + 1'
 *    para que las métricas del Dashboard reflejen en tiempo real la popularidad del ejemplar.
 * 2. Consultas en el hilo principal: Todas las mutaciones son suspendidas y las consultas reactivas
 *    utilizan 'Flow<T>' para refrescar el Dashboard automáticamente sin recargar la pantalla.
 */
@Dao
interface BookDao {

    // Lista completa de libros ordenada ascendentemente por código
    @Query("SELECT * FROM books ORDER BY code ASC")
    fun getAllBooks(): Flow<List<Book>>

    // Lista de libros disponibles ordenada ascendentemente por código
    @Query("SELECT * FROM books WHERE isLoaned = 0 ORDER BY code ASC")
    fun getAvailableBooks(): Flow<List<Book>>

    // Lista de libros prestados ordenada ascendentemente por código
    @Query("SELECT * FROM books WHERE isLoaned = 1 ORDER BY code ASC")
    fun getLoanedBooks(): Flow<List<Book>>

    // Dashboard: Libro más popular (mayor cantidad de préstamos acumulados)
    @Query("SELECT * FROM books ORDER BY loanCount DESC, title ASC LIMIT 1")
    fun getMostPopularBook(): Flow<Book?>

    // Obtener un libro por ID para comprobaciones atómicas de disponibilidad
    @Query("SELECT * FROM books WHERE id = :bookId LIMIT 1")
    suspend fun getBookById(bookId: Long): Book?

    // Verificación de código duplicado insensible a mayúsculas y espacios
    @Query("SELECT COUNT(*) FROM books WHERE UPPER(TRIM(code)) = UPPER(TRIM(:code))")
    suspend fun countBooksWithCode(code: String): Int

    // Inserción de libro nuevo
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertBook(book: Book): Long

    @Update
    suspend fun updateBook(book: Book)

    // Registro de préstamo e incremento del contador analítico de popularidad
    @Query("UPDATE books SET isLoaned = 1, loanedToStudent = :student, loanDate = :loanDate, dueDate = :dueDate, loanCount = loanCount + 1 WHERE id = :bookId")
    suspend fun registerLoan(bookId: Long, student: String, loanDate: Long, dueDate: Long)

    // Registro de devolución (restablece disponibilidad)
    @Query("UPDATE books SET isLoaned = 0, loanedToStudent = NULL, loanDate = NULL, dueDate = NULL WHERE id = :bookId")
    suspend fun returnBook(bookId: Long)
}
