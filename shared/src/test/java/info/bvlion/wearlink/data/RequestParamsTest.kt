package info.bvlion.wearlink.data

import info.bvlion.wearlink.data.RequestParams.Companion.deduplicateIds
import info.bvlion.wearlink.data.RequestParams.Companion.filterByIds
import info.bvlion.wearlink.data.RequestParams.Companion.findById
import info.bvlion.wearlink.data.RequestParams.Companion.isWatchSyncChangeAllowed
import info.bvlion.wearlink.data.RequestParams.Companion.mergeAdditiveImport
import info.bvlion.wearlink.data.RequestParams.Companion.moveById
import info.bvlion.wearlink.data.RequestParams.Companion.needsRequestIdMigration
import info.bvlion.wearlink.data.RequestParams.Companion.normalizeRequestParamsJson
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParam
import info.bvlion.wearlink.data.RequestParams.Companion.parseRequestParams
import info.bvlion.wearlink.data.RequestParams.Companion.removeById
import info.bvlion.wearlink.data.RequestParams.Companion.reorderByIds
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

    // Wear OS同期対象の抽出順も並び替え後の順序になることを確認する
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
  fun reorderByIdsUsesLatestContentAndKeepsAddedRequestsTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val added = reorderRequest("added")
    val updatedSecond = second.copy(title = "updated-second")

    val reordered = listOf(first, updatedSecond, added)
      .reorderByIds(listOf(second.id, first.id, third.id))

    assertEquals(listOf(updatedSecond, first, added), reordered)
  }

  @Test
  fun reorderByIdsWithSameOrderKeepsRequestsUnchangedTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val requests = listOf(first, second)

    assertEquals(requests, requests.reorderByIds(requests.map { it.id }))
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
  fun upsertByIdTurnsTileDisplayOnTest() {
    val requestParams = reorderRequest("tile-target")
    val turnedOn = requestParams.copy(watchSync = true)

    val updated = listOf(requestParams).upsertById(turnedOn)

    assertEquals(true, updated.single().watchSync)
  }

  @Test
  fun upsertByIdTurnsTileDisplayOffTest() {
    val requestParams = reorderRequest("tile-target", watchSync = true)
    val turnedOff = requestParams.copy(watchSync = false)

    val updated = listOf(requestParams).upsertById(turnedOff)

    assertEquals(false, updated.single().watchSync)
  }

  @Test
  fun upsertByIdTurnsWatchfaceDisplayOffWithoutAffectingOtherRequestsTest() {
    val target = reorderRequest("watchface-target").copy(watchfaceShortcut = true)
    val other = reorderRequest("other")
    val turnedOff = target.copy(watchfaceShortcut = false)

    val updated = listOf(target, other).upsertById(turnedOff)

    assertEquals(listOf(turnedOff, other), updated)
  }

  @Test
  fun upsertByIdPreservesTileAndWatchfaceStateOnPlainContentEditTest() {
    val requestParams = reorderRequest("edited").copy(watchSync = true, watchfaceShortcut = true)
    val contentOnlyEdit = requestParams.copy(title = "renamed", url = "https://example.com/renamed")

    val updated = listOf(requestParams).upsertById(contentOnlyEdit)

    assertEquals(true, updated.single().watchSync)
    assertEquals(true, updated.single().watchfaceShortcut)
    assertEquals("renamed", updated.single().title)
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

  // 並び替え前の古いindexや一覧ではなく、現在の一覧からIDで対象を特定する
  @Test
  fun upsertByIdAfterMoveByIdUpdatesCorrectRequestAndKeepsReorderedPositionTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val reordered = listOf(first, second, third).moveById(first.id, 1)

    val updated = reordered.upsertById(first.copy(watchSync = true))

    assertEquals(listOf(second, first.copy(watchSync = true), third), updated)
  }

  @Test
  fun removeByIdAfterMoveByIdDeletesCorrectRequestAndKeepsReorderedOrderTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val reordered = listOf(first, second, third).moveById(first.id, 1)

    val updated = reordered.removeById(second.id)

    assertEquals(listOf(first, third), updated)
  }

  @Test
  fun isWatchSyncChangeAllowedTrueWhenTurningOnBelowLimitTest() {
    val synced = reorderRequest("synced", watchSync = true)
    val target = reorderRequest("target")
    val requests = listOf(synced, target)

    assertTrue(requests.isWatchSyncChangeAllowed(target.copy(watchSync = true), maxSyncCount = 2))
  }

  @Test
  fun isWatchSyncChangeAllowedFalseWhenTurningOnDifferentRequestAtLimitTest() {
    val syncedA = reorderRequest("synced-a", watchSync = true)
    val syncedB = reorderRequest("synced-b", watchSync = true)
    val target = reorderRequest("target")
    val requests = listOf(syncedA, syncedB, target)

    assertFalse(requests.isWatchSyncChangeAllowed(target.copy(watchSync = true), maxSyncCount = 2))
  }

  @Test
  fun isWatchSyncChangeAllowedTrueForPlainEditOfAlreadySyncedRequestAtLimitTest() {
    val syncedA = reorderRequest("synced-a", watchSync = true)
    val syncedB = reorderRequest("synced-b", watchSync = true)
    val requests = listOf(syncedA, syncedB)
    val editedA = syncedA.copy(title = "synced-a-renamed")

    assertTrue(requests.isWatchSyncChangeAllowed(editedA, maxSyncCount = 2))
  }

  @Test
  fun isWatchSyncChangeAllowedTrueWhenTurningOffAtLimitTest() {
    val syncedA = reorderRequest("synced-a", watchSync = true)
    val syncedB = reorderRequest("synced-b", watchSync = true)
    val requests = listOf(syncedA, syncedB)

    assertTrue(requests.isWatchSyncChangeAllowed(syncedA.copy(watchSync = false), maxSyncCount = 2))
  }

  // DataStore transactionごとに最新一覧で上限を判定する必要がある
  @Test
  fun isWatchSyncChangeAllowedSequentialTogglesNeverExceedLimitTest() {
    val maxSyncCount = 2
    val syncedA = reorderRequest("synced-a", watchSync = true)
    val unsyncedB = reorderRequest("unsynced-b")
    val unsyncedC = reorderRequest("unsynced-c")
    val requests = listOf(syncedA, unsyncedB, unsyncedC)

    val turnOnB = unsyncedB.copy(watchSync = true)
    assertTrue(requests.isWatchSyncChangeAllowed(turnOnB, maxSyncCount))
    val afterB = requests.upsertById(turnOnB)

    val turnOnC = unsyncedC.copy(watchSync = true)
    assertFalse(afterB.isWatchSyncChangeAllowed(turnOnC, maxSyncCount))

    assertEquals(maxSyncCount, afterB.count { it.watchSync })
  }

  @Test
  fun filterByIdsReturnsOnlyMatchingRequestsTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    val filtered = requests.filterByIds(setOf(first.id, third.id))

    assertEquals(listOf(first, third), filtered)
  }

  @Test
  fun filterByIdsPreservesSavedListOrderRegardlessOfSetOrderTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val third = reorderRequest("third")
    val requests = listOf(first, second, third)

    // Setの列挙順に依存せず、保存済み一覧の順序で返ることを確認する
    val filtered = requests.filterByIds(setOf(third.id, first.id))

    assertEquals(listOf(first, third), filtered)
  }

  @Test
  fun filterByIdsWithEmptyIdsReturnsEmptyListTest() {
    val requests = listOf(reorderRequest("first"), reorderRequest("second"))

    assertTrue(requests.filterByIds(emptySet()).isEmpty())
  }

  @Test
  fun filterByIdsWithAllIdsReturnsAllInOriginalOrderTest() {
    val first = reorderRequest("first")
    val second = reorderRequest("second")
    val requests = listOf(first, second)

    assertEquals(requests, requests.filterByIds(requests.map { it.id }.toSet()))
  }

  @Test
  fun mergeAdditiveImportKeepsExistingRequestsTest() {
    val existing = listOf(reorderRequest("existing-first"), reorderRequest("existing-second"))
    val imported = listOf(reorderRequest("imported"))

    val merged = existing.mergeAdditiveImport(imported)

    assertTrue(merged.containsAll(existing))
  }

  @Test
  fun mergeAdditiveImportOrdersImportedBeforeExistingInJsonOrderTest() {
    val existingFirst = reorderRequest("existing-first")
    val existingSecond = reorderRequest("existing-second")
    val importedFirst = reorderRequest("imported-first")
    val importedSecond = reorderRequest("imported-second")

    val merged = listOf(existingFirst, existingSecond)
      .mergeAdditiveImport(listOf(importedFirst, importedSecond))

    assertEquals(
      listOf(importedFirst.id, importedSecond.id, existingFirst.id, existingSecond.id),
      merged.map { it.id }
    )
  }

  @Test
  fun mergeAdditiveImportAssignsNewIdOnCollisionWithExistingTest() {
    val existing = reorderRequest("existing")
    val colliding = reorderRequest("imported").copy(id = existing.id)

    val merged = listOf(existing).mergeAdditiveImport(listOf(colliding))

    assertEquals(2, merged.size)
    assertEquals(existing, merged.single { it.title == "existing" })
    assertNotEquals(existing.id, merged.single { it.title == "imported" }.id)
  }

  @Test
  fun mergeAdditiveImportDeduplicatesIdsWithinImportBatchTest() {
    val duplicateId = "duplicate-id"
    val importedFirst = reorderRequest("imported-first").copy(id = duplicateId)
    val importedSecond = reorderRequest("imported-second").copy(id = duplicateId)

    val merged = emptyList<RequestParams>().mergeAdditiveImport(listOf(importedFirst, importedSecond))

    assertEquals(2, merged.map { it.id }.toSet().size)
  }

  @Test
  fun mergeAdditiveImportKeepsNonCollidingIdsTest() {
    val existing = reorderRequest("existing")
    val imported = reorderRequest("imported")

    val merged = listOf(existing).mergeAdditiveImport(listOf(imported))

    assertEquals(imported.id, merged.single { it.title == "imported" }.id)
  }

  @Test
  fun mergeAdditiveImportSetsWatchSyncAndWatchfaceShortcutFalseForImportedTest() {
    val imported = reorderRequest("imported", watchSync = true).copy(watchfaceShortcut = true)

    val merged = emptyList<RequestParams>().mergeAdditiveImport(listOf(imported))

    assertFalse(merged.single().watchSync)
    assertFalse(merged.single().watchfaceShortcut)
  }

  @Test
  fun mergeAdditiveImportDoesNotChangeExistingWatchSyncOrWatchfaceShortcutTest() {
    val existingSynced = reorderRequest("existing-synced", watchSync = true)
    val existingShortcut = reorderRequest("existing-shortcut").copy(watchfaceShortcut = true)
    val imported = reorderRequest("imported")

    val merged = listOf(existingSynced, existingShortcut).mergeAdditiveImport(listOf(imported))

    assertEquals(existingSynced, merged.single { it.title == "existing-synced" })
    assertEquals(existingShortcut, merged.single { it.title == "existing-shortcut" })
  }

  @Test
  fun mergeAdditiveImportWithEmptyImportedReturnsExistingUnchangedTest() {
    val existing = listOf(reorderRequest("existing-first"), reorderRequest("existing-second"))

    val merged = existing.mergeAdditiveImport(emptyList())

    assertEquals(existing, merged)
  }
}
