package com.locco.app

import android.content.ComponentName
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.Settings
import android.provider.Settings.Secure
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import java.text.DateFormat
import java.util.Date

private data class InstalledAppSource(
    val packageName: String,
    val label: String,
    val version: String
)

@Composable
internal fun ObserverScreen() {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences(LoccoNotificationObserver.OBSERVER_PREFS, android.content.Context.MODE_PRIVATE) }
    var observedPackages by remember {
        mutableStateOf(prefs.getStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, emptySet())?.toSet() ?: emptySet())
    }
    var events by remember { mutableStateOf(emptyList<AppObservation>()) }
    var hasAccess by remember { mutableStateOf(notificationAccessEnabled(context)) }
    var hasUsageAccess by remember { mutableStateOf(UsageStatsObserver.hasAccess(context)) }
    var error by remember { mutableStateOf<String?>(null) }
    var search by remember { mutableStateOf("") }
    val installedApps = remember(context) { discoverLaunchableApps(context.packageManager, context.packageName) }

    fun setObserved(packageName: String, enabled: Boolean) {
        val next = if (enabled) observedPackages + packageName else observedPackages - packageName
        observedPackages = next
        prefs.edit().putStringSet(LoccoNotificationObserver.OBSERVED_PACKAGES_KEY, next.toSet()).apply()
    }

    fun refresh() {
        hasAccess = notificationAccessEnabled(context)
        hasUsageAccess = UsageStatsObserver.hasAccess(context)
        runCatching { LoccoDatabase.create(context.applicationContext) }
            .onSuccess { db ->
                try {
                    if (hasUsageAccess) UsageStatsObserver.collect(context, observedPackages, db)
                    events = (db.readObservations() + db.readUsageObservations()).sortedByDescending { it.receivedAt }.take(100)
                    error = null
                } catch (failure: Throwable) {
                    error = failure.message ?: "Could not read local observations."
                } finally {
                    db.close()
                }
            }
            .onFailure { failure -> error = failure.message ?: "Encrypted observation storage unavailable." }
    }

    LaunchedEffect(Unit) { refresh() }

    val filteredApps = installedApps.filter {
        search.isBlank() || it.label.contains(search, ignoreCase = true) ||
            it.packageName.contains(search, ignoreCase = true)
    }

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 18.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("App observation", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.SemiBold)
        Text(
            "Choose which apps Locco may observe. This first adapter stores notification metadata only—not message text, titles or notification extras.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (hasAccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (hasAccess) "Permission active" else "Permission required",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                    Spacer(Modifier.weight(1f))
                    TextButton(onClick = { refresh() }) { Text("Refresh") }
                }
                Text("Android notification access", fontWeight = FontWeight.SemiBold)
                Text(
                    "Android controls this permission. Locco cannot read notifications until you enable it here.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text(if (hasAccess) "Manage permission" else "Enable permission") }
            }
        }

        Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = if (hasUsageAccess) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.errorContainer,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            if (hasUsageAccess) "Usage access active" else "Usage access required",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
                Text("App lifecycle observation", fontWeight = FontWeight.SemiBold)
                Text(
                    "With Usage Access, Locco can read Android foreground/background events for selected apps. It cannot see screen contents or in-app actions. History is collected when this Observer screen refreshes.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Button(
                    onClick = {
                        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) { Text(if (hasUsageAccess) "Manage usage access" else "Enable usage access") }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Installed apps", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            Text("${observedPackages.size} selected", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
        }
        OutlinedTextField(
            value = search,
            onValueChange = { search = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            placeholder = { Text("Find an app") }
        )
        if (filteredApps.isEmpty()) {
            Text("No matching launchable apps found.", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        } else {
            Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column {
                    filteredApps.forEachIndexed { index, app ->
                        if (index > 0) HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f))
                        ListItem(
                            headlineContent = { Text(app.label, maxLines = 1) },
                            supportingContent = {
                                Text(
                                    "${app.version} · ${app.packageName}",
                                    style = MaterialTheme.typography.bodySmall,
                                    maxLines = 1
                                )
                            },
                            trailingContent = {
                                Switch(
                                    checked = app.packageName in observedPackages,
                                    onCheckedChange = { setObserved(app.packageName, it) }
                                )
                            }
                        )
                    }
                }
            }
        }
        Text("Recent observations", style = MaterialTheme.typography.titleMedium)
        if (error != null) {
            Text(error!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
        }
        if (events.isEmpty()) {
            Text(
                "No observations yet. Grant the relevant Android permission, select an app, then refresh after it is used or sends a notification.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodySmall
            )
        } else {
            events.forEach { event ->
                Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        Text(event.sourcePackage, fontWeight = FontWeight.SemiBold)
                        Text(event.eventType.replace('_', ' '), style = MaterialTheme.typography.bodyMedium)
                        Text(
                            "${event.category ?: "Uncategorised"} · ${event.acquisitionMethod}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(DateFormat.getDateTimeInstance().format(Date(event.occurredAt)), style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
    }
}

private fun discoverLaunchableApps(
    packageManager: PackageManager,
    ownPackage: String
): List<InstalledAppSource> {
    val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return packageManager.queryIntentActivities(launcherIntent, 0)
        .mapNotNull { resolve ->
            val packageName = resolve.activityInfo?.packageName ?: return@mapNotNull null
            if (packageName == ownPackage) return@mapNotNull null
            val appInfo = resolve.activityInfo.applicationInfo
            val label = runCatching { appInfo.loadLabel(packageManager).toString() }
                .getOrDefault(packageName)
            val version = runCatching {
                packageManager.getPackageInfo(packageName, 0).versionName?.takeIf { it.isNotBlank() } ?: "Version unknown"
            }.getOrDefault("Version unavailable")
            InstalledAppSource(packageName, label, version)
        }
        .distinctBy { it.packageName }
        .sortedBy { it.label.lowercase() }
}

private fun notificationAccessEnabled(context: android.content.Context): Boolean {
    val enabled = Secure.getString(context.contentResolver, "enabled_notification_listeners") ?: return false
    val expected = ComponentName(context, LoccoNotificationObserver::class.java)
    return enabled.split(':').any { ComponentName.unflattenFromString(it) == expected }
}
