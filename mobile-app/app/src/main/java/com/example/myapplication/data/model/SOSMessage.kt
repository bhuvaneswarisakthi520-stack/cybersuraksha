package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents an SOS message queued for delivery.
 *
 * When the device is offline, SOS messages are stored locally and retried
 * when network connectivity is restored. This ensures no emergency alert
 * is lost due to poor network conditions.
 */
@Entity(tableName = "sos_queue")
data class SOSMessage(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val incidentId: String,
    val contactPhone: String,
    val messageBody: String,
    val isSent: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val sentAt: Long? = null
)
