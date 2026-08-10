package com.sogo.golf.msl.domain.usecase.round

import java.util.Collections
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.fail
import org.junit.Test

class RoundSyncCoordinatorTest {

    @Test
    fun serializesRoundOperations() = runBlocking {
        val coordinator = RoundSyncCoordinator()
        val firstEntered = CompletableDeferred<Unit>()
        val releaseFirst = CompletableDeferred<Unit>()
        val secondStarted = CompletableDeferred<Unit>()
        val secondEntered = CompletableDeferred<Unit>()
        val events = Collections.synchronizedList(mutableListOf<String>())

        val first = launch(Dispatchers.Default) {
            coordinator.withRoundSync {
                events += "first-enter"
                firstEntered.complete(Unit)
                releaseFirst.await()
                events += "first-exit"
            }
        }
        firstEntered.await()

        val second = launch(Dispatchers.Default) {
            secondStarted.complete(Unit)
            coordinator.withRoundSync {
                events += "second-enter"
                secondEntered.complete(Unit)
            }
        }
        secondStarted.await()
        yield()

        assertFalse(secondEntered.isCompleted)
        releaseFirst.complete(Unit)
        joinAll(first, second)
        assertEquals(listOf("first-enter", "first-exit", "second-enter"), events)
    }

    @Test
    fun exceptionReleasesGate() = runBlocking {
        val coordinator = RoundSyncCoordinator()

        try {
            coordinator.withRoundSync<Unit> { error("expected") }
            fail("Expected the first operation to throw")
        } catch (_: IllegalStateException) {
            // Expected.
        }

        val result = withTimeout(1_000) {
            coordinator.withRoundSync { "released" }
        }
        assertEquals("released", result)
    }
}
