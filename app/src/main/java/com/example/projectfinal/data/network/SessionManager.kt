package com.example.projectfinal.data.network


import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.applicationContext.getSharedPreferences("pet_app_prefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
    }
    fun saveToken(token: String) {
        val editor = prefs.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }
    fun clearSession() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }
}