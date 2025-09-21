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
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
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
    private val getLocalGameUseCase: GetLocalGameUseCase,
    private val getMslGolferUseCase: GetMslGolferUseCase
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
            val sanitizedIndex2 = if (index2 > 0) index2 else index1 + 18
            val sanitizedIndex3 = index3?.takeIf { it > 0 } ?: (sanitizedIndex2 + 18)
            val holeScoreForCalcs = HoleScoreForCalcs(
                par = par,
                index1 = index1,
                index2 = sanitizedIndex2,
                index3 = sanitizedIndex3
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
        return if (isMainGolfer) {
            val updatedHoleScores = round.holeScores.map { holeScore ->
                if (holeScore.holeNumber == holeNumber) {
                    val newPickupState = !(holeScore.isBallPickedUp ?: false)
                    val finalStrokes = if (newPickupState) newStrokes else holeScore.strokes
                    val newScore = calculateScore(finalStrokes, holeScore, isMainGolfer, newPickupState)
                    
                    Log.d("UpdatePickup", "Main golfer - Pickup: $newPickupState, Strokes: $finalStrokes, Score: $newScore")
                    
                    holeScore.copy(
                        isBallPickedUp = newPickupState,
                        strokes = finalStrokes,
                        score = newScore
                    )
                } else {
                    holeScore
                }
            }
            round.copy(holeScores = updatedHoleScores)
        } else {
            val partnerRound = round.playingPartnerRound
            if (partnerRound != null) {
                val updatedPartnerHoleScores = partnerRound.holeScores.map { holeScore ->
                    if (holeScore.holeNumber == holeNumber) {
                        val newPickupState = !(holeScore.isBallPickedUp ?: false)
                        val finalStrokes = if (newPickupState) newStrokes else holeScore.strokes
                        val newScore = calculateScore(finalStrokes, holeScore, isMainGolfer, newPickupState)
                        
                        Log.d("UpdatePickup", "Partner - Pickup: $newPickupState, Strokes: $finalStrokes, Score: $newScore")
                        
                        holeScore.copy(
                            isBallPickedUp = newPickupState,
                            strokes = finalStrokes,
                            score = newScore
                        )
                    } else {
                        holeScore
                    }
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
            val competition = getLocalCompetitionUseCase().first()
            val game = getLocalGameUseCase().first()
            
            if (competition == null || game == null) {
                Log.w("UpdatePickup", "Missing competition or game data for score calculation")
                return 0f
            }

            val scoreType = competition.players.firstOrNull()?.scoreType ?: "Stableford"
            
            if (isBallPickedUp) {
                // For Par rounds, pickup results in -1 point
                // For other rounds (Stableford, Stroke), pickup results in 0 points
                val pickupScore = if (scoreType == "Par") -1f else 0f
                Log.d("UpdatePickup", "Ball picked up - scoreType: $scoreType, setting score to $pickupScore")
                return pickupScore
            }
            
            if (strokes == 0) {
                return 0f
            }
            
            val dailyHandicap = if (isMainGolfer) {
                game.dailyHandicap?.toDouble() ?: 0.0
            } else {
                val currentGolfer = getMslGolferUseCase().first()
                val correctPartner = game.playingPartners.find { partner ->
                    partner.markedByGolfLinkNumber == currentGolfer?.golfLinkNo
                }
                correctPartner?.dailyHandicap?.toDouble() ?: 0.0
            }

            val holeScoreForCalcs = mapToHoleScoreForCalcs(holeScore)

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

    private fun mapToHoleScoreForCalcs(holeScore: com.sogo.golf.msl.domain.model.HoleScore): HoleScoreForCalcs {
        val index1 = holeScore.index1
        val index2 = holeScore.index2.takeIf { it > 0 } ?: (index1 + 18)
        val index3 = holeScore.index3?.takeIf { it > 0 } ?: (index2 + 18)

        return HoleScoreForCalcs(
            par = holeScore.par,
            index1 = index1,
            index2 = index2,
            index3 = index3
        )
    }

}
