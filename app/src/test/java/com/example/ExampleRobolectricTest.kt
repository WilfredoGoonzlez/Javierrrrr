package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.BookRepository
import com.example.ui.ExportFormat
import com.example.ui.LibraryViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: BookRepository
    private lateinit var viewModel: LibraryViewModel

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = BookRepository(db.bookDao())
        viewModel = LibraryViewModel(repository, context)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun verifyAppNameResource() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Biblioteca de Aula", appName)
    }

    @Test
    fun testAscendingOrderingByCode() = runBlocking {
        repository.registerBook("Matemáticas Avanzadas", "Baldor", "MAT-01")
        repository.registerBook("Álgebra Lineal", "Grossman", "ALG-01")
        repository.registerBook("Biología General", "Campbell", "BIO-05")

        val books = repository.allBooks.first()
        assertEquals(3, books.size)
        assertEquals("ALG-01", books[0].code)
        assertEquals("BIO-05", books[1].code)
        assertEquals("MAT-01", books[2].code)
    }

    @Test
    fun testRealTimeSearchFiltering() = runBlocking {
        // M3 - 2: Búsqueda reactiva en tiempo real por título, autor o código
        repository.registerBook("Cien años de soledad", "Gabriel García Márquez", "LIT-01")
        repository.registerBook("Física Conceptual", "Paul Hewitt", "FIS-10")

        viewModel.updateSearchQuery("Hewitt")
        val results = viewModel.displayedBooks.first()
        assertEquals(1, results.size)
        assertEquals("FIS-10", results[0].code)

        viewModel.updateSearchQuery("LIT-01")
        val codeResults = viewModel.displayedBooks.first()
        assertEquals(1, codeResults.size)
        assertEquals("Cien años de soledad", codeResults[0].title)

        viewModel.updateSearchQuery("")
        val allResults = viewModel.displayedBooks.first()
        assertEquals(2, allResults.size)
    }

    @Test
    fun testExportDataCsvAndJson() = runBlocking {
        // M3 - 3: Exportación a CSV y JSON
        val bookId = repository.registerBook("Don Quijote", "Cervantes", "LIT-02").getOrThrow()
        repository.registerLoan(bookId, "Mateo Silva", 7)

        viewModel.setExportFormat(ExportFormat.CSV)
        val csv = viewModel.generateExportContent()
        assertTrue("El CSV debe contener cabeceras", csv.contains("CODIGO,TITULO,AUTOR,ESTADO,ESTUDIANTE"))
        assertTrue("El CSV debe incluir el código LIT-02", csv.contains("LIT-02"))
        assertTrue("El CSV debe incluir el estudiante", csv.contains("Mateo Silva"))

        viewModel.setExportFormat(ExportFormat.JSON)
        val json = viewModel.generateExportContent()
        assertTrue("El JSON debe comenzar con corchete", json.trim().startsWith("["))
        assertTrue("El JSON debe contener el código", json.contains("\"code\": \"LIT-02\""))
        assertTrue("El JSON debe contener el estado de préstamo", json.contains("\"isLoaned\": true"))
    }

    @Test
    fun testExecutiveAnalyticsDashboardMetricsUpdate() = runBlocking {
        // Tarea Megaproyecto: Dashboard Analítico en tiempo real
        // 1. Registro de 3 libros
        val id1 = repository.registerBook("Cien años de soledad", "García Márquez", "LIT-01").getOrThrow()
        val id2 = repository.registerBook("Don Quijote", "Cervantes", "LIT-02").getOrThrow()
        val id3 = repository.registerBook("Álgebra de Baldor", "Baldor", "MAT-01").getOrThrow()

        var total = repository.allBooks.first().size
        var available = repository.availableBooks.first().size
        var loaned = repository.loanedBooks.first().size

        assertEquals(3, total)
        assertEquals(3, available)
        assertEquals(0, loaned)

        // 2. Prestar el libro 1
        repository.registerLoan(id1, "Sofía Morales", 5)

        available = repository.availableBooks.first().size
        loaned = repository.loanedBooks.first().size
        var popular = repository.mostPopularBook.first()

        assertEquals(2, available)
        assertEquals(1, loaned)
        assertEquals("LIT-01", popular?.code)
        assertEquals(1, popular?.loanCount)

        // 3. Devolver el libro 1
        repository.returnBook(id1)

        available = repository.availableBooks.first().size
        loaned = repository.loanedBooks.first().size
        assertEquals(3, available)
        assertEquals(0, loaned)

        // 4. Prestar libro 1 de nuevo -> loanCount = 2
        repository.registerLoan(id1, "Carlos Gómez", 3)
        // Prestar libro 2 -> loanCount = 1
        repository.registerLoan(id2, "Lucía Méndez", 4)

        available = repository.availableBooks.first().size
        loaned = repository.loanedBooks.first().size
        popular = repository.mostPopularBook.first()

        assertEquals(1, available)
        assertEquals(2, loaned)
        assertEquals("LIT-01", popular?.code)
        assertEquals(2, popular?.loanCount)
    }

    @Test
    fun testPersistentThemeToggle() {
        val initialMode = viewModel.isDarkMode.value
        viewModel.toggleTheme()
        val toggledMode = viewModel.isDarkMode.value
        assertEquals(!initialMode, toggledMode)

        viewModel.toggleTheme()
        assertEquals(initialMode, viewModel.isDarkMode.value)
    }

    @Test
    fun testEnhancedCsvUtf8BomAndExcelCompatibility() = runBlocking {
        val id = repository.registerBook("Historia Universal", "Juan Brom", "SOC-01").getOrThrow()
        repository.registerLoan(id, "Lucía Méndez", 5)

        viewModel.setExportFormat(ExportFormat.CSV)
        val csv = viewModel.generateExportContent()

        // 1. Debe comenzar con la Marca de Orden de Bytes (BOM) UTF-8 (\uFEFF)
        assertTrue("El CSV debe iniciar con BOM UTF-8 (\\uFEFF) para Excel", csv.startsWith("\uFEFF"))

        // 2. Debe contener las cabeceras completas requeridas
        assertTrue(csv.contains("CODIGO,TITULO,AUTOR,ESTADO,ESTUDIANTE_RESPONSABLE,FECHA_PRESTAMO,FECHA_DEVOLUCION,DIAS_RESTANTES,ESTADO_PLAZO,TOTAL_PRESTAMOS"))

        // 3. Debe incluir el registro del libro prestado
        assertTrue(csv.contains("SOC-01"))
        assertTrue(csv.contains("Historia Universal"))
        assertTrue(csv.contains("Lucía Méndez"))
        assertTrue(csv.contains("PRESTADO"))
    }
}
