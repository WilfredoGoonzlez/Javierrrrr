package com.example.ui

import android.content.Context
import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AssignmentReturn
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Book
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

/**
 * Pantalla Principal de la Biblioteca de Aula - Versión Final de Producción.
 *
 * CARACTERÍSTICAS DE CALIDAD INTEGRADAS:
 * 1. Alternancia de tema Modo Oscuro / Modo Claro con persistencia automática.
 * 2. Módulo de exportación de reportes CSV mejorado con BOM UTF-8 (\uFEFF) para abrir
 *    directamente en Microsoft Excel o Google Sheets, con soporte para compartir o guardar.
 * 3. Animaciones de transición fluidas (fade-in / slide direccional) entre las 3 pestañas:
 *    - Dashboard (Métricas analíticas ejecutivas en tiempo real)
 *    - Catálogo (Búsqueda en tiempo real, filtros y solicitud de préstamos)
 *    - Historial (Seguimiento de préstamos activos, alertas de retraso y devolución)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val availableBooks by viewModel.availableBooks.collectAsStateWithLifecycle()
    val loanedBooks by viewModel.loanedBooks.collectAsStateWithLifecycle()
    val mostPopularBook by viewModel.mostPopularBook.collectAsStateWithLifecycle()

    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val catalogFilter by viewModel.catalogFilter.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val sortMode by viewModel.sortMode.collectAsStateWithLifecycle()

    val catalogBooks by viewModel.catalogBooks.collectAsStateWithLifecycle()
    val historyLoanedBooks by viewModel.historyLoanedBooks.collectAsStateWithLifecycle()

    val bannerAlert by viewModel.bannerAlert.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val bookToLoan by viewModel.bookToLoan.collectAsStateWithLifecycle()
    val bookToReturn by viewModel.bookToReturn.collectAsStateWithLifecycle()
    val showExportDialog by viewModel.showExportDialog.collectAsStateWithLifecycle()
    val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()

    // Launcher para guardar/descargar el archivo CSV en el almacenamiento del dispositivo
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(viewModel.generateExportContent().toByteArray(Charsets.UTF_8))
                    stream.flush()
                }
                viewModel.showBanner("¡Reporte CSV guardado exitosamente! Listo para abrir en Excel o Google Sheets.", BannerType.SUCCESS)
            } catch (e: Exception) {
                viewModel.showBanner("Error al guardar archivo: ${e.localizedMessage}", BannerType.ERROR)
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Biblioteca de Aula",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Gestión de Sección & Dashboard",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    // Botón 1: Alternancia de Tema (Modo Oscuro / Modo Claro) persistente
                    IconButton(
                        onClick = { viewModel.toggleTheme() },
                        modifier = Modifier.testTag("toggle_theme_button")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "Cambiar a Modo Claro" else "Cambiar a Modo Oscuro",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }

                    // Botón 2: Exportar Reportes para Excel / Google Sheets
                    IconButton(
                        onClick = { viewModel.openExportDialog() },
                        modifier = Modifier.testTag("export_data_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileDownload,
                            contentDescription = "Exportar inventario y préstamos",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.openAddDialog() },
                icon = { Icon(Icons.Default.Add, contentDescription = null) },
                text = { Text("Nuevo Libro", fontWeight = FontWeight.Bold) },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier.testTag("add_book_fab")
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .widthIn(max = 760.dp)
                    .fillMaxWidth()
            ) {
                // Banner de Alerta Visual animado
                AnimatedVisibility(
                    visible = bannerAlert != null,
                    enter = fadeIn(tween(250)) + expandVertically(tween(250)),
                    exit = fadeOut(tween(200)) + shrinkVertically(tween(200))
                ) {
                    bannerAlert?.let { alert ->
                        NotificationBanner(
                            alert = alert,
                            onDismiss = { viewModel.dismissBanner() },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Pestañas Principales: Dashboard, Catálogo, Historial
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        if (selectedTab < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = MaterialTheme.colorScheme.primary,
                                height = 3.dp
                            )
                        }
                    }
                ) {
                    // Pestaña 0: Dashboard
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { viewModel.selectTab(0) },
                        modifier = Modifier.testTag("tab_dashboard"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Analytics,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Dashboard",
                                    fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    )

                    // Pestaña 1: Catálogo
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { viewModel.selectTab(1) },
                        modifier = Modifier.testTag("tab_catalog"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.MenuBook,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Catálogo",
                                    fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = if (selectedTab == 1)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.outline
                                ) {
                                    Text(allBooks.size.toString())
                                }
                            }
                        }
                    )

                    // Pestaña 2: Historial / Préstamos
                    Tab(
                        selected = selectedTab == 2,
                        onClick = { viewModel.selectTab(2) },
                        modifier = Modifier.testTag("tab_history"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AssignmentTurnedIn,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Historial",
                                    fontWeight = if (selectedTab == 2) FontWeight.Bold else FontWeight.Normal
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Badge(
                                    containerColor = if (selectedTab == 2)
                                        Color(0xFFD97706)
                                    else
                                        MaterialTheme.colorScheme.outline
                                ) {
                                    Text(loanedBooks.size.toString())
                                }
                            }
                        }
                    )
                }

                // Animaciones de Transición Fluidas (Slide + Fade) al cambiar entre pestañas
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally(tween(320)) { it / 3 } + fadeIn(tween(320)))
                                .togetherWith(slideOutHorizontally(tween(260)) { -it / 3 } + fadeOut(tween(260)))
                        } else {
                            (slideInHorizontally(tween(320)) { -it / 3 } + fadeIn(tween(320)))
                                .togetherWith(slideOutHorizontally(tween(260)) { it / 3 } + fadeOut(tween(260)))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    label = "tab_navigation_transition"
                ) { currentTab ->
                    when (currentTab) {
                        0 -> DashboardTabView(
                            allBooks = allBooks,
                            availableBooks = availableBooks,
                            loanedBooks = loanedBooks,
                            mostPopularBook = mostPopularBook,
                            onNavigateToCatalog = { viewModel.selectTab(1) },
                            onNavigateToLoans = { viewModel.selectTab(2) },
                            onOpenAddDialog = { viewModel.openAddDialog() },
                            onOpenExport = { viewModel.openExportDialog() },
                            onSeedSamples = { viewModel.seedSampleBooks() }
                        )

                        1 -> CatalogTabView(
                            books = catalogBooks,
                            totalCount = allBooks.size,
                            availableCount = availableBooks.size,
                            catalogFilter = catalogFilter,
                            searchQuery = searchQuery,
                            sortMode = sortMode,
                            onFilterChange = { viewModel.setCatalogFilter(it) },
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            onSortChange = { viewModel.setSortMode(it) },
                            onLoanClick = { viewModel.openLoanDialog(it) },
                            onAddBookClick = { viewModel.openAddDialog() }
                        )

                        2 -> LoansHistoryTabView(
                            loanedBooks = historyLoanedBooks,
                            searchQuery = searchQuery,
                            sortMode = sortMode,
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            onSortChange = { viewModel.setSortMode(it) },
                            onReturnClick = { viewModel.openReturnDialog(it) },
                            onGoToCatalog = { viewModel.selectTab(1) }
                        )
                    }
                }
            }
        }
    }

    // DIÁLOGO MODAL: EXPORTACIÓN MEJORADA PARA EXCEL Y GOOGLE SHEETS
    if (showExportDialog) {
        EnhancedExportReportDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.closeExportDialog() },
            onDownloadCsv = {
                val timestamp = SimpleDateFormat("yyyyMMdd_HHmm", Locale.getDefault()).format(Date())
                createDocumentLauncher.launch("reporte_biblioteca_aula_$timestamp.csv")
            },
            onShareCsv = {
                try {
                    val file = viewModel.writeCsvToCache(context)
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        file
                    )
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/csv"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        putExtra(Intent.EXTRA_SUBJECT, "Reporte Biblioteca de Aula - Inventario y Préstamos")
                        putExtra(
                            Intent.EXTRA_TEXT,
                            "Reporte de la Biblioteca de Aula en formato CSV con codificación UTF-8 BOM, optimizado para abrir directamente en Microsoft Excel o Google Sheets."
                        )
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Abrir con Excel / Google Sheets o Enviar"))
                } catch (e: Exception) {
                    viewModel.showBanner("Error al compartir: ${e.localizedMessage}", BannerType.ERROR)
                }
            },
            onCopyClipboard = { content ->
                clipboardManager.setText(AnnotatedString(content))
                viewModel.showBanner("¡Datos copiados al portapapeles! Puedes pegarlos en Google Sheets o Excel.", BannerType.INFO)
            }
        )
    }

    // Diálogo para registrar libro nuevo
    if (showAddDialog) {
        AddBookDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { title, author, code, onError ->
                viewModel.registerBook(
                    title = title,
                    author = author,
                    code = code,
                    onError = onError,
                    onSuccess = {}
                )
            }
        )
    }

    // Diálogo para registrar préstamo
    bookToLoan?.let { book ->
        LoanBookDialog(
            book = book,
            onDismiss = { viewModel.closeLoanDialog() },
            onConfirm = { studentName, days, onError ->
                viewModel.registerLoan(
                    bookId = book.id,
                    studentName = studentName,
                    days = days,
                    onError = onError,
                    onSuccess = {}
                )
            }
        )
    }

    // Diálogo para registrar devolución
    bookToReturn?.let { book ->
        ReturnBookDialog(
            book = book,
            onDismiss = { viewModel.closeReturnDialog() },
            onConfirm = {
                viewModel.confirmReturnBook(book)
            }
        )
    }
}

// =========================================================================================
// PESTAÑA 0: DASHBOARD ANALÍTICO SUPERIOR
// =========================================================================================
@Composable
fun DashboardTabView(
    allBooks: List<Book>,
    availableBooks: List<Book>,
    loanedBooks: List<Book>,
    mostPopularBook: Book?,
    onNavigateToCatalog: () -> Unit,
    onNavigateToLoans: () -> Unit,
    onOpenAddDialog: () -> Unit,
    onOpenExport: () -> Unit,
    onSeedSamples: () -> Unit,
    modifier: Modifier = Modifier
) {
    val total = allBooks.size
    val availableCount = availableBooks.size
    val loanedCount = loanedBooks.size

    val availPercent = if (total > 0) (availableCount * 100f / total).toInt() else 100
    val progressFloat = if (total > 0) (availableCount.toFloat() / total.toFloat()) else 1f

    val currentTime = System.currentTimeMillis()
    val overdueCount = loanedBooks.count { (it.dueDate ?: Long.MAX_VALUE) < currentTime }
    val nearDueCount = loanedBooks.count {
        val due = it.dueDate ?: Long.MAX_VALUE
        due >= currentTime && (due - currentTime) <= (48L * 60L * 60L * 1000L)
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Tarjeta Principal del Dashboard Ejecutivo
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("executive_dashboard_card"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cabecera con indicador de tiempo real
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MÉTRICAS EN TIEMPO REAL",
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.sp
                        )
                    }

                    Text(
                        text = "Actualizado",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Grid 2x2 de métricas analíticas
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "Total Libros",
                        value = total.toString(),
                        subtitle = "Acervo del aula",
                        icon = Icons.Default.MenuBook,
                        iconBg = Color(0xFFEEF2FF),
                        iconTint = Color(0xFF3730A3),
                        modifier = Modifier.weight(1f)
                    )

                    MetricCard(
                        title = "Disponibles",
                        value = availableCount.toString(),
                        subtitle = "$availPercent% en estante",
                        icon = Icons.Default.CheckCircle,
                        iconBg = Color(0xFFECFDF5),
                        iconTint = Color(0xFF059669),
                        valueColor = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricCard(
                        title = "En Préstamo",
                        value = loanedCount.toString(),
                        subtitle = if (overdueCount > 0) "$overdueCount con retraso" else "Lectura activa",
                        icon = Icons.Default.AssignmentTurnedIn,
                        iconBg = if (overdueCount > 0) Color(0xFFFFE4E6) else Color(0xFFFEF3C7),
                        iconTint = if (overdueCount > 0) Color(0xFFE11D48) else Color(0xFFD97706),
                        valueColor = if (overdueCount > 0) Color(0xFFE11D48) else Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )

                    PopularBookMetricCard(
                        popularBook = mostPopularBook,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Barra de Porcentaje de Disponibilidad
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Disponibilidad del acervo:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$availPercent% disponible ($availableCount / $total)",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF059669)
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    LinearProgressIndicator(
                        progress = { progressFloat },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = Color(0xFF10B981),
                        trackColor = Color(0xFFFBBF24)
                    )
                }
            }
        }

        // Estado del Salud de Préstamos (Salud Operativa)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ESTADO DE PRÉSTAMOS ACTIVOS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LoanHealthIndicator(
                        label = "A tiempo",
                        count = loanedCount - overdueCount,
                        color = Color(0xFF059669),
                        modifier = Modifier.weight(1f)
                    )
                    LoanHealthIndicator(
                        label = "Por vencer",
                        count = nearDueCount,
                        color = Color(0xFFD97706),
                        modifier = Modifier.weight(1f)
                    )
                    LoanHealthIndicator(
                        label = "Con retraso",
                        count = overdueCount,
                        color = Color(0xFFE11D48),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (overdueCount > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFE4E6))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = null,
                                tint = Color(0xFFE11D48),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Hay $overdueCount libro(s) que han superado el plazo de entrega. Revisa la pestaña 'Historial'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9F1239)
                            )
                        }
                    }
                }
            }
        }

        // Acciones Rápidas del Administrador
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "ACCIONES RÁPIDAS",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onOpenAddDialog,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Registrar")
                    }

                    Button(
                        onClick = onOpenExport,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.TableChart, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Reporte CSV")
                    }
                }

                if (total == 0) {
                    TextButton(
                        onClick = onSeedSamples,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cargar libros de muestra para el aula")
                    }
                }
            }
        }
    }
}

@Composable
fun LoanHealthIndicator(
    label: String,
    count: Int,
    color: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

// =========================================================================================
// PESTAÑA 1: CATÁLOGO DE LIBROS (Búsqueda en tiempo real, filtros y préstamos)
// =========================================================================================
@Composable
fun CatalogTabView(
    books: List<Book>,
    totalCount: Int,
    availableCount: Int,
    catalogFilter: Int,
    searchQuery: String,
    sortMode: SortMode,
    onFilterChange: (Int) -> Unit,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onLoanClick: (Book) -> Unit,
    onAddBookClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxSize()) {
        // Barra de Búsqueda Rápida y Filtros
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_books_input"),
                placeholder = { Text("Buscar por título, autor o código de aula...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Limpiar búsqueda"
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Subfiltros: Todos / Solo Disponibles y Criterio de Orden
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = catalogFilter == 0,
                    onClick = { onFilterChange(0) },
                    label = { Text("Todos ($totalCount)") },
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = catalogFilter == 1,
                    onClick = { onFilterChange(1) },
                    label = { Text("Disponibles ($availableCount)") },
                    shape = RoundedCornerShape(8.dp),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFFECFDF5),
                        selectedLabelColor = Color(0xFF065F46)
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                FilterChip(
                    selected = sortMode == SortMode.CODE_ASC,
                    onClick = { onSortChange(SortMode.CODE_ASC) },
                    label = { Text("Cód A-Z") },
                    leadingIcon = if (sortMode == SortMode.CODE_ASC) {
                        { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(8.dp)
                )

                FilterChip(
                    selected = sortMode == SortMode.TITLE_ASC,
                    onClick = { onSortChange(SortMode.TITLE_ASC) },
                    label = { Text("Título A-Z") },
                    leadingIcon = if (sortMode == SortMode.TITLE_ASC) {
                        { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null,
                    shape = RoundedCornerShape(8.dp)
                )
            }
        }

        // Lista de Libros del Catálogo
        if (books.isEmpty()) {
            if (searchQuery.isNotEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.SearchOff,
                    title = "Sin resultados para \"$searchQuery\"",
                    description = "No encontramos libros que coincidan con la búsqueda. Intenta con otro término o limpia el filtro.",
                    primaryActionLabel = "Limpiar búsqueda",
                    onPrimaryAction = { onSearchChange("") }
                )
            } else if (catalogFilter == 1 && totalCount > 0) {
                EmptyStateView(
                    icon = Icons.Default.School,
                    title = "No hay libros disponibles en este momento",
                    description = "Todos los ejemplares del aula están actualmente prestados a los estudiantes.",
                    primaryActionLabel = "Ver todos los libros",
                    onPrimaryAction = { onFilterChange(0) }
                )
            } else {
                EmptyStateView(
                    icon = Icons.Default.MenuBook,
                    title = "El catálogo del aula está vacío",
                    description = "Registra el primer ejemplar físico para comenzar el inventario de la biblioteca.",
                    primaryActionLabel = "Registrar Primer Libro",
                    onPrimaryAction = onAddBookClick
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("catalog_books_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(books, key = { it.id }) { book ->
                    CatalogBookCard(
                        book = book,
                        onLoanClick = { onLoanClick(book) }
                    )
                }
            }
        }
    }
}

@Composable
fun CatalogBookCard(
    book: Book,
    onLoanClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("catalog_book_card_${book.code}"),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Código del Libro
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = book.code,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                // Insignia de Estado
                if (book.isLoaned) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PRESTADO",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF92400E)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFECFDF5))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "DISPONIBLE",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }

            // Título y Autor
            Text(
                text = book.title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Autor: ${book.author}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Historial: ${book.loanCount} préstamos",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!book.isLoaned) {
                    Button(
                        onClick = onLoanClick,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.testTag("loan_button_${book.code}")
                    ) {
                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Prestar")
                    }
                } else {
                    Text(
                        text = "Prestado a ${book.loanedToStudent ?: "alumno"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = Color(0xFFD97706),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// =========================================================================================
// PESTAÑA 2: HISTORIAL Y CONTROL DE PRÉSTAMOS
// =========================================================================================
@Composable
fun LoansHistoryTabView(
    loanedBooks: List<Book>,
    searchQuery: String,
    sortMode: SortMode,
    onSearchChange: (String) -> Unit,
    onSortChange: (SortMode) -> Unit,
    onReturnClick: (Book) -> Unit,
    onGoToCatalog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sdf = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }
    val currentTime = System.currentTimeMillis()

    Column(modifier = modifier.fillMaxSize()) {
        // Barra de Búsqueda de Préstamos
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_loans_input"),
                placeholder = { Text("Buscar préstamo por alumno, libro o código...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${loanedBooks.size} préstamo(s) activo(s)",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(
                        selected = sortMode == SortMode.CODE_ASC,
                        onClick = { onSortChange(SortMode.CODE_ASC) },
                        label = { Text("Cód A-Z") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = sortMode == SortMode.TITLE_ASC,
                        onClick = { onSortChange(SortMode.TITLE_ASC) },
                        label = { Text("Título A-Z") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }
            }
        }

        if (loanedBooks.isEmpty()) {
            EmptyStateView(
                icon = Icons.Default.AssignmentTurnedIn,
                title = if (searchQuery.isNotEmpty()) "No hay préstamos con \"$searchQuery\"" else "No hay préstamos activos",
                description = if (searchQuery.isNotEmpty())
                    "Prueba buscando por el nombre del estudiante o título del libro."
                else
                    "¡Excelente! Todos los libros de la sección están disponibles y en el estante.",
                primaryActionLabel = "Ver Catálogo de Libros",
                onPrimaryAction = onGoToCatalog
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("loaned_books_list"),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(loanedBooks, key = { it.id }) { book ->
                    val dueDate = book.dueDate ?: Long.MAX_VALUE
                    val isOverdue = dueDate < currentTime
                    val diffMs = dueDate - currentTime
                    val diffDays = (diffMs / (24L * 60L * 60L * 1000L)).toInt()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("loan_card_${book.code}"),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isOverdue) Color(0xFFFCA5A5) else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Cabecera: Código + Estado de Vencimiento
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = book.code,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                if (isOverdue) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFFE4E6))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "VENCIDO HACE ${abs(diffDays)} DÍAS",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF9F1239)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(Color(0xFFFEF3C7))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "VENCE EN $diffDays DÍA(S)",
                                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color(0xFF92400E)
                                        )
                                    }
                                }
                            }

                            // Título del libro
                            Text(
                                text = book.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )

                            // Alumno responsable
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Person,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Estudiante: ${book.loanedToStudent ?: "No especificado"}",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            // Fechas de préstamo y devolución
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val loanDateStr = if (book.loanDate != null && book.loanDate > 0)
                                    sdf.format(Date(book.loanDate))
                                else "-"
                                val dueDateStr = if (book.dueDate != null && book.dueDate > 0)
                                    sdf.format(Date(book.dueDate))
                                else "-"

                                Text(
                                    text = "Prestado: $loanDateStr",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Text(
                                    text = "Devolución: $dueDateStr",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isOverdue) FontWeight.Bold else FontWeight.Normal
                                    ),
                                    color = if (isOverdue) Color(0xFFE11D48) else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(2.dp))

                            // Botón de Registrar Devolución
                            Button(
                                onClick = { onReturnClick(book) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("return_button_${book.code}"),
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isOverdue) Color(0xFFE11D48) else Color(0xFF059669)
                                )
                            ) {
                                Icon(Icons.Default.AssignmentReturn, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Registrar Devolución", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================================
// DIÁLOGO MODAL: EXPORTACIÓN MEJORADA (EXCEL, GOOGLE SHEETS & JSON)
// =========================================================================================
@Composable
fun EnhancedExportReportDialog(
    viewModel: LibraryViewModel,
    onDismiss: () -> Unit,
    onDownloadCsv: () -> Unit,
    onShareCsv: () -> Unit,
    onCopyClipboard: (String) -> Unit
) {
    val exportFormat by viewModel.exportFormat.collectAsStateWithLifecycle()
    val allBooks by viewModel.allBooks.collectAsStateWithLifecycle()
    val exportContent = remember(exportFormat, allBooks) {
        viewModel.generateExportContent()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.TableChart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Exportar Inventario y Préstamos", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "El archivo CSV incluye codificación UTF-8 con BOM (\uFEFF) para abrir directamente en Microsoft Excel o Google Sheets con tildes y caracteres en español correctos.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Selector de Formato
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = exportFormat == ExportFormat.CSV,
                        onClick = { viewModel.setExportFormat(ExportFormat.CSV) },
                        label = { Text("CSV (Excel / Sheets)") },
                        modifier = Modifier.weight(1f)
                    )
                    FilterChip(
                        selected = exportFormat == ExportFormat.JSON,
                        onClick = { viewModel.setExportFormat(ExportFormat.JSON) },
                        label = { Text("JSON") },
                        modifier = Modifier.weight(1f)
                    )
                }

                // Vista previa de los datos
                Text(
                    text = "Vista previa del reporte (${allBooks.size} ejemplares):",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 140.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                        .padding(8.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = exportContent,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Botones de Acción de Exportación
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Button(
                        onClick = onDownloadCsv,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.FileDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Descargar Archivo CSV")
                    }

                    OutlinedButton(
                        onClick = onShareCsv,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Abrir con Excel / Google Sheets")
                    }

                    TextButton(
                        onClick = { onCopyClipboard(exportContent) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copiar datos al portapapeles")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
}

// =========================================================================================
// DIÁLOGO PARA REGISTRAR LIBRO NUEVO CON VALIDACIÓN ESTRICTA
// =========================================================================================
@Composable
fun AddBookDialog(
    onDismiss: () -> Unit,
    onConfirm: (title: String, author: String, code: String, onError: (String) -> Unit) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }

    var titleError by remember { mutableStateOf<String?>(null) }
    var authorError by remember { mutableStateOf<String?>(null) }
    var codeError by remember { mutableStateOf<String?>(null) }
    var serverError by remember { mutableStateOf<String?>(null) }

    fun validate(): Boolean {
        var isValid = true
        if (title.trim().isEmpty()) {
            titleError = "El título no puede estar vacío."
            isValid = false
        } else {
            titleError = null
        }

        if (author.trim().isEmpty()) {
            authorError = "El autor no puede estar vacío."
            isValid = false
        } else {
            authorError = null
        }

        if (code.trim().isEmpty()) {
            codeError = "El código no puede estar vacío."
            isValid = false
        } else {
            codeError = null
        }
        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AutoStories, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Nuevo Libro", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                serverError?.let { err ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFE4E6))
                            .padding(8.dp)
                    ) {
                        Text(text = err, style = MaterialTheme.typography.bodySmall, color = Color(0xFF9F1239))
                    }
                }

                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (titleError != null) titleError = null
                    },
                    label = { Text("Título del libro *") },
                    placeholder = { Text("Ej: Cien años de soledad") },
                    isError = titleError != null,
                    supportingText = { titleError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("book_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = author,
                    onValueChange = {
                        author = it
                        if (authorError != null) authorError = null
                    },
                    label = { Text("Autor *") },
                    placeholder = { Text("Ej: Gabriel García Márquez") },
                    isError = authorError != null,
                    supportingText = { authorError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("book_author_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = code,
                    onValueChange = {
                        code = it.uppercase()
                        if (codeError != null) codeError = null
                    },
                    label = { Text("Código de aula (Único) *") },
                    placeholder = { Text("Ej: LIT-01") },
                    isError = codeError != null,
                    supportingText = { codeError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth().testTag("book_code_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (validate()) {
                        onConfirm(title, author, code) { errMsg ->
                            serverError = errMsg
                        }
                    }
                },
                modifier = Modifier.testTag("save_book_button")
            ) {
                Text("Guardar Libro")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// =========================================================================================
// DIÁLOGO PARA REGISTRAR PRÉSTAMO CON VERIFICACIÓN DE DISPONIBILIDAD
// =========================================================================================
@Composable
fun LoanBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: (studentName: String, days: Int, onError: (String) -> Unit) -> Unit
) {
    var studentName by remember { mutableStateOf("") }
    var daysText by remember { mutableStateOf("7") }
    var studentError by remember { mutableStateOf<String?>(null) }
    var daysError by remember { mutableStateOf<String?>(null) }
    var generalError by remember { mutableStateOf<String?>(null) }

    val isAvailable = !book.isLoaned

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Préstamo", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Ficha del Libro
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "${book.author} • Cód: ${book.code}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (!isAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFFFE4E6))
                            .padding(10.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE11D48))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "El libro ya se encuentra prestado a ${book.loanedToStudent}. Debe ser devuelto antes de prestarse nuevamente.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF9F1239)
                            )
                        }
                    }
                }

                generalError?.let { err ->
                    Text(text = err, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                }

                OutlinedTextField(
                    value = studentName,
                    onValueChange = {
                        studentName = it
                        if (studentError != null) studentError = null
                    },
                    label = { Text("Nombre del Estudiante *") },
                    placeholder = { Text("Ej: Mateo Silva") },
                    isError = studentError != null,
                    supportingText = { studentError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = isAvailable,
                    modifier = Modifier.fillMaxWidth().testTag("student_name_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = daysText,
                    onValueChange = {
                        daysText = it
                        if (daysError != null) daysError = null
                    },
                    label = { Text("Plazo de préstamo (días) *") },
                    isError = daysError != null,
                    supportingText = { daysError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                    enabled = isAvailable,
                    modifier = Modifier.fillMaxWidth().testTag("loan_days_input"),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanStudent = studentName.trim()
                    val daysInt = daysText.toIntOrNull() ?: 0

                    var valid = true
                    if (cleanStudent.isEmpty()) {
                        studentError = "Debe ingresar el nombre del estudiante."
                        valid = false
                    }
                    if (daysInt <= 0) {
                        daysError = "El plazo debe ser de al menos 1 día."
                        valid = false
                    }

                    if (valid) {
                        onConfirm(cleanStudent, daysInt) { err ->
                            generalError = err
                        }
                    }
                },
                enabled = isAvailable,
                modifier = Modifier.testTag("confirm_loan_button")
            ) {
                Text("Confirmar Préstamo")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// =========================================================================================
// DIÁLOGO PARA REGISTRAR DEVOLUCIÓN
// =========================================================================================
@Composable
fun ReturnBookDialog(
    book: Book,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AssignmentReturn, contentDescription = null, tint = Color(0xFF059669))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Registrar Devolución", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("¿Confirmas la devolución física del siguiente ejemplar?")
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = book.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Código: ${book.code} • Responsable: ${book.loanedToStudent ?: "No registrado"}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(
                    text = "El libro volverá a estar disponible de inmediato en el catálogo de la sección.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                modifier = Modifier.testTag("confirm_return_button")
            ) {
                Text("Confirmar Devolución")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

// =========================================================================================
// COMPONENTES AUXILIARES DEL DASHBOARD Y NOTIFICACIONES
// =========================================================================================
@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = valueColor
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PopularBookMetricCard(
    popularBook: Book?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFEEF2FF).copy(alpha = 0.7f))
            .border(1.dp, Color(0xFFC7D2FE), RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Más Popular",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF3730A3)
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFD97706),
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = popularBook?.title ?: "Sin registros",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF1E1B4B),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = if (popularBook != null && popularBook.loanCount > 0)
                    "${popularBook.loanCount} préstamos • Cód: ${popularBook.code}"
                else
                    "Comienza a prestar libros",
                style = MaterialTheme.typography.labelSmall,
                color = Color(0xFF4338CA),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun NotificationBanner(
    alert: BannerAlert,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (bgColor, contentColor, icon) = when (alert.type) {
        BannerType.SUCCESS -> Triple(Color(0xFFECFDF5), Color(0xFF065F46), Icons.Default.CheckCircle)
        BannerType.INFO -> Triple(Color(0xFFEFF6FF), Color(0xFF1E40AF), Icons.Default.Info)
        BannerType.WARNING -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), Icons.Default.Warning)
        BannerType.ERROR -> Triple(Color(0xFFFFE4E6), Color(0xFF9F1239), Icons.Default.ErrorOutline)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(1.dp, contentColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = alert.message,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    color = contentColor
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar alerta",
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    description: String,
    primaryActionLabel: String?,
    onPrimaryAction: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.widthIn(max = 380.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(38.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (primaryActionLabel != null && onPrimaryAction != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onPrimaryAction,
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text(primaryActionLabel)
                }
            }
        }
    }
}
