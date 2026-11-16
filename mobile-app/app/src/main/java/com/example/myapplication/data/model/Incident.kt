package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a single emergency incident with all captured context.
 *
 * Created when Emergency Mode is activated. Tracks GPS, battery, network,
 * current escalation stage, and evidence metadata throughout the incident lifecycle.
 */
@Entity(tableName = "incidents")
data class Incident(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val createdAt: Long = System.currentTimeMillis(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    val networkStatus: String = "unknown",
    val batteryLevel: Int = -1,
    val stage: EmergencyStage = EmergencyStage.YELLOW_CHECK,
    val isActive: Boolean = true,
    val resolvedAt: Long? = null,
    val evidenceHash: String? = null,
    val audioFilePath: String? = null
)
