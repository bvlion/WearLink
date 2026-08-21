package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.RequestParams.Companion.deduplicateIds
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.moveById
import info.bvlion.wearlink.data.RequestParams.Companion.needsRequestIdMigration
import info.bvlion.wearlink.data.RequestParams.Companion.normalizeRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.removeById
import info.bvlion.wearlink.data.RequestParams.Companion.toRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.upsertById
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

  private fun legacyJsonWithId(title: String, id: String) = JSONObject().apply {
    put("title", title)
    put("url", "https://example.com/legacy")
    put("method", Constant.HttpMethod.GET.name)
    put("bodyType", Constant.BodyType.QUERY.name)
    put("headers", "")
    put("parameters", "")
    put("watchSync", false)
    put("watchfaceShortcut", false)
    put("id", id)
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
  fun needsRequestIdMigrationTrueWhenIdBlankTest() {
    val blankIdArray = JSONArray(listOf(legacyJsonWithId("blank id request", "   "))).toString()
    val emptyIdArray = JSONArray(listOf(legacyJsonWithId("empty id request", ""))).toString()

    assertTrue(blankIdArray.needsRequestIdMigration())
    assertTrue(emptyIdArray.needsRequestIdMigration())
  }

  @Test
  fun needsRequestIdMigrationTrueWhenIdDuplicatedTest() {
    val duplicatedArray = JSONArray(
      listOf(
        legacyJsonWithId("first", "duplicate-id"),
        legacyJsonWithId("second", "duplicate-id")
      )
    ).toString()

    assertTrue(duplicatedArray.needsRequestIdMigration())
  }

  @Test
  fun parseRequestParamWithBlankIdAssignsNonBlankIdTest() {
    val parsed = legacyJsonWithId("blank id request", "   ").parseRequestParam()

    assertTrue(parsed.id.isNotBlank())
  }

  @Test
  fun deduplicateIdsProducesUniqueNonBlankIdsTest() {
    val duplicatedArray = JSONArray(
      listOf(
        legacyJsonWithId("first", "duplicate-id"),
        legacyJsonWithId("second", "duplicate-id"),
        legacyJsonWithId("third", "duplicate-id")
      )
    ).toString()

    val normalized = duplicatedArray.parseRequestParams().deduplicateIds()

    assertTrue(normalized.all { it.id.isNotBlank() })
    assertEquals(normalized.size, normalized.map { it.id }.toSet().size)
  }

  @Test
  fun deduplicateIdsPreservesExistingValidUniqueIdsTest() {
    val requests = listOf(watchSyncRequest, watchfaceShortcutRequest)

    val normalized = requests.deduplicateIds()

    assertEquals(requests, normalized)
  }

  @Test
  fun deduplicateIdsKeepsFirstOccurrenceOfDuplicateIdTest() {
    val duplicatedArray = JSONArray(
      listOf(
        legacyJsonWithId("first", "duplicate-id"),
        legacyJsonWithId("second", "duplicate-id")
      )
    ).toString()

    val normalized = duplicatedArray.parseRequestParams().deduplicateIds()

    assertEquals("duplicate-id", normalized[0].id)
    assertNotEquals("duplicate-id", normalized[1].id)
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

  private fun reorderRequest(title: String, watchSync: Boolean = false) = RequestParams(
    title = title,
    url = "https://example.com/${title}",
    method = Constant.HttpMethod.GET,
    bodyType = Constant.BodyType.QUERY,
    watchSync = watchSync,
  )

  @Test
  fun moveByIdMovesMiddleItemUpTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(second.id, -1)

    assertEquals(listOf(second, first, third), moved)
  }

  @Test
  fun moveByIdMovesMiddleItemDownTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(second.id, 1)

    assertEquals(listOf(first, third, second), moved)
  }

  @Test
  fun moveByIdMovesFirstItemDownToMiddleTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(first.id, 1)

    assertEquals(listOf(second, first, third), moved)
  }

  @Test
  fun moveByIdMovesLastItemUpToMiddleTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(third.id, -1)

    assertEquals(listOf(first, third, second), moved)
  }

  @Test
  fun moveByIdOnFirstItemUpIsNoOpTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val requests = listOf(first, second)

    val moved = requests.moveById(first.id, -1)

    assertEquals(requests, moved)
  }

  @Test
  fun moveByIdOnLastItemDownIsNoOpTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val requests = listOf(first, second)

    val moved = requests.moveById(second.id, 1)

    assertEquals(requests, moved)
  }

  @Test
  fun moveByIdWithUnknownIdIsNoOpTest() {
    val requests = listOf(reorderRequest("first"), reorderRequest("second"))

    val moved = requests.moveById("unknown-id", 1)

    assertEquals(requests, moved)
  }

  @Test
  fun moveByIdPreservesIdsAndContentTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(third.id, -1)

    assertEquals(requests.map { it.id }.toSet(), moved.map { it.id }.toSet())
    moved.forEach { movedRequest ->
      assertEquals(requests.findById(movedRequest.id), movedRequest)
    }
  }

  @Test
  fun moveByIdPreservesRelativeOrderOfWatchSyncFilteredSubsetTest() {
    val syncedFirst = reorderRequest("synced-first", watchSync = true)
    val unsynced = reorderRequest("unsynced")
    val syncedSecond = reorderRequest("synced-second", watchSync = true)
    val requests = listOf(syncedFirst, unsynced, syncedSecond)

    val moved = requests.moveById(unsynced.id, -1)

    assertEquals(listOf(syncedFirst, syncedSecond), moved.filter { it.watchSync })
  }

  @Test
  fun moveByIdChangesRelativeOrderOfWatchSyncTargetsWhenSyncedItemsAreSwappedTest() {
    val syncedFirst = reorderRequest("synced-first", watchSync = true)
    val syncedSecond = reorderRequest("synced-second", watchSync = true)
    val unsynced = reorderRequest("unsynced")
    val requests = listOf(syncedFirst, syncedSecond, unsynced)

    val moved = requests.moveById(syncedSecond.id, -1)

    // Mirrors Sync.kt's `filter { it.watchSync || it.watchfaceShortcut }` extraction of Wear OS sync targets.
    assertEquals(
      listOf(syncedSecond, syncedFirst),
      moved.filter { it.watchSync || it.watchfaceShortcut }
    )
  }

  @Test
  fun moveByIdOrderSurvivesJsonRoundTripTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val moved = requests.moveById(third.id, -1)
    val reloaded = moved.toRequestParamsJson().parseRequestParams()

    assertEquals(moved, reloaded)
  }

  @Test
  fun upsertByIdReplacesMatchingRequestInPlaceTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val edited = second.copy(title = "second-edited")

    val updated = listOf(first, second, third).upsertById(edited)

    assertEquals(listOf(first, edited, third), updated)
  }

  @Test
  fun upsertByIdPrependsWhenIdNotFoundTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val newRequest = reorderRequest("new")

    val updated = listOf(first, second).upsertById(newRequest)

    assertEquals(listOf(newRequest, first, second), updated)
  }

  @Test
  fun upsertByIdClearsWatchfaceShortcutOnOtherRequestsTest() {
    val first = reorderRequest("first").copy(watchfaceShortcut = true)
    val second = reorderRequest("second")
    val editedSecond = second.copy(watchfaceShortcut = true, title = "second-shortcut")

    val updated = listOf(first, second).upsertById(editedSecond)

    assertEquals(listOf(first.copy(watchfaceShortcut = false), editedSecond), updated)
  }

  @Test
  fun upsertByIdWithoutWatchfaceShortcutLeavesOtherShortcutFlagsUntouchedTest() {
    val first = reorderRequest("first").copy(watchfaceShortcut = true)
    val second = reorderRequest("second")
    val editedSecond = second.copy(title = "second-edited")

    val updated = listOf(first, second).upsertById(editedSecond)

    assertEquals(listOf(first, editedSecond), updated)
  }

  @Test
  fun removeByIdRemovesOnlyMatchingRequestTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")

    val updated = listOf(first, second, third).removeById(second.id)

    assertEquals(listOf(first, third), updated)
  }

  @Test
  fun removeByIdWithUnknownIdIsNoOpTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")

    val updated = listOf(first, second).removeById("unknown-id")

    assertEquals(listOf(first, second), updated)
  }

  // Regression test for a race where a stale UI-held index/list, captured before a reorder,
  // could be written back to DataStore and undo the reorder or edit the wrong Request.
  // upsertById/removeById must locate the target purely by ID against the *current* list
  // (e.g. the one already reordered by moveById), never by a stale positional index.
  @Test
  fun upsertByIdAfterMoveByIdUpdatesCorrectRequestAndKeepsReorderedPositionTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val reordered = listOf(first, second, third).moveById(first.id, 1) // -> [second, first, third]

    val updated = reordered.upsertById(first.copy(watchSync = true))

    assertEquals(listOf(second, first.copy(watchSync = true), third), updated)
  }

  @Test
  fun removeByIdAfterMoveByIdDeletesCorrectRequestAndKeepsReorderedOrderTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val reordered = listOf(first, second, third).moveById(first.id, 1) // -> [second, first, third]

    val updated = reordered.removeById(second.id)

    assertEquals(listOf(first, third), updated)
  }
}
