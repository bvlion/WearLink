package info.bvlion.wearlink.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
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

  val getSavedResponse: Flow<String> = settingsDataStore.data.map { pref ->
    pref[SAVED_RESPONSE_KEY] ?: ""
  }.distinctUntilChanged()

  suspend fun saveResponse(response: String) = settingsDataStore.edit {
    it[SAVED_RESPONSE_KEY] = response
  }

  /**
   * 保存済みResponse履歴へ1件追加し、送信日時の降順でソートして保存する。
   * 読み取りから書き込みまでを`edit`の1トランザクション内で完結させることで、
   * 複数経路からほぼ同時に呼び出された場合でも互いの追加を失わないようにする。
   */
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