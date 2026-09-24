package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.ClipDescription
import android.content.ClipboardManager
import android.content.Context
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.preferences.AppPreferences
import com.example.ui.components.GlassBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.NeuIconButton
import com.example.ui.theme.CrimsonPrimary

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun NameGeneratorScreen(
    preferences: AppPreferences,
    isDarkMode: Boolean,
    onBack: () -> Unit,
    onSaveNamesToNote: (List<String>) -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = remember {
        context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    }

    // Load permanently saved names from SharedPreferences
    val savedNamesPrefs = remember {
        context.getSharedPreferences("au_copied_names_prefs", Context.MODE_PRIVATE)
    }

    val savedNamesList = remember {
        val raw = savedNamesPrefs.getString("saved_names_list", "") ?: ""
        mutableStateListOf<String>().apply {
            if (raw.isNotBlank()) {
                addAll(raw.split(";;;").filter { it.isNotBlank() })
            }
        }
    }

    fun persistSavedNames() {
        val joined = savedNamesList.joinToString(";;;")
        savedNamesPrefs.edit().putString("saved_names_list", joined).apply()
    }

    fun addCopiedName(name: String) {
        val clean = name.trim()
        if (clean.isNotBlank() && !savedNamesList.contains(clean)) {
            savedNamesList.add(0, clean)
            persistSavedNames()
            Toast.makeText(context, "Captured: $clean", Toast.LENGTH_SHORT).show()
        }
    }

    // Clipboard listener to auto-detect names copied from Nickfinder
    DisposableEffect(Unit) {
        val listener = ClipboardManager.OnPrimaryClipChangedListener {
            val clip = clipboardManager.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val item = clip.getItemAt(0)
                val text = item.text?.toString()
                if (!text.isNullOrBlank()) {
                    addCopiedName(text)
                }
            }
        }
        clipboardManager.addPrimaryClipChangedListener(listener)
        onDispose {
            clipboardManager.removePrimaryClipChangedListener(listener)
        }
    }

    var isRightDrawerOpen by remember { mutableStateOf(false) }
    var activeTab by remember { mutableStateOf("generate") } // "generate" or "your_names"
    var isFullScreenYourNames by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var isLoadingWeb by remember { mutableStateOf(true) }

    val selectedNamesForExport = remember { mutableStateListOf<String>() }

    GlassBackground(isDarkMode = isDarkMode) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top Custom App Bar (Clean, no browser address bar)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
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
                            onClick = onBack
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Name Generator",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDarkMode) Color.White else Color(0xFF111111)
                            )
                            Text(
                                text = if (activeTab == "generate") "Nickfinder in-app generator" else "Your Saved Names (${savedNamesList.size})",
                                fontSize = 11.sp,
                                color = CrimsonPrimary
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeTab == "generate") {
                            IconButton(onClick = { webViewRef?.reload() }) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reload",
                                    tint = if (isDarkMode) Color.White else Color.Black
                                )
                            }
                        }

                        // Right Sidebar Toggle Button
                        GlassCard(
                            modifier = Modifier.size(38.dp),
                            shape = CircleShape,
                            isDarkMode = isDarkMode,
                            onClick = { isRightDrawerOpen = !isRightDrawerOpen }
                        ) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Menu",
                                    tint = CrimsonPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Main Content: Either WebView (Generate) or Your Names Fullscreen
                Box(modifier = Modifier.fillMaxSize()) {
                    if (activeTab == "generate" && !isFullScreenYourNames) {
                        // In-App WebView for nickfinder.com
                        AndroidView(
                            factory = { ctx ->
                                WebView(ctx).apply {
                                    layoutParams = ViewGroup.LayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.MATCH_PARENT
                                    )
                                    settings.javaScriptEnabled = true
                                    settings.domStorageEnabled = true
                                    settings.loadWithOverviewMode = true
                                    settings.useWideViewPort = true
                                    settings.setSupportZoom(true)
                                    settings.builtInZoomControls = true
                                    settings.displayZoomControls = false

                                    webViewClient = object : WebViewClient() {
                                        override fun onPageFinished(view: WebView?, url: String?) {
                                            super.onPageFinished(view, url)
                                            isLoadingWeb = false
                                        }
                                    }
                                    webChromeClient = WebChromeClient()
                                    loadUrl("https://nickfinder.com/")
                                    webViewRef = this
                                }
                            },
                            modifier = Modifier.fillMaxSize()
                        )

                        if (isLoadingWeb) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = CrimsonPrimary)
                            }
                        }
                    } else {
                        // "Your Names" Fullscreen View
                        YourNamesContentView(
                            savedNames = savedNamesList,
                            selectedNames = selectedNamesForExport,
                            isDarkMode = isDarkMode,
                            onToggleSelect = { name ->
                                if (selectedNamesForExport.contains(name)) {
                                    selectedNamesForExport.remove(name)
                                } else {
                                    selectedNamesForExport.add(name)
                                }
                            },
                            onDeleteName = { name ->
                                savedNamesList.remove(name)
                                selectedNamesForExport.remove(name)
                                persistSavedNames()
                            },
                            onSaveToNotes = {
                                if (selectedNamesForExport.isNotEmpty()) {
                                    onSaveNamesToNote(selectedNamesForExport.toList())
                                    Toast.makeText(context, "Saved ${selectedNamesForExport.size} names to Note!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Please select names first", Toast.LENGTH_SHORT).show()
                                }
                            }
                        )
                    }
                }
            }

            // Right Slide Sidebar Panel (Dual Option: Generate Names / Your Names)
            AnimatedVisibility(
                visible = isRightDrawerOpen,
                enter = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                exit = slideOutHorizontally(targetOffsetX = { it }) + fadeOut(),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(300.dp)
                        .background(if (isDarkMode) Color(0xF0181A20) else Color(0xF5F0F4F9))
                        .border(
                            1.dp,
                            if (isDarkMode) Color(0x33FFFFFF) else Color(0x33000000),
                            RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                        )
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Name Hub",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = CrimsonPrimary
                            )
                            IconButton(onClick = { isRightDrawerOpen = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = if (isDarkMode) Color.White else Color.Black)
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Option 1: Generate Names (Website)
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isDarkMode = isDarkMode,
                            onClick = {
                                activeTab = "generate"
                                isFullScreenYourNames = false
                                isRightDrawerOpen = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, tint = CrimsonPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("1. Generate Names", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDarkMode) Color.White else Color.Black)
                                    Text("Nickfinder browser", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Option 2: Your Names (Permanent Copied List)
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isDarkMode = isDarkMode,
                            onClick = {
                                activeTab = "your_names"
                                isFullScreenYourNames = true
                                isRightDrawerOpen = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFB300))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("2. Your Names", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (isDarkMode) Color.White else Color.Black)
                                    Text("${savedNamesList.size} permanent saved", fontSize = 11.sp, color = Color.Gray)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = if (isDarkMode) Color(0x22FFFFFF) else Color(0x22000000))
                        Spacer(modifier = Modifier.height(16.dp))

                        // Action: Select Names to Save in Notes
                        GlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                            isDarkMode = isDarkMode,
                            onClick = {
                                if (savedNamesList.isNotEmpty()) {
                                    activeTab = "your_names"
                                    isFullScreenYourNames = true
                                    isRightDrawerOpen = false
                                    Toast.makeText(context, "Select names from list to save in Note", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "No names saved yet. Tap names on website to copy!", Toast.LENGTH_LONG).show()
                                }
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = CrimsonPrimary)
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = "Select Names to Save in Notes",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.5.sp,
                                    color = if (isDarkMode) Color.White else Color.Black
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun YourNamesContentView(
    savedNames: List<String>,
    selectedNames: List<String>,
    isDarkMode: Boolean,
    onToggleSelect: (String) -> Unit,
    onDeleteName: (String) -> Unit,
    onSaveToNotes: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Saved Nicknames (${savedNames.size})",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkMode) Color.White else Color.Black
            )

            if (savedNames.isNotEmpty()) {
                GlassCard(
                    shape = RoundedCornerShape(10.dp),
                    isDarkMode = isDarkMode,
                    onClick = onSaveToNotes
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.BookmarkAdd, contentDescription = null, tint = CrimsonPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Save in Note (${selectedNames.size})",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrimsonPrimary
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (savedNames.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        tint = Color.Gray,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "No copied names yet.",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Tap on any name in the website to auto-capture!",
                        color = CrimsonPrimary,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(savedNames) { name ->
                    val isSelected = selectedNames.contains(name)
                    GlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        isDarkMode = isDarkMode,
                        onClick = { onToggleSelect(name) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { onToggleSelect(name) },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = CrimsonPrimary,
                                        checkmarkColor = Color.White
                                    )
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isDarkMode) Color.White else Color(0xFF111111)
                                )
                            }

                            IconButton(
                                onClick = { onDeleteName(name) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = Color.Red.copy(alpha = 0.7f),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
