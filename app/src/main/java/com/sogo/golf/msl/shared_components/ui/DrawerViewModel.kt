package com.sogo.golf.msl.shared_components.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.usecase.msl_golfer.GetMslGolferUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DrawerViewModel @Inject constructor(
    private val getMslGolferUseCase: GetMslGolferUseCase
) : ViewModel() {

    val currentGolfer = getMslGolferUseCase()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    init {
        // ✅ DEBUG: Log what we're getting
        viewModelScope.launch {
            currentGolfer.collect { golfer ->
                android.util.Log.d("DrawerViewModel", "Golfer updated: ${golfer?.firstName} ${golfer?.surname}")
            }
        }
    }
}

