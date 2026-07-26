package com.sogo.golf.msl.data.local.preferences

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sogo.golf.msl.domain.model.mongodb.ClubType
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Disk cache for the /clubTypes list so the Track-tab club picker works on a cold offline
 * start (before any successful fetch). Plain SharedPreferences — this is public reference data.
 */
@Singleton
class ClubTypeCache @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()

    fun save(clubs: List<ClubType>) {
        if (clubs.isEmpty()) return
        prefs.edit().putString(KEY, gson.toJson(clubs)).apply()
    }

    fun load(): List<ClubType> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            gson.fromJson(json, object : TypeToken<List<ClubType>>() {}.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    companion object {
        private const val PREFS = "club_type_cache"
        private const val KEY = "club_types"
    }
}
