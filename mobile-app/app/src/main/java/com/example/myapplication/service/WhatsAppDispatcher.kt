package com.example.myapplication.service

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.net.URLEncoder

/**
 * Robust WhatsApp Dispatcher supporting official WhatsApp, WhatsApp Business,
 * multi-file evidence sharing, and fallback web deep links.
 */
object WhatsAppDispatcher {

    private const val TAG = "WhatsAppDispatcher"
    private const val PACKAGE_WHATSAPP = "com.whatsapp"
    private const val PACKAGE_WHATSAPP_BUSINESS = "com.whatsapp.w4b"

    /**
     * Resolves which WhatsApp app is installed on the user's phone.
     * Returns "com.whatsapp", "com.whatsapp.w4b", or null if neither detected.
     */
    private fun getInstalledWhatsAppPackage(context: Context): String? {
        val pm = context.packageManager
        return try {
            pm.getPackageInfo(PACKAGE_WHATSAPP, 0)
            PACKAGE_WHATSAPP
        } catch (e: Exception) {
            try {
                pm.getPackageInfo(PACKAGE_WHATSAPP_BUSINESS, 0)
                PACKAGE_WHATSAPP_BUSINESS
            } catch (e2: Exception) {
                null
            }
        }
    }

    /**
     * Formats phone number into international format without symbols (e.g. +91 9876543210 -> 919876543210).
     */
    fun formatPhoneNumberForWhatsApp(phone: String): String {
        val clean = phone.replace(Regex("[^0-9]"), "")
        return if (clean.length == 10) "91$clean" else clean
    }

    /**
     * Sends emergency text alert directly to WhatsApp contact.
     */
    fun sendWhatsAppTextMessage(context: Context, contactPhone: String, textMessage: String) {
        val formattedPhone = formatPhoneNumberForWhatsApp(contactPhone)
        val waPackage = getInstalledWhatsAppPackage(context)

        try {
            val encodedMsg = URLEncoder.encode(textMessage, "UTF-8")
            val uri = Uri.parse("https://api.whatsapp.com/send?phone=$formattedPhone&text=$encodedMsg")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                if (waPackage != null) {
                    setPackage(waPackage)
                }
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "Opened WhatsApp text message for $formattedPhone via package ${waPackage ?: "any"}")
        } catch (e: Exception) {
            Log.w(TAG, "Direct package WhatsApp launch failed, trying browser wa.me link", e)
            openWhatsAppBrowserLink(context, contactPhone, textMessage)
        }
    }

    /**
     * Shares evidence files (audio/video) + emergency summary directly to WhatsApp.
     */
    fun sendEvidenceFilesViaWhatsApp(
        context: Context,
        contactPhone: String,
        audioFile: File?,
        videoFile: File?,
        cloudLink: String? = null,
        customCaption: String? = null
    ) {
        val formattedPhone = formatPhoneNumberForWhatsApp(contactPhone)
        val waPackage = getInstalledWhatsAppPackage(context)
        val uris = ArrayList<Uri>()

        // Collect valid evidence files
        audioFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                try {
                    val uri = FileProvider.getUriForFile(context, "com.example.myapplication.fileprovider", file)
                    uris.add(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "FileProvider failed for audio file ${file.name}", e)
                }
            }
        }

        videoFile?.let { file ->
            if (file.exists() && file.length() > 0) {
                try {
                    val uri = FileProvider.getUriForFile(context, "com.example.myapplication.fileprovider", file)
                    uris.add(uri)
                } catch (e: Exception) {
                    Log.e(TAG, "FileProvider failed for video file ${file.name}", e)
                }
            }
        }

        val linkInfo = if (!cloudLink.isNull_or_empty()) "\n\n📁 Cloud Link: $cloudLink" else ""
        val caption = customCaption ?: "🚨 CyberSuraksha Emergency Evidence\nAttached: Audio/Video recordings.$linkInfo"

        if (uris.isNotEmpty()) {
            try {
                // Grant URI read permissions explicitly to WhatsApp package if known
                if (waPackage != null) {
                    for (u in uris) {
                        context.grantUriPermission(waPackage, u, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }

                val intent = if (uris.size == 1) {
                    Intent(Intent.ACTION_SEND).apply {
                        type = "*/*"
                        putExtra(Intent.EXTRA_STREAM, uris[0])
                        putExtra(Intent.EXTRA_TEXT, caption)
                        if (formattedPhone.isNotEmpty()) {
                            putExtra("jid", "$formattedPhone@s.whatsapp.net")
                        }
                        if (waPackage != null) setPackage(waPackage)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                } else {
                    Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                        type = "*/*"
                        putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                        putExtra(Intent.EXTRA_TEXT, caption)
                        if (formattedPhone.isNotEmpty()) {
                            putExtra("jid", "$formattedPhone@s.whatsapp.net")
                        }
                        if (waPackage != null) setPackage(waPackage)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                }

                context.startActivity(intent)
                Log.i(TAG, "Successfully launched WhatsApp dispatch with ${uris.size} file(s)")
                Toast.makeText(context, "📱 WhatsApp opened to send evidence", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e(TAG, "Failed to launch WhatsApp media intent", e)
                sendWhatsAppTextMessage(context, contactPhone, caption)
            }
        } else {
            // No local files, send text alert directly
            if (formattedPhone.isNotEmpty()) {
                sendWhatsAppTextMessage(context, contactPhone, caption)
            } else {
                // Open WhatsApp chat chooser with text
                try {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, caption)
                        if (waPackage != null) setPackage(waPackage)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                    Toast.makeText(context, "📱 Choose contact in WhatsApp to send report", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to launch text share intent", e)
                    openWhatsAppBrowserLink(context, "", caption)
                }
            }
        }
    }

    private fun openWhatsAppBrowserLink(context: Context, phone: String, message: String) {
        try {
            val formattedPhone = formatPhoneNumberForWhatsApp(phone)
            val webUrl = "https://wa.me/$formattedPhone?text=${URLEncoder.encode(message, "UTF-8")}"
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            Log.i(TAG, "Opened wa.me browser link for $formattedPhone")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open wa.me fallback link", e)
        }
    }

    private fun String?.isNull_or_empty(): Boolean = this == null || this.trim().isEmpty()
}
