package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.GlassCard
import com.example.ui.theme.CrimsonPrimary

@Composable
fun TableEditorScreen(
    initialTableData: String,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSaveTable: (String) -> Unit
) {
    // Parse existing Markdown/CSV/JSON table data or default to 3 rows x 2 cols
    val tableRows = remember {
        mutableStateListOf<MutableList<String>>().apply {
            val parsed = parseTableData(initialTableData)
            if (parsed.isNotEmpty() && parsed[0].isNotEmpty()) {
                parsed.forEach { row ->
                    add(row.toMutableList())
                }
            } else {
                // Default 3 rows x 2 columns matching user reference screenshot
                add(mutableListOf("Header 1", "Header 2"))
                add(mutableListOf("Row 1, Cell 1", "Row 1, Cell 2"))
                add(mutableListOf("Row 2, Cell 1", "Row 2, Cell 2"))
            }
        }
    }

    var selectedColIndex by remember { mutableIntStateOf(-1) }
    var selectedRowIndex by remember { mutableIntStateOf(-1) }

    val bgColor = if (isDarkMode) Color(0xFF0A0B0E) else Color(0xFFF6F8FB)
    val gridBorderColor = if (isDarkMode) Color.White.copy(alpha = 0.35f) else Color(0xFFCCCCCC)
    val textColor = if (isDarkMode) Color.White else Color(0xFF111111)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header: "< Table" and "✓" (Checkmark)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onBack() }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = textColor,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Table",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                }

                IconButton(
                    onClick = {
                        val serialized = serializeTable(tableRows)
                        onSaveTable(serialized)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Table",
                        tint = CrimsonPrimary,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Quick Table Controls Toolbar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isDarkMode = isDarkMode,
                    onClick = {
                        // Add Column
                        val colCount = if (tableRows.isNotEmpty()) tableRows[0].size + 1 else 1
                        tableRows.forEachIndexed { index, row ->
                            row.add(if (index == 0) "Col $colCount" else "")
                        }
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Column", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    }
                }

                GlassCard(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    isDarkMode = isDarkMode,
                    onClick = {
                        // Add Row
                        val cols = if (tableRows.isNotEmpty()) tableRows[0].size else 2
                        val newRow = MutableList(cols) { "" }
                        tableRows.add(newRow)
                    }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Row", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = textColor)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Main Table Editing Area (Scrollable in both dimensions)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                val horizontalScroll = rememberScrollState()
                val verticalScroll = rememberScrollState()

                Column(
                    modifier = Modifier
                        .verticalScroll(verticalScroll)
                        .horizontalScroll(horizontalScroll)
                ) {
                    // Column handles (top)
                    Row(modifier = Modifier.padding(start = 36.dp)) {
                        val numCols = if (tableRows.isNotEmpty()) tableRows[0].size else 0
                        for (colIdx in 0 until numCols) {
                            Box(
                                modifier = Modifier
                                    .width(140.dp)
                                    .padding(bottom = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedColIndex == colIdx) CrimsonPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                                        .clickable { selectedColIndex = colIdx }
                                        .padding(horizontal = 10.dp, vertical = 2.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "Column Options",
                                        tint = if (selectedColIndex == colIdx) CrimsonPrimary else textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = selectedColIndex == colIdx,
                                    onDismissRequest = { selectedColIndex = -1 }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Column", color = Color.Red) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                        onClick = {
                                            if (numCols > 1) {
                                                tableRows.forEach { it.removeAt(colIdx) }
                                            }
                                            selectedColIndex = -1
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Table rows with left-side row handles
                    tableRows.forEachIndexed { rowIdx, row ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Row handle (left)
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(48.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selectedRowIndex == rowIdx) CrimsonPrimary.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f))
                                        .clickable { selectedRowIndex = rowIdx }
                                        .padding(horizontal = 2.dp, vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.MoreVert,
                                        contentDescription = "Row Options",
                                        tint = if (selectedRowIndex == rowIdx) CrimsonPrimary else textColor.copy(alpha = 0.7f),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                DropdownMenu(
                                    expanded = selectedRowIndex == rowIdx,
                                    onDismissRequest = { selectedRowIndex = -1 }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("Delete Row", color = Color.Red) },
                                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = Color.Red) },
                                        onClick = {
                                            if (tableRows.size > 1) {
                                                tableRows.removeAt(rowIdx)
                                            }
                                            selectedRowIndex = -1
                                        }
                                    )
                                }
                            }

                            // Table Cells in this row
                            row.forEachIndexed { colIdx, cellValue ->
                                Box(
                                    modifier = Modifier
                                        .width(140.dp)
                                        .height(48.dp)
                                        .border(0.7.dp, gridBorderColor)
                                        .background(
                                            if (rowIdx == 0) {
                                                if (isDarkMode) Color(0xFF1E222B) else Color(0xFFE2E8F0)
                                            } else {
                                                Color.Transparent
                                            }
                                        )
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    BasicTextField(
                                        value = cellValue,
                                        onValueChange = { newVal ->
                                            row[colIdx] = newVal
                                        },
                                        textStyle = TextStyle(
                                            color = textColor,
                                            fontSize = if (rowIdx == 0) 14.sp else 13.sp,
                                            fontWeight = if (rowIdx == 0) FontWeight.Bold else FontWeight.Normal
                                        ),
                                        cursorBrush = SolidColor(CrimsonPrimary),
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseTableData(raw: String): List<List<String>> {
    if (raw.isBlank()) return emptyList()
    val lines = raw.lines().filter { it.trim().isNotBlank() }
    val result = mutableListOf<List<String>>()

    for (line in lines) {
        val trimmed = line.trim()
        if (trimmed.startsWith("|") && trimmed.endsWith("|")) {
            // Markdown table line
            val cells = trimmed.drop(1).dropLast(1).split("|").map { it.trim() }
            // Ignore markdown separator row like |---|---|
            if (cells.none { it.matches(Regex("^-+$")) }) {
                result.add(cells)
            }
        } else if (trimmed.contains(",")) {
            result.add(trimmed.split(",").map { it.trim() })
        } else if (trimmed.contains("\t")) {
            result.add(trimmed.split("\t").map { it.trim() })
        }
    }
    return result
}

private fun serializeTable(rows: List<List<String>>): String {
    if (rows.isEmpty()) return ""
    val sb = StringBuilder()
    val numCols = rows.maxOfOrNull { it.size } ?: 0

    // Header row
    val header = rows.firstOrNull() ?: emptyList()
    sb.append("|")
    for (i in 0 until numCols) {
        sb.append(" ").append(header.getOrElse(i) { "Col ${i + 1}" }.ifBlank { " " }).append(" |")
    }
    sb.append("\n|")
    for (i in 0 until numCols) {
        sb.append("---|")
    }
    sb.append("\n")

    // Data rows
    for (r in 1 until rows.size) {
        val row = rows[r]
        sb.append("|")
        for (i in 0 until numCols) {
            sb.append(" ").append(row.getOrElse(i) { "" }.ifBlank { " " }).append(" |")
        }
        sb.append("\n")
    }
    return sb.toString().trimEnd()
}
