package com.example.ui.components

import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.R
import com.example.data.model.NoteEntity
import com.example.ui.theme.CrimsonPrimary
import com.example.ui.util.RichTextFormatter
import java.io.File
import java.io.FileOutputStream
import java.util.Date

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ExportDialog(
    note: NoteEntity,
    isDarkMode: Boolean,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedExtension by remember {
        mutableStateOf(
            when (note.category) {
                "Code" -> ".py"
                "API" -> ".txt"
                else -> ".txt"
            }
        )
    }
    var fileNameWithoutExt by remember {
        mutableStateOf(
            note.title.replace(Regex("[^a-zA-Z0-9_-]"), "_").ifBlank { "AU_Note_${note.id}" }
        )
    }

    val extensions = listOf(".txt", ".pdf", ".docx", ".html", ".py", ".xml", ".json")

    val attachments: List<RichTextFormatter.AttachmentInfo> = remember(note.attachmentsJson) {
        RichTextFormatter.deserializeAttachments(note.attachmentsJson)
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            isDarkMode = isDarkMode,
            strong = true
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_export_download),
                            contentDescription = null,
                            tint = CrimsonPrimary,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = "Export System",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDarkMode) Color.White else Color(0xFF111111)
                        )
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = if (isDarkMode) Color.White else Color.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Select Export Extension:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CrimsonPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    extensions.forEach { ext ->
                        val isSelected = selectedExtension == ext
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) CrimsonPrimary else if (isDarkMode) Color(0x33FFFFFF) else Color(0x14000000)
                                )
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CrimsonPrimary else Color(0x22FFFFFF),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedExtension = ext }
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = ext.uppercase(),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else if (isDarkMode) Color.White.copy(alpha = 0.8f) else Color.DarkGray
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "File Name (Editable Extension):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = CrimsonPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = fileNameWithoutExt,
                        onValueChange = { input ->
                            fileNameWithoutExt = input.replace(Regex("[/\\\\:*?\"<>|]"), "")
                        },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            unfocusedTextColor = if (isDarkMode) Color.White else Color.Black,
                            focusedBorderColor = CrimsonPrimary,
                            unfocusedBorderColor = Color(0x44FF2D55)
                        )
                    )
                    Text(
                        text = selectedExtension,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = CrimsonPrimary,
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                // PART G: TWO EXPORT BUTTONS (1st Export to Storage, 2nd Share)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Option 1: Save / Export to Internal Storage (Documents)
                    Button(
                        onClick = {
                            val safeName = fileNameWithoutExt.trim().ifBlank { "AU_Note_${note.id}" }
                            val fullFileName = "$safeName$selectedExtension"
                            val savedFile = saveFileToInternalStorage(context, note, fullFileName, selectedExtension, attachments)
                            if (savedFile != null) {
                                Toast.makeText(context, "Saved to Storage: documents/${savedFile.name}", Toast.LENGTH_LONG).show()
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("1. Export", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }

                    // Option 2: Share via Android Share Sheet
                    Button(
                        onClick = {
                            val safeName = fileNameWithoutExt.trim().ifBlank { "AU_Note_${note.id}" }
                            val fullFileName = "$safeName$selectedExtension"
                            exportAndShareFile(context, note, fullFileName, selectedExtension, attachments)
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonPrimary),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("2. Share", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                    }
                }
            }
        }
    }
}

private fun saveFileToInternalStorage(
    context: Context,
    note: NoteEntity,
    fileName: String,
    extension: String,
    attachments: List<RichTextFormatter.AttachmentInfo>
): File? {
    return try {
        val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
        val targetFile = File(docsDir, fileName)
        writeNoteContentToFile(context, note, targetFile, extension, attachments)
        targetFile
    } catch (e: Exception) {
        Toast.makeText(context, "Export error: ${e.message}", Toast.LENGTH_SHORT).show()
        null
    }
}

private fun exportAndShareFile(
    context: Context,
    note: NoteEntity,
    fileName: String,
    extension: String,
    attachments: List<RichTextFormatter.AttachmentInfo>
) {
    try {
        val exportDir = File(context.cacheDir, "exports").apply { if (!exists()) mkdirs() }
        val targetFile = File(exportDir, fileName)
        writeNoteContentToFile(context, note, targetFile, extension, attachments)

        val mimeType = when (extension) {
            ".pdf" -> "application/pdf"
            ".docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            ".html" -> "text/html"
            ".xml" -> "application/xml"
            ".py" -> "text/x-python"
            ".json" -> "application/json"
            else -> "text/plain"
        }

        val uri: Uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            targetFile
        )

        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, targetFile.name)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val chooser = Intent.createChooser(sendIntent, "Share ${targetFile.name}")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    } catch (e: Exception) {
        Toast.makeText(context, "Share error: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

private fun writeNoteContentToFile(
    context: Context,
    note: NoteEntity,
    targetFile: File,
    extension: String,
    attachments: List<RichTextFormatter.AttachmentInfo>
) {
    val attachmentNamesText = if (attachments.isNotEmpty()) {
        "\n\n[Attachments]\n" + attachments.joinToString("\n") { "- ${it.fileName}" }
    } else ""

    when (extension) {
        ".pdf" -> {
            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = AndroidColor.rgb(255, 45, 85)
                textSize = 22f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = AndroidColor.rgb(30, 30, 30)
                textSize = 12f
                isAntiAlias = true
            }

            var y = 60f
            canvas.drawText(note.title, 40f, y, titlePaint)
            y += 30f
            for (line in note.content.lines()) {
                canvas.drawText(line, 40f, y, bodyPaint)
                y += 18f
                if (y > pageHeight - 60) break
            }
            document.finishPage(page)
            FileOutputStream(targetFile).use { out -> document.writeTo(out) }
            document.close()
        }
        ".html" -> {
            val html = "<html><head><title>${note.title}</title></head><body><h1>${note.title}</h1><pre>${note.content}</pre></body></html>"
            targetFile.writeText(html, Charsets.UTF_8)
        }
        ".json" -> {
            val json = """{"title": "${note.title}", "category": "${note.category}", "content": "${note.content.replace("\n", "\\n")}"}"""
            targetFile.writeText(json, Charsets.UTF_8)
        }
        else -> {
            val text = "${note.title}\n==================\n${note.content}$attachmentNamesText"
            targetFile.writeText(text, Charsets.UTF_8)
        }
    }
}
