package com.sogo.golf.msl.utils

import android.util.Log
import com.auth0.android.jwt.JWT
import javax.inject.Inject
import javax.inject.Singleton

data class MslTokenClaims(
    val golferId: String? = null,      // User identifier from "nameid" claim
    val clubId: String? = null,        // Club ID from "clubid" claim  
    val memberId: String? = null       // Member ID from "memberid" claim
)

@Singleton
class JwtTokenDecoder @Inject constructor() {
    
    companion object {
        private const val TAG = "JwtTokenDecoder"
    }
    
    fun decodeMslToken(token: String): MslTokenClaims? {
        return try {
            val jwt = JWT(token)
            
            val golferId = jwt.getClaim("nameid").asString()
            val clubId = jwt.getClaim("clubid").asString()
            val memberId = jwt.getClaim("memberid").asString()
            
            Log.d(TAG, "Decoded MSL token - Golfer ID: $golferId, Club ID: $clubId, Member ID: $memberId")
            
            MslTokenClaims(
                golferId = golferId,
                clubId = clubId,
                memberId = memberId
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to decode JWT token", e)
            null
        }
    }
}