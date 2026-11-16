package com.example.myapplication.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.myapplication.data.local.dao.AuditLogDao
import com.example.myapplication.data.local.dao.IncidentDao
import com.example.myapplication.data.local.dao.SOSMessageDao
import com.example.myapplication.data.local.dao.TrustedContactDao
import com.example.myapplication.data.model.AuditLogEntry
import com.example.myapplication.data.model.Incident
import com.example.myapplication.data.model.SOSMessage
import com.example.myapplication.data.model.TrustedContact

@Database(entities = [Incident::class, TrustedContact::class, SOSMessage::class, AuditLogEntry::class], version = 1)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun incidentDao(): IncidentDao
    abstract fun trustedContactDao(): TrustedContactDao
    abstract fun sosMessageDao(): SOSMessageDao
    abstract fun auditLogDao(): AuditLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
