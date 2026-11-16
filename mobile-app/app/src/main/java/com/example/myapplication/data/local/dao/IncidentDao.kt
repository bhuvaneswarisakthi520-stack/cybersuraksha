package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.model.Incident
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIncident(incident: Incident)

    @Update
    suspend fun updateIncident(incident: Incident)

    @Query("SELECT * FROM incidents WHERE isActive = 1 ORDER BY createdAt DESC LIMIT 1")
    fun getActiveIncident(): Flow<Incident?>

    @Query("SELECT * FROM incidents WHERE id = :id")
    suspend fun getIncidentById(id: String): Incident?

    @Query("SELECT * FROM incidents ORDER BY createdAt DESC")
    fun getAllIncidents(): Flow<List<Incident>>

    @Query("UPDATE incidents SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllIncidents()
}
