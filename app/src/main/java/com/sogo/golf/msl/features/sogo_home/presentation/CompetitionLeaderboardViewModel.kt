package com.sogo.golf.msl.features.sogo_home.presentation

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.domain.model.NetworkError
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.LeaderboardEntry
import com.sogo.golf.msl.domain.usecase.leaderboard.GetLeaderboardUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class CompetitionLeaderboardViewModel @Inject constructor(
    private val getLeaderboardUseCase: GetLeaderboardUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "CompLeaderboardVM"
    }

    // Get competition details from navigation arguments
    val competitionId: String = savedStateHandle.get<String>("competitionId") ?: ""
    val competitionName: String = savedStateHandle.get<String>("competitionName") ?: "Leaderboard"

    private val _uiState = MutableStateFlow(CompetitionLeaderboardUiState())
    val uiState: StateFlow<CompetitionLeaderboardUiState> = _uiState.asStateFlow()

    init {
        // Determine default holes based on competition name
        val defaultHoles = if (competitionName.contains("9", ignoreCase = true)) 9 else 18
        _uiState.value = _uiState.value.copy(selectedHoles = defaultHoles)
        
        // Fetch initial leaderboard data
        fetchLeaderboard()
    }

    fun fetchLeaderboard(
        numberHoles: Int? = null,
        topX: Int? = null,
        dateRange: DateRange? = null
    ) {
        viewModelScope.launch {
            // Determine competition type and set appropriate parameters
            val actualHoles = numberHoles ?: if (competitionName.contains("9", ignoreCase = true)) 9 else 18
            val actualTopX = topX ?: getTopXForCompetition(competitionName)
            val actualDateRange = dateRange ?: getDateRangeForCompetition(competitionName)
            
            Log.d(TAG, "Fetching leaderboard for competition: $competitionId ($competitionName), holes=$actualHoles, topX=$actualTopX, dateRange=$actualDateRange")
            
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            val (from, to) = getDateRange(actualDateRange)
            val dateRangeText = formatDateRange(from, to)
            val bestRoundsText = getBestRoundsText(actualTopX)
            
            Log.d(TAG, "Request params - from: $from, to: $to, topX: $actualTopX, holes: $actualHoles, id: $competitionId")
            
            when (val result = getLeaderboardUseCase(
                from = from,
                to = to,
                topX = actualTopX,
                numberHoles = actualHoles,
                leaderboardIdentifier = competitionId
            )) {
                is NetworkResult.Success -> {
                    val entries = result.data
                    Log.d(TAG, "Successfully fetched ${entries.size} leaderboard entries")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        leaderboardEntries = entries,
                        selectedHoles = actualHoles,
                        selectedDateRange = actualDateRange,
                        dateRangeText = dateRangeText,
                        bestRoundsText = bestRoundsText,
                        error = null
                    )
                }
                is NetworkResult.Error -> {
                    val errorMessage = result.error.toUserMessage()
                    Log.e(TAG, "Failed to fetch leaderboard: $errorMessage")
                    
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
                is NetworkResult.Loading -> {
                    Log.d(TAG, "Loading leaderboard...")
                }
            }
        }
    }

    private fun getTopXForCompetition(competitionName: String): Int {
        return when {
            competitionName.contains("weekly", ignoreCase = true) -> 1
            competitionName.contains("monthly", ignoreCase = true) -> 3
            competitionName.contains("annual", ignoreCase = true) -> 5
            else -> 10 // Default
        }
    }

    private fun getDateRangeForCompetition(competitionName: String): DateRange {
        return when {
            competitionName.contains("weekly", ignoreCase = true) -> DateRange.ThisWeek
            competitionName.contains("monthly", ignoreCase = true) -> DateRange.ThisMonth
            competitionName.contains("annual", ignoreCase = true) -> DateRange.AllTime
            else -> DateRange.ThisWeek // Default
        }
    }

    private fun getBestRoundsText(topX: Int): String {
        return when (topX) {
            1 -> "Best Round"
            else -> "Best $topX Rounds"
        }
    }

    private fun formatDateRange(from: String, to: String): String {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE
        val outputFormatter = DateTimeFormatter.ofPattern("d MMM yyyy", Locale.ENGLISH)
        
        val fromDate = LocalDate.parse(from, inputFormatter)
        val toDate = LocalDate.parse(to, inputFormatter)
        
        val fromFormatted = fromDate.format(outputFormatter)
        val toFormatted = toDate.format(outputFormatter)
        
        return if (from == to) {
            fromFormatted
        } else {
            "$fromFormatted - $toFormatted"
        }
    }

    private fun getDateRange(dateRange: DateRange): Pair<String, String> {
        val formatter = DateTimeFormatter.ISO_LOCAL_DATE
        val today = LocalDate.now()
        
        return when (dateRange) {
            DateRange.Today -> {
                val dateStr = today.format(formatter)
                dateStr to dateStr
            }
            DateRange.ThisWeek -> {
                // Weekly competitions start on Saturday and end on Friday
                val daysSinceSaturday = (today.dayOfWeek.value + 1) % 7 // Saturday = 0, Sunday = 1, ..., Friday = 6
                val startOfWeek = today.minusDays(daysSinceSaturday.toLong())
                val endOfWeek = startOfWeek.plusDays(6)
                startOfWeek.format(formatter) to endOfWeek.format(formatter)
            }
            DateRange.ThisMonth -> {
                val startOfMonth = today.withDayOfMonth(1)
                val endOfMonth = today.withDayOfMonth(today.lengthOfMonth())
                startOfMonth.format(formatter) to endOfMonth.format(formatter)
            }
            DateRange.LastMonth -> {
                val lastMonth = today.minusMonths(1)
                val startOfLastMonth = lastMonth.withDayOfMonth(1)
                val endOfLastMonth = lastMonth.withDayOfMonth(lastMonth.lengthOfMonth())
                startOfLastMonth.format(formatter) to endOfLastMonth.format(formatter)
            }
            DateRange.AllTime -> {
                // Annual competitions run from 1 Jan to 31 Dec of current year
                val startOfYear = today.withDayOfYear(1)
                val endOfYear = today.withMonth(12).withDayOfMonth(31)
                startOfYear.format(formatter) to endOfYear.format(formatter)
            }
        }
    }

    fun onHolesChanged(holes: Int) {
        fetchLeaderboard(numberHoles = holes)
    }

    fun onDateRangeChanged(dateRange: DateRange) {
        fetchLeaderboard(dateRange = dateRange)
    }
}

data class CompetitionLeaderboardUiState(
    val isLoading: Boolean = false,
    val leaderboardEntries: List<LeaderboardEntry> = emptyList(),
    val selectedHoles: Int = 18,
    val selectedDateRange: DateRange = DateRange.ThisWeek,
    val dateRangeText: String = "",
    val bestRoundsText: String = "",
    val error: String? = null
)

enum class DateRange {
    Today,
    ThisWeek,
    ThisMonth,
    LastMonth,
    AllTime
}