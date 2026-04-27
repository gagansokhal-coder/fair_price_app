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
        private const val KEY_CITIZEN_PHONE = "citizen_phone"
        private const val KEY_CITIZEN_ADDRESS = "citizen_address"
        private const val KEY_CITIZEN_DISTRICT = "citizen_district"
        private const val KEY_CITIZEN_SUBDISTRICT = "citizen_subdistrict"
        private const val KEY_CITIZEN_VILLAGE = "citizen_village"
        private const val KEY_PROFILE_COMPLETE = "profile_complete"
        private const val KEY_LANGUAGE = "app_language"

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
    fun saveCitizenProfile(
        name: String,
        rationCardNo: String = "",
        phone: String = "",
        address: String = "",
        districtName: String = "",
        subdistrictName: String = "",
        villageName: String = "",
    ) {
        prefs.edit()
            .putString(KEY_CITIZEN_NAME, name)
            .putString(KEY_CITIZEN_RATION_CARD, rationCardNo)
            .putString(KEY_CITIZEN_PHONE, phone)
            .putString(KEY_CITIZEN_ADDRESS, address)
            .putString(KEY_CITIZEN_DISTRICT, districtName)
            .putString(KEY_CITIZEN_SUBDISTRICT, subdistrictName)
            .putString(KEY_CITIZEN_VILLAGE, villageName)
            .putBoolean(KEY_PROFILE_COMPLETE, true)
            .apply()
    }

    /** Update only the editable citizen profile fields (name and address) */
    fun updateCitizenEditableFields(name: String, address: String) {
        prefs.edit()
            .putString(KEY_CITIZEN_NAME, name)
            .putString(KEY_CITIZEN_ADDRESS, address)
            .apply()
    }

    /** Check if a valid session exists (user has not logged out) */
    fun hasSession(): Boolean {
        return !getAccessToken().isNullOrEmpty()
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
    fun getCitizenPhone(): String = prefs.getString(KEY_CITIZEN_PHONE, "") ?: ""
    fun getCitizenAddress(): String = prefs.getString(KEY_CITIZEN_ADDRESS, "") ?: ""
    fun getCitizenDistrict(): String = prefs.getString(KEY_CITIZEN_DISTRICT, "") ?: ""
    fun getCitizenSubdistrict(): String = prefs.getString(KEY_CITIZEN_SUBDISTRICT, "") ?: ""
    fun getCitizenVillage(): String = prefs.getString(KEY_CITIZEN_VILLAGE, "") ?: ""

    /** Check if citizen profile is complete (used for profile gate on auto-login) */
    fun isProfileComplete(): Boolean = prefs.getBoolean(KEY_PROFILE_COMPLETE, false)

    /** Mark profile as complete */
    fun setProfileComplete(complete: Boolean) {
        prefs.edit().putBoolean(KEY_PROFILE_COMPLETE, complete).apply()
    }

    fun clearSession() {
        val lang = getLanguage() // preserve language across logouts
        prefs.edit().clear().apply()
        if (lang != "en") saveLanguage(lang)
    }

    /** Save the selected app language */
    fun saveLanguage(languageCode: String) {
        prefs.edit().putString(KEY_LANGUAGE, languageCode).apply()
    }

    /** Get the saved app language (defaults to English) */
    fun getLanguage(): String = prefs.getString(KEY_LANGUAGE, "en") ?: "en"
}
