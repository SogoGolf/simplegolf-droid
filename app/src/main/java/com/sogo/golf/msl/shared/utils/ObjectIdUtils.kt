package com.sogo.golf.msl.shared.utils

import java.security.SecureRandom

object ObjectIdUtils {
    
    /**
     * Generates a MongoDB-compatible ObjectId as a 24-character hex string.
     * Format: 4-byte timestamp + 5-byte random value + 3-byte incrementing counter
     */
    fun generateObjectId(): String {
        val timestamp = (System.currentTimeMillis() / 1000).toInt()
        val random = SecureRandom()
        
        // 4 bytes for timestamp
        val timestampBytes = ByteArray(4)
        timestampBytes[0] = (timestamp shr 24).toByte()
        timestampBytes[1] = (timestamp shr 16).toByte()
        timestampBytes[2] = (timestamp shr 8).toByte()
        timestampBytes[3] = timestamp.toByte()
        
        // 5 bytes random
        val randomBytes = ByteArray(5)
        random.nextBytes(randomBytes)
        
        // 3 bytes counter (using random for simplicity)
        val counterBytes = ByteArray(3)
        random.nextBytes(counterBytes)
        
        // Combine all bytes
        val objectIdBytes = timestampBytes + randomBytes + counterBytes
        
        // Convert to hex string
        return objectIdBytes.joinToString("") { "%02x".format(it) }
    }
}
