package com.fairprice.app.utils

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREF_NAME = "FairPriceSession"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_OFFICER_ID = "officer_id"
        private const val KEY_OFFICER_NAME = "officer_name"
        private const val KEY_OFFICER_ROLE = "officer_role"
        private const val KEY_OFFICER_DESIGNATION = "officer_designation"
        private const val KEY_OFFICER_DISTRICT = "officer_district_name"
        private const val KEY_IS_OFFICER = "is_officer"
        private const val KEY_CITIZEN_NAME = "citizen_name"
        private const val KEY_CITIZEN_RATION_CARD = "citizen_ration_card"

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
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, userId)
            .putBoolean(KEY_IS_OFFICER, false)
            .apply()
    }

    fun saveOfficerAuthData(
        accessToken: String,
        officerId: String,
        name: String,
        role: String,
        designation: String,
        districtName: String?,
    ) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_USER_ID, officerId)
            .putString(KEY_OFFICER_ID, officerId)
            .putString(KEY_OFFICER_NAME, name)
            .putString(KEY_OFFICER_ROLE, role)
            .putString(KEY_OFFICER_DESIGNATION, designation)
            .putString(KEY_OFFICER_DISTRICT, districtName ?: "")
            .putBoolean(KEY_IS_OFFICER, true)
            .apply()
    }

    /** Save citizen profile details after registration */
    fun saveCitizenProfile(name: String, rationCardNo: String = "") {
        prefs.edit()
            .putString(KEY_CITIZEN_NAME, name)
            .putString(KEY_CITIZEN_RATION_CARD, rationCardNo)
            .apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)
    fun getUserId(): String? = prefs.getString(KEY_USER_ID, null)
    fun isOfficer(): Boolean = prefs.getBoolean(KEY_IS_OFFICER, false)
    fun getOfficerName(): String = prefs.getString(KEY_OFFICER_NAME, "") ?: ""
    fun getOfficerRole(): String = prefs.getString(KEY_OFFICER_ROLE, "") ?: ""
    fun getOfficerDesignation(): String = prefs.getString(KEY_OFFICER_DESIGNATION, "") ?: ""
    fun getOfficerDistrictName(): String = prefs.getString(KEY_OFFICER_DISTRICT, "") ?: ""
    fun getCitizenName(): String = prefs.getString(KEY_CITIZEN_NAME, "") ?: ""
    fun getCitizenRationCard(): String = prefs.getString(KEY_CITIZEN_RATION_CARD, "") ?: ""

    fun clearSession() {
        prefs.edit().clear().apply()
    }
}
