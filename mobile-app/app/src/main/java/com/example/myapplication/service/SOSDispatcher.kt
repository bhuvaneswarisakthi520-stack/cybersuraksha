package com.example.myapplication.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Contact(val name: String, val phone: String)

class SOSDispatcher(private val context: Context) {

    fun sendSMSToContacts(
        contacts: List<Contact>,
        latitude: Double,
        longitude: Double,
        incidentId: String
    ) {
        val timeFormat = SimpleDateFormat("HH:mm:ss dd/MM/yy", Locale.getDefault())
        val formattedTime = timeFormat.format(Date())

        if (contacts.isEmpty()) {
            Log.w("SOSDispatcher", "No emergency contacts to notify!")
            Toast.makeText(
                context,
                "⚠️ No emergency contacts saved! Add contacts with ⭐ enabled.",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        @Suppress("DEPRECATION")
        val smsManager: SmsManager = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            context.getSystemService(SmsManager::class.java)
        } else {
            SmsManager.getDefault()
        }

        for (contact in contacts) {
            val locationUrl = if (latitude != 0.0 && longitude != 0.0)
                "https://maps.google.com/?q=$latitude,$longitude"
            else
                "Location unavailable"

            val message = "SOS! ${contact.name} needs HELP! $locationUrl Time: $formattedTime"

            try {
                val parts = smsManager.divideMessage(message)
                if (parts.size == 1) {
                    val sentIntent = PendingIntent.getBroadcast(
                        context,
                        contact.phone.hashCode(),
                        Intent("SMS_SENT_${contact.phone}"),
                        PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                    )
                    smsManager.sendTextMessage(contact.phone, null, message, sentIntent, null)
                } else {
                    val sentIntents = ArrayList<PendingIntent>(parts.size)
                    for (i in parts.indices) {
                        sentIntents.add(
                            PendingIntent.getBroadcast(
                                context,
                                contact.phone.hashCode() + i,
                                Intent("SMS_SENT_${contact.phone}_$i"),
                                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                            )
                        )
                    }
                    smsManager.sendMultipartTextMessage(contact.phone, null, parts, sentIntents, null)
                }
                Log.i("SOSDispatcher", "SMS dispatched to ${contact.name} (${contact.phone}), parts=${parts.size}")
                Toast.makeText(
                    context,
                    "✅ SOS SMS sent to ${contact.name}",
                    Toast.LENGTH_SHORT
                ).show()
            } catch (e: Exception) {
                Log.e("SOSDispatcher", "Failed to send SMS to ${contact.name} (${contact.phone})", e)
                Toast.makeText(
                    context,
                    "❌ SMS failed to ${contact.name}: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }

            // Send second urgent flash-style SMS to grab immediate attention
            try {
                val urgentMessage = "\u26a0\ufe0f URGENT SOS \u26a0\ufe0f - ${contact.name} IS IN DANGER! Call them NOW! \ud83d\udea8"
                smsManager.sendTextMessage(contact.phone, null, urgentMessage, null, null)
                Log.i("SOSDispatcher", "Urgent flash-style SMS dispatched to ${contact.name} (${contact.phone})")
            } catch (e: Exception) {
                Log.e("SOSDispatcher", "Failed to send urgent SMS to ${contact.name} (${contact.phone})", e)
            }
        }
    }

    fun makeEmergencyCall(phoneNumber: String) {
        val intent = Intent(Intent.ACTION_CALL).apply {
            data = Uri.parse("tel:$phoneNumber")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: SecurityException) {
            Log.e("SOSDispatcher", "Permission denied for CALL_PHONE", e)
        }
    }
}
