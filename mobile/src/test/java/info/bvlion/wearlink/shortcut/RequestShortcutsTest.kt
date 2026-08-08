package info.bvlion.wearlink.shortcut

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class RequestShortcutsTest {
  @Test
  fun shortcutIdIsStableForSameRequestIdTest() {
    val requestId = "request-id-1"

    assertEquals(RequestShortcuts.shortcutId(requestId), RequestShortcuts.shortcutId(requestId))
  }

  @Test
  fun shortcutIdDiffersForDifferentRequestIdsTest() {
    assertNotEquals(RequestShortcuts.shortcutId("request-id-1"), RequestShortcuts.shortcutId("request-id-2"))
  }
}
