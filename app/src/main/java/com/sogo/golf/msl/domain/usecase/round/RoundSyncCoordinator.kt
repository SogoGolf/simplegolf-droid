package com.sogo.golf.msl.domain.usecase.round

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

/**
 * App-wide serialization boundary for operations that read, replace, or submit a Round row.
 *
 * Room stores a round as one object, so independently correct score, pickup, and stats writers
 * can otherwise overwrite one another when they start from different snapshots. Callers must
 * acquire this gate before their authoritative Room read and retain it through any associated
 * remote write.
 */
@Singleton
class RoundSyncCoordinator @Inject constructor() {
    private val mutex = Mutex()

    suspend fun <T> withRoundSync(block: suspend () -> T): T = mutex.withLock { block() }
}
