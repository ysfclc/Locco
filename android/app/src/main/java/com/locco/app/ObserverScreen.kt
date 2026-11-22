package com.locco.app

import android.content.ComponentName
import android.content.Intent
import android.provider.Settings
import android.text.TextUtils
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
internal fun ObserverScreen(onBack: () -> Unit) {
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
        val database = runCatching { LoccoDatabase.create(context.applicationContext) }
        database.onSuccess { db ->
            try {
                events = db.readObservations()
                error = null
            } catch (failure: Throwable) {
                error = failure.message ?: "Unable to read the local observation ledger."
            } finally {
                db.close()
            }
        }.onFailure { failure ->
            error = failure.message ?: "Encrypted observation storage is unavailable."
        }
    }

    Column(Modifier.fillMaxSize().padding(horizontal = 18.dp, vertical = 12.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Android observer", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
            TextButton(onClick = onBack) { Text("Back") }
        }
        Text("Observe notification metadata from apps you explicitly select. Locco never stores notification titles, message text, actions or extras.", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(14.dp)) {
                Text(if (hasAccess) "Notification access enabled" else "Notification access required", fontWeight = FontWeight.SemiBold)
                Text("Android controls this permission. You can revoke it at any time in system settings.", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    }) { Text(if (hasAccess) "Manage permission" else "Grant access") }
                    OutlinedButton(onClick = { refresh() }) { Text("Refresh") }
                }
            }
        }
        Spacer(Modifier.height(14.dp))
        Text("Selected apps", style = MaterialTheme.typography.titleMedium)
        Text("Enter the Android package name of each app. Nothing is observed until it is on this list and Android notification access is enabled.", style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = packageInput,
                onValueChange = { packageInput = it },
                modifier = Modifier.weight(1f),
                label = { Text("Package name") },
                placeholder = { Text("com.example.app") },
                singleLine = true
            )
            Button(
                onClick = {
                    val value = packageInput.trim()
                    if (value.matches(Regex("[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)+"))) {
                        observedPackages = observedPackages + value
                        prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, observedPackages).apply()
                        packageInput = ""
                    } else {
                        error = "Enter a valid Android package name, such as com.example.app."
                    }
                },
                enabled = packageInput.isNotBlank()
            ) { Text("Add") }
        }
        observedPackages.sorted().forEach { packageName ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(packageName, modifier = Modifier.padding(vertical = 8.dp))
                TextButton(onClick = {
                    observedPackages = observedPackages - packageName
                    prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, observedPackages).apply()
                }) { Text("Remove") }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Observed activity", style = MaterialTheme.typography.titleMedium)
            TextButton(onClick = { refresh() }) { Text("Refresh feed") }
        }
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (events.isEmpty()) {
            Text("No observation receipts yet. Enable access, select an app, then receive a notification from it. Open Locco again and refresh this feed.", style = MaterialTheme.typography.bodySmall)
        } else {
            LazyColumn(Modifier.weight(1f, fill = false)) {
                items(events, key = { it.id }) { event ->
                    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Column(Modifier.padding(12.dp)) {
                            Text(event.sourcePackage, fontWeight = FontWeight.SemiBold)
                            Text(event.eventType.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                            Text("Via ${event.acquisitionMethod} · ${event.category ?: "uncategorised"}", style = MaterialTheme.typography.bodySmall)
                            Text(DateFormat.getDateTimeInstance().format(Date(event.occurredAt)), style = MaterialTheme.typography.labelSmall)
                            Text("Redacted: notification title, text and extras", style = MaterialTheme.typography.labelSmall)
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
