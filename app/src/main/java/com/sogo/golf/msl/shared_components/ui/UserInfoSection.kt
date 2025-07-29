package com.sogo.golf.msl.shared_components.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.domain.model.msl.MslGame
import com.sogo.golf.msl.domain.model.msl.MslGolfer
import com.sogo.golf.msl.shared.utils.TimeFormatUtils
import com.sogo.golf.msl.shared_components.ui.components.ColoredSquare
import org.threeten.bp.LocalDateTime

@Composable
fun UserInfoSection(
    golfer: MslGolfer?,
    game: MslGame?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp)
    ) {

        // Start time and hole
        val startTime = TimeFormatUtils.formatBookingTime(game?.bookingTime)

        val startHole = if (game?.startingHoleNumber != 0 && game?.startingHoleNumber != null) {
            "${game.startingHoleNumber}"
        } else {
            "-"
        }

        Text(
            text = "$startTime Starting Hole $startHole",
            fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 2.dp)
        )

        // Player name
        Text(
            text = "${golfer?.firstName ?: "-"} ${golfer?.surname ?: "-"}",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            style = TextStyle(
                fontWeight = FontWeight.Bold,
            ),
        )

        // Golf link number
        Text(
            text = golfer?.golfLinkNo ?: "-",
            fontSize = MaterialTheme.typography.bodyMedium.fontSize,
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )


        // Handicaps row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-5).dp)
            ) {
                Text(
                    text = "${game?.gaHandicap ?: "-"}",
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "GA Handicap",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy((-5).dp)
            ) {
                val dailyHcap = if (game?.startingHoleNumber != 0 && game?.startingHoleNumber != null) {
                    "${game.dailyHandicap}"
                } else {
                    "-"
                }
                Text(
                    text = dailyHcap,
                    fontSize = MaterialTheme.typography.headlineLarge.fontSize,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Daily Handicap",
                    fontSize = MaterialTheme.typography.titleMedium.fontSize,
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tee color and name
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (game?.teeColour != null) ColoredSquare(hexColor = "#${game.teeColour}")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = game?.teeName ?: "",
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
            )
        }

        Spacer(modifier = Modifier.height(5.dp))

        // Competition section header
        Text(
            text = "Your competition(s):",
            fontSize = MaterialTheme.typography.titleMedium.fontSize,
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )

        Spacer(modifier = Modifier.height(4.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun UserInfoSectionPreview() {
    MaterialTheme {
        val sampleGolfer = MslGolfer(
            firstName = "John",
            surname = "Smith",
            email = "john.smith@example.com",
            golfLinkNo = "1234567890",
            dateOfBirth = "1990-01-01",
            mobileNo = "0123456789",
            gender = "Male",
            country = "USA",
            state = "CA",
            postCode = "90001",
            primary = 10.0
        )
        
        val sampleGame = MslGame(
            errorMessage = null,
            scorecardMessageOfTheDay = null,
            bookingTime = LocalDateTime.of(2024, 1, 15, 14, 30), // 2:30 PM
            startingHoleNumber = 1,
            mainCompetitionId = 123,
            golflinkNumber = "1234567890",
            teeName = "Championship Tees",
            teeColourName = "Blue",
            teeColour = "0000FF", // Blue color hex
            dailyHandicap = 18,
            gaHandicap = 15.2,
            numberOfHoles = 18,
            playingPartners = emptyList(),
            competitions = emptyList()
        )
        
        UserInfoSection(
            golfer = sampleGolfer,
            game = sampleGame
        )
    }
}

@Preview(showBackground = true, name = "With null values")
@Composable
fun UserInfoSectionWithNullValuesPreview() {
    MaterialTheme {
        UserInfoSection(
            golfer = null,
            game = null
        )
    }
}

@Preview(showBackground = true, name = "With partial data")
@Composable
fun UserInfoSectionPartialDataPreview() {
    MaterialTheme {
        val partialGolfer = MslGolfer(
            firstName = "Jane",
            surname = "Do",
            email = "jane.doe@example.com",
            golfLinkNo = "1122334455",
            dateOfBirth = "1990-02-02",
            mobileNo = null,
            gender = null,
            country = "USA",
            state = null,
            postCode = null,
            primary = 0.0
        )
        
        val partialGame = MslGame(
            errorMessage = null,
            scorecardMessageOfTheDay = null,
            bookingTime = null,
            startingHoleNumber = 0, // This should show "-"
            mainCompetitionId = 123,
            golflinkNumber = null,
            teeName = "Red Tees",
            teeColourName = "Red",
            teeColour = "FF0000", // Red color hex
            dailyHandicap = null,
            gaHandicap = null,
            numberOfHoles = 9,
            playingPartners = emptyList(),
            competitions = emptyList()
        )
        
        UserInfoSection(
            golfer = partialGolfer,
            game = partialGame
        )
    }
}
