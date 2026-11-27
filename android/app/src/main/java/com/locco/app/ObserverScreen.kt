package com.locco.app

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.provider.Settings.Secure
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.DateFormat
import java.util.Date

private data class InstalledAppSource(val packageName: String, val label: String, val version: String)

@Composable
internal fun ObserverScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(LoccoNotificationObserver.OBSERVER_PREFS, Context.MODE_PRIVATE) }
    var selected by remember {
        mutableStateOf(prefs.getStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, emptySet())?.toSet() ?: emptySet())
    }
    var events by remember { mutableStateOf(emptyList<AppObservation>()) }
    var notificationAccess by remember { mutableStateOf(notificationAccessEnabled(context)) }
    var contentCaptureEnabled by remember {
        mutableStateOf(prefs.getBoolean(LoccoNotificationObserver.CAPTURE_NOTIFICATION_CONTENT_KEY, false))
    }
    var inspectedEvent by remember { mutableStateOf<AppObservation?>(null) }
    var usageAccess by remember { mutableStateOf(UsageStatsObserver.hasAccess(context)) }
    var error by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    var choosingApps by remember { mutableStateOf(false) }
    var refreshing by remember { mutableStateOf(false) }
    var refreshedAt by remember { mutableStateOf<Long?>(null) }
    var imported by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()
    val apps = remember(context) { discoverLaunchableApps(context.packageManager, context.packageName) }
    val labels = remember(apps) { apps.associate { it.packageName to it.label } }

    fun selectApp(packageName: String, enabled: Boolean) {
        selected = if (enabled) selected + packageName else selected - packageName
        prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, selected.toSet()).apply()
    }

    fun refresh() {
        if (refreshing) return
        refreshing = true
        error = null
        val allowlist = selected.toSet()
        scope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    val notifications = notificationAccessEnabled(context)
                    val usage = UsageStatsObserver.hasAccess(context)
                    val db = LoccoDatabase.create(context.applicationContext)
                    try {
                        val count = if (usage) UsageStatsObserver.collect(context, allowlist, db) else 0
                        val rows = (db.readObservations() + db.readUsageObservations())
                            .distinctBy { it.id }
                            .sortedWith(compareByDescending<AppObservation> { it.occurredAt }.thenBy { it.id })
                            .take(100)
                        Triple(notifications, usage, count to rows)
                    } finally {
                        db.close()
                    }
                }
            }
            result.onSuccess { (notifications, usage, data) ->
                notificationAccess = notifications
                usageAccess = usage
                imported = data.first
                events = data.second
                refreshedAt = System.currentTimeMillis()
            }.onFailure { failure ->
                error = failure.message ?: "Locco could not read its encrypted activity history."
            }
            refreshing = false
        }
    }

    LaunchedEffect(Unit) { refresh() }
    val filteredApps = apps.filter {
        search.isBlank() || it.label.contains(search, true) || it.packageName.contains(search, true)
    }
    val selectedNames = apps.filter { it.packageName in selected }.map { it.label }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Text("Your app activity", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            Text(
                "A private timeline from the apps you choose. Locco records only signals Android makes available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Activity overview", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Text(
                            refreshedAt?.let { "Last refreshed ${DateFormat.getTimeInstance(DateFormat.SHORT).format(Date(it))}" } ?: "Not synced yet",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (refreshing) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                    else Button(onClick = ::refresh) { Text("Refresh") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Metric(selected.size.toString(), "Apps", Modifier.weight(1f))
                    Metric(events.size.toString(), "Events", Modifier.weight(1f))
                    Metric(imported.toString(), "Scanned", Modifier.weight(1f))
                }
                Text(
                    "App history is imported on refresh. Notification receipts arrive separately when Android allows them.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text("Connect Android signals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        PermissionCard(
            "App activity",
            "Foreground/background transitions for selected apps. No screen contents or in-app actions.",
            usageAccess,
            if (usageAccess) "Enabled" else "Permission needed",
            if (usageAccess) "Manage" else "Enable"
        ) {
            context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        PermissionCard(
            "Notification receipts",
            "Receive notification events from selected apps when Android delivers them.",
            notificationAccess,
            if (notificationAccess) "Enabled" else "Permission needed",
            if (notificationAccess) "Manage" else "Enable"
        ) {
            context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text("Capture notification content", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (contentCaptureEnabled) "Enabled for selected apps" else "Metadata only",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = contentCaptureEnabled,
                        onCheckedChange = { enabled ->
                            contentCaptureEnabled = enabled
                            prefs.edit()
                                .putBoolean(LoccoNotificationObserver.CAPTURE_NOTIFICATION_CONTENT_KEY, enabled)
                                .apply()
                        }
                    )
                }
                Text(
                    "When enabled, Locco stores the title and message excerpt Android exposes for selected apps. " +
                        "Obvious one-time codes, credential-like values and payment-card-like numbers are filtered, " +
                        "but no automatic filter is perfect. Captured content stays in encrypted local storage.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text("Your selected apps", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    if (selectedNames.isEmpty()) "Nothing is observed until you choose apps."
                    else selectedNames.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            TextButton(onClick = { choosingApps = !choosingApps }) {
                Text(if (choosingApps) "Done" else "Choose apps")
            }
        }
        if (choosingApps) {
            OutlinedTextField(
                value = search, onValueChange = { search = it },
                modifier = Modifier.fillMaxWidth(), singleLine = true,
                label = { Text("Find an installed app") }
            )
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                if (filteredApps.isEmpty()) {
                    Text("No matching launchable apps found.", Modifier.padding(16.dp))
                } else {
                    Column {
                        filteredApps.forEachIndexed { index, app ->
                            if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                            ListItem(
                                headlineContent = { Text(app.label, maxLines = 1) },
                                supportingContent = { Text(app.version, style = MaterialTheme.typography.bodySmall) },
                                trailingContent = {
                                    Switch(checked = app.packageName in selected, onCheckedChange = { selectApp(app.packageName, it) })
                                }
                            )
                        }
                    }
                }
            }
            Text(
                "Selection stays on this device. Turning an app off stops future collection; existing history is kept.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Recent activity", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.weight(1f))
            Text("${events.size} shown", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        error?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
        if (events.isEmpty()) {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(if (selected.isEmpty()) "Choose your first app" else "Your timeline is ready", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (selected.isEmpty()) "Select an app and enable the Android signal you want to collect."
                        else "Use a selected app, then refresh to import its recent activity. Notification receipts appear when that app posts a notification.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (selected.isEmpty()) Button(onClick = { choosingApps = true }) { Text("Choose apps") }
                }
            }
        } else {
            events.forEach { event -> ActivityCard(event, labels[event.sourcePackage] ?: event.sourcePackage) { inspectedEvent = event } }
        }
        Spacer(Modifier.height(18.dp))
    }

    inspectedEvent?.let { event ->
        val title = when (event.eventType) {
            "notification_posted" -> "Notification received"
            "app_foregrounded" -> "App became visible"
            "app_backgrounded" -> "App left foreground"
            else -> event.eventType.replace('_', ' ').replaceFirstChar { it.uppercase() }
        }
        AlertDialog(
            onDismissRequest = { inspectedEvent = null },
            title = { Text(title) },
            text = {
                Column(
                    Modifier.fillMaxWidth().heightIn(max = 480.dp).verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    EventDetailLine("Source app", labels[event.sourcePackage] ?: event.sourcePackage)
                    EventDetailLine("Package", event.sourcePackage)
                    EventDetailLine("Occurred", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(Date(event.occurredAt)))
                    EventDetailLine("Received by Locco", DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.MEDIUM).format(Date(event.receivedAt)))
                    EventDetailLine("Acquisition method", event.acquisitionMethod)
                    event.category?.let { EventDetailLine("Notification category", it) }
                    if (event.eventType == "notification_posted") {
                        if (event.contentTitle == null && event.contentText == null) {
                            Text(
                                "This event contains metadata only. Notification content capture was off when it arrived; Locco cannot reconstruct older notification text.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text("Captured notification title", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(event.contentTitle ?: "No title exposed by Android", style = MaterialTheme.typography.bodyMedium)
                            Text("Captured notification text", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                            Text(event.contentText ?: "No message text exposed by Android", style = MaterialTheme.typography.bodyMedium)
                            event.contentHash?.let { EventDetailLine("Captured content SHA-256", it) }
                        }
                    } else {
                        Text(
                            "Android UsageEvents exposed only an app visibility transition. It did not expose the screen contents or an action you took.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    EventDetailLine("Fields withheld or redacted", event.redactedFields)
                    EventDetailLine("Event ID", event.id)
                }
            },
            confirmButton = {
                TextButton(onClick = { inspectedEvent = null }) { Text("Close") }
            }
        )
    }
}

@Composable
private fun Metric(value: String, label: String, modifier: Modifier = Modifier) {
    Surface(modifier, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f), shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun PermissionCard(
    title: String, detail: String, enabled: Boolean, status: String, action: String, onAction: () -> Unit
) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                Surface(
                    color = if (enabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(status, Modifier.padding(horizontal = 9.dp, vertical = 5.dp), style = MaterialTheme.typography.labelSmall)
                }
            }
            Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            TextButton(onClick = onAction, modifier = Modifier.align(Alignment.End)) { Text(action) }
        }
    }
}

@Composable
private fun ActivityCard(event: AppObservation, appName: String, onClick: () -> Unit) {
    val title = when (event.eventType) {
        "app_foregrounded" -> "App became visible"
        "app_backgrounded" -> "App left foreground"
        "notification_posted" -> if (event.contentText != null || event.contentTitle != null) "Notification received · content captured" else "Notification received · metadata only"
        else -> event.eventType.replace('_', ' ').replaceFirstChar { it.uppercase() }
    }
    val method = when (event.acquisitionMethod) {
        "android_usage_events" -> "Android UsageEvents"
        "android_notification_listener" -> "Android notification listener"
        else -> event.acquisitionMethod.replace('_', ' ')
    }
    Card(
        Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(Modifier.fillMaxWidth().padding(13.dp), horizontalArrangement = Arrangement.spacedBy(11.dp)) {
            Surface(Modifier.size(38.dp), color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Box(contentAlignment = Alignment.Center) {
                    Text(appName.firstOrNull()?.uppercase() ?: "•", fontWeight = FontWeight.SemiBold)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(appName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(title, style = MaterialTheme.typography.bodyMedium)
                Text(
                    "$method · ${event.category ?: "No category"}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(event.occurredAt)),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text("Tap to inspect event and captured evidence", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun EventDetailLine(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(label, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun discoverLaunchableApps(packageManager: PackageManager, ownPackage: String): List<InstalledAppSource> {
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(intent, 0).mapNotNull { resolve ->
        val packageName = resolve.activityInfo?.packageName ?: return@mapNotNull null
        if (packageName == ownPackage) return@mapNotNull null
        val appInfo = resolve.activityInfo.applicationInfo
        val label = runCatching { appInfo.loadLabel(packageManager).toString() }.getOrDefault(packageName)
        val version = runCatching {
            packageManager.getPackageInfo(packageName, 0).versionName?.takeIf { it.isNotBlank() } ?: "Version unknown"
        }.getOrDefault("Version unavailable")
        InstalledAppSource(packageName, label, version)
    }.distinctBy { it.packageName }.sortedBy { it.label.lowercase() }
}

private fun notificationAccessEnabled(context: Context): Boolean {
    val enabled = Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    val expected = ComponentName(context, LoccoNotificationObserver::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
}
