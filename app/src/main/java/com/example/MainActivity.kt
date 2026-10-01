package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AppDatabase
import com.example.data.BookRepository
import com.example.ui.LibraryScreen
import com.example.ui.LibraryViewModel
import com.example.ui.theme.MyApplicationTheme

/**
 * Actividad Principal de la aplicación "Biblioteca de Aula" - Versión Producción y Megaproyecto.
 *
 * Características principales integradas:
 * 1. Soporte de Modo Oscuro / Modo Claro reactivo y persistente mediante SharedPreferences.
 * 2. Inyección del repositorio Room SQLite y ViewModel a través de Factory con Contexto de aplicación.
 * 3. Habilitación de Edge-to-Edge para un diseño inmersivo y mobile-first moderno.
 */
class MainActivity : ComponentActivity() {

    private val viewModel: LibraryViewModel by viewModels {
        val database = AppDatabase.getDatabase(applicationContext)
        val repository = BookRepository(database.bookDao())
        LibraryViewModel.Factory(repository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                LibraryScreen(viewModel = viewModel)
            }
        }
    }
}
