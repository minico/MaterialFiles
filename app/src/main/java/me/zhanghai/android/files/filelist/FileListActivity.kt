/*
 * Copyright (c) 2018 Hai Zhang <dreaming.in.code.zh@gmail.com>
 * All Rights Reserved.
 */

package me.zhanghai.android.files.filelist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.commit
import java8.nio.file.Path
import me.zhanghai.android.files.R
import me.zhanghai.android.files.app.AppActivity
import me.zhanghai.android.files.file.MimeType
import me.zhanghai.android.files.navigation.NavigationFragment
import me.zhanghai.android.files.util.createIntent
import me.zhanghai.android.files.util.extraPath
import me.zhanghai.android.files.util.putArgs

class FileListActivity : AppActivity() {
    private lateinit var fileListFragment: FileListFragment
    private lateinit var navigationFragment: NavigationFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.file_list_activity);
        // Calls ensureSubDecor().
        //findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            fileListFragment = FileListFragment().putArgs(FileListFragment.Args(intent))
            navigationFragment = NavigationFragment()
            supportFragmentManager.commit { add(R.id.navigation_fragment_container, navigationFragment)
                add(R.id.file_list_fragment_container, fileListFragment) }
        } else {
            navigationFragment = supportFragmentManager.findFragmentById(R.id.navigation_fragment_container)
                    as NavigationFragment
            fileListFragment = supportFragmentManager.findFragmentById(R.id.file_list_fragment_container)
                as FileListFragment
        }
        navigationFragment.listener = fileListFragment
    }

    override fun onBackPressed() {
        if (fileListFragment.onBackPressed()) {
            return
        }
        super.onBackPressed()
    }

    companion object {
        fun createViewIntent(path: Path): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_VIEW)
                .apply { extraPath = path }
    }

    class PickDirectoryContract : ActivityResultContract<Path?, Path?>() {
        override fun createIntent(context: Context, input: Path?): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .apply { input?.let { extraPath = it } }

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }

    class PickFileContract : ActivityResultContract<List<MimeType>, Path?>() {
        override fun createIntent(context: Context, input: List<MimeType>): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
                .setType(MimeType.ANY.value)
                .putExtra(Intent.EXTRA_MIME_TYPES, input.map { it.value }.toTypedArray())

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }
}
