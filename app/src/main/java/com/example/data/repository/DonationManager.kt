package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.concurrent.TimeUnit

class DonationManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("geochronicle_donation_prefs", Context.MODE_PRIVATE)

    private val _isSupporter = MutableStateFlow(prefs.getBoolean(KEY_IS_SUPPORTER, false))
    val isSupporter: StateFlow<Boolean> = _isSupporter.asStateFlow()

    private val _supporterTier = MutableStateFlow(prefs.getString(KEY_SUPPORTER_TIER, "") ?: "")
    val supporterTier: StateFlow<String> = _supporterTier.asStateFlow()

    private val _completedOperationsCount =
        MutableStateFlow(prefs.getInt(KEY_OPERATIONS_COUNT, 0))
    val completedOperationsCount: StateFlow<Int> = _completedOperationsCount.asStateFlow()

    init {
        // Record first install time if not present
        if (!prefs.contains(KEY_FIRST_INSTALL_TIME)) {
            prefs.edit().putLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis()).apply()
        }
    }

    fun recordAppLaunch() {
        val launches = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0) + 1
        prefs.edit().putInt(KEY_APP_LAUNCH_COUNT, launches).apply()
    }

    fun recordOperationCompleted() {
        val newCount = prefs.getInt(KEY_OPERATIONS_COUNT, 0) + 1
        prefs.edit().putInt(KEY_OPERATIONS_COUNT, newCount).apply()
        _completedOperationsCount.value = newCount
    }

    fun dismissHomeBanner() {
        prefs.edit().putLong(KEY_LAST_BANNER_DISMISSED_TIME, System.currentTimeMillis()).apply()
    }

    fun recordAutoPromptShown() {
        prefs.edit().putLong(KEY_LAST_AUTO_PROMPT_TIME, System.currentTimeMillis()).apply()
    }

    fun markAsSupporter(tier: String) {
        prefs.edit()
            .putBoolean(KEY_IS_SUPPORTER, true)
            .putString(KEY_SUPPORTER_TIER, tier)
            .putLong(KEY_SUPPORTER_SINCE_TIME, System.currentTimeMillis())
            .apply()
        _isSupporter.value = true
        _supporterTier.value = tier
    }

    fun shouldShowHomeBanner(): Boolean {
        if (_isSupporter.value) return false

        val operations = prefs.getInt(KEY_OPERATIONS_COUNT, 0)
        val launches = prefs.getInt(KEY_APP_LAUNCH_COUNT, 0)
        val firstInstall = prefs.getLong(KEY_FIRST_INSTALL_TIME, System.currentTimeMillis())
        val daysSinceInstall =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - firstInstall)
        val lastDismissed = prefs.getLong(KEY_LAST_BANNER_DISMISSED_TIME, 0L)
        val daysSinceDismissed =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastDismissed)

        // Criteria: 3+ ops, 3+ launches, 2+ days installed, and 14+ days since last dismissal
        return operations >= 3 && launches >= 3 && daysSinceInstall >= 2 && daysSinceDismissed >= 14
    }

    fun shouldShowAutoPrompt(): Boolean {
        if (_isSupporter.value) return false

        val operations = prefs.getInt(KEY_OPERATIONS_COUNT, 0)
        val lastAutoPrompt = prefs.getLong(KEY_LAST_AUTO_PROMPT_TIME, 0L)
        val daysSinceLastPrompt =
            TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - lastAutoPrompt)

        // Criteria: 7+ completed operations and 30+ days since last auto-prompt
        return operations >= 7 && daysSinceLastPrompt >= 30
    }

    companion object {
        private const val KEY_FIRST_INSTALL_TIME = "first_install_time"
        private const val KEY_APP_LAUNCH_COUNT = "app_launch_count"
        private const val KEY_OPERATIONS_COUNT = "completed_operations_count"
        private const val KEY_LAST_BANNER_DISMISSED_TIME = "last_banner_dismissed_time"
        private const val KEY_LAST_AUTO_PROMPT_TIME = "last_auto_prompt_time"
        private const val KEY_IS_SUPPORTER = "is_supporter"
        private const val KEY_SUPPORTER_TIER = "supporter_tier"
        private const val KEY_SUPPORTER_SINCE_TIME = "supporter_since_time"
    }
}
