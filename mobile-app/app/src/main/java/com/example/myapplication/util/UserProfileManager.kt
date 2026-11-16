package com.example.myapplication.util

import android.content.Context
import android.content.SharedPreferences

/**
 * Manages persistent storage of the user profile, primary emergency contact,
 * and demographic details using SharedPreferences.
 * Ensures user details are permanently preserved across app restarts and logins.
 */
object UserProfileManager {

    private const val PREFS_NAME = "cybersuraksha_user_profile"

    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_PHONE = "user_phone"
    private const val KEY_PARENT_NAME = "parent_name"
    private const val KEY_PARENT_PHONE = "parent_phone"
    private const val KEY_STATE = "user_state"
    private const val KEY_DOB = "user_dob"
    private const val KEY_GENDER = "user_gender"
    private const val KEY_ABOUT_ME = "user_about_me"
    private const val KEY_E_CONTACT = "user_e_contact"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getUserName(context: Context): String =
        getPrefs(context).getString(KEY_USER_NAME, "User") ?: "User"

    fun setUserName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_USER_NAME, name).apply()
    }

    fun getUserPhone(context: Context): String =
        getPrefs(context).getString(KEY_USER_PHONE, "+91 9XXXXXXXXX") ?: "+91 9XXXXXXXXX"

    fun setUserPhone(context: Context, phone: String) {
        getPrefs(context).edit().putString(KEY_USER_PHONE, phone).apply()
    }

    fun getParentName(context: Context): String =
        getPrefs(context).getString(KEY_PARENT_NAME, "Primary Contact") ?: "Primary Contact"

    fun setParentName(context: Context, name: String) {
        getPrefs(context).edit().putString(KEY_PARENT_NAME, name).apply()
    }

    fun getParentPhone(context: Context): String =
        getPrefs(context).getString(KEY_PARENT_PHONE, "+91 9XXXXXXXXX") ?: "+91 9XXXXXXXXX"

    fun setParentPhone(context: Context, phone: String) {
        getPrefs(context).edit().putString(KEY_PARENT_PHONE, phone).apply()
    }

    fun getState(context: Context): String =
        getPrefs(context).getString(KEY_STATE, "State") ?: "State"

    fun setState(context: Context, state: String) {
        getPrefs(context).edit().putString(KEY_STATE, state).apply()
    }

    fun getDob(context: Context): String =
        getPrefs(context).getString(KEY_DOB, "DD-MM-YYYY") ?: "DD-MM-YYYY"

    fun setDob(context: Context, dob: String) {
        getPrefs(context).edit().putString(KEY_DOB, dob).apply()
    }

    fun getGender(context: Context): String =
        getPrefs(context).getString(KEY_GENDER, "Not Specified") ?: "Not Specified"

    fun setGender(context: Context, gender: String) {
        getPrefs(context).edit().putString(KEY_GENDER, gender).apply()
    }

    fun getAboutMe(context: Context): String =
        getPrefs(context).getString(KEY_ABOUT_ME, "Student") ?: "Student"

    fun setAboutMe(context: Context, aboutMe: String) {
        getPrefs(context).edit().putString(KEY_ABOUT_ME, aboutMe).apply()
    }

    fun getEContact(context: Context): String =
        getPrefs(context).getString(KEY_E_CONTACT, "Nil") ?: "Nil"

    fun setEContact(context: Context, eContact: String) {
        getPrefs(context).edit().putString(KEY_E_CONTACT, eContact).apply()
    }

    fun saveFullProfile(
        context: Context,
        userName: String,
        userPhone: String? = null,
        parentName: String,
        parentPhone: String,
        state: String? = null,
        dob: String? = null,
        gender: String? = null,
        aboutMe: String? = null
    ) {
        val editor = getPrefs(context).edit()
        editor.putString(KEY_USER_NAME, userName)
        userPhone?.let { editor.putString(KEY_USER_PHONE, it) }
        editor.putString(KEY_PARENT_NAME, parentName)
        editor.putString(KEY_PARENT_PHONE, parentPhone)
        editor.putString(KEY_E_CONTACT, parentPhone)
        state?.let { editor.putString(KEY_STATE, it) }
        dob?.let { editor.putString(KEY_DOB, it) }
        gender?.let { editor.putString(KEY_GENDER, it) }
        aboutMe?.let { editor.putString(KEY_ABOUT_ME, it) }
        editor.apply()
    }
}
