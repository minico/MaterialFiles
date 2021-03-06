/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import androidx.annotation.DrawableRes
import androidx.annotation.Size
import androidx.annotation.StringRes
import java8.nio.file.Path
import java8.nio.file.Paths
import me.zhanghai.android.files.R
import me.zhanghai.android.files.about.AboutActivity
import me.zhanghai.android.files.file.JavaFile
import me.zhanghai.android.files.file.asFileSize
import me.zhanghai.android.files.filelist.name
import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.settings.SettingsActivity
import me.zhanghai.android.files.storage.FileSystemRoot
import me.zhanghai.android.files.storage.Storage
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.valueCompat

val navigationItems: List<NavigationItem?>
    get() =
        mutableListOf<NavigationItem?>().apply {
            val bookmarkDirectoryItems = bookmarkDirectoryItems
            if (bookmarkDirectoryItems.isNotEmpty()) {
                add(DeviderItem("收藏夹"))
                addAll(bookmarkDirectoryItems)
            }

            val recentAccessFileItems = recentAcessFileItems
            if (recentAccessFileItems.isNotEmpty()) {
                add(DeviderItem("最近观看"))
                addAll(recentAccessFileItems)
            }

            if (Settings.FILE_LIST_ANIMATION.valueCompat) {
                add(DeviderItem("存储设备"))
                addAll(storageItems)
            }
            add(DeviderItem("其它"))
            add(AddStorageItem())
            addAll(menuItems)
        }

private val storageItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.STORAGES.valueCompat.filter { it.isVisible }.map { StorageItem(it) }

private abstract class PathItem(val path: Path) : NavigationItem() {
    override fun isChecked(listener: Listener): Boolean = listener.currentPath == path

    override fun onClick(listener: Listener) {
        if (this is NavigationRoot) {
            listener.navigateToRoot(path)
        } else if (path.name.contains(".")){
            listener.openFile(path, false)
        } else {
            listener.navigateTo(path)
        }
    }
}

private class StorageItem(
    private val storage: Storage
) : PathItem(storage.path), NavigationRoot {
    init {
        require(storage.isVisible)
    }

    override val id: Long
        get() = storage.id

    override val iconRes: Int
        @DrawableRes
        get() = storage.iconRes

    override fun getTitle(context: Context): String = storage.getName(context)

    override fun getSubtitle(context: Context): String? {
        val linuxPath = storage.linuxPath ?: return null
        var totalSpace = JavaFile.getTotalSpace(linuxPath)
        val freeSpace: Long
        when {
            totalSpace != 0L -> freeSpace = JavaFile.getFreeSpace(linuxPath)
            linuxPath == FileSystemRoot.LINUX_PATH -> {
                // Root directory may not be an actual partition on legacy Android versions (can be
                // a ramdisk instead). On modern Android the system partition will be mounted as
                // root instead so let's try with the system partition again.
                // @see https://source.android.com/devices/bootloader/system-as-root
                val systemPath = Environment.getRootDirectory().path
                totalSpace = JavaFile.getTotalSpace(systemPath)
                freeSpace = JavaFile.getFreeSpace(systemPath)
            }
            else -> freeSpace = 0
        }
        if (totalSpace == 0L) {
            return null
        }
        val freeSpaceString = freeSpace.asFileSize().formatHumanReadable(context)
        val totalSpaceString = totalSpace.asFileSize().formatHumanReadable(context)
        return context.getString(
            R.string.navigation_storage_subtitle_format, freeSpaceString, totalSpaceString
        )
    }

    override fun onLongClick(listener: Listener): Boolean {
        listener.onEditStorage(storage)
        return true
    }

    override fun getName(context: Context): String = getTitle(context)
}

private class AddStorageItem : NavigationItem() {
    override val id: Long = R.string.navigation_add_storage.toLong()

    @DrawableRes
    override val iconRes: Int = R.drawable.add_icon_white_24dp

    override fun getTitle(context: Context): String =
        context.getString(R.string.navigation_add_storage)

    override fun onClick(listener: Listener) {
        listener.onAddStorage()
    }
}

private val standardDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() =
        StandardDirectoriesLiveData.valueCompat
            .filter { it.isEnabled }
            .map { StandardDirectoryItem(it) }

private class StandardDirectoryItem(
    private val standardDirectory: StandardDirectory
) : PathItem(Paths.get(getExternalStorageDirectory(standardDirectory.relativePath))) {
    init {
        require(standardDirectory.isEnabled)
    }

    override val id: Long
        get() = standardDirectory.id

    override val iconRes: Int
        @DrawableRes
        get() = standardDirectory.iconRes

    override fun getTitle(context: Context): String = standardDirectory.getTitle(context)
}

val standardDirectories: List<StandardDirectory>
    get() {
        val settingsMap = Settings.STANDARD_DIRECTORY_SETTINGS.valueCompat.associateBy { it.id }
        return defaultStandardDirectories.map {
            val settings = settingsMap[it.key]
            if (settings != null) it.withSettings(settings) else it
        }
    }

private const val relativePathSeparator = ":"

private val defaultStandardDirectories: List<StandardDirectory>
    // HACK: Show QQ, TIM and WeChat standard directories based on whether the directory exists.
    get() =
        DEFAULT_STANDARD_DIRECTORIES.mapNotNull {
            when (it.iconRes) {
                R.drawable.qq_icon_white_24dp, R.drawable.tim_icon_white_24dp,
                R.drawable.wechat_icon_white_24dp -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        // Direct access to Android/data is blocked since Android 11.
                        null
                    } else {
                        for (relativePath in it.relativePath.split(relativePathSeparator)) {
                            val path = getExternalStorageDirectory(relativePath)
                            if (JavaFile.isDirectory(path)) {
                                return@mapNotNull it.copy(relativePath = relativePath)
                            }
                        }
                        null
                    }
                }
                else -> it
            }
        }

// @see android.os.Environment#STANDARD_DIRECTORIES
private val DEFAULT_STANDARD_DIRECTORIES = listOf(
    StandardDirectory(
        R.drawable.alarm_icon_white_24dp, R.string.navigation_standard_directory_alarms,
        Environment.DIRECTORY_ALARMS, false
    ),
    StandardDirectory(
        R.drawable.camera_icon_white_24dp, R.string.navigation_standard_directory_dcim,
        Environment.DIRECTORY_DCIM, true
    ),
    StandardDirectory(
        R.drawable.document_icon_white_24dp, R.string.navigation_standard_directory_documents,
        Environment.DIRECTORY_DOCUMENTS, false),
    StandardDirectory(
        R.drawable.download_icon_white_24dp, R.string.navigation_standard_directory_downloads,
        Environment.DIRECTORY_DOWNLOADS, true
    ),
    StandardDirectory(
        R.drawable.video_icon_white_24dp, R.string.navigation_standard_directory_movies,
        Environment.DIRECTORY_MOVIES, true
    ),
    StandardDirectory(
        R.drawable.audio_icon_white_24dp, R.string.navigation_standard_directory_music,
        Environment.DIRECTORY_MUSIC, true
    ),
    StandardDirectory(
        R.drawable.notification_icon_white_24dp,
        R.string.navigation_standard_directory_notifications, Environment.DIRECTORY_NOTIFICATIONS,
        false
    ),
    StandardDirectory(
        R.drawable.image_icon_white_24dp, R.string.navigation_standard_directory_pictures,
        Environment.DIRECTORY_PICTURES, true
    ),
    StandardDirectory(
        R.drawable.podcast_icon_white_24dp, R.string.navigation_standard_directory_podcasts,
        Environment.DIRECTORY_PODCASTS, false
    ),
    StandardDirectory(
        R.drawable.ringtone_icon_white_24dp, R.string.navigation_standard_directory_ringtones,
        Environment.DIRECTORY_RINGTONES, false
    ),
    StandardDirectory(
        R.drawable.qq_icon_white_24dp, R.string.navigation_standard_directory_qq,
        listOf("Android/data/com.tencent.mobileqq/Tencent/QQfile_recv", "Tencent/QQfile_recv")
            .joinToString(relativePathSeparator), true
    ),
    StandardDirectory(
        R.drawable.tim_icon_white_24dp, R.string.navigation_standard_directory_tim,
        listOf("Android/data/com.tencent.tim/Tencent/TIMfile_recv", "Tencent/TIMfile_recv")
            .joinToString(relativePathSeparator), true
    ),
    StandardDirectory(
        R.drawable.wechat_icon_white_24dp, R.string.navigation_standard_directory_wechat,
        listOf("Android/data/com.tencent.mm/MicroMsg/Download", "Tencent/MicroMsg/Download")
            .joinToString(relativePathSeparator), true
    )
)

internal fun getExternalStorageDirectory(relativePath: String): String =
    @Suppress("DEPRECATION")
    Environment.getExternalStoragePublicDirectory(relativePath).path

private val bookmarkDirectoryItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.BOOKMARK_DIRECTORIES.valueCompat.map { BookmarkDirectoryItem(it) }

private class BookmarkDirectoryItem(
    private val bookmarkDirectory: BookmarkDirectory
) : PathItem(bookmarkDirectory.path) {
    // We cannot simply use super.getId() because different bookmark directories may have
    // the same path.
    override val id: Long
        get() = bookmarkDirectory.id

    @DrawableRes
    override val iconRes: Int = R.drawable.directory_icon_white_24dp

    override fun getTitle(context: Context): String = bookmarkDirectory.name

    override fun onLongClick(listener: Listener): Boolean {
        listener.onEditBookmarkDirectory(bookmarkDirectory)
        return true
    }
}

private val recentAcessFileItems: List<NavigationItem>
    @Size(min = 0)
    get() = Settings.RECENT_ACCESS_FILES.valueCompat.map { RecentAccessFileItem(it) }

private class RecentAccessFileItem(
    private val recentAccessFile: RecentAccessFile
) : PathItem(recentAccessFile.path) {
    // We cannot simply use super.getId() because different bookmark directories may have
    // the same path.
    override val id: Long
        get() = recentAccessFile.id

    @DrawableRes
    override val iconRes: Int = R.drawable.file_video_icon

    override fun getTitle(context: Context): String {
        val idx = recentAccessFile.name.lastIndexOf(".")
        if (idx > 0) {
            return recentAccessFile.name.substring(0, idx)
        } else {
            return recentAccessFile.name
        }
    }

    override fun onLongClick(listener: Listener): Boolean {
        listener.openFile(path, true)
        return true
    }
}

private val menuItems: List<NavigationItem>
    @Size(2)
    get() = listOf(
        ActivityMenuItem(
            R.drawable.settings_icon_white_24dp, R.string.navigation_settings,
            SettingsActivity::class.createIntent()
        ),
        ActivityMenuItem(
            R.drawable.about_icon_white_24dp, R.string.navigation_about,
            AboutActivity::class.createIntent()
        )
    )

private abstract class MenuItem(
    @DrawableRes override val iconRes: Int,
    @StringRes val titleRes: Int
) : NavigationItem() {
    override fun getTitle(context: Context): String = context.getString(titleRes)
}

private class ActivityMenuItem(
    @DrawableRes iconRes: Int,
    @StringRes titleRes: Int,
    private val intent: Intent
) : MenuItem(iconRes, titleRes) {
    override val id: Long
        get() = intent.component.hashCode().toLong()

    override fun onClick(listener: Listener) {
        // TODO: startActivitySafe()?
        listener.startActivity(intent)
    }
}

public class DeviderItem (
    private var text: String
) : NavigationItem() {
    override fun getTitle(context: Context): String = text
    override val id: Long
        get() = text.hashCode().toLong()
    override val iconRes: Int?
        get() = -1

    override fun onClick(listener: Listener) {

    }
}
