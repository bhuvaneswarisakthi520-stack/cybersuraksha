package com.example.myapplication.service

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * Uploads audio/video evidence to file.io — a completely FREE temporary file hosting service.
 *
 * - No account or API key required
 * - Files auto-expire after 14 days (configurable)
 * - Returns a direct download link to send via SMS
 * - Supports files up to 2 GB
 */
object CloudUploader {

    private const val TAG = "CloudUploader"
    private const val UPLOAD_URL = "https://file.io/?expires=14d"

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(120, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    /**
     * Uploads a file to file.io and returns a temporary download URL (valid 14 days).
     *
     * @param file The audio (.m4a) or video (.mp4) evidence file.
     * @param incidentId Used for logging only.
     * @return A publicly accessible download URL string, or null if upload failed.
     */
    suspend fun uploadEvidenceFile(file: File, incidentId: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val fileSizeKb = file.length() / 1024
                Log.i(TAG, "Uploading ${file.name} ($fileSizeKb KB) for incident $incidentId...")

                val mimeType = when {
                    file.name.endsWith(".mp4") -> "video/mp4"
                    file.name.endsWith(".m4a") -> "audio/mp4"
                    else -> "application/octet-stream"
                }

                val requestBody = MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart(
                        "file",
                        file.name,
                        file.asRequestBody(mimeType.toMediaTypeOrNull())
                    )
                    .build()

                val request = Request.Builder()
                    .url(UPLOAD_URL)
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    if (json.optBoolean("success", false)) {
                        val link = json.optString("link")
                        Log.i(TAG, "Upload successful! Link: $link")
                        return@withContext link
                    } else {
                        Log.e(TAG, "file.io returned error: $responseBody")
                    }
                } else {
                    Log.e(TAG, "Upload failed. HTTP ${response.code}: $responseBody")
                }
                null
            } catch (e: Exception) {
                Log.e(TAG, "Exception during evidence upload", e)
                null
            }
        }
    }
}
