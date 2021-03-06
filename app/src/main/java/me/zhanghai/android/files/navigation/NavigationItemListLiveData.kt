/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.navigation

import androidx.lifecycle.MediatorLiveData
import me.zhanghai.android.files.settings.Settings

object NavigationItemListLiveData : MediatorLiveData<List<NavigationItem?>>() {
    init {
        // Initialize value before we have any active observer.
        loadValue()
        addSource(Settings.STORAGES) { loadValue() }
        addSource(StandardDirectoriesLiveData) { loadValue() }
        addSource(Settings.BOOKMARK_DIRECTORIES) { loadValue() }
        addSource(Settings.RECENT_ACCESS_FILES) { loadValue() }
    }

    private fun loadValue() {
        value = navigationItems
    }
}
