package com.sogo.golf.msl.domain.usecase.round

import com.sogo.golf.msl.domain.model.Round
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BulkSyncRoundGuardTest {

    @Test
    fun unchangedSnapshotCanBeMarkedSynced() {
        val snapshot = Round(id = "round-1", lastUpdated = 100L, isSynced = false)

        assertTrue(canMarkBulkSnapshotSynced(snapshot, snapshot.copy(isSynced = false)))
    }

    @Test
    fun newerEditAndDifferentOrMissingRoundCannotBeOverwritten() {
        val snapshot = Round(id = "round-1", lastUpdated = 100L)

        assertFalse(canMarkBulkSnapshotSynced(snapshot, snapshot.copy(lastUpdated = 101L)))
        assertFalse(canMarkBulkSnapshotSynced(snapshot, snapshot.copy(id = "round-2")))
        assertFalse(canMarkBulkSnapshotSynced(snapshot, null))
    }
}
