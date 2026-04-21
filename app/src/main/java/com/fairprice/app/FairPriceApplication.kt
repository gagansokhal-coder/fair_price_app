package com.fairprice.app

import android.app.Application
import android.util.Log

/**
 * Custom Application class for PDS Fair Price App.
 *
 * Installs a global uncaught exception handler that logs crash details
 * before delegating to the default handler. This ensures crash stack traces
 * are captured in Logcat even when the app terminates abruptly.
 */
class FairPriceApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Install global crash logger
        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("FairPrice-CRASH", "═══════════════════════════════════════")
            Log.e("FairPrice-CRASH", "UNCAUGHT EXCEPTION on thread: ${thread.name}")
            Log.e("FairPrice-CRASH", "Exception: ${throwable.javaClass.simpleName}: ${throwable.message}")
            Log.e("FairPrice-CRASH", "Stack trace:", throwable)
            Log.e("FairPrice-CRASH", "═══════════════════════════════════════")

            // Delegate to the default handler to show the ANR/crash dialog
            defaultHandler?.uncaughtException(thread, throwable)
        }

        Log.d("FairPriceApp", "Application initialized with crash logger")
    }
}
