package net.ambitious.android.httprequesttile.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class AppDataStore(context: Context) {
  private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
  private val settingsDataStore = context.dataStore

  val getSavedRequest: Flow<String?> = settingsDataStore.data.map { pref ->
    pref[SAVED_REQUEST_KEY]
  }.distinctUntilChanged()

  suspend fun saveRequest(request: String) = settingsDataStore.edit {
    it[SAVED_REQUEST_KEY] = request
  }

  val getSavedResponse: Flow<String> = settingsDataStore.data.map { pref ->
    pref[SAVED_RESPONSE_KEY] ?: ""
  }.distinctUntilChanged()

  suspend fun saveResponse(response: String) = settingsDataStore.edit {
    it[SAVED_RESPONSE_KEY] = response
  }

  val getViewType: Flow<Int> = settingsDataStore.data.map { it[VIEW_TYPE_KEY] ?: 0 }

  suspend fun setViewType(type: Int) = settingsDataStore.edit {
    it[VIEW_TYPE_KEY] = type
  }

  companion object {
    private val SAVED_REQUEST_KEY = stringPreferencesKey("saved_request")
    private val SAVED_RESPONSE_KEY = stringPreferencesKey("saved_response")
    private val VIEW_TYPE_KEY = intPreferencesKey("view_type")

    private var dataStore: AppDataStore? = null
    fun getDataStore(context: Context) = dataStore ?: AppDataStore(context).also { dataStore = it }
  }
}