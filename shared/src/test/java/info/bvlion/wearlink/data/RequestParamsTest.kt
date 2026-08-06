package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.needsRequestIdMigration
import info.bvlion.wearlink.data.RequestParams.Companion.normalizeRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RequestParamsTest {
  private val watchSyncRequest = RequestParams(
    title = "watch sync request",
    url = "https://example.com/sync",
    method = Constant.HttpMethod.GET,
    bodyType = Constant.BodyType.QUERY,
    watchSync = true
  )
  private val watchfaceShortcutRequest = RequestParams(
    title = "watchface shortcut request",
    url = "https://example.com/shortcut",
    method = Constant.HttpMethod.POST,
    bodyType = Constant.BodyType.JSON,
    watchfaceShortcut = true
  )

  private fun legacyJsonWithoutId(title: String) = JSONObject().apply {
    put("title", title)
    put("url", "https://example.com/legacy")
    put("method", Constant.HttpMethod.GET.name)
    put("bodyType", Constant.BodyType.QUERY.name)
    put("headers", "")
    put("parameters", "")
    put("watchSync", false)
    put("watchfaceShortcut", false)
  }.toString()

  @Test
  fun toRequestParamsJsonEmptyListTest() {
    assertEquals("[]", emptyList<RequestParams>().toRequestParamsJson())
  }

  @Test
  fun parseRequestParamsEmptyJsonArrayTest() {
    assertTrue("[]".parseRequestParams().isEmpty())
  }

  @Test
  fun toRequestParamsJsonRoundTripTest() {
    val requests = listOf(watchSyncRequest, watchfaceShortcutRequest)

    val actual = requests.toRequestParamsJson().parseRequestParams()

    assertEquals(requests, actual)
  }

  @Test
  fun normalizeRequestParamsJsonBlankTest() {
    assertEquals("[]", "".normalizeRequestParamsJson())
    assertEquals("[]", "   ".normalizeRequestParamsJson())
  }

  @Test
  fun normalizeRequestParamsJsonBlankParsesWithoutExceptionTest() {
    assertTrue("".normalizeRequestParamsJson().parseRequestParams().isEmpty())
  }

  @Test
  fun normalizeRequestParamsJsonNonBlankUnchangedTest() {
    val json = listOf(watchSyncRequest).toRequestParamsJson()

    assertEquals(json, json.normalizeRequestParamsJson())
  }

  @Test
  fun toJsonStringRoundTripPreservesIdTest() {
    val parsed = watchSyncRequest.toJsonString().parseRequestParam()

    assertEquals(watchSyncRequest.id, parsed.id)
    assertEquals(watchSyncRequest, parsed)
  }

  @Test
  fun parseRequestParamWithoutIdAssignsNonBlankIdTest() {
    val parsed = legacyJsonWithoutId("legacy request").parseRequestParam()

    assertTrue(parsed.id.isNotBlank())
  }

  @Test
  fun parseRequestParamsWithoutIdAssignsUniqueIdsTest() {
    val legacyArray = JSONArray(
      listOf(legacyJsonWithoutId("legacy 1"), legacyJsonWithoutId("legacy 2"))
    ).toString()

    val parsed = legacyArray.parseRequestParams()

    assertEquals(2, parsed.size)
    assertNotEquals(parsed[0].id, parsed[1].id)
  }

  @Test
  fun copyPreservesIdTest() {
    val edited = watchSyncRequest.copy(title = "renamed")

    assertEquals(watchSyncRequest.id, edited.id)
    assertNotEquals(watchSyncRequest.title, edited.title)
  }

  @Test
  fun needsRequestIdMigrationTrueWhenIdMissingTest() {
    val legacyArray = JSONArray(listOf(legacyJsonWithoutId("legacy request"))).toString()

    assertTrue(legacyArray.needsRequestIdMigration())
  }

  @Test
  fun needsRequestIdMigrationFalseWhenIdPresentTest() {
    val migratedArray = listOf(watchSyncRequest, watchfaceShortcutRequest).toRequestParamsJson()

    assertFalse(migratedArray.needsRequestIdMigration())
  }

  @Test
  fun needsRequestIdMigrationFalseForEmptyArrayTest() {
    assertFalse("[]".needsRequestIdMigration())
    assertFalse("".needsRequestIdMigration())
  }

  @Test
  fun findByIdReturnsMatchingRequestTest() {
    val requests = listOf(watchSyncRequest, watchfaceShortcutRequest)

    assertEquals(watchfaceShortcutRequest, requests.findById(watchfaceShortcutRequest.id))
  }

  @Test
  fun findByIdSurvivesInsertAtFrontAndRemovalOfOtherRequestTest() {
    val newRequest = RequestParams(
      title = "new request",
      url = "https://example.com/new",
      method = Constant.HttpMethod.GET,
      bodyType = Constant.BodyType.QUERY,
    )
    val requests = listOf(newRequest, watchSyncRequest, watchfaceShortcutRequest)
      .filterNot { it.id == watchSyncRequest.id }

    assertEquals(watchfaceShortcutRequest, requests.findById(watchfaceShortcutRequest.id))
    assertNull(requests.findById(watchSyncRequest.id))
  }

  @Test
  fun findByIdReturnsNullForUnknownIdTest() {
    assertNull(listOf(watchSyncRequest).findById("unknown-id"))
  }
}
