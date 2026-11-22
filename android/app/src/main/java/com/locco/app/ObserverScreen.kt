package com.locco.app

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.provider.Settings.Secure
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

@Composable
internal fun ObserverScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(LoccoNotificationObserver.OBSERVER_PREFS, android.content.Context.MODE_PRIVATE) }
    var packageInput by remember { mutableStateOf("") }
    var observedPackages by remember {
        mutableStateOf(prefs.getStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, emptySet())?.toSet() ?: emptySet())
    }
    var events by remember { mutableStateOf(emptyList<AppObservation>()) }
    var hasAccess by remember { mutableStateOf(notificationAccessEnabled(context)) }
    var error by remember { mutableStateOf<String?>(null) }

    fun refresh() {
        hasAccess = notificationAccessEnabled(context)
        runCatching { LoccoDatabase.create(context.applicationContext) }
            .onSuccess { db ->
                try {
                    events = db.readObservations()
                    error = null
                } catch (failure: Throwable) {
                    error = failure.message ?: "Could not read local observations."
                } finally {
                    db.close()
                }
            }
            .onFailure { failure -> error = failure.message ?: "Encrypted observation storage unavailable." }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 8.dp)) {
        Text("See what reaches Locco", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text("Only selected apps. Notification metadata only—no message text or titles.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 4.dp, bottom = 12.dp))
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp)) {
                Text(if (hasAccess) "Access is enabled" else "Permission needed", fontWeight = FontWeight.SemiBold)
                Text("Android notification access", style = MaterialTheme.typography.bodySmall)
                Button(onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }, modifier = Modifier.padding(top = 8.dp)) {
                    Text(if (hasAccess) "Manage access" else "Enable access")
                }
            }
        }
        Text("Apps to watch", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 14.dp, bottom = 6.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = packageInput, onValueChange = { packageInput = it }, modifier = Modifier.weight(1f), placeholder = { Text("App package name") }, singleLine = true)
            Button(onClick = {
                val value = packageInput.trim()
                if (value.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) {
                    observedPackages = observedPackages + value
                    prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, observedPackages).apply()
                    packageInput = ""
                    error = null
                } else error = "Enter a valid package name."
            }, enabled = packageInput.isNotBlank()) { Text("Add") }
        }
        observedPackages.sorted().forEach { pkg ->
            Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(pkg, modifier = Modifier.padding(vertical = 8.dp), style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = {
                    observedPackages = observedPackages - pkg
                    prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, observedPackages).apply()
            }) { Text("Remove") }
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Recent observations", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            TextButton(onClick = { refresh() }) { Text("Refresh") }
        }
        if (error != null) Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        if (events.isEmpty()) {
            Text("Nothing captured yet. Enable access, select an app and wait for its notification.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn(Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                items(events, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(event.sourcePackage, fontWeight = FontWeight.SemiBold)
                            Text(event.eventType.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                            Text("${event.category ?: "Uncategorised"} · ${event.acquisitionMethod}", style = MaterialTheme.typography.bodySmall)
                            Text(DateFormat.getDateTimeInstance().format(Date(event.occurredAt)), style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}

private fun notificationAccessEnabled(context: android.content.Context): Boolean {
    val enabled = Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    val expected = ComponentName(context, LoccoNotificationObserver::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
}
