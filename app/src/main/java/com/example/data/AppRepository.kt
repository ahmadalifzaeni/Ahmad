package com.example.data

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppRepository(private val context: Context) {

    suspend fun loadInstalledApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val mainIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }
        val resolved: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(mainIntent, 0)
        }

        val myPackage = context.packageName

        resolved
            .asSequence()
            .filter { it.activityInfo != null && it.activityInfo.packageName != myPackage }
            .distinctBy { "${it.activityInfo.packageName}/${it.activityInfo.name}" }
            .map { info ->
                val pkg = info.activityInfo.packageName
                val act = info.activityInfo.name
                val label = info.loadLabel(pm)?.toString()?.trim().orEmpty().ifEmpty { pkg }
                val iconBitmap = runCatching {
                    drawableToImageBitmap(info.loadIcon(pm), targetSizePx = 144)
                }.getOrNull()
                val appFlags = info.activityInfo.applicationInfo?.flags ?: 0
                val isSystem = (appFlags and ApplicationInfo.FLAG_SYSTEM) != 0

                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    iconBitmap = iconBitmap,
                    category = inferCategory(pkg, label),
                    isSystemApp = isSystem
                )
            }
            .sortedBy { it.label.lowercase() }
            .toList()
    }

    fun launchApp(app: InstalledApp): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
            }
            context.startActivity(intent)
            true
        }.getOrElse {
            runCatching {
                val fallback = context.packageManager.getLaunchIntentForPackage(app.packageName)
                if (fallback != null) {
                    fallback.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallback)
                    true
                } else {
                    false
                }
            }.getOrDefault(false)
        }
    }

    fun openAppInfo(packageName: String) {
        runCatching {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", packageName, null)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun requestUninstall(packageName: String) {
        runCatching {
            val intent = Intent(Intent.ACTION_DELETE).apply {
                data = Uri.parse("package:$packageName")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun openDefaultHomeSettings() {
        runCatching {
            val intent = Intent(Settings.ACTION_HOME_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }.onFailure {
            runCatching {
                val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }

    fun isDefaultLauncher(): Boolean {
        return runCatching {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.resolveActivity(
                    intent,
                    PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_DEFAULT_ONLY.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
            }
            resolveInfo?.activityInfo?.packageName == context.packageName
        }.getOrDefault(false)
    }

    fun pickDefaultDockApps(apps: List<InstalledApp>): List<InstalledApp> {
        if (apps.isEmpty()) return emptyList()
        val priorityKeywords = listOf(
            listOf("dialer", "phone", "telepon", "contacts", "call"),
            listOf("messaging", "mms", "whatsapp", "telegram", "sms", "pesan", "chat"),
            listOf("chrome", "browser", "firefox", "opera", "edge", "mi.globalbrowser"),
            listOf("camera", "kamera", "photos", "gallery", "galeri")
        )
        val picked = mutableListOf<InstalledApp>()
        for (group in priorityKeywords) {
            val match = apps.firstOrNull { app ->
                picked.none { it.packageName == app.packageName } &&
                    group.any { kw ->
                        app.packageName.lowercase().contains(kw) || app.label.lowercase().contains(kw)
                    }
            }
            if (match != null) picked.add(match)
        }
        for (app in apps) {
            if (picked.size >= 4) break
            if (picked.none { it.packageName == app.packageName }) {
                picked.add(app)
            }
        }
        return picked.take(4)
    }

    private fun inferCategory(pkg: String, label: String): AppCategory {
        val text = "${pkg.lowercase()} ${label.lowercase()}"
        return when {
            listOf("phone", "dialer", "contact", "telepon", "sms", "mms", "message", "pesan", "whatsapp", "telegram", "mail", "gmail", "chat", "discord", "instagram", "tiktok", "twitter", "facebook").any { text.contains(it) } ->
                AppCategory.COMMUNICATION
            listOf("camera", "kamera", "photo", "gallery", "galeri", "music", "spotify", "youtube", "video", "netflix", "audio", "podcast", "play.games").any { text.contains(it) } ->
                AppCategory.MEDIA
            listOf("calendar", "kalender", "note", "catatan", "docs", "drive", "sheets", "chrome", "browser", "keep", "tasks", "office", "pdf", "maps", "peta").any { text.contains(it) } ->
                AppCategory.PRODUCTIVITY
            else -> AppCategory.UTILITIES
        }
    }

    private fun drawableToImageBitmap(drawable: Drawable, targetSizePx: Int): ImageBitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val bmp = drawable.bitmap
            if (bmp.width <= targetSizePx && bmp.height <= targetSizePx) {
                return bmp.asImageBitmap()
            }
            return Bitmap.createScaledBitmap(bmp, targetSizePx, targetSizePx, true).asImageBitmap()
        }
        val width = targetSizePx.coerceAtLeast(1)
        val height = targetSizePx.coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap.asImageBitmap()
    }
}
