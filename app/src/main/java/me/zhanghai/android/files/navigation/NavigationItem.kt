/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.storage.StorageVolume
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import java8.nio.file.Path
import me.zhanghai.android.files.compat.getDrawableCompat
import me.zhanghai.android.files.file.DocumentTreeUri
import me.zhanghai.android.files.storage.Storage

abstract class NavigationItem {
    abstract val id: Long

    fun getIcon(context: Context): Drawable = context.getDrawableCompat(iconRes!!)

    @get:DrawableRes
    protected abstract val iconRes: Int?

    abstract fun getTitle(context: Context): String

    open fun getSubtitle(context: Context): String? = null

    open fun isChecked(listener: Listener): Boolean = false

    abstract fun onClick(listener: Listener)

    open fun onLongClick(listener: Listener): Boolean = false

    interface Listener {
        val currentPath: Path
        fun navigateTo(path: Path)
        fun openFile(path: Path, withChooser: Boolean)
        fun navigateToRoot(path: Path)
        fun onAddStorage()
        fun onEditStorage(storage: Storage)
        fun onEditBookmarkDirectory(bookmarkDirectory: BookmarkDirectory)
        fun startActivity(intent: Intent)
    }
}
