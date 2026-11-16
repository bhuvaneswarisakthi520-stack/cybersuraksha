package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Audit log entry recording specific events during an emergency incident.
 */
@Entity(tableName = "audit_logs")
data class AuditLogEntry(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val incidentId: String,
    val timestamp: Long = System.currentTimeMillis(),
    val message: String,
    val hashSignature: String? = null
)
