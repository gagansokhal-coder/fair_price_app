package com.gagan.lokdiksha

import android.app.Application
import android.content.Context
import android.util.Log
import com.gagan.lokdiksha.utils.LocaleManager

/**
 * Custom Application class for PDS Lok Diksha App.
 *
 * Installs a global uncaught exception handler that logs crash details
 * before delegating to the default handler. This ensures crash stack traces
 * are captured in Logcat even when the app terminates abruptly.
 *
 * Also wraps the base context for locale support.
 */
class FairPriceApplication : Application() {

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(LocaleManager.wrapContext(base))
    }

    override fun onCreate() {
        super.onCreate()

        // Install global crash logger
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LokDiksha-CRASH", "═══════════════════════════════════════")
            Log.e("LokDiksha-CRASH", "UNCAUGHT EXCEPTION on thread: ${thread.name}")
            Log.e("LokDiksha-CRASH", "Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
            Log.e("LokDiksha-CRASH", "Stack trace:", throwable)
            Log.e("LokDiksha-CRASH", "═══════════════════════════════════════")

            // Delegate to the default handler to show the ANR/crash dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.d("LokDikshaApp", "Application initialized with crash logger")
    }
}
