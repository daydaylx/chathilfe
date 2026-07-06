package de.disaai.chathilfe.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.core.content.ContextCompat

enum class PermissionState { GRANTED, MISSING }

data class PermissionStatus(
    val overlay: PermissionState,
    val usageAccess: PermissionState,
    val notification: PermissionState,
)

fun checkOverlayPermission(context: Context): PermissionState =
    if (Settings.canDrawOverlays(context)) PermissionState.GRANTED else PermissionState.MISSING

fun checkUsageAccessPermission(context: Context): PermissionState {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = appOps.unsafeCheckOpNoThrow(
        AppOpsManager.OPSTR_GET_USAGE_STATS,
        Process.myUid(),
        context.packageName,
    )
    return if (mode == AppOpsManager.MODE_ALLOWED) PermissionState.GRANTED else PermissionState.MISSING
}

// POST_NOTIFICATIONS only exists as a runtime permission from API 33+; below that,
// notifications are implicitly allowed.
fun checkNotificationPermission(context: Context): PermissionState {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return PermissionState.GRANTED
    val granted = ContextCompat.checkSelfPermission(
        context,
        android.Manifest.permission.POST_NOTIFICATIONS,
    ) == PackageManager.PERMISSION_GRANTED
    return if (granted) PermissionState.GRANTED else PermissionState.MISSING
}

fun currentPermissionStatus(context: Context): PermissionStatus = PermissionStatus(
    overlay = checkOverlayPermission(context),
    usageAccess = checkUsageAccessPermission(context),
    notification = checkNotificationPermission(context),
)

fun overlayPermissionSettingsIntent(context: Context): Intent =
    Intent(
        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
        Uri.parse("package:${context.packageName}"),
    )

// ACTION_USAGE_ACCESS_SETTINGS does not reliably deep-link to a specific app across OEMs,
// unlike ACTION_MANAGE_OVERLAY_PERMISSION, so it intentionally opens the general list screen.
fun usageAccessSettingsIntent(): Intent =
    Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
