package com.sogo.golf.msl.domain.usecase.round

import android.util.Log
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.repository.RoundLocalDbRepository
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import com.sogo.golf.msl.domain.usecase.scoring.CalcHoleNetParUseCase
import javax.inject.Inject

class UpdatePickupUseCase @Inject constructor(
    private val roundLocalDbRepository: RoundLocalDbRepository,
    private val sogoMongoRepository: SogoMongoRepository,
    private val calcHoleNetParUseCase: CalcHoleNetParUseCase
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
            Log.d("UpdatePickup", "Syncing pickup state to remote API")
            sogoMongoRepository.updateRound(round.id, round)
            Log.d("UpdatePickup", "✅ Pickup state synced to remote")
        } catch (e: Exception) {
            Log.w("UpdatePickup", "⚠️ Failed to sync pickup state to remote (silent failure)", e)
        }
    }
    
    private fun updateRoundPickupState(
        round: Round,
        holeNumber: Int,
        isMainGolfer: Boolean,
        newStrokes: Int
    ): Round {
        val holeIndex = holeNumber - 1
        
        return if (isMainGolfer) {
            val updatedHoleScores = round.holeScores.toMutableList()
            if (holeIndex < updatedHoleScores.size) {
                updatedHoleScores[holeIndex] = updatedHoleScores[holeIndex].copy(
                    isBallPickedUp = true,
                    strokes = newStrokes
                )
            }
            round.copy(holeScores = updatedHoleScores)
        } else {
            val partnerRound = round.playingPartnerRound
            if (partnerRound != null) {
                val updatedPartnerHoleScores = partnerRound.holeScores.toMutableList()
                if (holeIndex < updatedPartnerHoleScores.size) {
                    updatedPartnerHoleScores[holeIndex] = updatedPartnerHoleScores[holeIndex].copy(
                        isBallPickedUp = true,
                        strokes = newStrokes
                    )
                }
                round.copy(
                    playingPartnerRound = partnerRound.copy(holeScores = updatedPartnerHoleScores)
                )
            } else round
        }
    }
}
