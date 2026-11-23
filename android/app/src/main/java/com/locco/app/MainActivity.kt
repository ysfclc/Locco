package com.locco.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
private enum class LoccoTab { Capture, Timeline, Library, Observe }

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
        if (intent == null) return null
        return SharedCaptureParser.parse(
            action = intent.action,
            mimeType = intent.type,
            sharedText = intent.getCharSequenceExtra(Intent.EXTRA_TEXT),
            subject = intent.getCharSequenceExtra(Intent.EXTRA_SUBJECT),
            fallbackTitle = intent.getCharSequenceExtra(Intent.EXTRA_TITLE),
            token = UUID.randomUUID().toString()
        )
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
    var sessions by remember { mutableStateOf<List<Session>>(emptyList()) }
    var activeSessionId by remember { mutableStateOf<String?>(null) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    val activeSession = sessions.firstOrNull { it.id == activeSessionId }
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

    fun createSession() {
        val db = database ?: return
        if (storageError != null || isSaving) return
        val session = Session(UUID.randomUUID().toString(), "New session", System.currentTimeMillis(), emptyList())
        scope.launch {
            isSaving = true
            sessionError = null
            val result = withContext(Dispatchers.IO) { runCatching { db.insertSession(session) } }
            result.onSuccess {
                sessions = (sessions + session).sortedWith(compareByDescending<Session> { it.time }.thenBy { it.id })
                activeSessionId = session.id
                selectedTab = LoccoTab.Capture
                captureNotice = "Session created. Add your first note below."
            }.onFailure { failure ->
                sessionError = failure.message ?: "Locco could not create this session."
            }
            isSaving = false
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
        if (captureText.isBlank()) {
            captureError = "Add some text before saving."
            return
        }
        val capturedAt = System.currentTimeMillis()
        val nextSession = SessionKernel.capture(
            current = activeSession,
            text = captureText,
            timestamp = capturedAt,
            noteId = UUID.randomUUID().toString(),
            sessionId = UUID.randomUUID().toString()
        ) ?: run {
            captureError = "Add some text before saving."
            return
        }
        val note = nextSession.notes.last()
        val prepared = runCatching {
            CaptureKernel.prepare(
                text = note.text,
                title = captureTitle,
                locator = captureLocator,
                capturedAt = capturedAt,
                sourceId = UUID.randomUUID().toString(),
                evidenceId = UUID.randomUUID().toString(),
                sourceKind = captureKind
            )
        }.getOrElse { failure ->
            captureError = failure.message ?: "This content could not be captured."
            return
        } ?: run {
            captureError = "Add some text before saving."
            return
        }

        scope.launch {
            isSaving = true
            captureError = null
            sessionError = null
            val result = withContext(Dispatchers.IO) {
                runCatching { db.storeSessionNoteWithEvidence(nextSession, note, prepared.source, prepared.evidence) }
            }
            result.onSuccess {
                sessions = (sessions.filterNot { it.id == nextSession.id } + nextSession)
                    .sortedWith(compareByDescending<Session> { it.time }.thenBy { it.id })
                activeSessionId = nextSession.id
                captureText = ""
                captureTitle = ""
                captureLocator = ""
                captureKind = "manual_text"
                captureNotice = "Saved in session \"${nextSession.title}\" and linked to source evidence."
                selectedTab = LoccoTab.Capture
                val pageResult = withContext(Dispatchers.IO) { runCatching { db.readTimelinePage(limit = 40) } }
                pageResult.onSuccess { page ->
                    timeline = page.entries
                    nextCursor = page.nextCursor
                    timelineError = null
                }.onFailure { failure ->
                    timelineError = failure.message ?: "The note was saved, but the evidence timeline could not be refreshed."
                }
            }.onFailure { failure ->
                captureError = failure.message ?: "Locco could not save this note and its evidence."
            }
            isSaving = false
        }
    }
    LaunchedEffect(database) {
        reloadTimeline()
        val db = database ?: return@LaunchedEffect
        val result = withContext(Dispatchers.IO) { runCatching { db.readSessions() } }
        result.onSuccess { loaded ->
            sessions = loaded
            if (activeSessionId == null) activeSessionId = loaded.firstOrNull()?.id
            sessionError = null
        }.onFailure { failure ->
            sessionError = failure.message ?: "Locco could not read saved sessions."
        }
    }

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
                            LoccoTab.Library -> "Notes"
                            LoccoTab.Observe -> "Observer"
                        },
                        color = Color.White, style = MaterialTheme.typography.titleMedium
                    )
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        LoccoTab.Capture -> CaptureScreen(
                            activeSession = activeSession,
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
                            sessionError = sessionError,
                            isSaving = isSaving,
                            onSave = ::saveCapture,
                            onNewSession = ::createSession,
                            onOpenSessions = { selectedTab = LoccoTab.Library },
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
                        LoccoTab.Library -> SessionLibraryScreen(
                            sessions = sessions,
                            error = sessionError,
                            onOpen = { session -> activeSessionId = session.id; selectedTab = LoccoTab.Capture },
                            onNew = ::createSession
                        )
                        LoccoTab.Observe -> ObserverScreen()
                    }
                }
                NavigationBar(containerColor = Color(0xFF111722), contentColor = Color.White) {
                    listOf(
                        LoccoTab.Capture to "Capture",
                        LoccoTab.Timeline to "Timeline",
                        LoccoTab.Library to "Sessions",
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
    activeSession: Session?,
    text: String,
    onTextChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    locator: String,
    onLocatorChange: (String) -> Unit,
    sourceKind: String,
    notice: String?,
    error: String?,
    sessionError: String?,
    isSaving: Boolean,
    onSave: () -> Unit,
    onNewSession: () -> Unit,
    onOpenSessions: () -> Unit,
    onViewTimeline: () -> Unit
) {
    Column(
        Modifier.fillMaxSize().imePadding().verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Keep what matters.", color = Color.White, style = MaterialTheme.typography.headlineLarge)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(activeSession?.title ?: "New session on save", color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("${activeSession?.notes?.size ?: 0} notes in this session", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            TextButton(onClick = onOpenSessions) { Text("Switch") }
            TextButton(onClick = onNewSession, enabled = !isSaving) { Text("New") }
        }
        if (activeSession == null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF171F2C))) {
                Text("Save your first note to create a session, or start an empty session with New.",
                    color = Color.Gray, modifier = Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
            }
        } else if (activeSession.notes.isNotEmpty()) {
            Text("Session notes", color = Color.White, style = MaterialTheme.typography.titleMedium)
            activeSession.notes.forEach { note ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF171F2C))) {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(note.text, color = Color(0xFFE5EAF2), style = MaterialTheme.typography.bodyMedium)
                        Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(note.time)),
                            color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Text("Add a note or share text from another app. Nothing is stored until you tap Save note.", color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
        if (sourceKind == "shared_text" && notice != null) {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF182B35))) {
                Text(notice, color = Color(0xFFBDEFE2), modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
            }
        } else if (notice != null) {
            Text(notice, color = Color(0xFFBDEFE2), style = MaterialTheme.typography.bodySmall)
        }
        OutlinedTextField(
            value = text, onValueChange = onTextChange, modifier = Modifier.fillMaxWidth(),
            label = { Text("Note") }, placeholder = { Text("Write something worth keeping…") },
            minLines = 5, maxLines = 12
        )
        OutlinedTextField(
            value = title, onValueChange = onTitleChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Source title (optional)") }, placeholder = { Text("A short name for this source") }
        )
        OutlinedTextField(
            value = locator, onValueChange = onLocatorChange, modifier = Modifier.fillMaxWidth(), singleLine = true,
            label = { Text("Source URL (optional)") }, placeholder = { Text("https://…") }
        )
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (sessionError != null) Text(sessionError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onViewTimeline) { Text("Evidence") }
            Spacer(Modifier.weight(1f))
            Button(onClick = onSave, enabled = text.isNotBlank() && !isSaving && error == null) {
                Text(if (isSaving) "Saving…" else "Save note")
            }
        }
        Text("Each note is stored in its session and linked to encrypted source evidence.",
            color = Color.Gray, style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.align(Alignment.CenterHorizontally))
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


@Composable
private fun SessionLibraryScreen(
    sessions: List<Session>,
    error: String?,
    onOpen: (Session) -> Unit,
    onNew: () -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Sessions", color = Color.White, style = MaterialTheme.typography.headlineSmall)
                Text("${sessions.size} saved sessions", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
            }
            Button(onClick = onNew) { Text("New session") }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (sessions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No sessions yet.", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Text("Create a session and start adding notes.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
            }
        } else {
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(vertical = 12.dp)) {
                items(sessions, key = { it.id }) { session ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(session) },
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF171F2C))) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(session.title, color = Color.White, fontWeight = FontWeight.SemiBold)
                            Text("${session.notes.size} notes · ${DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(session.time))}",
                                color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                            session.notes.takeLast(2).forEach { note ->
                                Text(note.text, color = Color(0xFFE5EAF2), maxLines = 2, style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Open session", color = Color(0xFF93A5FF), style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }
}
