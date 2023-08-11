package net.ambitious.android.httprequesttile.httpexecute

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.ambitious.android.httprequesttile.data.AppDataStore
import net.ambitious.android.httprequesttile.data.RequestParams
import net.ambitious.android.httprequesttile.data.RequestParams.Companion.parseRequestParam
import net.ambitious.android.httprequesttile.data.RequestParams.Companion.parseRequestParams
import net.ambitious.android.httprequesttile.data.ResponseParams
import net.ambitious.android.httprequesttile.data.ResponseParams.Companion.parseResponseParams
import net.ambitious.android.httprequesttile.request.HttpRequester
import org.json.JSONArray
import java.util.Date

class HttpExecuteViewModel(application: Application) : AndroidViewModel(application) {
  private val dataStore = AppDataStore.getDataStore(application)
  private val requester = HttpRequester()

  private val _savedResponse = MutableStateFlow("")

  private val _isSent = MutableStateFlow(false)
  val isSent = _isSent.asStateFlow()

  init {
    viewModelScope.launch(Dispatchers.IO) {
      dataStore.getSavedResponse.collect {
        _savedResponse.value = it
      }
    }
  }

  fun sendRequest(param: String?) {
    if (param.isNullOrEmpty()) {
      _isSent.value = true
      return
    }

    val request = param.parseRequestParam()
    val start = System.currentTimeMillis()
    viewModelScope.launch(Dispatchers.IO) {
      val response = try {
        requester.execute(request, false)
      } catch (e: Exception) {
        ResponseParams(
          request.title,
          -1,
          System.currentTimeMillis() - start,
          "",
          "通信エラーが発生しました。\n${e.message}",
          Date().time,
          false
        )
      }
      if (_savedResponse.value.isBlank()) {
        mutableListOf()
      } else {
        _savedResponse.value.parseResponseParams().toMutableList()
      }
        .apply { add(response) }
        .sortedByDescending { it.sendDateTime }
        .map { it.toJsonString() }
        .let { JSONArray(it).toString() }
        .let { dataStore.saveResponse(it) }
      _isSent.value = true
    }
  }
}