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
        val recentAccessFiles = Settings.RECENT_ACCESS_FILES.valueCompat.toMutableList()
        val res: RecentAccessFile? = recentAccessFiles.find { it.path == recentAccessFile.path }
        if (res != null) {
            recentAccessFiles.remove(res)
        }

        recentAccessFiles.add(0, recentAccessFile)

        while (recentAccessFiles.count() > 3) {
            recentAccessFiles.removeAt(recentAccessFiles.count() - 1)
        }
        Settings.RECENT_ACCESS_FILES.putValue(recentAccessFiles)
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
