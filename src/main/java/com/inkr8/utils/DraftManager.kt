package com.inkr8.utils

import android.content.Context

object DraftManager {
    private const val PREFS_NAME = "inkr8_drafts"
    
    private fun getPrefs(context: Context) = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveDraft(context: Context, key: String, text: String) {
        getPrefs(context).edit().putString(key, text).apply()
    }

    fun getDraft(context: Context, key: String): String {
        return getPrefs(context).getString(key, "") ?: ""
    }

    fun clearDraft(context: Context, key: String) {
        getPrefs(context).edit().remove(key).apply()
    }

    fun getDraftKey(gamemode: String, playmode: String, tournamentId: String?): String {
        return "draft_${gamemode}_${playmode}_${tournamentId ?: "none"}"
    }
}
