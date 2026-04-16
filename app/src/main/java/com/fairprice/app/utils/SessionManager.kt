package com.fairprice.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FairPriceSession"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"

        @Volatile
        private var INSTANCE: SessionManager? = null

        fun getInstance(context: Context): SessionManager {
            return INSTANCE ?: synchronized(this) {
                val instance = SessionManager(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }

    fun saveAuthData(accessToken: String, userId: String) {
        val editor = prefs.edit()
        editor.putString(KEY_ACCESS_TOKEN, accessToken)
        editor.putString(KEY_USER_ID, userId)
        editor.apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
