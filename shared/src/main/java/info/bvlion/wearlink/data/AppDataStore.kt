package info.bvlion.wearlink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import info.bvlion.wearlink.data.RequestParams.Companion.isWatchSyncChangeAllowed
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.reorderByIds
import info.bvlion.wearlink.data.RequestParams.Companion.removeById
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.upsertById
import info.bvlion.wearlink.data.ResponseParams.Companion.parseResponseParams
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.json.JSONArray

class AppDataStore(context: Context) {
  private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("settings")
  private val settingsDataStore = context.dataStore

  val getSavedRequest: Flow<String?> = settingsDataStore.data.map { pref ->
    pref[SAVED_REQUEST_KEY]
  }.distinctUntilChanged()

  suspend fun saveRequest(request: String) = settingsDataStore.edit {
    it[SAVED_REQUEST_KEY] = request
  }

  suspend fun reorderRequests(ids: List<String>) = settingsDataStore.edit { pref ->
    pref[SAVED_REQUEST_KEY]?.let { current ->
      pref[SAVED_REQUEST_KEY] = current.parseRequestParams().reorderByIds(ids).toRequestParamsJson()
    }
  }

  suspend fun upsertRequest(
    request: RequestParams,
    shouldToggleWatchSync: Boolean = false,
    shouldToggleWatchfaceShortcut: Boolean = false,
  ): Boolean {
    var accepted = true
    settingsDataStore.edit { pref ->
      val current = pref[SAVED_REQUEST_KEY]?.parseRequestParams() ?: emptyList()
      val currentRequest = current.find { it.id == request.id }
      val requestToSave = if (currentRequest != null) {
        request.copy(
          watchSync = if (shouldToggleWatchSync) !currentRequest.watchSync else currentRequest.watchSync,
          watchfaceShortcut = if (shouldToggleWatchfaceShortcut) {
            !currentRequest.watchfaceShortcut
          } else {
            currentRequest.watchfaceShortcut
          },
        )
      } else {
        request
      }
      if (!current.isWatchSyncChangeAllowed(requestToSave, Constant.MAX_SYNC_COUNT)) {
        accepted = false
        return@edit
      }
      pref[SAVED_REQUEST_KEY] = current.upsertById(requestToSave).toRequestParamsJson()
    }
    return accepted
  }

  suspend fun deleteRequestById(id: String) = settingsDataStore.edit { pref ->
    pref[SAVED_REQUEST_KEY]?.let { current ->
      pref[SAVED_REQUEST_KEY] = current.parseRequestParams().removeById(id).toRequestParamsJson()
    }
  }

  val getSavedResponse: Flow<String> = settingsDataStore.data.map { pref ->
    pref[SAVED_RESPONSE_KEY] ?: ""
  }.distinctUntilChanged()

  suspend fun saveResponse(response: String) = settingsDataStore.edit {
    it[SAVED_RESPONSE_KEY] = response
  }

  suspend fun appendResponse(response: ResponseParams) = settingsDataStore.edit { pref ->
    val current = pref[SAVED_RESPONSE_KEY] ?: ""
    val savedList = if (current.isBlank()) {
      mutableListOf()
    } else {
      current.parseResponseParams().toMutableList()
    }
    pref[SAVED_RESPONSE_KEY] = savedList
      .apply { add(response) }
      .sortedByDescending { it.sendDateTime }
      .map { it.toJsonString() }
      .let { JSONArray(it).toString() }
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
