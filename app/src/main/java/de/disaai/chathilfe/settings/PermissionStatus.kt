package de.disaai.chathilfe.settings

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import android.provider.Settings

enum class PermissionState { GRANTED, MISSING }

/**
 * No foreground service exists yet in Phase 2, so this is always PLANNED.
 * It only exists so the UI has a typed value instead of a hardcoded string.
 */
enum class FutureRequirementStatus { PLANNED }

data class PermissionStatus(
    val overlay: PermissionState,
    val usageAccess: PermissionState,
    val foregroundServiceNotification: FutureRequirementStatus = FutureRequirementStatus.PLANNED,
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

fun currentPermissionStatus(context: Context): PermissionStatus = PermissionStatus(
    overlay = checkOverlayPermission(context),
    usageAccess = checkUsageAccessPermission(context),
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
