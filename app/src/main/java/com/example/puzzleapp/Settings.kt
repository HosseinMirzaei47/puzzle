package com.example.puzzleapp

import android.content.Context
import androidx.datastore.DataStore
import androidx.datastore.preferences.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

class Settings(context: Context) {

    private val dataStore: DataStore<Preferences> = context.createDataStore(name = DATA_STORE_NAME)

    suspend fun storePuzzleSrcType(srcType: String) = dataStore.edit { preferences ->
        preferences[KEY_TYPE] = srcType
    }

    suspend fun storePuzzleSrcPath(path: String) = dataStore.edit { preferences ->
        preferences[KEY_PATH] = path
    }

    suspend fun storePuzzleSrcDrawable(drawable: Int) = dataStore.edit { preferences ->
        preferences[KEY_DRAWABLE] = drawable
    }

    val puzzleType: Flow<String> = dataStore.data.catch {
        emit(emptyPreferences())
    }.map { preferences ->
        preferences[KEY_TYPE] ?: ""
    }

    val puzzleSrcPath: Flow<String> = dataStore.data.catch {
        emit(emptyPreferences())
    }.map { preferences ->
        preferences[KEY_PATH] ?: ""
    }

    val puzzleSrcDrawable: Flow<Int> = dataStore.data.catch {
        emit(emptyPreferences())
    }.map { preferences ->
        preferences[KEY_DRAWABLE] ?: -1
    }

    companion object Key {
        const val DATA_STORE_NAME = "settings"
        const val TYPE_CUSTOM = "CUSTOM_PUZZLE"
        const val TYPE_DEFAULT = "DEFAULT_PUZZLE"

        val KEY_TYPE = preferencesKey<String>("TOKEN_KEY")
        val KEY_DRAWABLE = preferencesKey<Int>("DRAWABLE_KEY")
        val KEY_PATH = preferencesKey<String>("PATH_KEY")
    }

}