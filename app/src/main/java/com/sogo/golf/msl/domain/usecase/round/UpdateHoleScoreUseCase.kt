package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.data.network.NetworkChecker
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.sogo.golf.msl.domain.usecase.scoring.CalcStablefordUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcParUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcStrokeUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdateHoleScoreUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val networkChecker: NetworkChecker,
    private val calcStablefordUseCase: CalcStablefordUseCase,
    private val calcParUseCase: CalcParUseCase,
    private val calcStrokeUseCase: CalcStrokeUseCase,
    private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase,
    private val getLocalGameUseCase: GetLocalGameUseCase
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
            val holeIndex = getHoleIndex(holeNumber)
            val mainGolferStrokes = if (holeIndex >= 0 && holeIndex < round.holeScores.size) {
                round.holeScores[holeIndex]?.strokes ?: 0
            } else 0
            val mainGolferScore = if (holeIndex >= 0 && holeIndex < round.holeScores.size) {
                round.holeScores[holeIndex]?.score?.toInt() ?: 0
            } else 0
            val partnerStrokes = if (round.playingPartnerRound != null && holeIndex >= 0 && holeIndex < round.playingPartnerRound.holeScores.size) {
                round.playingPartnerRound.holeScores[holeIndex]?.strokes ?: 0
            } else 0
            val partnerScore = if (round.playingPartnerRound != null && holeIndex >= 0 && holeIndex < round.playingPartnerRound.holeScores.size) {
                round.playingPartnerRound.holeScores[holeIndex]?.score?.toInt() ?: 0
            } else 0

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

    private suspend fun updateRoundHoleScores(round: Round, holeNumber: Int, newStrokes: Int, isMainGolfer: Boolean): Round {
        val holeIndex = getHoleIndex(holeNumber)
        
        return if (isMainGolfer) {
            val updatedHoleScores = round.holeScores.toMutableList()
            if (holeIndex >= 0 && holeIndex < updatedHoleScores.size) {
                val currentHoleScore = updatedHoleScores[holeIndex]
                val newScore = calculateScore(newStrokes, currentHoleScore, isMainGolfer = true)
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
                if (holeIndex >= 0 && holeIndex < updatedHoleScores.size) {
                    val currentHoleScore = updatedHoleScores[holeIndex]
                    val newScore = calculateScore(newStrokes, currentHoleScore, isMainGolfer = false)
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

    private suspend fun calculateScore(
        strokes: Int, 
        holeScore: com.sogo.golf.msl.domain.model.HoleScore,
        isMainGolfer: Boolean
    ): Float {
        return try {
            if (strokes == 0) {
                return 0f
            }
            
            val competition = getLocalCompetitionUseCase().first()
            val game = getLocalGameUseCase().first()
            
            if (competition == null || game == null) {
                Log.w("UpdateHoleScore", "Missing competition or game data for score calculation")
                return 0f
            }

            val scoreType = competition.players.firstOrNull()?.scoreType ?: "Stableford"
            val dailyHandicap = if (isMainGolfer) {
                game.dailyHandicap?.toDouble() ?: 0.0
            } else {
                game.playingPartners.firstOrNull()?.dailyHandicap?.toDouble() ?: 0.0
            }

            val holeScoreForCalcs = HoleScoreForCalcs(
                par = holeScore.par,
                index1 = holeScore.index1,
                index2 = holeScore.index2,
                index3 = holeScore.index3 ?: 0
            )

            when (scoreType.lowercase()) {
                "stableford" -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                "par" -> calcParUseCase(strokes, holeScoreForCalcs, dailyHandicap) ?: 0f
                "stroke" -> calcStrokeUseCase(strokes, holeScoreForCalcs, dailyHandicap)
                else -> {
                    Log.w("UpdateHoleScore", "Unknown score type: $scoreType, defaulting to Stableford")
                    calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                }
            }
        } catch (e: Exception) {
            Log.e("UpdateHoleScore", "Error calculating score", e)
            0f
        }
    }

    private suspend fun getHoleIndex(holeNumber: Int): Int {
        val game = getLocalGameUseCase().first()
        val startingHole = game?.startingHoleNumber ?: 1
        val numberOfHoles = game?.numberOfHoles ?: 18
        
        val cycle = getCycleIndices(startingHole, numberOfHoles)
        return cycle.indexOf(holeNumber - 1)
    }

    private fun getCycleIndices(startingHole: Int, numberOfHoles: Int): List<Int> {
        val size = numberOfHoles
        val startIndex = startingHole - 1
        return (startIndex until size).toList() + (0 until startIndex).toList()
    }
}
