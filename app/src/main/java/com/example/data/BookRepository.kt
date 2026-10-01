package com.example.data

import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de la Biblioteca de Aula - Nivel Dashboard Analítico.
 * Provee datos ordenados ascendentemente y centraliza las validaciones de negocio.
 *
 * PUNTOS DONDE ALGUIEN PUEDE EQUIVOCARSE:
 * 1. Omitir validación de espacios vacíos con '.trim()'.
 * 2. Asumir disponibilidad del libro sin verificar 'book.isLoaned' en la base de datos local.
 * 3. Cálculos de tiempo con enteros de 32 bits en vez de 'Long' (sufijo L).
 */
class BookRepository(private val bookDao: BookDao) {

    // Todos los libros ordenados ascendentemente por código
    val allBooks: Flow<List<Book>> = bookDao.getAllBooks()

    // Libros disponibles ordenados ascendentemente por código
    val availableBooks: Flow<List<Book>> = bookDao.getAvailableBooks()

    // Libros prestados ordenados ascendentemente por código
    val loanedBooks: Flow<List<Book>> = bookDao.getLoanedBooks()

    // Dashboard: Libro más popular
    val mostPopularBook: Flow<Book?> = bookDao.getMostPopularBook()

    /**
     * Verifica si un código de inventario físico está disponible
     */
    suspend fun isCodeAvailable(code: String): Boolean {
        val cleanCode = code.trim().uppercase()
        if (cleanCode.isEmpty()) return false
        return bookDao.countBooksWithCode(cleanCode) == 0
    }

    /**
     * Registrar libro con validación estricta de vacíos y unicidad de código
     */
    suspend fun registerBook(title: String, author: String, code: String): Result<Long> {
        val cleanTitle = title.trim()
        val cleanAuthor = author.trim()
        val cleanCode = code.trim().uppercase()

        if (cleanTitle.isEmpty()) {
            return Result.failure(IllegalArgumentException("El título del libro no puede estar vacío ni contener solo espacios."))
        }
        if (cleanAuthor.isEmpty()) {
            return Result.failure(IllegalArgumentException("El autor del libro no puede estar vacío ni contener solo espacios."))
        }
        if (cleanCode.isEmpty()) {
            return Result.failure(IllegalArgumentException("El código del libro no puede estar vacío ni contener solo espacios."))
        }

        val existingCount = bookDao.countBooksWithCode(cleanCode)
        if (existingCount > 0) {
            return Result.failure(IllegalArgumentException("No se puede registrar: Ya existe un libro registrado con el código '$cleanCode'."))
        }

        val newBook = Book(
            title = cleanTitle,
            author = cleanAuthor,
            code = cleanCode,
            isLoaned = false,
            loanCount = 0
        )

        return try {
            val insertedId = bookDao.insertBook(newBook)
            Result.success(insertedId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registrar préstamo verificando que el libro realmente esté disponible
     * e incrementando el contador analítico de popularidad.
     */
    suspend fun registerLoan(bookId: Long, studentName: String, daysToReturn: Int): Result<Unit> {
        val cleanStudent = studentName.trim()

        if (cleanStudent.isEmpty()) {
            return Result.failure(IllegalArgumentException("Debe ingresar el nombre del estudiante responsable."))
        }

        if (daysToReturn <= 0) {
            return Result.failure(IllegalArgumentException("El plazo de préstamo debe ser de al menos 1 día."))
        }

        val currentBook = bookDao.getBookById(bookId)
            ?: return Result.failure(IllegalArgumentException("El libro seleccionado no existe en el inventario."))

        if (currentBook.isLoaned) {
            return Result.failure(
                IllegalStateException(
                    "Aviso: El libro '${currentBook.title}' (Cód: ${currentBook.code}) ya se encuentra prestado a ${currentBook.loanedToStudent ?: "otro estudiante"}. No está disponible para un nuevo préstamo hasta que sea devuelto."
                )
            )
        }

        val now = System.currentTimeMillis()
        val returnPeriodMs = daysToReturn.toLong() * 24L * 60L * 60L * 1000L
        val calculatedDueDate = now + returnPeriodMs

        return try {
            bookDao.registerLoan(
                bookId = bookId,
                student = cleanStudent,
                loanDate = now,
                dueDate = calculatedDueDate
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Registrar devolución para reincorporar el libro al inventario disponible
     */
    suspend fun returnBook(bookId: Long): Result<Unit> {
        val currentBook = bookDao.getBookById(bookId)
            ?: return Result.failure(IllegalArgumentException("El libro no existe en la biblioteca."))

        if (!currentBook.isLoaned) {
            return Result.failure(IllegalStateException("El libro '${currentBook.title}' ya se encuentra disponible en la biblioteca."))
        }

        return try {
            bookDao.returnBook(bookId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
