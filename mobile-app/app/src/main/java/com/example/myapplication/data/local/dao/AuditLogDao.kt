package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.myapplication.data.model.AuditLogEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface AuditLogDao {
    @Insert
    suspend fun insertLog(log: AuditLogEntry)

    @Query("SELECT * FROM audit_logs WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    fun getLogsForIncident(incidentId: String): Flow<List<AuditLogEntry>>

    @Query("SELECT * FROM audit_logs WHERE incidentId = :incidentId ORDER BY timestamp ASC")
    suspend fun getLogsForIncidentList(incidentId: String): List<AuditLogEntry>
}
