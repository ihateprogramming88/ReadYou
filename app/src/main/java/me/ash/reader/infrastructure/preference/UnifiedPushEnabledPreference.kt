package me.ash.reader.infrastructure.preference

import android.content.Context
import me.ash.reader.R
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

sealed class UnifiedPushEnabledPreference(val value: Boolean) {

    object On : UnifiedPushEnabledPreference(true)
    object Off : UnifiedPushEnabledPreference(false)

    fun put(accountId: Int, viewModel: AccountViewModel) {
        viewModel.update(accountId) { unifiedPushEnabled = this@UnifiedPushEnabledPreference }
    }

    fun toDesc(context: Context): String =
        when (this) {
            On -> context.getString(R.string.on)
            Off -> context.getString(R.string.off)
        }

    companion object {
        val default = Off
        val values = listOf(On, Off)
    }
}

operator fun UnifiedPushEnabledPreference.not(): UnifiedPushEnabledPreference =
    when (value) {
        true -> UnifiedPushEnabledPreference.Off
        false -> UnifiedPushEnabledPreference.On
    }
