package com.snapp.android

import android.app.Application
import android.util.Log
import com.snapp.android.di.AndroidModule
import com.snapp.android.ui.widget.registerWidgets
import com.snapp.di.sharedKoinModules
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class SnappApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        try {
            startKoin {
                androidLogger()
                androidContext(this@SnappApplication)
                modules(sharedKoinModules(this@SnappApplication) + listOf(AndroidModule))
            }
            registerWidgets()
        } catch (e: Throwable) {
            Log.e(TAG, "onCreate failed", e)
            throw e
        }
    }

    companion object {
        private const val TAG = "SnappApplication"
    }
}
