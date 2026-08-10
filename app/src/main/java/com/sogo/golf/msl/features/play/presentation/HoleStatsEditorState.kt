package com.sogo.golf.msl.features.play.presentation

import com.sogo.golf.msl.domain.model.HoleStats

/**
 * UI state shared by the in-round Track tab and the pre-submit Round Stats editor.
 *
 * Counters deliberately remain nullable. A missing value means "not tracked", while an
 * explicit zero means the golfer tracked the stat and recorded zero. Rendering a missing value
 * as zero must not silently turn it into an explicit zero when another field is edited.
 */
data class HoleStatsEditorState(
    val teeClub: String? = null,
    val fairwayHit: Boolean? = null,
    val fairwayMiss: String? = null,
    val approachClub: String? = null,
    val greenHit: Boolean? = null,
    val approachMisses: Set<String> = emptySet(),
    val putts: Int? = null,
    val bunkerShots: Int? = null,
    val penalties: Int? = null
) {
    fun toggleFairwayHit(hit: Boolean): HoleStatsEditorState {
        val next = if (fairwayHit == hit) null else hit
        return copy(
            fairwayHit = next,
            fairwayMiss = if (next == false) fairwayMiss else null
        )
    }

    fun toggleFairwayMiss(direction: String): HoleStatsEditorState {
        if (fairwayHit != false || direction !in FAIRWAY_MISS_DIRECTIONS) return this
        return copy(fairwayMiss = if (fairwayMiss == direction) null else direction)
    }

    fun toggleGreenHit(hit: Boolean): HoleStatsEditorState {
        val next = if (greenHit == hit) null else hit
        return copy(
            greenHit = next,
            approachMisses = if (next == false) approachMisses else emptySet()
        )
    }

    /** Approach misses allow one horizontal and one distance direction. */
    fun toggleApproachMiss(direction: String): HoleStatsEditorState {
        val axis = if (direction == "left" || direction == "right") {
            setOf("left", "right")
        } else {
            setOf("short", "long")
        }
        val wasSelected = direction in approachMisses
        val withoutAxis = approachMisses - axis
        return copy(approachMisses = if (wasSelected) withoutAxis else withoutAxis + direction)
    }

    /** Build the complete overwrite payload without dropping fields the user did not touch. */
    fun toHoleStats(): HoleStats = HoleStats(
        teeClub = teeClub,
        fairwayHit = fairwayHit,
        fairwayMiss = if (fairwayHit == false) fairwayMiss else null,
        approachClub = approachClub,
        greenHit = greenHit,
        approachMiss = if (greenHit == false && approachMisses.isNotEmpty()) {
            approachMisses.sorted().joinToString(",")
        } else {
            null
        },
        putts = putts,
        bunkerShots = bunkerShots,
        penalties = penalties
    )

    companion object {
        private val FAIRWAY_MISS_DIRECTIONS = setOf("left", "right", "short")

        fun from(existing: HoleStats?): HoleStatsEditorState = HoleStatsEditorState(
            teeClub = existing?.teeClub,
            fairwayHit = existing?.fairwayHit,
            fairwayMiss = existing?.fairwayMiss
                ?.trim()
                ?.lowercase()
                ?.takeIf { it.isNotEmpty() && existing.fairwayHit == false },
            approachClub = existing?.approachClub,
            greenHit = existing?.greenHit,
            approachMisses = existing?.approachMiss
                ?.split(",")
                ?.map { it.trim().lowercase() }
                ?.filter { it.isNotEmpty() }
                ?.toSet()
                .orEmpty(),
            putts = existing?.putts,
            bunkerShots = existing?.bunkerShots,
            penalties = existing?.penalties
        )
    }
}

internal fun incrementTrackedCounter(value: Int?): Int = (value ?: 0) + 1

internal fun decrementTrackedCounter(value: Int?): Int? = when {
    value == null -> null
    value > 0 -> value - 1
    else -> value
}
