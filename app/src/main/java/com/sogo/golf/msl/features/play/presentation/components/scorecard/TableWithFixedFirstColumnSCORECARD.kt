package com.sogo.golf.msl.features.play.presentation.components.scorecard

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.sogo.golf.msl.ui.theme.MSLColors.mslBlack

@Composable
fun <T> TableWithFixedFirstColumnSCORECARD(
    columnCount: Int,
    cellWidth: (index: Int) -> Dp,
    firstColumnWidth: (index: Int) -> Dp,
    data: List<T>,
    modifier: Modifier = Modifier,
    headerCellContent: @Composable (index: Int) -> Unit,
    cellContent: @Composable (index: Int, item: T) -> Unit,
) {
    Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
        // Fixed First Column
        Column(
            modifier = Modifier
                .width(firstColumnWidth(0))
                .fillMaxHeight()
                .padding(0.dp)
        ) {
            // Header Cell for the First Column
            Surface(
                border = BorderStroke(0.5.dp, Color.LightGray),
                color = Color.White, //forces a white background even in dark mode
                contentColor = mslBlack,
                modifier = Modifier.fillMaxWidth()
            ) {
                headerCellContent(0)
            }

            // Data Cells for the First Column
            data.forEach { item ->
                Surface(
                    border = BorderStroke(0.5.dp, Color.LightGray),
                    color = Color.White, //forces a white background even in dark mode
                    contentColor = mslBlack,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    cellContent(0, item)
                }
            }
        }

        // Scrollable Columns for Remaining Data
        LazyRow(
            modifier = Modifier
                .weight(1f)
                .padding(0.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            items((1 until columnCount).toList()) { columnIndex ->
                Column {
                    // Header Cell for the Current Column
                    Surface(
                        border = BorderStroke(0.5.dp, Color.LightGray),
                        color = Color.White, //forces a white background even in dark mode
                        contentColor = mslBlack,
                        modifier = Modifier.width(cellWidth(columnIndex))
                    ) {
                        headerCellContent(columnIndex)
                    }

                    // Data Cells for the Current Column
                    data.forEach { item ->
                        Surface(
                            border = BorderStroke(0.5.dp, Color.LightGray),
                            color = Color.White, //forces a white background even in dark mode
                            contentColor = mslBlack,
                            modifier = Modifier.width(cellWidth(columnIndex))
                        ) {
                            cellContent(columnIndex, item)
                        }
                    }
                }
            }
        }
    }

}
