package com.seeker.datastores

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
val PASSWORD_PREFERENCE_KEY: Preferences.Key<String> = stringPreferencesKey("password")
val USERNAME_PREFERENCE_KEY: Preferences.Key<String> = stringPreferencesKey("username")

private suspend fun doStorePreference(context: Context, password: String, preferencesKey: Preferences.Key<String>) {
    context.dataStore.edit { settings -> settings[preferencesKey] = password }
}

fun storePreference(context: Context, password: String, preferencesKey: Preferences.Key<String>) {
    runBlocking{ doStorePreference(context, password, preferencesKey) }
}

private suspend fun doReadPreference(context: Context, preferencesKey: Preferences.Key<String>): String {
    val preferences = context.dataStore.data.first()
    return preferences[preferencesKey].orEmpty()
}

fun readPreference(context: Context, preferencesKey: Preferences.Key<String>): String {
    return runBlocking { doReadPreference(context, preferencesKey) }
}