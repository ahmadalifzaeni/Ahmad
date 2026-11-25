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
import android.provider.AlarmClock
import android.provider.CalendarContract
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
                val appInfo = info.activityInfo.applicationInfo
                val appFlags = appInfo?.flags ?: 0
                val isSystem = (appFlags and ApplicationInfo.FLAG_SYSTEM) != 0

                InstalledApp(
                    packageName = pkg,
                    activityName = act,
                    label = label,
                    iconBitmap = iconBitmap,
                    category = inferCategory(pkg, label, appInfo),
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

    fun openCalendarApp() {
        runCatching {
            val uri = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").build()
            val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        }
    }

    fun openClockApp() {
        runCatching {
            val intent = Intent(AlarmClock.ACTION_SHOW_ALARMS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
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

    fun launchMiBrowser(): Boolean {
        val miBrowserPackages = listOf(
            "com.mi.globalbrowser",
            "com.android.browser",
            "com.mi.globalbrowser.mini",
            "com.android.chrome"
        )
        for (pkg in miBrowserPackages) {
            val launchIntent = context.packageManager.getLaunchIntentForPackage(pkg)
            if (launchIntent != null) {
                val ok = runCatching {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
                    context.startActivity(launchIntent)
                    true
                }.getOrDefault(false)
                if (ok) return true
            }
        }
        return runCatching {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(webIntent)
            true
        }.getOrDefault(false)
    }

    fun openSystemGestureSettings() {
        val candidates = listOf(
            Intent().setComponent(
                ComponentName("com.android.settings", "com.android.settings.SubSettings")
            ).putExtra(":settings:show_fragment", "com.android.settings.gestures.SystemNavigationGestureSettings"),
            Intent(Settings.ACTION_HOME_SETTINGS),
            Intent(Settings.ACTION_DISPLAY_SETTINGS),
            Intent(Settings.ACTION_SETTINGS)
        )
        for (intent in candidates) {
            val ok = runCatching {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                true
            }.getOrDefault(false)
            if (ok) return
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
            listOf("whatsapp", "telegram", "messaging", "mms", "sms", "pesan"),
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

    fun pickCuratedHomeApps(
        visibleApps: List<InstalledApp>,
        dockPackages: Set<String>
    ): List<InstalledApp> {
        val pool = visibleApps.filterNot { dockPackages.contains(it.packageName) }
        val curated = mutableListOf<InstalledApp>()

        // Up to 2 top AI apps, up to 1 top Game app, and remaining top Essential apps -> max 4 clean icons on Home Page 0!
        pool.filter { it.category == AppCategory.AI }.take(2).forEach { curated.add(it) }
        pool.filter { it.category == AppCategory.GAMES }.take(1).forEach {
            if (curated.size < 4) curated.add(it)
        }
        pool.filter { it.category == AppCategory.ESSENTIALS }.forEach { app ->
            if (curated.size < 4 && curated.none { it.packageName == app.packageName }) {
                curated.add(app)
            }
        }
        pool.forEach { app ->
            if (curated.size < 4 && curated.none { it.packageName == app.packageName }) {
                curated.add(app)
            }
        }
        return curated.take(4)
    }

    private fun inferCategory(
        pkg: String,
        label: String,
        appInfo: ApplicationInfo?
    ): AppCategory {
        val pkgLower = pkg.lowercase()
        val labelLower = label.lowercase()
        val text = "$pkgLower $labelLower"

        // 1. AI & Assistants
        val aiKeywords = listOf(
            "openai", "chatgpt", "gemini", "bard", "claude", "anthropic",
            "perplexity", "deepseek", "copilot", "grok", "poe", "character.ai",
            "meta.ai", "midjourney", "cici", "qwen", "assistant", "aistudio",
            "bing", "lensa", "remini", "chatbot"
        )
        if (aiKeywords.any { text.contains(it) } ||
            labelLower == "ai" ||
            labelLower.endsWith(" ai") ||
            labelLower.startsWith("ai ")
        ) {
            return AppCategory.AI
        }

        // 2. Games (Android official ApplicationInfo.CATEGORY_GAME + popular game packages)
        val isAndroidGame = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            appInfo?.category == ApplicationInfo.CATEGORY_GAME
        } else {
            @Suppress("DEPRECATION")
            ((appInfo?.flags ?: 0) and ApplicationInfo.FLAG_IS_GAME) != 0
        }
        val gameKeywords = listOf(
            "mobile.legends", "mlbb", "pubg", "freefire", "dts.freefire",
            "genshin", "mihoyo", "hoyoverse", "honkai", "roblox", "minecraft",
            "miniclip", "eightballpool", "8ball", "supercell", "clash",
            "brawlstars", "codm", "activision", "garena", "efootball", "fifa",
            "fc.mobile", "valorant", "wildrift", "subway", "kiloo", "ea.game",
            "gameloft", "zynga", "play.games", "steam", "game", "arena", "chess",
            "stumble", "amongus", "billiard", "pool"
        )
        if (isAndroidGame || gameKeywords.any { text.contains(it) }) {
            return AppCategory.GAMES
        }

        // 3. Essentials (Important daily apps)
        val essentialKeywords = listOf(
            "whatsapp", "telegram", "phone", "dialer", "telepon", "contacts", "kontak",
            "mms", "sms", "message", "pesan", "chrome", "browser", "camera", "kamera",
            "gallery", "galeri", "photos", "settings", "pengaturan", "setelan",
            "gmail", "mail", "maps", "peta", "calendar", "kalender", "clock", "jam",
            "dana", "gopay", "gojek", "grab", "ovo", "bca", "mandiri", "bri", "bni",
            "livin", "brimo", "seabank", "shopee", "tokopedia", "notes", "catatan",
            "keep", "drive", "files", "filemanager", "calculator", "kalkulator"
        )
        if (essentialKeywords.any { text.contains(it) }) {
            return AppCategory.ESSENTIALS
        }

        // 4. Media & Social
        val mediaKeywords = listOf(
            "youtube", "spotify", "music", "musik", "tiktok", "instagram", "netflix",
            "disney", "vidio", "video", "twitch", "facebook", "twitter", "threads",
            "discord", "podcast", "audio", "recorder"
        )
        if (mediaKeywords.any { text.contains(it) }) {
            return AppCategory.MEDIA
        }

        return AppCategory.UTILITIES
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
