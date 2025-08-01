package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.HoleScoreForCalcs
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.sogo.golf.msl.domain.usecase.scoring.CalcHoleNetParUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcStablefordUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcParUseCase
import com.sogo.golf.msl.domain.usecase.scoring.CalcStrokeUseCase
import com.sogo.golf.msl.domain.usecase.competition.GetLocalCompetitionUseCase
import com.sogo.golf.msl.domain.usecase.game.GetLocalGameUseCase
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class UpdatePickupUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase,
    private val calcStablefordUseCase: CalcStablefordUseCase,
    private val calcParUseCase: CalcParUseCase,
    private val calcStrokeUseCase: CalcStrokeUseCase,
    private val getLocalCompetitionUseCase: GetLocalCompetitionUseCase,
    private val getLocalGameUseCase: GetLocalGameUseCase
) {
    suspend operator fun invoke(
        round: Round,
        holeNumber: Int,
        isMainGolfer: Boolean,
        dailyHandicap: Double,
        par: Int,
        index1: Int,
        index2: Int,
        index3: Int?
    ) {
        try {
            val holeScoreForCalcs = com.sogo.golf.msl.domain.model.HoleScoreForCalcs(
                par = par,
                index1 = index1,
                index2 = index2,
                index3 = index3 ?: 0
            )
            val netPar = calcHoleNetParUseCase(holeScoreForCalcs, dailyHandicap)
            val newStrokes = netPar.toInt() + 2
            
            Log.d("UpdatePickup", "Setting pickup for hole $holeNumber, netPar: $netPar, strokes: $newStrokes")
            
            val updatedRound = updateRoundPickupState(round, holeNumber, isMainGolfer, newStrokes)
            roundLocalDbRepository.saveRound(updatedRound)
            Log.d("UpdatePickup", "✅ Pickup state updated in Room database")
            
        } catch (e: Exception) {
            Log.e("UpdatePickup", "❌ Error updating pickup state", e)
            throw e
        }
    }
    
    suspend fun syncToRemote(round: Round) {
        try {
            Log.d("UpdatePickup", "Syncing pickup state to remote API for round: ${round.id}")
            Log.d("UpdatePickup", "Hole scores count: ${round.holeScores.size}")
            Log.d("UpdatePickup", "Partner hole scores count: ${round.playingPartnerRound?.holeScores?.size ?: 0}")
            sogoMongoRepository.updateRound(round.id, round)
            Log.d("UpdatePickup", "✅ Pickup state synced to remote successfully")
        } catch (e: Exception) {
            Log.e("UpdatePickup", "❌ Failed to sync pickup state to remote: ${e.message}", e)
        }
    }
    
    private suspend fun updateRoundPickupState(
        round: Round,
        holeNumber: Int,
        isMainGolfer: Boolean,
        newStrokes: Int
    ): Round {
        val holeIndex = getHoleIndex(holeNumber)
        
        return if (isMainGolfer) {
            val updatedHoleScores = round.holeScores.toMutableList()
            if (holeIndex >= 0 && holeIndex < updatedHoleScores.size) {
                val currentHoleScore = updatedHoleScores[holeIndex]
                val newPickupState = !(currentHoleScore.isBallPickedUp ?: false)
                val finalStrokes = if (newPickupState) newStrokes else currentHoleScore.strokes
                val newScore = calculateScore(finalStrokes, currentHoleScore, isMainGolfer, newPickupState)
                
                Log.d("UpdatePickup", "Main golfer - Pickup: $newPickupState, Strokes: $finalStrokes, Score: $newScore")
                
                updatedHoleScores[holeIndex] = currentHoleScore.copy(
                    isBallPickedUp = newPickupState,
                    strokes = finalStrokes,
                    score = newScore
                )
            }
            round.copy(holeScores = updatedHoleScores)
        } else {
            val partnerRound = round.playingPartnerRound
            if (partnerRound != null) {
                val updatedPartnerHoleScores = partnerRound.holeScores.toMutableList()
                if (holeIndex >= 0 && holeIndex < updatedPartnerHoleScores.size) {
                    val currentHoleScore = updatedPartnerHoleScores[holeIndex]
                    val newPickupState = !(currentHoleScore.isBallPickedUp ?: false)
                    val finalStrokes = if (newPickupState) newStrokes else currentHoleScore.strokes
                    val newScore = calculateScore(finalStrokes, currentHoleScore, isMainGolfer, newPickupState)
                    
                    Log.d("UpdatePickup", "Partner - Pickup: $newPickupState, Strokes: $finalStrokes, Score: $newScore")
                    
                    updatedPartnerHoleScores[holeIndex] = currentHoleScore.copy(
                        isBallPickedUp = newPickupState,
                        strokes = finalStrokes,
                        score = newScore
                    )
                }
                round.copy(
                    playingPartnerRound = partnerRound.copy(holeScores = updatedPartnerHoleScores)
                )
            } else round
        }
    }
    
    private suspend fun calculateScore(
        strokes: Int, 
        holeScore: com.sogo.golf.msl.domain.model.HoleScore,
        isMainGolfer: Boolean,
        isBallPickedUp: Boolean
    ): Float {
        return try {
            if (isBallPickedUp) {
                Log.d("UpdatePickup", "Ball picked up - setting score to 0")
                return 0f
            }
            
            if (strokes == 0) {
                return 0f
            }
            
            val competition = getLocalCompetitionUseCase().first()
            val game = getLocalGameUseCase().first()
            
            if (competition == null || game == null) {
                Log.w("UpdatePickup", "Missing competition or game data for score calculation")
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

            val calculatedScore = when (scoreType.lowercase()) {
                "stableford" -> calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                "par" -> calcParUseCase(strokes, holeScoreForCalcs, dailyHandicap) ?: 0f
                "stroke" -> calcStrokeUseCase(strokes, holeScoreForCalcs, dailyHandicap)
                else -> {
                    Log.w("UpdatePickup", "Unknown score type: $scoreType, defaulting to Stableford")
                    calcStablefordUseCase(holeScoreForCalcs, dailyHandicap, strokes)
                }
            }
            
            Log.d("UpdatePickup", "Calculated score: $calculatedScore for strokes: $strokes, scoreType: $scoreType")
            calculatedScore
        } catch (e: Exception) {
            Log.e("UpdatePickup", "Error calculating score", e)
            0f
        }
    }

    private suspend fun getHoleIndex(holeNumber: Int): Int {
        val game = getLocalGameUseCase().first()
        val startingHole = game?.startingHoleNumber ?: 1
        return holeNumber - startingHole
    }
}
