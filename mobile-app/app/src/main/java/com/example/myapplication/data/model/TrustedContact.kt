package com.example.myapplication.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * A person the user trusts to be notified during an emergency.
 *
 * Contacts marked as [isEmergencyContact] receive SOS messages during Stage 2 escalation.
 * The [order] field determines notification priority (lower = notified first).
 */
@Entity(tableName = "trusted_contacts")
data class TrustedContact(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val phoneNumber: String,
    val isEmergencyContact: Boolean = false,
    val order: Int = 0
)
