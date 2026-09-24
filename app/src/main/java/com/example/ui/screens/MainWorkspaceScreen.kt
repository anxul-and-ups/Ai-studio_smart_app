package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.NoteEntity
import com.example.data.preferences.AppPreferences
import com.example.data.repository.NoteRepository
import com.example.ui.components.ExportDialog
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.NeuIconButton
import com.example.ui.components.PinLockDialog
import com.example.ui.theme.CategoryApiColor
import com.example.ui.theme.CategoryCodeColor
import com.example.ui.theme.CategoryGeneralColor
import com.example.ui.theme.CategoryMediaColor
import com.example.ui.theme.CategoryPersonalColor
import com.example.ui.theme.CrimsonPrimary
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun MainWorkspaceScreen(
    repository: NoteRepository,
    preferences: AppPreferences,
    initialFolder: String = "All Notes",
    isDarkMode: Boolean,
    onToggleDarkMode: () -> Unit,
    onOpenSidebar: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenAiChat: () -> Unit,
    onOpenNote: (NoteEntity) -> Unit,
    onCreateNote: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()

    val blurApis by preferences.blurApis.collectAsState()
    val lockPin by preferences.lockPin.collectAsState()
    val lockedFolders by preferences.lockedFolders.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var isDeepSearch by remember { mutableStateOf(false) } // PART A Item 5: Deep Search toggle
    var selectedFolder by remember(initialFolder) { mutableStateOf(initialFolder) }

    // Telegram Long-Press Action Sheet state (PART A Item 2)
    var longPressedNote by remember { mutableStateOf<NoteEntity?>(null) }
    var longPressedFolder by remember { mutableStateOf<String?>(null) }
    var noteToExport by remember { mutableStateOf<NoteEntity?>(null) }

    // PIN lock prompt for protected notes
    var pendingLockedNote by remember { mutableStateOf<NoteEntity?>(null) }
    var unlockedFoldersThisSession by remember { mutableStateOf(setOf<String>()) }
    var pendingFolderToOpen by remember { mutableStateOf<String?>(null) }

    fun openFolder(folderName: String) {
        val requiresLock = preferences.isFolderLocked(folderName)
        if (requiresLock && folderName !in unlockedFoldersThisSession) {
            pendingFolderToOpen = folderName
        } else {
            selectedFolder = folderName
        }
    }

    // Observe active notes
    val allNotes by repository.allActiveNotes.collectAsState(initial = emptyList())

    val filteredNotes = remember(allNotes, selectedFolder, searchQuery, isDeepSearch) {
        allNotes.filter { note ->
            if (note.isTrash) return@filter false

            // Folder Filter
            val matchesFolder = when (selectedFolder) {
                "All Notes" -> true
                "Favorites" -> note.isFavorite
                "APIs Keys" -> note.category == "API"
                "Code" -> note.category == "Code"
                "Media" -> note.category == "Media"
                "Personal" -> note.category == "Personal"
                else -> note.folder == selectedFolder
            }

            if (!matchesFolder) return@filter false

            // Search Query Filter
            if (searchQuery.isBlank()) {
                true
            } else {
                if (isDeepSearch) {
                    // Deep search = Title or Content
                    note.title.contains(searchQuery, ignoreCase = true) ||
                            note.content.contains(searchQuery, ignoreCase = true)
                } else {
                    // Normal search = Title only
                    note.title.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }

    val folderChips = listOf("All Notes", "Favorites", "APIs Keys", "Code", "Media", "Personal")

    fun exportFolderAsZip(folderName: String) {
        coroutineScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val notesInFolder = allNotes.filter {
                    when (folderName) {
                        "All Notes" -> true
                        "Favorites" -> it.isFavorite
                        "APIs Keys" -> it.category == "API"
                        "Code" -> it.category == "Code"
                        "Media" -> it.category == "Media"
                        "Personal" -> it.category == "Personal"
                        else -> it.folder == folderName
                    }
                }

                val exportDir = File(context.filesDir, "exports").apply { mkdirs() }
                val zipFile = File(exportDir, "${folderName.replace(" ", "_")}_notes.zip.txt")
                ZipOutputStream(FileOutputStream(zipFile)).use { zos ->
                    notesInFolder.forEachIndexed { index, note ->
                        val fileName = "${index + 1}_${note.title.replace("[^a-zA-Z0-9.-]".toRegex(), "_")}.txt"
                        val entry = ZipEntry(fileName)
                        zos.putNextEntry(entry)
                        val noteBody = "TITLE: ${note.title}\nCATEGORY: ${note.category}\nDATE: ${Date(note.updatedAt)}\n\n${note.content}"
                        zos.write(noteBody.toByteArray())
                        zos.closeEntry()
                    }
                }

                launch(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Exported ${notesInFolder.size} notes to ${zipFile.name}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                launch(kotlinx.coroutines.Dispatchers.Main) {
                    Toast.makeText(context, "Export failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    GlassBackground(isDarkMode = isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .imePadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp)
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeuIconButton(
                            icon = Icons.Default.Menu,
                            contentDescription = "Open Sidebar",
                            isDarkMode = isDarkMode,
                            size = 40.dp,
                            iconSize = 20.dp,
                            tint = if (isDarkMode) Color.White else Color(0xFF222222),
                            onClick = onOpenSidebar
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = "AU NOTES",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CrimsonPrimary,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "${filteredNotes.size} notes in $selectedFolder",
                                fontSize = 11.sp,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.6f) else Color(0xFF666666),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        // Dark/Light Mode Switcher using SVG Icons (PART J)
                        NeuIconButton(
                            painter = painterResource(if (isDarkMode) R.drawable.ic_theme_sun else R.drawable.ic_theme_moon),
                            contentDescription = "Toggle Day/Night Mode",
                            isDarkMode = isDarkMode,
                            size = 38.dp,
                            iconSize = 18.dp,
                            tint = if (isDarkMode) Color(0xFFFFCA72) else Color(0xFF37474F),
                            onClick = onToggleDarkMode
                        )

                        NeuIconButton(
                            painter = painterResource(R.drawable.ic_settings_gear),
                            contentDescription = "Settings",
                            isDarkMode = isDarkMode,
                            size = 38.dp,
                            iconSize = 18.dp,
                            tint = CrimsonPrimary,
                            onClick = onOpenSettings
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Search Bar with Deep Search Toggle (PART A Item 5)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                text = if (isDeepSearch) "Deep Search (content & title)..." else "Search title...",
                                fontSize = 13.sp,
                                color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(20.dp))
                        },
                        trailingIcon = {
                            // Deep Search Toggle Chip
                            Row(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isDeepSearch) CrimsonPrimary else (if (isDarkMode) Color(0x33FFFFFF) else Color(0x1F000000)))
                                    .clickable { isDeepSearch = !isDeepSearch }
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Deep",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDeepSearch) Color.White else (if (isDarkMode) Color.White.copy(0.7f) else Color.DarkGray)
                                )
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = if (isDarkMode) Color(0x22FFFFFF) else Color(0x1F718096),
                            focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkMode) Color.White else Color.Black
                        ),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Horizontal Folder Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    folderChips.forEach { folder ->
                        val isSelected = selectedFolder == folder
                        val isFolderLocked = lockedFolders.contains(folder)

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    if (isSelected) CrimsonPrimary
                                    else (if (isDarkMode) Color(0x1AFFFFFF) else Color(0x0F000000))
                                )
                                .border(
                                    1.dp,
                                    if (isSelected) CrimsonPrimary else (if (isDarkMode) Color(0x26FFFFFF) else Color(0x1F718096)),
                                    RoundedCornerShape(12.dp)
                                )
                                .combinedClickable(
                                    onClick = { openFolder(folder) },
                                    onLongClick = {
                                        longPressedFolder = folder
                                    }
                                )
                                .padding(horizontal = 12.dp, vertical = 7.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isFolderLocked) {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked Folder",
                                        tint = if (isSelected) Color.White else CrimsonPrimary,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = folder,
                                    fontSize = 12.5.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else (if (isDarkMode) Color.White.copy(0.85f) else Color(0xFF333333))
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Notes List with Compact Balanced Size (PART A Item 1)
                if (filteredNotes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                painter = painterResource(R.drawable.ic_sticky_note),
                                contentDescription = null,
                                tint = Color.Gray,
                                modifier = Modifier.size(44.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No notes found in $selectedFolder",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Tap '+' to create your first note",
                                color = CrimsonPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        items(filteredNotes, key = { it.id }) { note ->
                            CompactNoteCard(
                                note = note,
                                isDarkMode = isDarkMode,
                                blurApis = blurApis,
                                onOpen = {
                                    if (note.isLocked) {
                                        pendingLockedNote = note
                                    } else {
                                        onOpenNote(note)
                                    }
                                },
                                onLongPress = {
                                    longPressedNote = note
                                },
                                onToggleFavorite = {
                                    coroutineScope.launch {
                                        repository.toggleFavorite(note.id, note.isFavorite)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // Floating Buttons Row: Floating AU Bot + Create Note FAB
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(20.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Floating AU AI Bot with custom sphere mascot icon (PART F Item 2)
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .shadow(8.dp, CircleShape)
                            .clip(CircleShape)
                            .background(Color.Black)
                            .clickable { onOpenAiChat() }
                    ) {
                        Image(
                            painter = painterResource(R.drawable.au_bot_icon_1790271144581),
                            contentDescription = "AU AI Assistant",
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // Create Note FAB
                    FloatingActionButton(
                        onClick = onCreateNote,
                        containerColor = CrimsonPrimary,
                        contentColor = Color.White,
                        shape = CircleShape,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Create Note", modifier = Modifier.size(28.dp))
                    }
                }
            }
        }
    }

    // Telegram-Style Long-Press Bottom Action Sheet for Notes (PART A Item 2)
    longPressedNote?.let { note ->
        ModalBottomSheet(
            onDismissRequest = { longPressedNote = null },
            containerColor = if (isDarkMode) Color(0xFF1E222B) else Color(0xFFF6F8FB),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = note.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Quick Actions (Telegram Style)",
                    fontSize = 11.5.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDarkMode) Color(0x22FFFFFF) else Color(0x1F000000))
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Reorder / Pin Note
                TelegramActionItem(
                    icon = painterResource(R.drawable.ic_edit_note),
                    title = if (note.isPinned) "Unpin Note" else "Reorder / Pin to Top",
                    isDarkMode = isDarkMode,
                    onClick = {
                        coroutineScope.launch {
                            repository.togglePin(note.id, note.isPinned)
                            longPressedNote = null
                            Toast.makeText(context, if (note.isPinned) "Unpinned" else "Pinned to top", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 2. Lock / Unlock Note with SVG
                TelegramActionItem(
                    icon = painterResource(if (note.isLocked) R.drawable.ic_security_unlock else R.drawable.ic_security_lock),
                    title = if (note.isLocked) "Unlock Note" else "Lock Note with Passcode",
                    isDarkMode = isDarkMode,
                    onClick = {
                        coroutineScope.launch {
                            repository.toggleLock(note.id, note.isLocked)
                            longPressedNote = null
                            Toast.makeText(context, if (note.isLocked) "Note Unlocked" else "Note Locked", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                // 3. Export as ZIP / File
                TelegramActionItem(
                    icon = painterResource(R.drawable.ic_export_download),
                    title = "Export Note",
                    isDarkMode = isDarkMode,
                    onClick = {
                        noteToExport = note
                        longPressedNote = null
                    }
                )

                // 4. Delete Note
                TelegramActionItem(
                    icon = painterResource(R.drawable.ic_recycle_bin),
                    title = "Delete (Move to Recycle Bin)",
                    isDarkMode = isDarkMode,
                    isDestructive = true,
                    onClick = {
                        coroutineScope.launch {
                            repository.moveToTrash(note.id)
                            longPressedNote = null
                            Toast.makeText(context, "Moved '${note.title}' to Recycle Bin", Toast.LENGTH_SHORT).show()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Telegram-Style Long-Press Bottom Action Sheet for Folders (PART A Item 2)
    longPressedFolder?.let { folder ->
        ModalBottomSheet(
            onDismissRequest = { longPressedFolder = null },
            containerColor = if (isDarkMode) Color(0xFF1E222B) else Color(0xFFF6F8FB),
            sheetState = rememberModalBottomSheetState()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Folder: $folder",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = CrimsonPrimary
                )
                Text(
                    text = "Folder Management Options",
                    fontSize = 11.5.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = if (isDarkMode) Color(0x22FFFFFF) else Color(0x1F000000))
                Spacer(modifier = Modifier.height(10.dp))

                // 1. Lock / Unlock Folder
                val isLocked = preferences.isFolderLocked(folder)
                TelegramActionItem(
                    icon = painterResource(if (isLocked) R.drawable.ic_security_unlock else R.drawable.ic_security_lock),
                    title = if (isLocked) "Unlock Folder" else "Lock Folder with PIN",
                    isDarkMode = isDarkMode,
                    onClick = {
                        preferences.toggleFolderLock(folder)
                        longPressedFolder = null
                        Toast.makeText(context, if (isLocked) "Folder Unlocked" else "Folder Locked with PIN", Toast.LENGTH_SHORT).show()
                    }
                )

                // 2. Export Folder as ZIP (.txt extension)
                TelegramActionItem(
                    icon = painterResource(R.drawable.ic_export_download),
                    title = "Export as ZIP (${folder})",
                    isDarkMode = isDarkMode,
                    onClick = {
                        exportFolderAsZip(folder)
                        longPressedFolder = null
                    }
                )

                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }

    // Export Dialog (PART G)
    noteToExport?.let { note ->
        ExportDialog(
            note = note,
            isDarkMode = isDarkMode,
            onDismiss = { noteToExport = null }
        )
    }

    // PIN dialog for unlocking note
    pendingLockedNote?.let { note ->
        PinLockDialog(
            correctPin = lockPin,
            isDarkMode = isDarkMode,
            onDismiss = { pendingLockedNote = null },
            onUnlocked = {
                pendingLockedNote = null
                onOpenNote(note)
            }
        )
    }

    // PIN dialog for unlocking folder
    pendingFolderToOpen?.let { folder ->
        PinLockDialog(
            correctPin = lockPin,
            isDarkMode = isDarkMode,
            onDismiss = { pendingFolderToOpen = null },
            onUnlocked = {
                unlockedFoldersThisSession = unlockedFoldersThisSession + folder
                selectedFolder = folder
                pendingFolderToOpen = null
            }
        )
    }
}

@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun CompactNoteCard(
    note: NoteEntity,
    isDarkMode: Boolean,
    blurApis: Boolean,
    onOpen: () -> Unit,
    onLongPress: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val categoryColor = when (note.category) {
        "API" -> CategoryApiColor
        "Code" -> CategoryCodeColor
        "Media" -> CategoryMediaColor
        "Personal" -> CategoryPersonalColor
        else -> CategoryGeneralColor
    }

    val displayContent = if (note.category == "API" && blurApis) {
        "•••••••••••••••••••• (API Key Blurred)"
    } else {
        note.content.lines().firstOrNull { it.isNotBlank() } ?: ""
    }

    val formattedTime = remember(note.updatedAt) {
        SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date(note.updatedAt))
    }

    // Compact Card with height ~75dp
    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onOpen,
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(14.dp),
        isDarkMode = isDarkMode,
        elevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Category Indicator bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(categoryColor)
            )

            Spacer(modifier = Modifier.width(10.dp))

            // Main Title & Subtitle Info
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (note.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    if (note.isLocked) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Locked",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    Text(
                        text = note.title,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkMode) Color.White else Color(0xFF111111),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = displayContent,
                    fontSize = 12.sp,
                    color = if (isDarkMode) Color.White.copy(alpha = 0.65f) else Color(0xFF555555),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = 10.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "• ${note.category}",
                        fontSize = 10.sp,
                        color = categoryColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Favorite Icon Button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(34.dp)
            ) {
                Icon(
                    imageVector = if (note.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                    contentDescription = "Favorite",
                    tint = if (note.isFavorite) Color(0xFFFFCA28) else Color.Gray.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun TelegramActionItem(
    icon: androidx.compose.ui.graphics.painter.Painter,
    title: String,
    isDarkMode: Boolean,
    isDestructive: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = icon,
            contentDescription = title,
            tint = if (isDestructive) Color.Red else CrimsonPrimary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) Color.Red else (if (isDarkMode) Color.White else Color(0xFF222222))
        )
    }
}
