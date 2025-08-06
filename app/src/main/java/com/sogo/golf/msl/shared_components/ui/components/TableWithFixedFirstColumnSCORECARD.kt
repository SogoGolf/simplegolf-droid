package com.sogo.golf.msl.shared_components.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun TableWithFixedFirstColumnSCORECARD(
    columnCount: Int,
    cellWidth: Dp,
    data: List<List<@Composable () -> Unit>>,
    modifier: Modifier = Modifier,
    headerBackgroundColor: Color = MaterialTheme.colorScheme.primary,
    cellBackgroundColor: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outline
) {
    val cellHeight = 40.dp

    Row(modifier = modifier) {
        Column {
            data.forEachIndexed { rowIndex, row ->
                Box(
                    modifier = Modifier
                        .width(cellWidth)
                        .height(cellHeight)
                        .background(
                            if (rowIndex == 0) headerBackgroundColor else cellBackgroundColor
                        )
                        .border(0.5.dp, borderColor)
                ) {
                    row.firstOrNull()?.invoke()
                }
            }
        }

        LazyRow {
            itemsIndexed((1 until columnCount).toList()) { columnIndex, _ ->
                Column {
                    data.forEachIndexed { rowIndex, row ->
                        Box(
                            modifier = Modifier
                                .width(cellWidth)
                                .height(cellHeight)
                                .background(
                                    if (rowIndex == 0) headerBackgroundColor else cellBackgroundColor
                                )
                                .border(0.5.dp, borderColor)
                        ) {
                            row.getOrNull(columnIndex + 1)?.invoke()
                        }
                    }
                }
            }
        }
    }
}
