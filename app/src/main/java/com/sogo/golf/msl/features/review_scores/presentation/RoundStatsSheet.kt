package com.sogo.golf.msl.features.review_scores.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Adjust
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.SportsGolf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.domain.model.HoleScore
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.Round
import com.sogo.golf.msl.domain.model.mongodb.ClubType
import com.sogo.golf.msl.features.play.presentation.HoleStatsEditorState
import com.sogo.golf.msl.features.play.presentation.HoleStatsTrackEditor
import com.sogo.golf.msl.ui.theme.MSLColors

private val RoundStatsBackground = Color(0xFFF2F2F5)
private val RoundStatsLabel = Color(0xFF6E6E73)
private val RoundStatsText = Color(0xFF212121)
private val MissingTint = MSLColors.mslYellow.copy(alpha = 0.12f)
private val MissingBorder = MSLColors.mslYellow.copy(alpha = 0.55f)

/** Main-golfer-only stats review. Selecting a played hole opens the shared Track controls. */
@Composable
internal fun RoundStatsSheet(
    round: Round,
    clubs: List<ClubType>,
    isSaving: Boolean,
    onSaveHole: (Int, HoleStats) -> Unit,
    onClose: () -> Unit
) {
    var selectedHoleNumber by remember(round.id) { mutableStateOf<Int?>(null) }
    val playedHoles = round.playedHoleScoresForStats()
    val selectedHole = playedHoles.firstOrNull { it.holeNumber == selectedHoleNumber }

    if (selectedHole != null) {
        RoundStatsHoleEditor(
            hole = selectedHole,
            clubs = clubs,
            isSaving = isSaving,
            onSave = onSaveHole,
            onBack = { selectedHoleNumber = null }
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(RoundStatsBackground)
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
    ) {
        RoundStatsHeader(
            title = "Round summary",
            subtitle = "Putts, greens in regulation, and fairways hit."
        )

        if (playedHoles.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxWidth().weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text("No played holes to review.", color = RoundStatsLabel)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item { BigThreeBand(round.roundStatsSummary()) }
                item { Spacer(Modifier.height(4.dp)) }
                item {
                    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                        Text(
                            "Played holes",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoundStatsText
                        )
                        Text(
                            "Tap a hole to add or edit stats before submitting.",
                            fontSize = 13.sp,
                            color = RoundStatsLabel,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                    }
                }
                items(playedHoles, key = { it.holeNumber }) { hole ->
                    PlayedHoleStatsRow(hole = hole, onClick = { selectedHoleNumber = hole.holeNumber })
                }
                item { Spacer(Modifier.height(6.dp)) }
            }
        }

        RoundStatsBottomAction(
            label = "Done",
            isSaving = isSaving,
            contentDescription = "Close round stats",
            onClick = onClose
        )
    }
}

@Composable
private fun RoundStatsHoleEditor(
    hole: HoleScore,
    clubs: List<ClubType>,
    isSaving: Boolean,
    onSave: (Int, HoleStats) -> Unit,
    onBack: () -> Unit
) {
    var editorState by remember(hole.holeNumber) {
        mutableStateOf(HoleStatsEditorState.from(hole.stats))
    }
    val baseline = remember(hole.holeNumber) { editorState.toHoleStats() }
    val latestEditorState = rememberUpdatedState(editorState)
    val latestSave = rememberUpdatedState(onSave)

    // Returning to the summary and every outer sheet-dismiss path enqueue the latest edit.
    // ReviewScoresViewModel serializes the durable save and blocks outer dismissal while saving.
    DisposableEffect(hole.holeNumber) {
        onDispose {
            val stats = latestEditorState.value.toHoleStats()
            if (stats != baseline) latestSave.value(hole.holeNumber, stats)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(RoundStatsBackground)
            .padding(horizontal = 16.dp)
            .padding(bottom = 10.dp)
    ) {
        RoundStatsHeader(
            title = "Hole ${hole.holeNumber}",
            subtitle = "Par ${hole.par}"
        )
        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
            item {
                HoleStatsTrackEditor(
                    clubs = clubs,
                    state = editorState,
                    onStateChange = { editorState = it }
                )
            }
        }
        RoundStatsBottomAction(
            label = "Done",
            isSaving = isSaving,
            contentDescription = "Save Hole ${hole.holeNumber} stats and return to round summary",
            onClick = onBack
        )
    }
}

@Composable
private fun RoundStatsHeader(title: String, subtitle: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .padding(bottom = 12.dp)
    ) {
        Text(title, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RoundStatsText)
        Text(subtitle, fontSize = 12.sp, color = RoundStatsLabel)
    }
}

private data class BigThreeCardModel(
    val label: String,
    val value: String?,
    val coverage: String,
    val missingText: String?,
    val icon: ImageVector,
    val backgroundColor: Color,
    val accentColor: Color
)

@Composable
private fun BigThreeBand(summary: RoundStatsSummary) {
    val cards = listOf(
        BigThreeCardModel(
            label = "Putts",
            value = summary.totalPutts.takeIf { summary.puttsTracked > 0 }?.toString(),
            coverage = "${summary.puttsTracked}/${summary.playedHoles} holes",
            missingText = (summary.playedHoles - summary.puttsTracked)
                .takeIf { it > 0 }
                ?.let { "$it todo" },
            icon = Icons.Filled.SportsGolf,
            backgroundColor = MSLColors.mslBlue.copy(alpha = 0.14f),
            accentColor = MSLColors.mslBlue
        ),
        BigThreeCardModel(
            label = "GIR",
            value = if (summary.greensTracked > 0) {
                "${summary.greensHit}/${summary.greensTracked}"
            } else null,
            coverage = "${summary.greensTracked}/${summary.playedHoles} holes",
            missingText = (summary.playedHoles - summary.greensTracked)
                .takeIf { it > 0 }
                ?.let { "$it todo" },
            icon = Icons.Filled.Adjust,
            backgroundColor = MSLColors.mslGreen.copy(alpha = 0.16f),
            accentColor = MSLColors.mslGreen
        ),
        BigThreeCardModel(
            label = "Fairways",
            value = if (summary.fairwaysTracked > 0) {
                "${summary.fairwaysHit}/${summary.fairwaysTracked}"
            } else null,
            coverage = "${summary.fairwaysTracked}/${summary.fairwayEligibleHoles} holes",
            missingText = (summary.fairwayEligibleHoles - summary.fairwaysTracked)
                .takeIf { it > 0 }
                ?.let { "$it todo" },
            icon = Icons.Filled.Flag,
            backgroundColor = MSLColors.mslYellow.copy(alpha = 0.28f),
            accentColor = MSLColors.mslGunMetal
        )
    )
    val fontScale = LocalDensity.current.fontScale
    val useSingleColumn = fontScale >= 1.3f
    val cardHeight = (136f + ((fontScale - 1f).coerceAtLeast(0f) * 44f)).dp

    if (useSingleColumn) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            cards.forEach { model ->
                BigThreeStat(
                    model = model,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                )
            }
        }
    } else {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            cards.forEach { model ->
                BigThreeStat(
                    model = model,
                    modifier = Modifier
                        .weight(1f)
                        .height(cardHeight)
                )
            }
        }
    }
}

@Composable
private fun BigThreeStat(model: BigThreeCardModel, modifier: Modifier) {
    val accessibilityValue = model.value?.let { ", $it" }.orEmpty()
    val accessibilityFooter = model.missingText ?: model.coverage
    Card(
        modifier = modifier
            .border(
                BorderStroke(1.dp, model.accentColor.copy(alpha = 0.18f)),
                RoundedCornerShape(16.dp)
            )
            .semantics(mergeDescendants = true) {
                contentDescription = "${model.label}$accessibilityValue, $accessibilityFooter"
            },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = model.backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp)
                .padding(top = 14.dp, bottom = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = model.icon,
                contentDescription = null,
                tint = model.accentColor,
                modifier = Modifier.size(19.dp)
            )
            // A non-breaking-space placeholder preserves the scaled text line height when a
            // value is untracked, avoiding a fixed-height box that can clip large system fonts.
            Text(
                text = model.value ?: "\u00A0",
                fontSize = if ((model.value?.length ?: 0) > 6) 14.sp else 21.sp,
                fontWeight = FontWeight.Bold,
                color = if (model.value == null) Color.Transparent else RoundStatsText,
                maxLines = 1
            )
            Text(
                model.label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = RoundStatsText,
                maxLines = 1
            )
            if (model.missingText != null) {
                Text(
                    model.missingText,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MSLColors.mslRed,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            } else {
                Text(
                    model.coverage,
                    fontSize = 10.sp,
                    color = Color(0xFF4F4F54),
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun PlayedHoleStatsRow(hole: HoleScore, onClick: () -> Unit) {
    val isPuttsMissing = hole.stats?.putts == null
    val puttsValue = hole.stats?.putts?.toString() ?: "?"
    val girValue = hitMissValue(hole.stats?.greenHit)
    val fairwayValue = fairwayValue(hole)
    val accessibilityText = "Hole ${hole.holeNumber}, par ${hole.par}, " +
        "putts ${hole.stats?.putts?.toString() ?: "not tracked"}, " +
        "GIR $girValue, fairway $fairwayValue. Opens the stats editor."
    val shape = RoundedCornerShape(16.dp)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                BorderStroke(1.dp, if (isPuttsMissing) MissingBorder else Color.Transparent),
                shape
            )
            .clickable(onClick = onClick)
            .semantics(mergeDescendants = true) { contentDescription = accessibilityText },
        shape = shape,
        colors = CardDefaults.cardColors(
            containerColor = if (isPuttsMissing) MissingTint else Color.White
        )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "Hole ${hole.holeNumber}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = RoundStatsText
                )
                Text(
                    "Par ${hole.par}",
                    fontSize = 13.sp,
                    color = RoundStatsLabel,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                if (isPuttsMissing) {
                    Row(
                        modifier = Modifier
                            .background(
                                MSLColors.mslYellow.copy(alpha = 0.30f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Error,
                            contentDescription = null,
                            tint = MSLColors.mslRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            "Add putts",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MSLColors.mslRed,
                            maxLines = 1
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = Color(0xFF8A8A8E),
                    modifier = Modifier.padding(start = 5.dp).size(18.dp)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                HoleValue(
                    label = "Putts",
                    value = puttsValue,
                    valueColor = if (isPuttsMissing) MSLColors.mslRed else RoundStatsText,
                    modifier = Modifier.weight(1f)
                )
                HoleValue("GIR", girValue, modifier = Modifier.weight(1f))
                HoleValue("Fairway", fairwayValue, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun HoleValue(
    label: String,
    value: String,
    modifier: Modifier,
    valueColor: Color = RoundStatsText
) {
    val fontScale = LocalDensity.current.fontScale
    val valueFontSize = when {
        value.length > 7 && fontScale >= 1.3f -> 12.sp
        value.length > 7 -> 14.sp
        else -> 15.sp
    }

    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            label.uppercase(),
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF8A8A8E),
            maxLines = 1
        )
        Text(
            value,
            fontSize = valueFontSize,
            fontWeight = FontWeight.SemiBold,
            color = valueColor,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun RoundStatsBottomAction(
    label: String,
    isSaving: Boolean,
    contentDescription: String,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = !isSaving,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .height(52.dp)
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MSLColors.mslYellow,
            contentColor = MSLColors.mslGunMetal,
            disabledContainerColor = Color.LightGray,
            disabledContentColor = Color.DarkGray
        )
    ) {
        if (isSaving) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = Color.DarkGray
            )
        } else {
            Text(label, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

private fun hitMissValue(hit: Boolean?): String = when (hit) {
    true -> "Hit"
    false -> "Miss"
    null -> "—"
}

private fun fairwayValue(hole: HoleScore): String {
    return when (hole.stats?.fairwayHit) {
        true -> "Hit"
        false -> hole.stats.fairwayMiss
            ?.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            ?.let { "Miss $it" }
            ?: "Miss"
        null -> "—"
    }
}
