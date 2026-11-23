package com.locco.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.nio.charset.StandardCharsets
import java.text.DateFormat
import java.util.Date
import java.util.UUID

// Kept for the legacy session kernel and encrypted-store compatibility tests.
internal data class Note(val id: String, val text: String, val time: Long)
internal data class Session(val id: String, val title: String, val time: Long, val notes: List<Note>)

internal data class SharedCapture(val token: String, val text: String, val title: String?, val error: String? = null)
private enum class LoccoTab { Capture, Timeline, Observe }

class MainActivity : ComponentActivity() {
    private var incomingCapture by mutableStateOf<SharedCapture?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        incomingCapture = parseSharedText(intent)
        setContent { LoccoCore(incomingCapture) }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        incomingCapture = parseSharedText(intent)
    }

    private fun parseSharedText(intent: Intent?): SharedCapture? {
        if (intent?.action != Intent.ACTION_SEND || intent.type != "text/plain") return null
        val value = intent.getCharSequenceExtra(Intent.EXTRA_TEXT)?.toString()?.trim().orEmpty()
        if (value.isEmpty()) return null
        val title = (
            intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT)
                ?: intent.getCharSequenceExtra(Intent.EXTRA_TITLE)
            )?.toString()?.trim()?.takeIf { it.isNotEmpty() }

        if (value.toByteArray(StandardCharsets.UTF_8).size > CaptureKernel.MAX_CONTENT_BYTES) {
            return SharedCapture(
                UUID.randomUUID().toString(), "", title,
                "The shared text is larger than Locco's 64 KiB capture limit."
            )
        }
        return SharedCapture(UUID.randomUUID().toString(), value, title)
    }
}

@Composable
private fun LoccoCore(sharedCapture: SharedCapture?) {
    val context = LocalContext.current
    val databaseResult = remember(context) { runCatching { LoccoDatabase.create(context.applicationContext) } }
    val database = databaseResult.getOrNull()
    val scope = rememberCoroutineScope()

    var selectedTab by remember { mutableStateOf(LoccoTab.Capture) }
    var captureText by remember { mutableStateOf("") }
    var captureTitle by remember { mutableStateOf("") }
    var captureLocator by remember { mutableStateOf("") }
    var captureKind by remember { mutableStateOf("manual_text") }
    var captureError by remember { mutableStateOf<String?>(null) }
    var captureNotice by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }

    var timeline by remember { mutableStateOf<List<TimelineEntry>>(emptyList()) }
    var nextCursor by remember { mutableStateOf<TimelineCursor?>(null) }
    var isTimelineLoading by remember { mutableStateOf(false) }
    var timelineError by remember { mutableStateOf<String?>(null) }
    var storageError by remember { mutableStateOf(databaseResult.exceptionOrNull()?.message) }

    DisposableEffect(database) {
        onDispose { runCatching { database?.close() } }
    }

    LaunchedEffect(sharedCapture?.token) {
        sharedCapture?.let { incoming ->
            selectedTab = LoccoTab.Capture
            captureError = incoming.error
            captureNotice = if (incoming.error == null) "Shared text received. Review it before saving." else null
            if (incoming.error == null) {
                captureText = incoming.text
                captureTitle = incoming.title.orEmpty()
                captureLocator = ""
                captureKind = "shared_text"
            } else {
                captureText = ""
            }
        }
    }

    fun reloadTimeline() {
        val db = database ?: return
        if (isTimelineLoading) return
        scope.launch {
            isTimelineLoading = true
            val result = withContext(Dispatchers.IO) { runCatching { db.readTimelinePage(limit = 40) } }
            result.onSuccess { page ->
                timeline = page.entries
                nextCursor = page.nextCursor
                timelineError = null
                storageError = null
            }.onFailure { failure ->
                val message = failure.message ?: "Locco could not read its encrypted timeline."
                timelineError = message
                storageError = message
            }
            isTimelineLoading = false
        }
    }

    fun loadOlder() {
        val db = database ?: return
        val cursor = nextCursor ?: return
        if (isTimelineLoading) return
        scope.launch {
            isTimelineLoading = true
            val result = withContext(Dispatchers.IO) { runCatching { db.readTimelinePage(40, cursor) } }
            result.onSuccess { page ->
                timeline = timeline + page.entries
                nextCursor = page.nextCursor
                timelineError = null
            }.onFailure { failure ->
                timelineError = failure.message ?: "Locco could not load the next timeline page."
            }
            isTimelineLoading = false
        }
    }

    fun saveCapture() {
        val db = database
        if (db == null || storageError != null || isSaving) return
        val prepared = runCatching {
            CaptureKernel.prepare(
                text = captureText,
                title = captureTitle,
                locator = captureLocator,
                capturedAt = System.currentTimeMillis(),
                sourceId = UUID.randomUUID().toString(),
                evidenceId = UUID.randomUUID().toString(),
                sourceKind = captureKind
            )
        }.getOrElse { failure ->
            captureError = failure.message ?: "This content could not be captured."
            return
        }
        if (prepared == null) {
            captureError = "Add some text before saving."
            return
        }

        scope.launch {
            isSaving = true
            captureError = null
            val result = withContext(Dispatchers.IO) {
                runCatching { db.storeSourceWithEvidence(prepared.source, prepared.evidence) }
            }
            result.onSuccess {
                captureText = ""
                captureTitle = ""
                captureLocator = ""
                captureKind = "manual_text"
                captureNotice = "Saved to Locco. Your source and evidence are linked."
                selectedTab = LoccoTab.Timeline
                val pageResult = withContext(Dispatchers.IO) { runCatching { db.readTimelinePage(limit = 40) } }
                pageResult.onSuccess { page ->
                    timeline = page.entries
                    nextCursor = page.nextCursor
                    timelineError = null
                    storageError = null
                }.onFailure { failure ->
                    val message = failure.message ?: "The capture was saved, but the timeline could not be refreshed."
                    timelineError = message
                    storageError = message
                }
            }.onFailure { failure ->
                captureError = failure.message ?: "Locco could not save this capture."
            }
            isSaving = false
        }
    }

    LaunchedEffect(database) { reloadTimeline() }

    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF93A5FF), secondary = Color(0xFF84DCC9),
            background = Color(0xFF0B0E13), surface = Color(0xFF171C26)
        )
    ) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0E13)) {
            Column(Modifier.fillMaxSize()) {
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("LOCCO", color = Color(0xFF93A5FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(
                        when (selectedTab) {
                            LoccoTab.Capture -> "Capture"
                            LoccoTab.Timeline -> "Timeline"
                            LoccoTab.Observe -> "Observer"
                        },
                        color = Color.White, style = MaterialTheme.typography.titleMedium
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        LoccoTab.Capture -> CaptureScreen(
                            text = captureText,
                            onTextChange = {
                                if (it.toByteArray(StandardCharsets.UTF_8).size <= CaptureKernel.MAX_CONTENT_BYTES) {
                                    captureText = it
                                    captureError = null
                                } else captureError = "Capture text is limited to 64 KiB."
                            },
                            title = captureTitle,
                            onTitleChange = { captureTitle = it; captureError = null },
                            locator = captureLocator,
                            onLocatorChange = { captureLocator = it; captureError = null },
                            sourceKind = captureKind,
                            notice = captureNotice,
                            error = captureError ?: storageError,
                            isSaving = isSaving,
                            onSave = ::saveCapture,
                            onViewTimeline = { selectedTab = LoccoTab.Timeline }
                        )
                        LoccoTab.Timeline -> TimelineScreen(
                            entries = timeline,
                            isLoading = isTimelineLoading,
                            error = timelineError ?: storageError,
                            hasMore = nextCursor != null,
                            onRefresh = ::reloadTimeline,
                            onLoadOlder = ::loadOlder
                        )
                        LoccoTab.Observe -> ObserverScreen()
                    }
                }
                NavigationBar(containerColor = Color(0xFF111722), contentColor = Color.White) {
                    listOf(
                        LoccoTab.Capture to "Capture",
                        LoccoTab.Timeline to "Timeline",
                        LoccoTab.Observe to "Observe"
                    ).forEach { (tab, label) ->
                        NavigationBarItem(
                            selected = selectedTab == tab,
                            onClick = { selectedTab = tab },
                            icon = {},
                            label = { Text(label) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureScreen(
    text: String,
    onTextChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    locator: String,
    onLocatorChange: (String) -> Unit,
    sourceKind: String,
    notice: String?,
    error: String?,
    isSaving: Boolean,
    onSave: () -> Unit,
    onViewTimeline: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Keep what matters.", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Text(
            "Share text from another app to Locco, or add something here. Nothing is stored until you tap Save.",
            color = Color.Gray, style = MaterialTheme.typography.bodyMedium
        )
        if (sourceKind == "shared_text" && notice != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF182B35))) {
                Text(notice, color = Color(0xFFBDEFE2), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        }
        OutlinedTextField(
            value = text, onValueChange = onTextChange,
            modifier = Modifier.fillMaxWidth().weight(1f, fill = false),
            label = { Text("Content") },
            placeholder = { Text("Paste a thought, excerpt, or useful information…") },
            minLines = 6, maxLines = 12
        )
        OutlinedTextField(
            value = title, onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Title (optional)") },
            placeholder = { Text("A short name for this source") }
        )
        OutlinedTextField(
            value = locator, onValueChange = onLocatorChange,
            modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Source URL (optional)") },
            placeholder = { Text("https://…") }
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onViewTimeline) { Text("Open timeline") }
            Spacer(Modifier.weight(1f))
            Button(onClick = onSave, enabled = text.isNotBlank() && !isSaving && error == null) {
                Text(if (isSaving) "Saving…" else "Save capture")
            }
        }
        Text(
            "Manual and shared text are stored as source-linked evidence in encrypted local storage.",
            color = Color.Gray, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
    }
}

@Composable
private fun TimelineScreen(
    entries: List<TimelineEntry>,
    isLoading: Boolean,
    error: String?,
    hasMore: Boolean,
    onRefresh: () -> Unit,
    onLoadOlder: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Your evidence", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text("${entries.size} items loaded", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onRefresh, enabled = !isLoading) { Text("Refresh") }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (entries.isEmpty() && !isLoading) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Nothing captured yet.", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Save text from Capture or share it here from another app.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(
                Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)
            ) {
                items(entries, key = { it.evidence.id }) { entry ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF171F2C))) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(
                                entry.source.title ?: entry.source.kind.replace('_', ' '),
                                color = Color.White, fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                "${entry.source.kind.replace('_', ' ')} · ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(entry.evidence.capturedAt))}",
                                color = Color.Gray, style = MaterialTheme.typography.labelSmall
                            )
                            val excerpt = entry.evidence.excerpt
                            Text(
                                excerpt?.let { if (it.length > 520) it.take(520) + "…" else it }
                                    ?: entry.evidence.contentHash.orEmpty(),
                                color = Color(0xFFE5EAF2), style = MaterialTheme.typography.bodyMedium
                            )
                            entry.source.locator?.let {
                                Text(it, color = Color(0xFF93A5FF), style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                item {
                    if (hasMore) {
                        OutlinedButton(onClick = onLoadOlder, enabled = !isLoading, modifier = Modifier.fillMaxWidth()) {
                            Text(if (isLoading) "Loading…" else "Load older evidence")
                        }
                    } else if (entries.isNotEmpty()) {
                        Text("You’re up to date.", color = Color.Gray, style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.fillMaxWidth().padding(12.dp))
                    }
                }
            }
        }
    }
}
