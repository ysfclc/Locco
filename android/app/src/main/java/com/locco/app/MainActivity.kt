package com.locco.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
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
import java.util.UUID

internal data class Note(val id: String, val text: String, val time: Long)
internal data class Session(val id: String, val title: String, val time: Long, val notes: List<Note>)

private class LocalStore(context: android.content.Context) {
    private val db = LoccoDatabase.create(context)
    fun load(): List<Session> = db.readSessions()
    fun save(list: List<Session>) = db.replaceSessions(list)
}

private enum class LoccoTab { Capture, Library, Observe }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { LoccoCore() }
    }
}

@Composable
private fun LoccoCore() {
    val context = LocalContext.current
    val store = remember { LocalStore(context.applicationContext) }
    var sessions by remember { mutableStateOf(store.load()) }
    var activeId by remember { mutableStateOf(sessions.firstOrNull()?.id) }
    var text by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(LoccoTab.Capture) }
    val active = sessions.firstOrNull { it.id == activeId }

    fun update(next: List<Session>) {
        sessions = next.sortedByDescending { it.time }
        store.save(sessions)
    }
    fun start() {
        val session = Session(UUID.randomUUID().toString(), "New session", System.currentTimeMillis(), emptyList())
        update(sessions + session)
        activeId = session.id
        selectedTab = LoccoTab.Capture
    }
    fun addNote() {
        val value = text.trim()
        if (value.isEmpty()) return
        val captured = SessionKernel.capture(active, value, System.currentTimeMillis(), UUID.randomUUID().toString(), UUID.randomUUID().toString()) ?: return
        val next = if (active == null) sessions + captured else sessions.map { if (it.id == active.id) captured else it }
        update(next)
        activeId = captured.id
        selectedTab = LoccoTab.Capture
        text = ""
    }

    MaterialTheme(colorScheme = darkColorScheme(
        primary = Color(0xFF93A5FF), secondary = Color(0xFF84DCC9),
        background = Color(0xFF0B0E13), surface = Color(0xFF171C26)
    )) {
        Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0E13)) {
            Column(Modifier.fillMaxSize()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("LOCCO", color = Color(0xFF93A5FF), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.weight(1f))
                    Text(when (selectedTab) {
                        LoccoTab.Capture -> "Capture"
                        LoccoTab.Library -> "Library"
                        LoccoTab.Observe -> "Observer"
                    }, color = Color.White, style = MaterialTheme.typography.titleMedium)
                }
                Box(Modifier.weight(1f).fillMaxWidth()) {
                    when (selectedTab) {
                        LoccoTab.Capture -> CaptureScreen(active, text, { text = it }, ::start, ::addNote)
                        LoccoTab.Library -> LibraryScreen(sessions, { selected ->
                            activeId = selected.id
                            selectedTab = LoccoTab.Capture
                        }, ::start)
                        LoccoTab.Observe -> ObserverScreen(onBack = { selectedTab = LoccoTab.Capture })
                    }
                }
                NavigationBar(containerColor = Color(0xFF111722), contentColor = Color.White) {
                    listOf(LoccoTab.Capture to "Capture", LoccoTab.Library to "Library", LoccoTab.Observe to "Observe").forEach { (tab, label) ->
                        NavigationBarItem(selected = selectedTab == tab, onClick = { selectedTab = tab }, icon = {}, label = { Text(label) })
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptureScreen(active: Session?, text: String, onTextChange: (String) -> Unit, onStart: () -> Unit, onSave: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        if (active == null) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
                    Text("Catch a thought.", color = Color.White, style = MaterialTheme.typography.headlineLarge)
                    Text("Keep ideas and information in one place.", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
                    Button(onClick = onStart, modifier = Modifier.padding(top = 20.dp)) { Text("Start a session") }
                }
            }
        } else {
            Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(active.title, color = Color.White, style = MaterialTheme.typography.titleLarge)
                    Text("${active.notes.size} notes", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                }
                TextButton(onClick = onStart) { Text("New session") }
            }
            LazyColumn(Modifier.weight(1f).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(active.notes, key = { it.id }) { note ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF192331))) {
                        Text(note.text, color = Color(0xFFE5EAF2), modifier = Modifier.padding(16.dp))
                    }
                }
                if (active.notes.isEmpty()) item { Text("Add your first thought below.", color = Color.Gray, modifier = Modifier.padding(vertical = 8.dp)) }
            }
        }
        Row(Modifier.fillMaxWidth().padding(vertical = 12.dp), verticalAlignment = Alignment.Bottom) {
            OutlinedTextField(value = text, onValueChange = onTextChange, modifier = Modifier.weight(1f), placeholder = { Text("Write something…") }, maxLines = 4)
            Spacer(Modifier.width(8.dp))
            Button(onClick = onSave, enabled = text.isNotBlank()) { Text("Save") }
        }
        Text("Saved locally · Encryption migration still pending", color = Color.Gray, style = MaterialTheme.typography.labelSmall, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 8.dp))
    }
}

@Composable
private fun LibraryScreen(sessions: List<Session>, onOpen: (Session) -> Unit, onNew: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp)) {
        Row(Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Text("${sessions.size} saved sessions", color = Color.Gray, modifier = Modifier.weight(1f))
            Button(onClick = onNew) { Text("New") }
        }
        if (sessions.isEmpty()) {
            Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) { Text("Your saved sessions will appear here.", color = Color.Gray) }
        } else {
            LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sessions, key = { it.id }) { session ->
                    Card(Modifier.fillMaxWidth().clickable { onOpen(session) }, colors = CardDefaults.cardColors(containerColor = Color(0xFF171C26))) {
                        Column(Modifier.padding(16.dp)) {
                            Text(session.title, color = Color.White, style = MaterialTheme.typography.titleMedium)
                            Text("${session.notes.size} notes · ${java.text.DateFormat.getDateInstance().format(session.time)}", color = Color.Gray, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
}
