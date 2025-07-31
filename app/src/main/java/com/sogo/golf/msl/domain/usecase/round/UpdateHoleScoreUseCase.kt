package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import javax.inject.Inject

class UpdateHoleScoreUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val networkChecker: NetworkChecker
) {
    suspend operator fun invoke(
        round: Round,
        holeNumber: Int,
        newStrokes: Int,
        isMainGolfer: Boolean
    ) {
        try {
            val updatedRound = updateRoundHoleScores(round, holeNumber, newStrokes, isMainGolfer)
            roundLocalDbRepository.saveRound(updatedRound)
            Log.d("UpdateHoleScore", "✅ Round updated in Room database")

            if (networkChecker.isNetworkAvailable()) {
                syncToMongoDB(updatedRound, holeNumber)
            } else {
                Log.d("UpdateHoleScore", "⚠️ No internet connection - skipping MongoDB sync")
            }
        } catch (e: Exception) {
            Log.e("UpdateHoleScore", "❌ Error updating hole score", e)
            throw e
        }
    }

    private suspend fun syncToMongoDB(round: Round, holeNumber: Int) {
        try {
            val holeIndex = holeNumber - 1
            val mainGolferStrokes = round.holeScores.getOrNull(holeIndex)?.strokes ?: 0
            val mainGolferScore = round.holeScores.getOrNull(holeIndex)?.score?.toInt() ?: 0
            val partnerStrokes = round.playingPartnerRound?.holeScores?.getOrNull(holeIndex)?.strokes ?: 0
            val partnerScore = round.playingPartnerRound?.holeScores?.getOrNull(holeIndex)?.score?.toInt() ?: 0

            when (val result = sogoMongoRepository.updateHoleScore(
                roundId = round.id,
                holeNumber = holeNumber,
                strokes = mainGolferStrokes,
                score = mainGolferScore,
                playingPartnerStrokes = partnerStrokes,
                playingPartnerScore = partnerScore
            )) {
                is NetworkResult.Success -> {
                    Log.d("UpdateHoleScore", "✅ Successfully synced to MongoDB")
                }
                is NetworkResult.Error -> {
                    Log.w("UpdateHoleScore", "⚠️ Failed to sync to MongoDB (silent): ${result.error}")
                }
                is NetworkResult.Loading -> { }
            }
        } catch (e: Exception) {
            Log.w("UpdateHoleScore", "⚠️ MongoDB sync failed (silent)", e)
        }
    }

    private fun updateRoundHoleScores(round: Round, holeNumber: Int, newStrokes: Int, isMainGolfer: Boolean): Round {
        val holeIndex = holeNumber - 1
        
        return if (isMainGolfer) {
            val updatedHoleScores = round.holeScores.toMutableList()
            if (holeIndex < updatedHoleScores.size) {
                val currentHoleScore = updatedHoleScores[holeIndex]
                val newScore = calculateStablefordScore(newStrokes, currentHoleScore.par)
                updatedHoleScores[holeIndex] = currentHoleScore.copy(
                    strokes = newStrokes,
                    score = newScore
                )
            }
            round.copy(
                holeScores = updatedHoleScores,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )
        } else {
            val updatedPartnerRound = round.playingPartnerRound?.let { partnerRound ->
                val updatedHoleScores = partnerRound.holeScores.toMutableList()
                if (holeIndex < updatedHoleScores.size) {
                    val currentHoleScore = updatedHoleScores[holeIndex]
                    val newScore = calculateStablefordScore(newStrokes, currentHoleScore.par)
                    updatedHoleScores[holeIndex] = currentHoleScore.copy(
                        strokes = newStrokes,
                        score = newScore
                    )
                }
                partnerRound.copy(holeScores = updatedHoleScores)
            }
            round.copy(
                playingPartnerRound = updatedPartnerRound,
                lastUpdated = System.currentTimeMillis(),
                isSynced = false
            )
        }
    }

    private fun calculateStablefordScore(strokes: Int, par: Int): Float {
        return when {
            strokes == 0 -> 0f
            strokes == par - 2 -> 4f
            strokes == par - 1 -> 3f
            strokes == par -> 2f
            strokes == par + 1 -> 1f
            else -> 0f
        }
    }
}
