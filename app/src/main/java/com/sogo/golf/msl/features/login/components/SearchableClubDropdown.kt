// app/src/main/java/com/sogo/golf/msl/features/login/components/SearchableClubDropdown.kt
package com.sogo.golf.msl.features.login.components

import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.domain.model.msl.MslClub
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchableClubDropdown(
    clubs: List<MslClub>,
    selectedClub: MslClub?,
    onClubSelected: (MslClub) -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    var expanded by remember { mutableStateOf(false) }
    var searchText by remember { mutableStateOf("") }

    // Update search text when selected club changes externally
    LaunchedEffect(selectedClub) {
        if (selectedClub != null && searchText != selectedClub.name) {
            searchText = selectedClub.name
        } else if (selectedClub == null) {
            // Clear search text when no club is selected
            searchText = ""
        }
    }

    // Filter clubs based on search text
    val filteredClubs = remember(clubs, searchText) {
        if (searchText.isBlank()) {
            clubs
        } else {
            clubs.filter { club ->
                club.name.contains(searchText, ignoreCase = true)
            }
        }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it && !isLoading },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = searchText,
            onValueChange = { newText ->
                searchText = newText
                expanded = true
                // Clear selection if text doesn't match any club exactly
                if (selectedClub != null && newText != selectedClub.name) {
                    // Don't clear immediately - let user finish typing
                }
            },
            label = {
                Text(
                    if (selectedClub == null) "Search or select a club" else "Selected club",
                    color = Color.White.copy(alpha = 0.7f)
                )
            },
            placeholder = {
                Text(
                    if (selectedClub == null) "Type to search clubs..." else selectedClub.name,
                    color = Color.White.copy(alpha = 0.5f)
                )
            },
            trailingIcon = {
                if (searchText.isNotEmpty()) {
                    IconButton(
                        onClick = {
                            searchText = ""
                            expanded = false
                        }
                    ) {
                        Icon(
                            Icons.Default.Clear,
                            contentDescription = "Clear",
                            tint = Color.White
                        )
                    }
                } else {
                    ExposedDropdownMenuDefaults.TrailingIcon(
                        expanded = expanded,
                        modifier = Modifier.menuAnchor()
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color.White.copy(alpha = 0.7f),
                focusedLabelColor = Color.White,
                unfocusedLabelColor = Color.White.copy(alpha = 0.7f),
                cursorColor = Color.White
            ),
            singleLine = true,
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor()
        )

        if (expanded && filteredClubs.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = true,
                onDismissRequest = { expanded = false },
                modifier = Modifier.heightIn(max = 200.dp)
            ) {
                filteredClubs.forEach { club ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = club.name,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        onClick = {
                            searchText = club.name
                            onClubSelected(club)
                            expanded = false
                        }
                    )
                }

                // Show "No clubs found" if search returns no results
                if (filteredClubs.isEmpty() && searchText.isNotBlank()) {
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = "No clubs found",
                                style = MaterialTheme.typography.bodyMedium,
                                color = mslBlue
                            )
                        },
                        onClick = { expanded = false }
                    )
                }
            }
        }
    }
}