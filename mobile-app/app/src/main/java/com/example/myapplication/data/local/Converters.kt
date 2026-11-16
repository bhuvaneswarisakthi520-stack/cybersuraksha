package com.example.myapplication.data.local

import androidx.room.TypeConverter
import com.example.myapplication.data.model.EmergencyStage

class Converters {
    @TypeConverter
    fun fromEmergencyStage(stage: EmergencyStage): String {
        return stage.name
    }

    @TypeConverter
    fun toEmergencyStage(stage: String): EmergencyStage {
        return try {
            EmergencyStage.valueOf(stage)
        } catch (e: IllegalArgumentException) {
            EmergencyStage.YELLOW_CHECK
        }
    }
}
