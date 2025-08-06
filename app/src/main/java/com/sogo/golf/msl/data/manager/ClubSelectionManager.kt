// app/src/main/java/com/sogo/golf/msl/data/manager/ClubSelectionManager.kt
package com.sogo.golf.msl.data.manager

import com.sogo.golf.msl.domain.model.msl.MslClub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubSelectionManager @Inject constructor() {

    companion object {
        private const val TAG = "ClubSelectionManager"
    }

    private val _selectedClub = MutableStateFlow<MslClub?>(null)
    val selectedClub: StateFlow<MslClub?> = _selectedClub.asStateFlow()

    private val _allClubs = MutableStateFlow<List<MslClub>>(emptyList())
    val allClubs: StateFlow<List<MslClub>> = _allClubs.asStateFlow()

    fun setSelectedClub(club: MslClub) {
        android.util.Log.d(TAG, "=== SETTING SELECTED CLUB ===")
        android.util.Log.d(TAG, "Club: ${club.name}")
        android.util.Log.d(TAG, "Club ID: ${club.clubId}")
        android.util.Log.d(TAG, "Tenant ID: ${club.tenantId}")

        _selectedClub.value = club

        android.util.Log.d(TAG, "✅ Club selection updated in StateFlow")
        android.util.Log.d(TAG, "Current selected club: ${_selectedClub.value?.name}")
    }

    fun setAllClubs(clubs: List<MslClub>) {
//        android.util.Log.d(TAG, "=== SETTING ALL CLUBS ===")
        android.util.Log.d(TAG, "Clubs count: ${clubs.size}")

        _allClubs.value = clubs

        // Log all clubs for debugging
//        clubs.forEachIndexed { index, club ->
//            android.util.Log.d(TAG, "Club $index: ${club.name} (ID: ${club.clubId}, TenantID: ${club.tenantId})")
//        }

//        android.util.Log.d(TAG, "✅ All clubs updated in StateFlow")
    }

    fun clearSelection() {
        android.util.Log.d(TAG, "=== CLEARING CLUB SELECTION ===")
        android.util.Log.d(TAG, "Previous selection: ${_selectedClub.value?.name}")

        _selectedClub.value = null

        android.util.Log.d(TAG, "✅ Club selection cleared")
    }

    fun getSelectedClub(): MslClub? {
        val club = _selectedClub.value
        android.util.Log.d(TAG, "=== GETTING SELECTED CLUB ===")
        android.util.Log.d(TAG, "Current selection: ${club?.name}")
        return club
    }

    // Debug method to check current state
    fun debugCurrentState(): String {
        val selected = _selectedClub.value
        val allCount = _allClubs.value.size

        return buildString {
            appendLine("=== CLUB SELECTION MANAGER STATE ===")
            appendLine("Selected Club: ${selected?.name ?: "NONE"}")
            if (selected != null) {
                appendLine("  - Club ID: ${selected.clubId}")
                appendLine("  - Tenant ID: ${selected.tenantId}")
            }
            appendLine("Total Clubs: $allCount")
            appendLine("All Clubs: ${_allClubs.value.joinToString(", ") { it.name }}")
        }
    }
}