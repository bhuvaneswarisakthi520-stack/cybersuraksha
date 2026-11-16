package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.myapplication.data.model.TrustedContact
import kotlinx.coroutines.flow.Flow

@Dao
interface TrustedContactDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: TrustedContact)

    @Update
    suspend fun updateContact(contact: TrustedContact)

    @Delete
    suspend fun deleteContact(contact: TrustedContact)

    @Query("SELECT * FROM trusted_contacts ORDER BY `order` ASC")
    fun getAllContacts(): Flow<List<TrustedContact>>

    @Query("SELECT * FROM trusted_contacts WHERE isEmergencyContact = 1 ORDER BY `order` ASC")
    suspend fun getEmergencyContacts(): List<TrustedContact>

    @Query("SELECT COUNT(*) FROM trusted_contacts")
    fun getContactCount(): Flow<Int>
}
