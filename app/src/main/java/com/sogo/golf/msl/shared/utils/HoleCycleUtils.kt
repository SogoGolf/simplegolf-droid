package com.sogo.golf.msl.shared.utils

object HoleCycleUtils {

    /**
     * The 1-based sequence of holes a round plays, in order. Single source of
     * truth for hole-cycle order — the Play screen's pace math and the Review
     * screen's Round Time both need "which hole is the final one", and
     * PlayRoundViewModel.getCycleIndices mirrors this 0-based.
     *
     * A front-nine (1-9) 9-hole round wraps 9->1 and a back-nine (10-18)
     * 9-hole round wraps 18->10; only 18-hole rounds wrap 18->1.
     */
    fun buildHoleCycle(startingHole: Int, numberOfHoles: Int): List<Int> {
        val start = startingHole.coerceIn(1, 18)
        val count = numberOfHoles.takeIf { it > 0 } ?: 18
        val maxHole = when {
            count == 18 -> 18
            start >= 10 && count == 9 -> 18
            start in 1..9 && count == 9 -> 9
            else -> start + count - 1
        }
        val holes = mutableListOf<Int>()
        var currentHole = start

        repeat(count) {
            holes.add(currentHole)
            currentHole += 1
            if (currentHole > maxHole) {
                currentHole = if (start >= 10 && count == 9) 10 else 1
            }
        }

        return holes
    }
}
