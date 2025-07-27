package com.sogo.golf.msl.data.manager

import com.sogo.golf.msl.domain.model.msl.MslClub
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClubSelectionManager @Inject constructor() {

    private val _selectedClub = MutableStateFlow<MslClub?>(null)
    val selectedClub: StateFlow<MslClub?> = _selectedClub.asStateFlow()

    private val _allClubs = MutableStateFlow<List<MslClub>>(emptyList())
    val allClubs: StateFlow<List<MslClub>> = _allClubs.asStateFlow()

    fun setSelectedClub(club: MslClub) {
        _selectedClub.value = club
    }

    fun setAllClubs(clubs: List<MslClub>) {
        _allClubs.value = clubs
        // Auto-select first club if none selected
        if (_selectedClub.value == null && clubs.isNotEmpty()) {
            _selectedClub.value = clubs.first()
        }
    }

    fun clearSelection() {
        _selectedClub.value = null
    }

    fun getSelectedClub(): MslClub? = _selectedClub.value
}