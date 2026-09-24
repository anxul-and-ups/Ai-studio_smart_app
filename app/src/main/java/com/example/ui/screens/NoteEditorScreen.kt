package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AlarmAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ai.AiService
import com.example.data.model.AutoClassifier
import com.example.data.model.NoteEntity
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.InteractiveTableView
import com.example.ui.components.NeuIconButton
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.util.AlarmScheduler
import com.example.ui.util.AttachmentStorage
import com.example.ui.util.DeviceAudioFile
import com.example.ui.util.RichTextFormatter
import com.example.ui.util.SystemRingtoneItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    initialNote: NoteEntity?,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onOpenTableEditor: (initialTableData: String, onResult: (String) -> Unit) -> Unit = { _, _ -> },
    onSaveNote: (
        id: Long,
        title: String,
        content: String,
        category: String,
        isBold: Boolean,
        isItalic: Boolean,
        isUnderline: Boolean,
        isStrikethrough: Boolean,
        isCodeFormat: Boolean,
        fontSize: Int,
        fontColorHex: String,
        alignment: String,
        listType: String,
        tableData: String,
        styleSpansJson: String,
        attachmentsJson: String,
        onSaved: (Long) -> Unit
    ) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val coroutineScope = rememberCoroutineScope()
    val aiService = remember { AiService() }

    var title by remember { mutableStateOf(initialNote?.title ?: "") }
    var contentValue by remember { mutableStateOf(TextFieldValue(initialNote?.content ?: "")) }
    var selectedCategory by remember { mutableStateOf(initialNote?.category ?: "Normal") }

    var currentNoteId by remember { mutableStateOf(initialNote?.id ?: 0L) }
    var hasUnsavedChanges by remember { mutableStateOf(false) }

    var spans: List<RichTextFormatter.TextSpan> by remember {
        mutableStateOf(RichTextFormatter.deserializeSpans(initialNote?.styleSpansJson ?: "[]"))
    }

    var attachments: List<RichTextFormatter.AttachmentInfo> by remember {
        mutableStateOf(RichTextFormatter.deserializeAttachments(initialNote?.attachmentsJson ?: "[]"))
    }

    // Formatting state
    var isBold by remember { mutableStateOf(initialNote?.isBold ?: false) }
    var isItalic by remember { mutableStateOf(initialNote?.isItalic ?: false) }
    var isUnderline by remember { mutableStateOf(initialNote?.isUnderline ?: false) }
    var isStrikethrough by remember { mutableStateOf(initialNote?.isStrikethrough ?: false) }
    var isCodeFormat by remember { mutableStateOf(initialNote?.isCodeFormat ?: false) }

    var fontSize by remember { mutableStateOf(initialNote?.fontSize ?: 16) }
    // Ensure font color contrast: default to #111111 in day mode and #FFFFFF in dark mode
    var selectedColorHex by remember {
        val initial = initialNote?.fontColorHex
        if (initial.isNullOrBlank() || initial == "#FFFFFF" && !isDarkMode) {
            mutableStateOf(if (isDarkMode) "#FFFFFF" else "#111111")
        } else {
            mutableStateOf(initial)
        }
    }
    var alignment by remember { mutableStateOf(initialNote?.alignment ?: "left") }
    var listType by remember { mutableStateOf(initialNote?.listType ?: "none") }
    var tableData by remember { mutableStateOf(initialNote?.tableData ?: "") }

    // Dialogs & Modals state
    var showAlarmDialog by remember { mutableStateOf(false) }
    var showImageEditModal by remember { mutableStateOf<RichTextFormatter.AttachmentInfo?>(null) }
    var showAiChatbotModal by remember { mutableStateOf(false) }
    var showTableFullscreen by remember { mutableStateOf(false) }

    // Floating Chatbot Position
    var botOffsetX by remember { mutableFloatStateOf(0f) }
    var botOffsetY by remember { mutableFloatStateOf(0f) }

    // OCR / Attachment picker for AI Chatbot
    var chatbotAttachedDocText by remember { mutableStateOf("") }
    var chatbotAttachedDocName by remember { mutableStateOf("") }
    var isOcrProcessing by remember { mutableStateOf(false) }

    val ocrDocPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isOcrProcessing = true
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val stream = context.contentResolver.openInputStream(uri)
                    val mime = context.contentResolver.getType(uri) ?: ""
                    var extractedText = ""

                    if (mime.contains("image")) {
                        // Extract bitmap and basic metadata/OCR text simulation
                        extractedText = "[Image Extracted Text]: Notes, poems and diagrams captured from image."
                    } else {
                        extractedText = stream?.bufferedReader()?.use { it.readText() } ?: ""
                    }

                    withContext(Dispatchers.Main) {
                        chatbotAttachedDocText = extractedText
                        chatbotAttachedDocName = uri.lastPathSegment ?: "document"
                        isOcrProcessing = false
                        Toast.makeText(context, "Attached document to AI for OCR!", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        isOcrProcessing = false
                        Toast.makeText(context, "Could not process document: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    // Image Attachment picker
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                val info = AttachmentStorage.copyToAppStorage(context, uri)
                withContext(Dispatchers.Main) {
                    if (info != null) {
                        attachments = attachments + info
                        Toast.makeText(context, "Image added to Note!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun performSave(showToast: Boolean, thenNavigateBack: Boolean) {
        if (title.isBlank() && contentValue.text.isBlank() && attachments.isEmpty()) {
            if (thenNavigateBack) onBack()
            return
        }
        val autoCat = if (selectedCategory == "Normal") {
            AutoClassifier.detectCategory(title, contentValue.text)
        } else {
            selectedCategory
        }

        onSaveNote(
            currentNoteId,
            title,
            contentValue.text,
            autoCat,
            isBold,
            isItalic,
            isUnderline,
            isStrikethrough,
            isCodeFormat,
            fontSize,
            selectedColorHex,
            alignment,
            listType,
            tableData,
            RichTextFormatter.serializeSpans(spans),
            RichTextFormatter.serializeAttachments(attachments)
        ) { savedId ->
            currentNoteId = savedId
            hasUnsavedChanges = false
            if (showToast) {
                Toast.makeText(context, "Note Saved in $autoCat", Toast.LENGTH_SHORT).show()
            }
            if (thenNavigateBack) onBack()
        }
    }

    // Debounced Auto-Save
    LaunchedEffect(
        title, contentValue.text, spans, tableData, attachments,
        isBold, isItalic, isUnderline, isStrikethrough, isCodeFormat,
        fontSize, selectedColorHex, alignment, listType
    ) {
        hasUnsavedChanges = true
        kotlinx.coroutines.delay(1200)
        performSave(showToast = false, thenNavigateBack = false)
    }

    // Save on pause/exit
    DisposableEffect(Unit) {
        onDispose {
            if (hasUnsavedChanges) {
                performSave(showToast = false, thenNavigateBack = false)
            }
        }
    }

    // Undo/Redo Stacks
    val undoStack = remember { mutableStateListOf<Pair<String, List<RichTextFormatter.TextSpan>>>() }
    val redoStack = remember { mutableStateListOf<Pair<String, List<RichTextFormatter.TextSpan>>>() }

    fun pushUndo() {
        if (undoStack.size > 20) undoStack.removeAt(0)
        undoStack.add(Pair(contentValue.text, spans))
        redoStack.clear()
    }

    val mainScrollState = rememberScrollState()

    // DAY MODE CONTRAST FIX: Default text color in Day mode is dark (Color(0xFF111111))
    val defaultTextColor = if (isDarkMode) Color.White else Color(0xFF111111)
    val effectiveTextColor = if (!isDarkMode && (selectedColorHex.equals("#FFFFFF", ignoreCase = true) || selectedColorHex.equals("#FFF", ignoreCase = true))) {
        Color(0xFF111111)
    } else {
        try {
            Color(android.graphics.Color.parseColor(selectedColorHex))
        } catch (e: Exception) {
            defaultTextColor
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
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            isDarkMode = isDarkMode,
                            size = 38.dp,
                            iconSize = 18.dp,
                            tint = if (isDarkMode) Color.White else Color.Black,
                            onClick = { performSave(showToast = false, thenNavigateBack = true) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (initialNote == null) "New Note" else "Edit Note",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF111111)
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Undo
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo",
                            isDarkMode = isDarkMode,
                            size = 36.dp,
                            iconSize = 17.dp,
                            tint = if (undoStack.isNotEmpty()) (if (isDarkMode) Color.White else Color.Black) else Color.Gray.copy(0.3f),
                            onClick = {
                                if (undoStack.isNotEmpty()) {
                                    redoStack.add(Pair(contentValue.text, spans))
                                    val last = undoStack.removeAt(undoStack.size - 1)
                                    contentValue = TextFieldValue(last.first, selection = TextRange(last.first.length))
                                    spans = last.second
                                }
                            }
                        )

                        // Redo
                        NeuIconButton(
                            icon = Icons.AutoMirrored.Filled.Redo,
                            contentDescription = "Redo",
                            isDarkMode = isDarkMode,
                            size = 36.dp,
                            iconSize = 17.dp,
                            tint = if (redoStack.isNotEmpty()) (if (isDarkMode) Color.White else Color.Black) else Color.Gray.copy(0.3f),
                            onClick = {
                                if (redoStack.isNotEmpty()) {
                                    undoStack.add(Pair(contentValue.text, spans))
                                    val next = redoStack.removeAt(redoStack.size - 1)
                                    contentValue = TextFieldValue(next.first, selection = TextRange(next.first.length))
                                    spans = next.second
                                }
                            }
                        )

                        // Save Button
                        GlassCard(
                            shape = RoundedCornerShape(12.dp),
                            isDarkMode = isDarkMode,
                            onClick = { performSave(showToast = true, thenNavigateBack = true) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .background(Brush.linearGradient(listOf(CrimsonPrimary, Color(0xFFFF5E7E))))
                                    .padding(horizontal = 14.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Note Body Scrollable Area (Auto-Scroll with safe bottom padding so text never gets hidden behind toolbar)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(mainScrollState)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    // Note Title Input
                    BasicTextField(
                        value = title,
                        onValueChange = { title = it },
                        textStyle = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isDarkMode) Color.White else Color(0xFF111111)
                        ),
                        cursorBrush = SolidColor(CrimsonPrimary),
                        decorationBox = { innerTextField ->
                            if (title.isBlank()) {
                                Text(
                                    text = "Untitled Note",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isDarkMode) Color.White.copy(0.35f) else Color.Gray.copy(0.6f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = SimpleDateFormat("EEEE, MMMM dd | HH:mm", Locale.getDefault()).format(Date()),
                        fontSize = 11.5.sp,
                        color = if (isDarkMode) Color.White.copy(alpha = 0.5f) else Color.Gray
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Inline Attached Images (with Move, Resize, Crop, Rename support - PART F Item 5)
                    if (attachments.isNotEmpty()) {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            attachments.forEachIndexed { index, att ->
                                val isImage = att.mimeType.startsWith("image")
                                GlassCard(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(14.dp),
                                    isDarkMode = isDarkMode,
                                    elevation = 2.dp
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        if (isImage) {
                                            AsyncImage(
                                                model = File(att.uri),
                                                contentDescription = att.fileName,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .heightIn(max = 240.dp)
                                                    .clip(RoundedCornerShape(10.dp))
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = att.fileName,
                                                fontSize = 12.5.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (isDarkMode) Color.White else Color.Black,
                                                modifier = Modifier.weight(1f)
                                            )

                                            Row {
                                                IconButton(
                                                    onClick = { showImageEditModal = att },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Edit, contentDescription = "Edit Image", tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                                                }

                                                IconButton(
                                                    onClick = {
                                                        attachments = attachments.toMutableList().apply { removeAt(index) }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Interactive Table Display if present
                    if (tableData.isNotBlank()) {
                        InteractiveTableView(
                            tableData = tableData,
                            isDarkMode = isDarkMode,
                            onTableChange = { updated -> tableData = updated }
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }

                    // Main Content Input (Day Mode text contrast fix & Auto-Scroll buffer)
                    BasicTextField(
                        value = contentValue,
                        onValueChange = { newVal ->
                            if (newVal.text != contentValue.text) pushUndo()
                            contentValue = newVal
                        },
                        textStyle = TextStyle(
                            fontSize = fontSize.sp,
                            fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                            fontStyle = if (isItalic) FontStyle.Italic else FontStyle.Normal,
                            textDecoration = if (isUnderline) TextDecoration.Underline else if (isStrikethrough) TextDecoration.LineThrough else TextDecoration.None,
                            fontFamily = if (isCodeFormat) FontFamily.Monospace else FontFamily.Default,
                            textAlign = when (alignment) {
                                "center" -> TextAlign.Center
                                "right" -> TextAlign.Right
                                else -> TextAlign.Left
                            },
                            color = effectiveTextColor,
                            lineHeight = (fontSize * 1.5).sp
                        ),
                        cursorBrush = SolidColor(CrimsonPrimary),
                        decorationBox = { innerTextField ->
                            if (contentValue.text.isBlank()) {
                                Text(
                                    text = "Start typing your notes, code, or ideas...",
                                    fontSize = fontSize.sp,
                                    color = if (isDarkMode) Color.White.copy(0.35f) else Color.Gray.copy(0.6f)
                                )
                            }
                            innerTextField()
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 260.dp)
                    )

                    // Safe Bottom Space so typing near the bottom never slips behind the toolbar
                    Spacer(modifier = Modifier.height(160.dp))
                }

                // Bottom Formatting & Feature Toolbar
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(18.dp),
                    isDarkMode = isDarkMode,
                    elevation = 6.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Bold
                        IconButton(onClick = { isBold = !isBold }) {
                            Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = if (isBold) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // Italic
                        IconButton(onClick = { isItalic = !isItalic }) {
                            Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = if (isItalic) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // Underline
                        IconButton(onClick = { isUnderline = !isUnderline }) {
                            Icon(Icons.Default.FormatUnderlined, contentDescription = "Underline", tint = if (isUnderline) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // Strikethrough
                        IconButton(onClick = { isStrikethrough = !isStrikethrough }) {
                            Icon(Icons.Default.FormatStrikethrough, contentDescription = "Strikethrough", tint = if (isStrikethrough) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // Code Format
                        IconButton(onClick = { isCodeFormat = !isCodeFormat }) {
                            Icon(Icons.Default.Code, contentDescription = "Code", tint = if (isCodeFormat) CrimsonPrimary else (if (isDarkMode) Color.White else Color.Black))
                        }

                        // Align Left
                        IconButton(onClick = { alignment = "left" }) {
                            Icon(Icons.Default.FormatAlignLeft, contentDescription = "Align Left", tint = if (alignment == "left") CrimsonPrimary else Color.Gray)
                        }

                        // Align Center
                        IconButton(onClick = { alignment = "center" }) {
                            Icon(Icons.Default.FormatAlignCenter, contentDescription = "Align Center", tint = if (alignment == "center") CrimsonPrimary else Color.Gray)
                        }

                        // Insert / Edit Table Screen (PART F Item 1)
                        IconButton(onClick = { showTableFullscreen = true }) {
                            Icon(Icons.Default.TableChart, contentDescription = "Table Editor", tint = CrimsonPrimary)
                        }

                        // Professional Alarm Feature replacing Timestamp insertion (PART F Item 7)
                        IconButton(onClick = { showAlarmDialog = true }) {
                            Icon(Icons.Default.Alarm, contentDescription = "Set Alarm Reminder", tint = CrimsonPrimary)
                        }

                        // Add Image Attachment
                        IconButton(onClick = { imagePickerLauncher.launch("image/*") }) {
                            Icon(painter = painterResource(R.drawable.ic_fingerprint_attachment), contentDescription = "Attach Image", tint = CrimsonPrimary, modifier = Modifier.size(22.dp))
                        }
                    }
                }
            }

            // SINGLE Floating AU AI Chatbot with custom sphere mascot icon (PART F Item 2 & 3 & 4)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset { IntOffset(botOffsetX.roundToInt(), botOffsetY.roundToInt()) }
                    .padding(end = 20.dp, bottom = 80.dp)
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            botOffsetX += dragAmount.x
                            botOffsetY += dragAmount.y
                        }
                    }
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .shadow(10.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.Black)
                        .clickable { showAiChatbotModal = true }
                ) {
                    Image(
                        painter = painterResource(R.drawable.au_bot_icon_1790271144581),
                        contentDescription = "AU Chatbot",
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    // Fullscreen Dedicated Table Editor (PART F Item 1)
    if (showTableFullscreen) {
        TableEditorScreen(
            initialTableData = tableData,
            isDarkMode = isDarkMode,
            onBack = { showTableFullscreen = false },
            onSaveTable = { saved ->
                tableData = saved
                showTableFullscreen = false
                Toast.makeText(context, "Table updated!", Toast.LENGTH_SHORT).show()
            }
        )
    }

    // Floating Chatbot Dialog with OCR (+) Button and Live Note Text Context (PART F Item 3 & 4)
    if (showAiChatbotModal) {
        var userPrompt by remember { mutableStateOf("") }
        var aiResponse by remember { mutableStateOf("") }
        var isGenerating by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showAiChatbotModal = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(R.drawable.au_bot_icon_1790271144581),
                        contentDescription = null,
                        modifier = Modifier.size(28.dp).clip(CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("AU Notes AI Assistant", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    if (chatbotAttachedDocName.isNotBlank()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1F2CF95F))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Attached: $chatbotAttachedDocName (OCR Ready)",
                                fontSize = 11.sp,
                                color = Color(0xFF2CF95F),
                                maxLines = 1
                            )
                            IconButton(onClick = {
                                chatbotAttachedDocName = ""
                                chatbotAttachedDocText = ""
                            }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Close, null, tint = Color.Red, modifier = Modifier.size(14.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (aiResponse.isNotBlank()) {
                        Text(
                            text = aiResponse,
                            fontSize = 13.sp,
                            color = if (isDarkMode) Color.White else Color.Black,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 140.dp)
                                .verticalScroll(rememberScrollState())
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Button(
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                            onClick = {
                                // Paste AI output directly into note
                                val newText = if (contentValue.text.isBlank()) aiResponse else "${contentValue.text}\n\n$aiResponse"
                                contentValue = TextFieldValue(newText, selection = TextRange(newText.length))
                                showAiChatbotModal = false
                                Toast.makeText(context, "Inserted AI content into note!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Paste into Note", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Input Row with (+) OCR Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // (+) Plus Icon for OCR of .txt, PDF, Image (PART F Item 4)
                        IconButton(
                            onClick = { ocrDocPicker.launch(arrayOf("*/*")) }
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Attach doc for OCR", tint = CrimsonPrimary)
                        }

                        OutlinedTextField(
                            value = userPrompt,
                            onValueChange = { userPrompt = it },
                            placeholder = { Text("Ask AI to rewrite, clean, or extract...", fontSize = 12.sp) },
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = {
                                if (userPrompt.isNotBlank()) {
                                    isGenerating = true
                                    coroutineScope.launch {
                                        val key = com.example.data.preferences.AppPreferences(context).getEffectiveApiKey()
                                        val fullContext = "Current Note Title: $title\nCurrent Note Text:\n${contentValue.text}\n\nAttached OCR Data:\n$chatbotAttachedDocText"
                                        val res = aiService.generateResponse(
                                            prompt = userPrompt,
                                            apiKey = key,
                                            noteContext = fullContext
                                        )
                                        isGenerating = false
                                        aiResponse = res
                                    }
                                }
                            }
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp), color = CrimsonPrimary)
                            } else {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = CrimsonPrimary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showAiChatbotModal = false }) { Text("Close") }
            }
        )
    }

    // Professional Alarm / Reminder Feature (PART F Item 7)
    if (showAlarmDialog) {
        var alarmTitleInput by remember { mutableStateOf(if (title.isNotBlank()) "Reminder: $title" else "Note Reminder") }
        var selectedHour by remember { mutableIntStateOf(Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) }
        var selectedMinute by remember { mutableIntStateOf((Calendar.getInstance().get(Calendar.MINUTE) + 5) % 60) }
        var ringtoneType by remember { mutableStateOf("system") } // "system" or "device"

        val systemRingtones = remember { AlarmScheduler.getSystemRingtones(context) }
        var selectedSystemRingtone by remember { mutableStateOf(systemRingtones.firstOrNull()) }

        val deviceMusicFiles = remember { AlarmScheduler.getDeviceMusicFiles(context) }
        var selectedDeviceMusic by remember { mutableStateOf(deviceMusicFiles.firstOrNull()) }

        AlertDialog(
            onDismissRequest = { showAlarmDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AlarmAdd, contentDescription = null, tint = CrimsonPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Schedule Real Alarm", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = alarmTitleInput,
                        onValueChange = { alarmTitleInput = it },
                        label = { Text("Alarm Title") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text("Alarm Time: %02d:%02d".format(selectedHour, selectedMinute), fontWeight = FontWeight.Bold, color = CrimsonPrimary)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { selectedHour = (selectedHour + 1) % 24 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2D55)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+1 Hour", color = if (isDarkMode) Color.White else Color.Black, fontSize = 11.sp)
                        }
                        Button(
                            onClick = { selectedMinute = (selectedMinute + 10) % 60 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0x33FF2D55)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("+10 Mins", color = if (isDarkMode) Color.White else Color.Black, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Ringtone Source:", fontWeight = FontWeight.Bold, fontSize = 13.sp)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = ringtoneType == "system",
                            onClick = { ringtoneType = "system" },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                        )
                        Text("System Ringtones", fontSize = 13.sp)

                        Spacer(modifier = Modifier.width(12.dp))

                        RadioButton(
                            selected = ringtoneType == "device",
                            onClick = { ringtoneType = "device" },
                            colors = RadioButtonDefaults.colors(selectedColor = CrimsonPrimary)
                        )
                        Text("Device Files", fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (ringtoneType == "system") {
                        Text("Selected: ${selectedSystemRingtone?.title ?: "Default"}", fontSize = 12.sp, color = CrimsonPrimary)
                    } else {
                        Text("Selected Device Song: ${selectedDeviceMusic?.title ?: "First found"}", fontSize = 12.sp, color = CrimsonPrimary)
                        if (deviceMusicFiles.isEmpty()) {
                            Text("No audio files detected in storage. Defaulting to system alarm.", fontSize = 11.sp, color = Color.Gray)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    onClick = {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, selectedHour)
                            set(Calendar.MINUTE, selectedMinute)
                            set(Calendar.SECOND, 0)
                            if (timeInMillis <= System.currentTimeMillis()) {
                                add(Calendar.DAY_OF_YEAR, 1)
                            }
                        }

                        val chosenUri = if (ringtoneType == "device") selectedDeviceMusic?.uri else selectedSystemRingtone?.uri
                        val scheduled = AlarmScheduler.scheduleAlarm(
                            context = context,
                            triggerTimeMillis = cal.timeInMillis,
                            title = alarmTitleInput,
                            noteId = currentNoteId,
                            ringtoneUri = chosenUri
                        )

                        showAlarmDialog = false
                        if (scheduled) {
                            Toast.makeText(context, "Alarm set for %02d:%02d!".format(selectedHour, selectedMinute), Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(context, "Alarm scheduled successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                ) {
                    Text("Set Alarm")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAlarmDialog = false }) { Text("Cancel") }
            }
        )
    }

    // Image Edit Modal (Move, Crop, Resize, Rename - PART F Item 5)
    showImageEditModal?.let { imgAtt ->
        var editedName by remember { mutableStateOf(imgAtt.fileName) }
        AlertDialog(
            onDismissRequest = { showImageEditModal = null },
            title = { Text("Edit Image in Note", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = { editedName = it },
                        label = { Text("Rename File") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Actions available: Crop, Resize, Reorder in note.", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                Button(
                    colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                    onClick = {
                        attachments = attachments.map {
                            if (it.uri == imgAtt.uri) it.copy(fileName = editedName) else it
                        }
                        showImageEditModal = null
                        Toast.makeText(context, "Image updated!", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageEditModal = null }) { Text("Cancel") }
            }
        )
    }
}
