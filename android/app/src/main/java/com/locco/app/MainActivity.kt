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

internal data class Note(val id:String,val text:String,val time:Long)
internal data class Session(val id:String,val title:String,val time:Long,val notes:List<Note>)
private class LocalStore(context:android.content.Context) {
 private val db=LoccoDatabase.create(context)
 fun load():List<Session> = db.readSessions()
 fun save(list:List<Session>) = db.replaceSessions(list)
}
class MainActivity:ComponentActivity() {
 override fun onCreate(savedInstanceState:Bundle?) { super.onCreate(savedInstanceState); setContent { LoccoCore() } }
}
@Composable private fun LoccoCore() {
 val context=LocalContext.current
 val store=remember { LocalStore(context.applicationContext) }
 var sessions by remember { mutableStateOf(store.load()) }
 var activeId by remember { mutableStateOf(sessions.firstOrNull()?.id) }
 var text by remember { mutableStateOf("") }
 var library by remember { mutableStateOf(false) }
 var observer by remember { mutableStateOf(false) }
 val active=sessions.firstOrNull { it.id==activeId }
 fun update(next:List<Session>) { sessions=next.sortedByDescending { it.time }; store.save(sessions) }
 fun start() { val s=Session(UUID.randomUUID().toString(),"New session",System.currentTimeMillis(),emptyList()); update(sessions+s); activeId=s.id; library=false }
 fun addNote() {
  val value=text.trim(); if(value.isEmpty()) return
  val captured=SessionKernel.capture(active,value,System.currentTimeMillis(),UUID.randomUUID().toString(),UUID.randomUUID().toString()) ?: return
  val next=if(active==null) sessions+captured else sessions.map { if(it.id==active.id) captured else it }
  update(next)
  activeId=captured.id
  library=false
  text=""
 }
 MaterialTheme(colorScheme=darkColorScheme(primary=Color(0xFF93A5FF),secondary=Color(0xFF84DCC9),background=Color(0xFF0B0E13),surface=Color(0xFF171C26))) {
  Surface(Modifier.fillMaxSize(),color=Color(0xFF0B0E13)) {
   Column(Modifier.fillMaxSize()) {
    Row(Modifier.fillMaxWidth().padding(20.dp),verticalAlignment=Alignment.CenterVertically) {
     Column { Text("LOCCO",color=Color(0xFF93A5FF),fontWeight=FontWeight.Bold); Text(if(library) "Library" else "Think · Capture · Connect",color=Color.White,style=MaterialTheme.typography.titleLarge) }
     Spacer(Modifier.weight(1f))
     TextButton(onClick={observer=!observer; library=false}) { Text(if(observer) "Close observer" else "Observer",color=Color(0xFF84DCC9)) }
     TextButton(onClick={library=!library; observer=false}) { Text(if(library) "Workspace" else "Library",color=Color(0xFF84DCC9)) }
    }
    if(observer) {
     ObserverScreen(onBack={observer=false})
    } else if(library) {
     Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text("${sessions.size} sessions",color=Color.LightGray); Button(onClick={start()}) { Text("New") } }
     if(sessions.isEmpty()) Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { Text("Your saved sessions appear here.",color=Color.Gray) }
     else LazyColumn(Modifier.fillMaxSize().padding(horizontal=12.dp)) { items(sessions,key={it.id}) { s ->
      Card(Modifier.fillMaxWidth().padding(vertical=5.dp).clickable { activeId=s.id; library=false },colors=CardDefaults.cardColors(containerColor=Color(0xFF171C26))) { Column(Modifier.padding(16.dp)) { Text(s.title,color=Color.White); Text("${s.notes.size} notes · ${java.text.DateFormat.getDateInstance().format(s.time)}",color=Color.Gray,style=MaterialTheme.typography.bodySmall) } }
     } }
    } else if(active==null) {
     Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center) { Column(horizontalAlignment=Alignment.CenterHorizontally,modifier=Modifier.padding(28.dp)) {
      Text("A space to think, collect and connect.",color=Color.White,style=MaterialTheme.typography.headlineSmall)
      Text("Start with a thought, question or piece of information.",color=Color.Gray,modifier=Modifier.padding(top=8.dp))
      Button(onClick={start()},modifier=Modifier.padding(top=18.dp)) { Text("Start a session") }
      TextButton(onClick={library=true}) { Text("Browse saved sessions",color=Color(0xFF84DCC9)) }
     } }
    } else {
     LazyColumn(Modifier.weight(1f).fillMaxWidth().padding(horizontal=16.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
      item { Text(active.title,color=Color.White,style=MaterialTheme.typography.titleMedium,modifier=Modifier.padding(vertical=10.dp)) }
      items(active.notes,key={it.id}) { n -> Card(colors=CardDefaults.cardColors(containerColor=Color(0xFF192331)),modifier=Modifier.fillMaxWidth()) { Text(n.text,color=Color(0xFFE5EAF2),modifier=Modifier.padding(16.dp)) } }
      if(active.notes.isEmpty()) item { Text("Capture ideas and information here. Notes are stored locally.",color=Color.Gray) }
     }
     Row(Modifier.fillMaxWidth().padding(12.dp),verticalAlignment=Alignment.Bottom) {
      OutlinedTextField(value=text,onValueChange={text=it},modifier=Modifier.weight(1f),placeholder={Text("Capture a thought…")},maxLines=5)
      Spacer(Modifier.width(8.dp)); Button(onClick={addNote()},enabled=text.isNotBlank()) { Text("Save") }
     }
     Text("Local-only · SQLCipher storage · Legacy migration pending · AI not connected",color=Color.Gray,style=MaterialTheme.typography.labelSmall,modifier=Modifier.align(Alignment.CenterHorizontally).padding(bottom=10.dp))
    }
   }
  }
 }
}
