package com.sogo.golf.msl.features.sogo_home.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.GolfCourse
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.ui.theme.MSLColors
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import java.util.Locale

@Composable
fun RoundSummaryCard(
    roundDate: LocalDateTime?,
    clubName: String?,
    countOfHoleScores: Int?,
    score: Int?,
    playingPartnerFirstName: String?,
    playingPartnerLastName: String?,
    compType: String?,
    isSubmitted: Boolean?,
    scratchRating: Float?,
    slopeRating: Float?,
    golfLinkHandicap: Float?,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Date and submission status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = MSLColors.mslBlue,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = roundDate?.format(DateTimeFormatter.ofPattern("d MMM yyyy")) ?: "No date",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (isSubmitted == true) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = MSLColors.mslGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "SUBMITTED",
                            fontSize = 10.sp,
                            color = MSLColors.mslGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Club name
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.GolfCourse,
                    contentDescription = "Club",
                    tint = MSLColors.mslBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = clubName ?: "Unknown Club",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MSLColors.mslBlue,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Score information row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Holes played
                ScoreInfo(
                    label = "Holes",
                    value = countOfHoleScores?.toString() ?: "-"
                )
                
                // Score
                ScoreInfo(
                    label = compType?.replaceFirstChar { 
                        if (it.isLowerCase()) it.titlecase(Locale.getDefault()) 
                        else it.toString() 
                    } ?: "Score",
                    value = score?.toString() ?: "-",
                    highlight = true
                )
            }
            
            // Course ratings and handicap
            if (scratchRating != null || slopeRating != null || golfLinkHandicap != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Scratch Rating
                    if (scratchRating != null) {
                        RatingInfo(
                            label = "Scratch Rating",
                            value = String.format("%.1f", scratchRating)
                        )
                    }
                    
                    // Slope Rating  
                    if (slopeRating != null) {
                        RatingInfo(
                            label = "Slope Rating",
                            value = slopeRating.toInt().toString()
                        )
                    }
                    
                    // Played off (Handicap)
                    if (golfLinkHandicap != null) {
                        RatingInfo(
                            label = "Played off",
                            value = String.format("%.1f", golfLinkHandicap)
                        )
                    }
                }
            }
            
            // Playing partner
            if (!playingPartnerFirstName.isNullOrBlank() || !playingPartnerLastName.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = Color.Gray.copy(alpha = 0.05f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .padding(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Playing Partner",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Partner: ${playingPartnerFirstName ?: ""} ${playingPartnerLastName ?: ""}".trim(),
                        fontSize = 13.sp,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun ScoreInfo(
    label: String,
    value: String,
    highlight: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Normal
        )
        Box(
            modifier = Modifier
                .background(
                    color = if (highlight) MSLColors.mslYellow else Color.Transparent,
                    shape = RoundedCornerShape(6.dp)
                )
                .padding(horizontal = 12.dp)
        ) {
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = if (highlight) Color.White else MSLColors.mslBlue
            )
        }
    }
}

@Composable
private fun RatingInfo(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Normal
        )
        Text(
            text = value,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = MSLColors.mslBlue
        )
    }
}