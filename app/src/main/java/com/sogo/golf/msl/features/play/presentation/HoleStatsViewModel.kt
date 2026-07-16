package com.sogo.golf.msl.features.play.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.data.local.preferences.ClubTypeCache
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.mongodb.ClubType
import com.sogo.golf.msl.domain.model.mongodb.HoleInsights
import com.sogo.golf.msl.domain.repository.remote.SogoMongoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.threeten.bp.LocalDate
import org.threeten.bp.ZoneId
import org.threeten.bp.ZoneOffset
import org.threeten.bp.ZonedDateTime
import javax.inject.Inject

/**
 * Read-only data for the Hole Stats sheet: the club list (Track tab picker) and the per-hole
 * insights (Insights tab). Screen-scoped, so `clubs` is fetched once and cached; `insights` is
 * reloaded each time the sheet opens on a hole.
 */
@HiltViewModel
class HoleStatsViewModel @Inject constructor(
    private val sogoMongoRepository: SogoMongoRepository,
    private val clubTypeCache: ClubTypeCache
) : ViewModel() {

    data class InsightsUiState(
        val isLoading: Boolean = false,
        val error: String? = null,
        val data: HoleInsights? = null
    )

    private val _clubs = MutableStateFlow<List<ClubType>>(emptyList())
    val clubs: StateFlow<List<ClubType>> = _clubs.asStateFlow()

    private val _insights = MutableStateFlow(InsightsUiState())
    val insights: StateFlow<InsightsUiState> = _insights.asStateFlow()

    fun loadClubs() {
        if (_clubs.value.isNotEmpty()) return
        viewModelScope.launch {
            // Seed from the disk cache first so the picker works offline / on a cold start.
            val cached = clubTypeCache.load()
            if (cached.isNotEmpty()) _clubs.value = cached
            // Then refresh from the server and update the cache.
            when (val result = sogoMongoRepository.getClubTypes()) {
                is NetworkResult.Success -> {
                    _clubs.value = result.data
                    clubTypeCache.save(result.data)
                }
                is NetworkResult.Error -> {}
                is NetworkResult.Loading -> {}
            }
        }
    }

    fun loadInsights(golfLinkNo: String, entityId: String, holeNumber: Int, dailyHandicap: Double) {
        if (golfLinkNo.isBlank() || entityId.isBlank()) {
            _insights.value = InsightsUiState(error = "No round data")
            return
        }
        viewModelScope.launch {
            _insights.value = InsightsUiState(isLoading = true)
            // "Today" window (local day) as ISO-8601 UTC instants, matching the iOS client.
            val zone = ZoneId.systemDefault()
            val fromDate = LocalDate.now().atStartOfDay(zone)
                .withZoneSameInstant(ZoneOffset.UTC).toInstant().toString()
            val toDate = ZonedDateTime.now(zone)
                .withZoneSameInstant(ZoneOffset.UTC).toInstant().toString()

            when (val result = sogoMongoRepository.getHoleInsights(
                golfLinkNo = golfLinkNo,
                entityId = entityId,
                holeNumber = holeNumber,
                dailyHandicap = dailyHandicap,
                fromDate = fromDate,
                toDate = toDate
            )) {
                is NetworkResult.Success -> _insights.value = InsightsUiState(data = result.data)
                is NetworkResult.Error -> _insights.value = InsightsUiState(error = result.error.toUserMessage())
                is NetworkResult.Loading -> {}
            }
        }
    }
}
