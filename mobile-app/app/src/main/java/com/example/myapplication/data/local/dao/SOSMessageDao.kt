package com.example.myapplication.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.myapplication.data.model.SOSMessage

@Dao
interface SOSMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: SOSMessage)

    @Query("SELECT * FROM sos_queue WHERE isSent = 0 ORDER BY createdAt ASC")
    suspend fun getUnsentMessages(): List<SOSMessage>

    @Query("UPDATE sos_queue SET isSent = 1, sentAt = :sentAt WHERE id = :id")
    suspend fun markAsSent(id: String, sentAt: Long)

    @Query("DELETE FROM sos_queue WHERE isSent = 1 AND sentAt < :beforeTimestamp")
    suspend fun deleteOldSentMessages(beforeTimestamp: Long)
}
