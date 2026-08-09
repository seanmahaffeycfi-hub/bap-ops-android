package com.seanmahaffey.bapops

import android.content.Context

class SettingsManager(context: Context) {

    private val prefs = context.getSharedPreferences("bapops_settings", Context.MODE_PRIVATE)

    var serverBaseUrl: String
        get() = prefs.getString("server_base_url", "") ?: ""
        set(value) = prefs.edit().putString("server_base_url", value).apply()

    var targetWifiSsid: String
        get() = prefs.getString("target_wifi_ssid", "") ?: ""
        set(value) = prefs.edit().putString("target_wifi_ssid", value).apply()

    var authToken: String
        get() = prefs.getString("auth_token", "") ?: ""
        set(value) = prefs.edit().putString("auth_token", value).apply()
}