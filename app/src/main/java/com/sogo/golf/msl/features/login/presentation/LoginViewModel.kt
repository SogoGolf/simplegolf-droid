// app/src/main/java/com/sogo/golf/msl/features/login/presentation/LoginViewModel.kt
package com.sogo.golf.msl.features.login.presentation

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sogo.golf.msl.data.manager.ClubSelectionManager
import com.sogo.golf.msl.domain.model.NetworkResult
import com.sogo.golf.msl.domain.model.msl.MslClub
import com.sogo.golf.msl.domain.repository.remote.MslRepository
import com.sogo.golf.msl.domain.usecase.auth.ProcessMslAuthCodeUseCase
import com.sogo.golf.msl.domain.usecase.club.GetMslClubAndTenantIdsUseCase
import com.sogo.golf.msl.domain.usecase.club.SetSelectedClubUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LoginUiState(
    val isLoadingClubs: Boolean = false,
    val clubs: List<MslClub> = emptyList(),
    val selectedClub: MslClub? = null,
    val isProcessingAuth: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val mslRepository: MslRepository,
    private val processMslAuthCodeUseCase: ProcessMslAuthCodeUseCase,
    private val clubSelectionManager: ClubSelectionManager,
    private val setSelectedClubUseCase: SetSelectedClubUseCase,
    private val getMslClubAndTenantIdsUseCase: GetMslClubAndTenantIdsUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
    }

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _authSuccessEvent = MutableSharedFlow<Unit>()
    val authSuccessEvent: SharedFlow<Unit> = _authSuccessEvent

    private val _navigateToWebAuth = MutableSharedFlow<String>()
    val navigateToWebAuth: SharedFlow<String> = _navigateToWebAuth

    init {
        loadClubs() // initapi - loads clubs during ViewModel initialization

        // CRITICAL: Load previously selected club from SharedPreferences
        loadSavedClubSelection()

        // Observe club selection changes
        viewModelScope.launch {
            combine(
                clubSelectionManager.allClubs,
                clubSelectionManager.selectedClub
            ) { clubs, selectedClub ->
                Log.d(TAG, "Club selection changed - Clubs: ${clubs.size}, Selected: ${selectedClub?.name}")
                _uiState.value = _uiState.value.copy(
                    clubs = clubs,
                    selectedClub = selectedClub
                )
            }.collect { }
        }
    }

    private fun loadSavedClubSelection() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "=== LOADING SAVED CLUB SELECTION ===")

                val savedClub = getMslClubAndTenantIdsUseCase()
                if (savedClub != null) {
                    Log.d(TAG, "Found saved club selection: ID=${savedClub.clubId}, TenantID=${savedClub.tenantId}")

                    // Wait for clubs to load, then find and select the saved club
                    clubSelectionManager.allClubs.collect { clubs ->
                        if (clubs.isNotEmpty()) {
                            val matchingClub = clubs.find { it.clubId == savedClub.clubId }
                            if (matchingClub != null) {
                                Log.d(TAG, "✅ Restored saved club selection: ${matchingClub.name}")
                                clubSelectionManager.setSelectedClub(matchingClub)
                                _uiState.value = _uiState.value.copy(selectedClub = matchingClub)
                            } else {
                                Log.w(TAG, "⚠️ Saved club ID ${savedClub.clubId} not found in available clubs")
                            }
                            return@collect // Exit the collect loop
                        }
                    }
                } else {
                    Log.d(TAG, "No saved club selection found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading saved club selection", e)
            }
        }
    }

    private fun loadClubs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingClubs = true,
                errorMessage = null
            )

            when (val result = mslRepository.getClubs()) { // initapi - API call to fetch clubs list
                is NetworkResult.Success -> {
                    Log.d(TAG, "Loaded ${result.data.size} clubs")

                    clubSelectionManager.setAllClubs(result.data.sortedBy { it.name })

                    _uiState.value = _uiState.value.copy(
                        isLoadingClubs = false
                    )
                }
                is NetworkResult.Error -> {
                    Log.e(TAG, "Failed to load clubs: ${result.error}")
                    _uiState.value = _uiState.value.copy(
                        isLoadingClubs = false,
                        errorMessage = "Failed to load clubs: ${result.error.toUserMessage()}"
                    )
                }
                is NetworkResult.Loading -> {
                    // Already handled above
                }
            }
        }
    }

    fun selectClub(club: MslClub) {
        Log.d(TAG, "=== SELECTING CLUB ===")
        Log.d(TAG, "Club: ${club.name} (ID: ${club.clubId}, TenantID: ${club.tenantId})")

        // Update both manager and UI state immediately
        clubSelectionManager.setSelectedClub(club)
        _uiState.value = _uiState.value.copy(
            selectedClub = club,
            errorMessage = null
        )

        // Store in SharedPreferences for persistence
        viewModelScope.launch {
            try {
                val result = setSelectedClubUseCase(club)
                if (result.isSuccess) {
                    Log.d(TAG, "✅ Club stored in SharedPreferences successfully")
                } else {
                    Log.e(TAG, "❌ Failed to store club: ${result.exceptionOrNull()}")
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Exception storing club", e)
            }
        }
    }

    fun startWebAuth() {
        val selectedClub = _uiState.value.selectedClub

        Log.d(TAG, "=== STARTING WEB AUTH ===")
        Log.d(TAG, "Selected club from UI state: ${selectedClub?.name}")
        Log.d(TAG, "Selected club from manager: ${clubSelectionManager.getSelectedClub()?.name}")

        if (selectedClub == null) {
            Log.e(TAG, "❌ No club selected!")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a club first"
            )
            return
        }

        if (selectedClub.tenantId.isBlank()) {
            Log.e(TAG, "❌ Club has no tenantId: ${selectedClub.name}")
            _uiState.value = _uiState.value.copy(
                errorMessage = "Club ${selectedClub.name} does not have a valid tenant ID"
            )
            return
        }

        val authUrl = "https://id.micropower.com.au/${selectedClub.tenantId}?returnUrl=msl://success"
        Log.d(TAG, "Generated auth URL: $authUrl")

        viewModelScope.launch {
            _navigateToWebAuth.emit(authUrl)
        }
    }


    private fun getAuthPathForClub(club: MslClub): String? {
        // Then try to derive from club name (convert to lowercase, remove spaces, etc.)
        val clubName = club.name
        if (clubName.isBlank()) {
            Log.w(TAG, "Club name is blank for club ID: ${club.clubId}")
            return null
        }

        val derivedPath = clubName
            .lowercase()
            .replace(" ", "")
            .replace("golf", "golf")
            .replace("club", "club")

        Log.d(TAG, "Derived auth path for ${club.name}: $derivedPath")
        return derivedPath
    }

    fun handleUrlRedirect(url: String) {
        Log.d(TAG, "=== URL REDIRECT RECEIVED ===")
        Log.d(TAG, "Full URL: $url")

        if (url.startsWith("msl://success")) {
            try {
                val uri = Uri.parse(url)

                Log.d(TAG, "=== URL PARSING ===")
                Log.d(TAG, "Query: ${uri.query}")

                // Log all query parameters
                Log.d(TAG, "=== QUERY PARAMETERS ===")
                uri.queryParameterNames?.forEach { paramName ->
                    val paramValue = uri.getQueryParameter(paramName)
                    Log.d(TAG, "$paramName = $paramValue")
                }

                // Look for auth code
                val code = uri.getQueryParameter("code")
                val token = uri.getQueryParameter("token")
                val accessToken = uri.getQueryParameter("access_token")
                val authCode = uri.getQueryParameter("authCode")
                val error = uri.getQueryParameter("error")

                // Check if there's an error
                if (error != null) {
                    Log.e(TAG, "Authentication error: $error")
                    val errorDescription = uri.getQueryParameter("error_description")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "Authentication failed: $error - $errorDescription"
                    )
                    return
                }

                // Extract the auth code
                val extractedCode = code ?: authCode ?: token ?: accessToken

                if (extractedCode != null) {
                    Log.d(TAG, "✅ Authentication successful - extracted code: ${extractedCode.take(10)}...")
                    processAuthCode(extractedCode)
                } else {
                    Log.w(TAG, "⚠️ No auth code found in success URL")
                    _uiState.value = _uiState.value.copy(
                        errorMessage = "No authentication code received"
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error parsing auth URL", e)
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Error processing authentication response: ${e.message}"
                )
            }
        }
    }

    private fun processAuthCode(authCode: String) {
        val selectedClub = clubSelectionManager.getSelectedClub()
        if (selectedClub == null) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "No club selected"
            )
            return
        }

        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(
                    isProcessingAuth = true,
                    errorMessage = null
                )

                Log.d(TAG, "Processing auth code for club: ${selectedClub.name} (${selectedClub.clubId})")

                val result = processMslAuthCodeUseCase(authCode, selectedClub.clubId.toString())

                if (result.isSuccess) {
                    Log.d(TAG, "✅ MSL authentication completed successfully")
                    _authSuccessEvent.emit(Unit)
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Authentication failed"
                    Log.e(TAG, "❌ MSL authentication failed: $error")
                    _uiState.value = _uiState.value.copy(
                        isProcessingAuth = false,
                        errorMessage = error
                    )
                }

            } catch (e: Exception) {
                Log.e(TAG, "Exception during auth code processing", e)
                _uiState.value = _uiState.value.copy(
                    isProcessingAuth = false,
                    errorMessage = "Authentication failed: ${e.message}"
                )
            }
        }
    }

    fun getSelectedClubForWebAuth(): MslClub? {
        val club = _uiState.value.selectedClub
        Log.d(TAG, "Getting selected club for WebAuth: ${club?.name}")
        return club
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    fun retryLoadClubs() {
        loadClubs()
    }
}
