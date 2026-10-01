package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Base de datos local SQLite utilizando Room.
 *
 * PUNTOS DONDE ALGUIEN PUEDE EQUIVOCARSE:
 * 1. Instanciar la base de datos sin un Singleton protegido por '@Volatile' y 'synchronized':
 *    Si dos hilos inicializan la base de datos simultáneamente, se generan conexiones duplicadas
 *    y errores de "SQLiteDatabaseLockedException".
 * 2. Usar 'context' en lugar de 'context.applicationContext': Pasar el contexto de la Activity
 *    provoca fugas de memoria (Memory Leaks) cuando el teléfono rota.
 */
@Database(
    entities = [Book::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun bookDao(): BookDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "biblioteca_aula.db"
                )
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
