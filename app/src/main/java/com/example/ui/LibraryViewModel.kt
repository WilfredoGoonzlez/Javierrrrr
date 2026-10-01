package com.example.ui

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.Book
import com.example.data.BookRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Tipos de alertas visuales en banner para la UI.
 */
enum class BannerType {
    SUCCESS,
    INFO,
    WARNING,
    ERROR
}

data class BannerAlert(
    val message: String,
    val type: BannerType = BannerType.SUCCESS,
    val timestamp: Long = System.currentTimeMillis()
)

enum class SortMode {
    CODE_ASC,
    TITLE_ASC
}

enum class ExportFormat {
    CSV,
    JSON
}

/**
 * ViewModel para la Biblioteca de Aula - Nivel Producción & Megaproyecto.
 *
 * CARACTERÍSTICAS Y ARQUITECTURA:
 * 1. Persistencia de Tema: Guarda y recupera la preferencia de Modo Oscuro / Modo Claro en SharedPreferences.
 * 2. Navegación en 3 Pestañas Principales: Dashboard (0), Catálogo (1) e Historial de Préstamos (2).
 * 3. Módulo de Exportación Avanzado: Genera archivos CSV con BOM UTF-8 (\uFEFF) para visualización inmediata
 *    y sin corrupción de caracteres en Microsoft Excel y Google Sheets.
 * 4. Métricas en Tiempo Real: Conectadas a Room SQLite mediante Flows atómicos.
 */
class LibraryViewModel(
    private val repository: BookRepository,
    context: Context? = null
) : ViewModel() {

    private val prefs: SharedPreferences? = try {
        context?.getSharedPreferences("biblioteca_aula_prefs", Context.MODE_PRIVATE)
    } catch (e: Exception) {
        null
    }

    // Estado reactivo y persistente del Modo Oscuro
    private val _isDarkMode = MutableStateFlow(
        prefs?.getBoolean("dark_mode_enabled", false) ?: false
    )
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Flujos base desde Room (actualización automática en tiempo real)
    val allBooks: StateFlow<List<Book>> = repository.allBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val availableBooks: StateFlow<List<Book>> = repository.availableBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val loanedBooks: StateFlow<List<Book>> = repository.loanedBooks
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Dashboard: Libro más popular o solicitado (mayor historial de préstamos)
    val mostPopularBook: StateFlow<Book?> = repository.mostPopularBook
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    // Pestaña principal activa:
    // 0 = Dashboard Ejecutivo
    // 1 = Catálogo de Libros
    // 2 = Historial y Control de Préstamos
    private val _selectedTab = MutableStateFlow(0)
    val selectedTab: StateFlow<Int> = _selectedTab.asStateFlow()

    // Subfiltro del Catálogo (0 = Todos, 1 = Solo Disponibles)
    private val _catalogFilter = MutableStateFlow(0)
    val catalogFilter: StateFlow<Int> = _catalogFilter.asStateFlow()

    // Búsqueda en tiempo real
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Ordenamiento ascendente (Por Código o Por Título)
    private val _sortMode = MutableStateFlow(SortMode.CODE_ASC)
    val sortMode: StateFlow<SortMode> = _sortMode.asStateFlow()

    // Exportación de datos
    private val _showExportDialog = MutableStateFlow(false)
    val showExportDialog: StateFlow<Boolean> = _showExportDialog.asStateFlow()

    private val _exportFormat = MutableStateFlow(ExportFormat.CSV)
    val exportFormat: StateFlow<ExportFormat> = _exportFormat.asStateFlow()

    // Alerta visual en pantalla (Banner animado)
    private val _bannerAlert = MutableStateFlow<BannerAlert?>(null)
    val bannerAlert: StateFlow<BannerAlert?> = _bannerAlert.asStateFlow()
    private var bannerDismissJob: Job? = null

    // Estados de diálogos modales
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog.asStateFlow()

    private val _bookToLoan = MutableStateFlow<Book?>(null)
    val bookToLoan: StateFlow<Book?> = _bookToLoan.asStateFlow()

    private val _bookToReturn = MutableStateFlow<Book?>(null)
    val bookToReturn: StateFlow<Book?> = _bookToReturn.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    /**
     * Libros filtrados y ordenados para el Catálogo (Pestaña 1).
     */
    val catalogBooks: StateFlow<List<Book>> = combine(
        allBooks,
        catalogFilter,
        searchQuery,
        sortMode
    ) { all, filter, query, sort ->
        val baseList = when (filter) {
            1 -> all.filter { !it.isLoaned }
            else -> all
        }

        val filtered = if (query.isBlank()) {
            baseList
        } else {
            val q = query.trim().lowercase()
            baseList.filter { book ->
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                book.code.lowercase().contains(q) ||
                (book.loanedToStudent?.lowercase()?.contains(q) == true)
            }
        }

        when (sort) {
            SortMode.CODE_ASC -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.code })
            SortMode.TITLE_ASC -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Alias para compatibilidad con pruebas unitarias
    val displayedBooks: StateFlow<List<Book>> = catalogBooks

    /**
     * Libros en préstamo para la Pestaña de Historial (Pestaña 2).
     */
    val historyLoanedBooks: StateFlow<List<Book>> = combine(
        loanedBooks,
        searchQuery,
        sortMode
    ) { loaned, query, sort ->
        val filtered = if (query.isBlank()) {
            loaned
        } else {
            val q = query.trim().lowercase()
            loaned.filter { book ->
                book.title.lowercase().contains(q) ||
                book.author.lowercase().contains(q) ||
                book.code.lowercase().contains(q) ||
                (book.loanedToStudent?.lowercase()?.contains(q) == true)
            }
        }

        when (sort) {
            SortMode.CODE_ASC -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.code })
            SortMode.TITLE_ASC -> filtered.sortedWith(compareBy(String.CASE_INSENSITIVE_ORDER) { it.title })
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    /**
     * Alterna y persiste el tema entre Modo Claro y Modo Oscuro.
     */
    fun toggleTheme() {
        val newMode = !_isDarkMode.value
        _isDarkMode.value = newMode
        prefs?.edit()?.putBoolean("dark_mode_enabled", newMode)?.apply()
        val text = if (newMode) "Modo Oscuro activado" else "Modo Claro activado"
        showBanner(text, BannerType.INFO)
    }

    fun selectTab(tabIndex: Int) {
        _selectedTab.value = tabIndex
    }

    fun setCatalogFilter(filterIndex: Int) {
        _catalogFilter.value = filterIndex
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSortMode(mode: SortMode) {
        _sortMode.value = mode
    }

    fun openExportDialog() {
        _showExportDialog.value = true
    }

    fun closeExportDialog() {
        _showExportDialog.value = false
    }

    fun setExportFormat(format: ExportFormat) {
        _exportFormat.value = format
    }

    fun openAddDialog() {
        _showAddDialog.value = true
    }

    fun closeAddDialog() {
        _showAddDialog.value = false
    }

    fun openLoanDialog(book: Book) {
        _bookToLoan.value = book
    }

    fun closeLoanDialog() {
        _bookToLoan.value = null
    }

    fun openReturnDialog(book: Book) {
        _bookToReturn.value = book
    }

    fun closeReturnDialog() {
        _bookToReturn.value = null
    }

    fun dismissBanner() {
        bannerDismissJob?.cancel()
        _bannerAlert.value = null
    }

    fun showBanner(message: String, type: BannerType) {
        bannerDismissJob?.cancel()
        _bannerAlert.value = BannerAlert(message, type)
        bannerDismissJob = viewModelScope.launch {
            delay(4500)
            _bannerAlert.value = null
        }
    }

    /**
     * Carga de libros de muestra iniciales con préstamo activo para demostración visual inmediata.
     */
    fun seedSampleBooks() {
        if (_isSubmitting.value) return
        viewModelScope.launch {
            _isSubmitting.value = true
            val b1 = repository.registerBook("Cien años de soledad", "Gabriel García Márquez", "LIT-01").getOrNull()
            repository.registerBook("Don Quijote de la Mancha", "Miguel de Cervantes", "LIT-02")
            repository.registerBook("Álgebra de Baldor", "Aurelio Baldor", "MAT-01")
            repository.registerBook("Física Conceptual", "Paul Hewitt", "CIEN-01")
            repository.registerBook("Historia Universal", "Juan Brom", "SOC-01")

            // Registrar un préstamo activo de muestra
            if (b1 != null) {
                repository.registerLoan(b1, "Sofía Morales", 7)
            }

            _isSubmitting.value = false
            showBanner("¡Se cargaron 5 libros con préstamos activos para demostración!", BannerType.SUCCESS)
        }
    }

    /**
     * Generación de contenido para exportación (CSV optimizado para Excel/Sheets o JSON).
     */
    fun generateExportContent(): String {
        val books = allBooks.value
        return when (_exportFormat.value) {
            ExportFormat.CSV -> buildCsvExport(books)
            ExportFormat.JSON -> buildJsonExport(books)
        }
    }

    /**
     * Construcción de CSV con BOM UTF-8 (\uFEFF) para abrir directamente en Excel y Google Sheets
     * sin corrupción de acentos, tildes ni caracteres del español.
     */
    fun buildCsvExport(books: List<Book>): String {
        val sb = StringBuilder()
        // Byte Order Mark (BOM) UTF-8: Indispensable para que Microsoft Excel en Windows/Mac
        // identifique automáticamente la codificación UTF-8 y no rompa caracteres como 'Á', 'ñ', 'í'.
        sb.append("\uFEFF")
        sb.append("CODIGO,TITULO,AUTOR,ESTADO,ESTUDIANTE_RESPONSABLE,FECHA_PRESTAMO,FECHA_DEVOLUCION,DIAS_RESTANTES,ESTADO_PLAZO,TOTAL_PRESTAMOS\n")

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val now = System.currentTimeMillis()

        for (b in books) {
            val status = if (b.isLoaned) "PRESTADO" else "DISPONIBLE"
            val student = b.loanedToStudent ?: ""
            val loanDate = if (b.loanDate != null && b.loanDate > 0) sdf.format(Date(b.loanDate)) else ""
            val dueDate = if (b.dueDate != null && b.dueDate > 0) sdf.format(Date(b.dueDate)) else ""

            val (diasRestantesStr, estadoPlazo) = if (b.isLoaned && b.dueDate != null) {
                val diffMs = b.dueDate - now
                val diffDays = (diffMs / (24L * 60L * 60L * 1000L)).toInt()
                if (diffMs < 0) {
                    val overdueDays = abs(diffDays)
                    "$overdueDays días" to "VENCIDO"
                } else {
                    "$diffDays días" to "A TIEMPO"
                }
            } else {
                "-" to "EN ESTANTE"
            }

            val cleanTitle = b.title.replace("\"", "\"\"")
            val cleanAuthor = b.author.replace("\"", "\"\"")
            val cleanStudent = student.replace("\"", "\"\"")

            sb.append("\"${b.code}\",\"$cleanTitle\",\"$cleanAuthor\",\"$status\",\"$cleanStudent\",\"$loanDate\",\"$dueDate\",\"$diasRestantesStr\",\"$estadoPlazo\",${b.loanCount}\n")
        }
        return sb.toString()
    }

    /**
     * Escribe el reporte CSV en la memoria caché para compartir mediante FileProvider.
     */
    fun writeCsvToCache(context: Context): File {
        val file = File(context.cacheDir, "reporte_biblioteca_aula.csv")
        file.writeText(buildCsvExport(allBooks.value), Charsets.UTF_8)
        return file
    }

    private fun buildJsonExport(books: List<Book>): String {
        val sb = StringBuilder()
        sb.append("[\n")
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())

        books.forEachIndexed { index, b ->
            val loanDateStr = if (b.loanDate != null && b.loanDate > 0) "\"${sdf.format(Date(b.loanDate))}\"" else "null"
            val dueDateStr = if (b.dueDate != null && b.dueDate > 0) "\"${sdf.format(Date(b.dueDate))}\"" else "null"
            val studentStr = if (b.loanedToStudent != null) "\"${escapeJson(b.loanedToStudent)}\"" else "null"

            sb.append("  {\n")
            sb.append("    \"id\": ${b.id},\n")
            sb.append("    \"code\": \"${escapeJson(b.code)}\",\n")
            sb.append("    \"title\": \"${escapeJson(b.title)}\",\n")
            sb.append("    \"author\": \"${escapeJson(b.author)}\",\n")
            sb.append("    \"isLoaned\": ${b.isLoaned},\n")
            sb.append("    \"loanedToStudent\": $studentStr,\n")
            sb.append("    \"loanDate\": $loanDateStr,\n")
            sb.append("    \"dueDate\": $dueDateStr,\n")
            sb.append("    \"loanCount\": ${b.loanCount}\n")
            sb.append("  }")
            if (index < books.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }

    private fun escapeJson(text: String): String {
        return text.replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }

    /**
     * Registrar libro nuevo con validación estricta y persistencia robusta
     */
    fun registerBook(
        title: String,
        author: String,
        code: String,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.registerBook(title, author, code)
            _isSubmitting.value = false

            result.fold(
                onSuccess = {
                    showBanner("¡Libro '${title.trim()}' registrado con éxito! (Cód: ${code.trim().uppercase()})", BannerType.SUCCESS)
                    _showAddDialog.value = false
                    onSuccess()
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Error al registrar el libro"
                    onError(errorMsg)
                    showBanner(errorMsg, BannerType.ERROR)
                }
            )
        }
    }

    /**
     * Registrar préstamo verificando disponibilidad en SQLite
     */
    fun registerLoan(
        bookId: Long,
        studentName: String,
        days: Int,
        onError: (String) -> Unit,
        onSuccess: () -> Unit
    ) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.registerLoan(bookId, studentName, days)
            _isSubmitting.value = false

            result.fold(
                onSuccess = {
                    showBanner("Préstamo registrado exitosamente a '$studentName'. Plazo: $days días.", BannerType.INFO)
                    _bookToLoan.value = null
                    onSuccess()
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Error al registrar el préstamo"
                    onError(errorMsg)
                    showBanner(errorMsg, BannerType.WARNING)
                }
            )
        }
    }

    /**
     * Registrar devolución con actualización inmediata del estado
     */
    fun confirmReturnBook(book: Book) {
        if (_isSubmitting.value) return

        viewModelScope.launch {
            _isSubmitting.value = true
            val result = repository.returnBook(book.id)
            _isSubmitting.value = false

            result.fold(
                onSuccess = {
                    showBanner("¡Devolución confirmada! El libro '${book.title}' vuelve a estar disponible en el aula.", BannerType.SUCCESS)
                    _bookToReturn.value = null
                },
                onFailure = { error ->
                    val errorMsg = error.localizedMessage ?: "Error al procesar devolución"
                    showBanner(errorMsg, BannerType.ERROR)
                    _bookToReturn.value = null
                }
            )
        }
    }

    class Factory(
        private val repository: BookRepository,
        private val context: Context? = null
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
                return LibraryViewModel(repository, context?.applicationContext) as T
            }
            throw IllegalArgumentException("Clase ViewModel desconocida")
        }
    }
}
