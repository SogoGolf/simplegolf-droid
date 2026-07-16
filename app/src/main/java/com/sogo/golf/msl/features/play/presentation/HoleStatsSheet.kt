package com.sogo.golf.msl.features.play.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sogo.golf.msl.domain.model.HoleStats
import com.sogo.golf.msl.domain.model.mongodb.ClubType
import com.sogo.golf.msl.domain.model.mongodb.HoleInsights
import com.sogo.golf.msl.domain.model.mongodb.HoleInsightsItem
import com.sogo.golf.msl.ui.theme.MSLColors
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

private val SheetBg = Color(0xFFF2F2F5)
private val CardBg = Color.White
private val LabelGrey = Color(0xFF8A8A8E)
private val DarkText = Color(0xFF1C1C1E)

// Score colours (shared with the distribution bar), mirroring the iOS sheet.
private val cEagle = Color(0xFF4CAF50)
private val cBirdie = Color(0xFF8BC34A)
private val cLevel = Color(0xFF9E9E9E)
private val cBogey = Color(0xFFFF9800)
private val cDoublePlus = Color(0xFFE53935)

/**
 * Full-height Hole Stats sheet with Track (entry) + Insights (analytics) tabs. Track state is
 * seeded once from [existingStats] and saved on dismiss (any path) via [onSaveStats].
 */
@Composable
fun HoleStatsSheet(
    holeNumber: Int,
    par: Int,
    existingStats: HoleStats?,
    golfLinkNo: String,
    dailyHandicap: Int,
    entityId: String,
    viewModel: HoleStatsViewModel,
    onSaveStats: (HoleStats) -> Unit,
    onClose: () -> Unit
) {
    var tab by remember { mutableStateOf(0) } // 0 = Track, 1 = Insights

    // Track state (seeded once).
    var teeClub by remember { mutableStateOf(existingStats?.teeClub) }
    var fairwayHit by remember { mutableStateOf(existingStats?.fairwayHit) }
    var approachClub by remember { mutableStateOf(existingStats?.approachClub) }
    var greenHit by remember { mutableStateOf(existingStats?.greenHit) }
    var missDir by remember { mutableStateOf(existingStats?.approachMiss) }
    var putts by remember { mutableStateOf(existingStats?.putts ?: 0) }
    var bunker by remember { mutableStateOf(existingStats?.bunkerShots ?: 0) }
    var penalties by remember { mutableStateOf(existingStats?.penalties ?: 0) }

    fun currentStats() = HoleStats(
        teeClub = teeClub,
        fairwayHit = fairwayHit,
        // Not modelled in the Track UI (iOS-authored) — carry it through so we never wipe it.
        fairwayMiss = existingStats?.fairwayMiss,
        approachClub = approachClub,
        greenHit = greenHit,
        approachMiss = if (greenHit == false) missDir else null,
        putts = putts,
        bunkerShots = bunker,
        penalties = penalties
    )

    LaunchedEffect(holeNumber) {
        viewModel.loadClubs()
        viewModel.loadInsights(golfLinkNo, entityId, holeNumber, dailyHandicap.toDouble())
    }

    // Snapshot of the seeded state; we only persist on dismiss if the user actually changed
    // something. Prevents a pure Insights view (or open→close) from re-writing / clobbering
    // stats and firing a needless PATCH.
    val baseline = remember { currentStats() }
    val save = rememberUpdatedState(onSaveStats)
    DisposableEffect(Unit) {
        onDispose {
            val now = currentStats()
            if (now != baseline) save.value(now)
        }
    }

    val clubs by viewModel.clubs.collectAsState()
    val insightsState by viewModel.insights.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.92f)
            .background(SheetBg)
            .padding(horizontal = 16.dp)
            .padding(top = 6.dp, bottom = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("HOLE $holeNumber", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Text("Par $par", fontSize = 14.sp, color = LabelGrey)
            }
            TextButton(onClick = onClose) {
                Text("Done", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = MSLColors.mslBlue)
            }
        }

        // Segmented control
        SegmentedControl(
            options = listOf("Track", "Insights"),
            selectedIndex = tab,
            onSelected = { tab = it }
        )
        Spacer(Modifier.height(16.dp))

        val scroll = rememberScrollState()
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(scroll)) {
            if (tab == 0) {
                TrackTab(
                    clubs = clubs,
                    teeClub = teeClub, onTeeClub = { teeClub = it },
                    fairwayHit = fairwayHit, onFairwayHit = { fairwayHit = it },
                    approachClub = approachClub, onApproachClub = { approachClub = it },
                    greenHit = greenHit, onGreenHit = { greenHit = it },
                    missDir = missDir, onMissDir = { missDir = it },
                    putts = putts, onPutts = { putts = it },
                    bunker = bunker, onBunker = { bunker = it },
                    penalties = penalties, onPenalties = { penalties = it }
                )
            } else {
                InsightsTab(par = par, state = insightsState)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Track tab
// ---------------------------------------------------------------------------

@Composable
private fun TrackTab(
    clubs: List<ClubType>,
    teeClub: String?, onTeeClub: (String?) -> Unit,
    fairwayHit: Boolean?, onFairwayHit: (Boolean?) -> Unit,
    approachClub: String?, onApproachClub: (String?) -> Unit,
    greenHit: Boolean?, onGreenHit: (Boolean?) -> Unit,
    missDir: String?, onMissDir: (String?) -> Unit,
    putts: Int, onPutts: (Int) -> Unit,
    bunker: Int, onBunker: (Int) -> Unit,
    penalties: Int, onPenalties: (Int) -> Unit
) {
    // Fairway
    StatCard(title = "FAIRWAY", clubLabel = "Tee", selectedClub = teeClub, clubs = clubs, onSelectClub = onTeeClub) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceButton("Hit", fairwayHit == true, MSLColors.mslGreen, Modifier.weight(1f)) { onFairwayHit(true) }
            ChoiceButton("Miss", fairwayHit == false, MSLColors.mslRed, Modifier.weight(1f)) { onFairwayHit(false) }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Approach to green
    StatCard(title = "APPROACH TO GREEN", clubLabel = "Club", selectedClub = approachClub, clubs = clubs, onSelectClub = onApproachClub) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            ChoiceButton("Green", greenHit == true, MSLColors.mslGreen, Modifier.weight(1f)) { onGreenHit(true) }
            ChoiceButton("Miss", greenHit == false, MSLColors.mslRed, Modifier.weight(1f)) { onGreenHit(false) }
        }
        if (greenHit == false) {
            Spacer(Modifier.height(10.dp))
            Text("Missed…", fontSize = 14.sp, color = LabelGrey)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("left" to "Left", "right" to "Right", "short" to "Short", "long" to "Long").forEach { (id, label) ->
                    DirectionChip(label, missDir == id, Modifier.weight(1f)) { onMissDir(id) }
                }
            }
        }
    }

    Spacer(Modifier.height(12.dp))

    // Steppers
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        StatStepper("Putts", null, putts, onPutts)
        ThinDivider()
        StatStepper("Greenside bunker shots", "Shots played from a bunker by the green", bunker, onBunker)
        ThinDivider()
        StatStepper("Penalties", "Penalty strokes on the hole", penalties, onPenalties)
    }
    Spacer(Modifier.height(24.dp))
}

@Composable
private fun StatCard(
    title: String,
    clubLabel: String,
    selectedClub: String?,
    clubs: List<ClubType>,
    onSelectClub: (String?) -> Unit,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LabelGrey, modifier = Modifier.weight(1f))
            ClubPickerChip(clubLabel, selectedClub, clubs, onSelectClub)
        }
        content()
    }
}

@Composable
private fun ClubPickerChip(label: String, selected: String?, clubs: List<ClubType>, onSelect: (String?) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    Box {
        Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFFEDEDF0),
            modifier = Modifier.clickable(enabled = clubs.isNotEmpty()) { expanded = true }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(label.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, color = LabelGrey)
                Spacer(Modifier.width(6.dp))
                Text(selected ?: "—", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF212121))
                Icon(Icons.Default.ArrowDropDown, null, tint = LabelGrey, modifier = Modifier.size(18.dp))
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            clubs.map { it.category }.distinct().forEach { category ->
                Text(
                    category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LabelGrey,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                clubs.filter { it.category == category }.forEach { club ->
                    DropdownMenuItem(
                        text = { Text(club.name + if (selected == club.code) "  ✓" else "") },
                        onClick = { onSelect(club.code); expanded = false }
                    )
                }
            }
            if (selected != null) {
                DropdownMenuItem(
                    text = { Text("Clear", color = MSLColors.mslRed) },
                    onClick = { onSelect(null); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun ChoiceButton(text: String, selected: Boolean, selectedColor: Color, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (selected) selectedColor else Color(0xFFE0E0E3)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) selectedColor else Color.White,
            contentColor = if (selected) Color.White else DarkText
        )
    ) {
        Text(text, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun DirectionChip(label: String, selected: Boolean, modifier: Modifier, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(44.dp),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.5.dp, if (selected) MSLColors.mslBlue else Color(0xFFE0E0E3)),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MSLColors.mslBlue else Color.White,
            contentColor = if (selected) Color.White else DarkText
        )
    ) {
        Text(label, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
    }
}

@Composable
private fun StatStepper(label: String, desc: String?, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = DarkText)
            if (desc != null) Text(desc, fontSize = 12.sp, color = LabelGrey)
        }
        StepButton("−") { if (value > 0) onChange(value - 1) }
        Text(
            "$value",
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = DarkText,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(min = 28.dp).padding(horizontal = 4.dp)
        )
        StepButton("+") { onChange(value + 1) }
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(50),
        color = Color(0xFFEDEDF0),
        modifier = Modifier.size(34.dp).clickable { onClick() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(symbol, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = DarkText)
        }
    }
}

@Composable
private fun ThinDivider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFEFEFF2)))
}

// ---------------------------------------------------------------------------
// Insights tab
// ---------------------------------------------------------------------------

@Composable
private fun InsightsTab(par: Int, state: HoleStatsViewModel.InsightsUiState) {
    when {
        state.isLoading -> {
            Box(modifier = Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MSLColors.mslBlue)
            }
        }
        state.error != null -> {
            InfoCard { Text(state.error, color = LabelGrey, fontSize = 14.sp) }
        }
        state.data == null || state.data.yourRoundsCount == 0 -> {
            InfoCard {
                Text("No history on this hole yet", fontWeight = FontWeight.SemiBold, color = DarkText)
                Spacer(Modifier.height(4.dp))
                Text("Play this hole in a submitted round to build your stats.", fontSize = 13.sp, color = LabelGrey)
            }
        }
        else -> InsightsContent(par, state.data)
    }
}

@Composable
private fun InsightsContent(par: Int, data: HoleInsights) {
    // Your averages
    InfoCard {
        Text("YOUR SCORING", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LabelGrey)
        Spacer(Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            StatBlock("Avg", data.yourAvg?.let { fmt1(it) } ?: "—", Modifier.weight(1f))
            StatBlock("Best", data.yourBest?.toString() ?: "—", Modifier.weight(1f))
            StatBlock("Rounds", data.yourRoundsCount.toString(), Modifier.weight(1f))
        }
    }

    // Last 8
    if (data.history.isNotEmpty()) {
        Spacer(Modifier.height(12.dp))
        InfoCard {
            Text("LAST ${data.history.size} ON THIS HOLE", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LabelGrey)
            Spacer(Modifier.height(12.dp))
            Last8Chart(data.history)
        }
    }

    // Distribution
    data.distribution?.let { dist ->
        if (dist.total > 0) {
            Spacer(Modifier.height(12.dp))
            InfoCard {
                Text("SCORE MIX", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LabelGrey)
                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    DistBlock("Eagle+", dist.eagle, cEagle)
                    DistBlock("Birdie", dist.birdie, cBirdie)
                    DistBlock("Par", dist.par, cLevel)
                    DistBlock("Bogey", dist.bogey, cBogey)
                    DistBlock("Dbl+", dist.doublePlus, cDoublePlus)
                }
            }
        }
    }

    // Field today
    Spacer(Modifier.height(12.dp))
    InfoCard {
        Text("FIELD TODAY", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LabelGrey)
        Spacer(Modifier.height(10.dp))
        val gross = data.fieldGross
        if (gross != null && gross.count > 0) {
            Text(
                "Gross avg ${fmt1(gross.avg)}",
                fontSize = 17.sp, fontWeight = FontWeight.Bold, color = DarkText
            )
            Text(
                "handicap ${fmt1(gross.hcpLow)}–${fmt1(gross.hcpHigh)} · ${gross.count} golfer${if (gross.count == 1) "" else "s"}" +
                    if (gross.widened) " (widened)" else "",
                fontSize = 12.sp, color = LabelGrey
            )
        } else {
            Text("No comparable golfers today", fontSize = 13.sp, color = LabelGrey)
        }
        val net = data.fieldNet
        if (net != null && net.count > 0) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Net avg ${fmt1(net.avg)}",
                fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = DarkText
            )
            Text("all ${net.count} golfer${if (net.count == 1) "" else "s"} today (normalised)", fontSize = 12.sp, color = LabelGrey)
        }
    }
    Spacer(Modifier.height(24.dp))
}

private val Last8TrackHeight = 52.dp

@Composable
private fun Last8Chart(history: List<HoleInsightsItem>) {
    // Oldest -> newest, so the most recent round is on the right.
    val items = history.reversed()
    val maxStrokes = (items.maxOfOrNull { it.strokes } ?: 1).coerceAtLeast(1)
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Top
    ) {
        items.forEach { item ->
            val frac = item.strokes.toFloat() / maxStrokes.toFloat()
            val toPar = item.strokes - item.par
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("${item.strokes}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = DarkText)
                Spacer(Modifier.height(3.dp))
                // Fixed-height track: every column is identical, the bar grows from the bottom,
                // so a tall bar can never push the date out of view.
                Box(
                    modifier = Modifier.fillMaxWidth().height(Last8TrackHeight),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height((frac * Last8TrackHeight.value).dp.coerceAtLeast(4.dp))
                            .background(scoreColor(toPar), RoundedCornerShape(4.dp))
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(shortDate(item.date), fontSize = 9.sp, color = LabelGrey, maxLines = 1)
            }
        }
    }
}

@Composable
private fun StatBlock(label: String, value: String, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Text(label, fontSize = 12.sp, color = LabelGrey)
    }
}

@Composable
private fun DistBlock(label: String, count: Int, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(modifier = Modifier.size(10.dp).background(color, RoundedCornerShape(50)))
        Spacer(Modifier.height(4.dp))
        Text("$count", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = DarkText)
        Text(label, fontSize = 10.sp, color = LabelGrey)
    }
}

@Composable
private fun InfoCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardBg, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) { content() }
}

// ---------------------------------------------------------------------------
// Segmented control
// ---------------------------------------------------------------------------

@Composable
private fun SegmentedControl(options: List<String>, selectedIndex: Int, onSelected: (Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFE4E4E9), RoundedCornerShape(10.dp))
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(36.dp)
                    .background(if (selected) Color.White else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { onSelected(index) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    fontSize = 15.sp,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                    color = if (selected) MSLColors.mslBlue else LabelGrey
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Helpers
// ---------------------------------------------------------------------------

private fun scoreColor(toPar: Int): Color = when {
    toPar <= -2 -> cEagle
    toPar == -1 -> cBirdie
    toPar == 0 -> cLevel
    toPar == 1 -> cBogey
    else -> cDoublePlus
}

private fun fmt1(value: Double): String = ((Math.round(value * 10.0)) / 10.0).toString()

private val shortDateFmt = DateTimeFormatter.ofPattern("MMM d")

private fun shortDate(iso: String?): String {
    if (iso.isNullOrBlank()) return ""
    return try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(shortDateFmt)
    } catch (e: Exception) {
        iso.take(10)
    }
}
