package me.ash.reader.domain.model.account

import androidx.room.TypeConverter
import me.ash.reader.infrastructure.preference.UnifiedPushEnabledPreference

class UnifiedPushEnabledConverters {

    @TypeConverter
    fun toUnifiedPushEnabled(value: Boolean): UnifiedPushEnabledPreference {
        return when (value) {
            true -> UnifiedPushEnabledPreference.On
            false -> UnifiedPushEnabledPreference.Off
        }
    }

    @TypeConverter
    fun fromUnifiedPushEnabled(preference: UnifiedPushEnabledPreference): Boolean {
        return preference.value
    }
}
