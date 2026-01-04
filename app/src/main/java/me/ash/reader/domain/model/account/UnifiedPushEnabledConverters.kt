package me.ash.reader.domain.model.account

import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import me.ash.reader.infrastructure.preference.UnifiedPushEnabledPreference

/**
 * Provide [TypeConverter] of [UnifiedPushEnabledPreference] for [RoomDatabase].
 */
class UnifiedPushEnabledConverters {

    @TypeConverter
    fun toUnifiedPushEnabled(unifiedPushEnabled: Boolean): UnifiedPushEnabledPreference {
        return UnifiedPushEnabledPreference.values.find { it.value == unifiedPushEnabled } ?: UnifiedPushEnabledPreference.default
    }

    @TypeConverter
    fun fromUnifiedPushEnabled(unifiedPushEnabled: UnifiedPushEnabledPreference): Boolean {
        return unifiedPushEnabled.value
    }
}
