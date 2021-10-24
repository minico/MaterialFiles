/*
 * Copyright (c) 2019 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import me.zhanghai.android.files.settings.Settings
import me.zhanghai.android.files.util.removeFirst
import me.zhanghai.android.files.util.valueCompat

object RecentAccessFiles {
    fun add(recentAccessFile: RecentAccessFile) {
        val recentAccessFile = Settings.RECENT_ACCESS_FILES.valueCompat.toMutableList()
            .apply { add(0, recentAccessFile) }
        while (recentAccessFile.count() > 3) {
            recentAccessFile.removeAt(recentAccessFile.count() - 1)
        }
        Settings.RECENT_ACCESS_FILES.putValue(recentAccessFile)
    }

    fun move(fromPosition: Int, toPosition: Int) {
        val recentAccessFiles = Settings.RECENT_ACCESS_FILES.valueCompat.toMutableList()
            .apply { add(toPosition, removeAt(fromPosition)) }
        Settings.RECENT_ACCESS_FILES.putValue(recentAccessFiles)
    }

    fun replace(recentAccessFile: RecentAccessFile) {
        val recentAccessFile = Settings.RECENT_ACCESS_FILES.valueCompat.toMutableList()
            .apply { this[indexOfFirst { it.id == recentAccessFile.id }] = recentAccessFile }
        Settings.RECENT_ACCESS_FILES.putValue(recentAccessFile)
    }

    fun remove(recentAccessFile: RecentAccessFile) {
        val recentAccessFile = Settings.RECENT_ACCESS_FILES.valueCompat.toMutableList()
            .apply { removeFirst { it.id == recentAccessFile.id } }
        Settings.RECENT_ACCESS_FILES.putValue(recentAccessFile)
    }
}
