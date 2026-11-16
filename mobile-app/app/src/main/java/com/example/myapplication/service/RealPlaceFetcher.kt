package com.example.myapplication.service

import android.location.Location
import android.util.Log
import com.example.myapplication.ui.screens.safepoints.RealSafePointUiItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit

/**
 * Fetches REAL, EXACT safe places (Police stations, Hospitals, Pharmacies, Shelters)
 * around the user's actual GPS coordinates using OpenStreetMap Overpass API.
 * Completely FREE, no API key required, highly accurate worldwide.
 */
object RealPlaceFetcher {

    private const val TAG = "RealPlaceFetcher"
    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    suspend fun fetchNearbyRealSafePoints(
        userLat: Double,
        userLng: Double
    ): List<RealSafePointUiItem> {
        return withContext(Dispatchers.IO) {
            if (userLat == 0.0 && userLng == 0.0) {
                return@withContext getFallbackHelplines()
            }

            try {
                // Overpass QL query: search police, hospital, pharmacy, shelter within 5km radius
                val query = """
                    [out:json][timeout:10];
                    (
                      node["amenity"="police"](around:5000,$userLat,$userLng);
                      way["amenity"="police"](around:5000,$userLat,$userLng);
                      node["amenity"="hospital"](around:5000,$userLat,$userLng);
                      way["amenity"="hospital"](around:5000,$userLat,$userLng);
                      node["amenity"="pharmacy"](around:5000,$userLat,$userLng);
                      node["social_facility"](around:5000,$userLat,$userLng);
                    );
                    out center 25;
                """.trimIndent()

                val url = "https://overpass-api.de/api/interpreter?data=" + java.net.URLEncoder.encode(query, "UTF-8")

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "CyberSuraksha-SafetyApp/1.0")
                    .build()

                val response = client.newCall(request).execute()
                val responseBody = response.body?.string()

                if (response.isSuccessful && responseBody != null) {
                    val json = JSONObject(responseBody)
                    val elements = json.optJSONArray("elements") ?: return@withContext getFallbackHelplines()

                    val placesList = mutableListOf<RealSafePointUiItem>()

                    for (i in 0 until elements.length()) {
                        val elem = elements.getJSONObject(i)
                        val tags = elem.optJSONObject("tags") ?: continue

                        // Extract name or fallback to category title
                        val rawName = tags.optString("name", "")
                        val amenity = tags.optString("amenity", "")
                        val social = tags.optString("social_facility", "")
                        val street = tags.optString("addr:street", "")
                        val suburb = tags.optString("addr:suburb", tags.optString("addr:city", ""))

                        val category = when {
                            amenity == "police" -> "Police"
                            amenity == "hospital" -> "Hospital"
                            amenity == "pharmacy" -> "Pharmacy"
                            social.isNotEmpty() || amenity == "shelter" -> "Safe Haven"
                            else -> "Emergency"
                        }

                        val name = if (rawName.isNotBlank()) rawName else "$category Station"

                        // Get exact place lat/lon
                        val placeLat = elem.optDouble("lat", elem.optJSONObject("center")?.optDouble("lat") ?: 0.0)
                        val placeLon = elem.optDouble("lon", elem.optJSONObject("center")?.optDouble("lon") ?: 0.0)

                        if (placeLat == 0.0 || placeLon == 0.0) continue

                        // Calculate exact distance in meters / kilometers
                        val distanceKmStr = calculateDistanceKm(userLat, userLng, placeLat, placeLon)

                        val addressStr = when {
                            street.isNotBlank() && suburb.isNotBlank() -> "$street, $suburb"
                            street.isNotBlank() -> street
                            suburb.isNotBlank() -> suburb
                            else -> "${String.format(Locale.US, "%.3f", placeLat)}, ${String.format(Locale.US, "%.3f", placeLon)}"
                        }

                        val phoneStr = tags.optString("phone", tags.optString("contact:phone", when (category) {
                            "Police" -> "112"
                            "Hospital" -> "108"
                            "Pharmacy" -> "102"
                            else -> "1091"
                        }))

                        placesList.add(
                            RealSafePointUiItem(
                                id = elem.optLong("id").toString(),
                                name = name,
                                category = category,
                                address = addressStr,
                                searchQuery = "$name $addressStr",
                                phone = phoneStr,
                                isOpen24x7 = true,
                                distanceKmStr = distanceKmStr
                            )
                        )
                    }

                    // Sort by exact distance (closest first)
                    val sortedList = placesList.sortedBy { parseDistanceValue(it.distanceKmStr) }

                    // Always prepend national emergency 24/7 helplines at top
                    return@withContext getNationalHelplines() + sortedList
                }
            } catch (e: Exception) {
                Log.e(TAG, "Overpass API fetch error", e)
            }

            return@withContext getFallbackHelplines()
        }
    }

    private fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): String {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
        val meters = results[0]
        return if (meters < 1000) {
            "${meters.toInt()} m"
        } else {
            String.format(Locale.US, "%.1f km", meters / 1000f)
        }
    }

    private fun parseDistanceValue(distStr: String): Float {
        return try {
            if (distStr.contains("m") && !distStr.contains("km")) {
                distStr.replace("m", "").trim().toFloat() / 1000f
            } else {
                distStr.replace("km", "").trim().toFloat()
            }
        } catch (e: Exception) {
            999f
        }
    }

    private fun getNationalHelplines(): List<RealSafePointUiItem> {
        return listOf(
            RealSafePointUiItem(
                id = "helpline_112",
                name = "National Emergency Helpline (112)",
                category = "Police",
                address = "24/7 Emergency Police Response & Dispatch",
                searchQuery = "police station",
                phone = "112",
                isOpen24x7 = true,
                distanceKmStr = "Instant"
            ),
            RealSafePointUiItem(
                id = "helpline_181",
                name = "Women Emergency Distress Helpline (181)",
                category = "Helpline",
                address = "24/7 National Women Safety Response Service",
                searchQuery = "women helpline center",
                phone = "181",
                isOpen24x7 = true,
                distanceKmStr = "Instant"
            )
        )
    }

    private fun getFallbackHelplines(): List<RealSafePointUiItem> {
        return getNationalHelplines() + listOf(
            RealSafePointUiItem(
                id = "fallback_hospital",
                name = "Nearest Emergency Hospital / Medical Center",
                category = "Hospital",
                address = "Tap Google Maps button below for exact nearby hospitals",
                searchQuery = "emergency hospital nearby",
                phone = "108",
                isOpen24x7 = true,
                distanceKmStr = "Nearby"
            ),
            RealSafePointUiItem(
                id = "fallback_police",
                name = "Nearest Local Police Station & Women Desk",
                category = "Police",
                address = "Tap Google Maps button below for exact nearby police stations",
                searchQuery = "police station nearby",
                phone = "112",
                isOpen24x7 = true,
                distanceKmStr = "Nearby"
            )
        )
    }
}
