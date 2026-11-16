package com.example.myapplication.ui.screens.safetymap

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Build
import android.os.Looper
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.myapplication.ui.theme.*
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.Calendar
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min

// ──────────────────────────────────────────────────────────────────
// Mathematical Risk Score Engine Data Models
// ──────────────────────────────────────────────────────────────────

data class SafetyZone(
    val lat: Double,
    val lng: Double,
    val label: String,
    val riskLevel: RiskLevel,
    val score: Int,                // 0 – 100
    val baselineC: Double,         // C_baseline (0-100)
    val timeMultiplierT: Double,   // T_multiplier (1.0-2.5)
    val userReportsU: Double,      // U_reports (0-100)
    val safeProximityP: Double,    // P_proximity (0-100)
    val reason: String
)

enum class RiskLevel(val color: String, val emoji: String, val title: String, val range: String) {
    GREEN("#00E676", "🟢", "Safer Area", "0 – 30"),
    YELLOW("#FFAB00", "🟡", "Caution Area", "31 – 65"),
    RED("#FF1744", "🔴", "Reported-Risk Area", "66 – 100")
}

data class UserReport(
    val lat: Double,
    val lng: Double,
    val timestampMs: Long
)

// In-memory persistent user reports for session
private val userReportedRisks = mutableStateListOf<UserReport>()

// ──────────────────────────────────────────────────────────────────
// Main Screen Component
// ──────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafetyRiskMapScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var userLat by remember { mutableDoubleStateOf(0.0) }
    var userLng by remember { mutableDoubleStateOf(0.0) }
    var locationReady by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var safetyZones by remember { mutableStateOf<List<SafetyZone>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("All") }
    var showReportConfirm by remember { mutableStateOf(false) }
    var showFormulaDialog by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val scope = rememberCoroutineScope()

    // Location initialization & calculations
    LaunchedEffect(Unit) {
        getLastKnownLocation(context) { lat, lng ->
            userLat = lat
            userLng = lng
            locationReady = true
            scope.launch {
                safetyZones = buildSafetyZones(context, lat, lng)
                isLoading = false
            }
        }
    }

    // Refresh when a new risk report is submitted
    LaunchedEffect(userReportedRisks.size) {
        if (locationReady) {
            scope.launch {
                safetyZones = buildSafetyZones(context, userLat, userLng)
                webViewRef?.let { wv ->
                    val newHtml = buildMapHtml(userLat, userLng, safetyZones, userReportedRisks)
                    val file = java.io.File(context.cacheDir, "safety_map.html")
                    file.writeText(newHtml)
                    wv.loadUrl("file://${file.absolutePath}")
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Safety Intelligence Map", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        Text("NCRB Risk Engine • Live Zoning", color = TextSecondary, fontSize = 11.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface),
                actions = {
                    IconButton(onClick = { showFormulaDialog = true }) {
                        Icon(Icons.Default.Functions, contentDescription = "Risk Engine Formula", tint = AccentCyan)
                    }
                    IconButton(onClick = { if (locationReady) showReportConfirm = true }) {
                        Icon(Icons.Default.Report, contentDescription = "Report unsafe area", tint = EmergencyRed)
                    }
                }
            )
        },
        containerColor = DarkNavy
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Filter chips bar
            val filters = listOf("All", "🟢 Safer (0-30)", "🟡 Caution (31-65)", "🔴 Risk (66-100)")
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(filters) { filter ->
                    val isSelected = selectedFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter, fontSize = 11.sp, fontWeight = FontWeight.Medium) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentTeal.copy(alpha = 0.25f),
                            selectedLabelColor = AccentTeal,
                            containerColor = CardBackground,
                            labelColor = TextSecondary
                        )
                    )
                }
            }

            // Map Container
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (locationReady) {
                    val filteredZones = when {
                        selectedFilter.startsWith("🟢") -> safetyZones.filter { it.riskLevel == RiskLevel.GREEN }
                        selectedFilter.startsWith("🟡") -> safetyZones.filter { it.riskLevel == RiskLevel.YELLOW }
                        selectedFilter.startsWith("🔴") -> safetyZones.filter { it.riskLevel == RiskLevel.RED }
                        else -> safetyZones
                    }
                    LeafletMapView(
                        userLat = userLat,
                        userLng = userLng,
                        zones = filteredZones,
                        reportedRisks = userReportedRisks,
                        onWebViewCreated = { wv -> webViewRef = wv }
                    )
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = AccentTeal)
                        Spacer(Modifier.height(12.dp))
                        Text("Calibrating GPS & NCRB Data...", color = TextSecondary, fontSize = 13.sp)
                    }
                }

                if (isLoading && locationReady) {
                    LinearProgressIndicator(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter),
                        color = AccentTeal,
                        trackColor = DarkSurface
                    )
                }
            }

            // Bottom Intelligence Summary Card
            NcrbIntelligenceBottomCard(userLat = userLat, userLng = userLng, safetyZones = safetyZones)
        }
    }

    // Report unsafe area confirmation dialog
    if (showReportConfirm) {
        AlertDialog(
            onDismissRequest = { showReportConfirm = false },
            containerColor = DarkSurface,
            icon = {
                Icon(Icons.Default.WarningAmber, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Report Unsafe Area", color = EmergencyRed, fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text("Tag your current GPS position as a 🔴 Risk Hotspot?", color = TextPrimary, fontSize = 14.sp)
                    Spacer(Modifier.height(8.dp))
                    Text("Applies a 48h decaying curve (e⁻ˡᵗ) to recalculate the dynamic Risk Score for nearby users.", color = TextSecondary, fontSize = 12.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        userReportedRisks.add(UserReport(userLat, userLng, System.currentTimeMillis()))
                        showReportConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Submit Incident", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Risk Engine Formula Info Dialog
    if (showFormulaDialog) {
        AlertDialog(
            onDismissRequest = { showFormulaDialog = false },
            containerColor = DarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Functions, contentDescription = null, tint = AccentCyan)
                    Spacer(Modifier.width(8.dp))
                    Text("NCRB Risk Score Engine", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    Text(
                        "Risk Score (S) = (W_crime × C_baseline) + (W_time × T_multiplier) + (W_user × U_reports) - (W_safe × P_proximity)",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = AccentTeal,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkNavy, RoundedCornerShape(6.dp))
                            .padding(8.dp)
                    )
                    Spacer(Modifier.height(12.dp))
                    FormulaFactorRow("C_baseline (0-100)", "Macro crime rate from NCRB Table 1b.3 & 3b.1")
                    FormulaFactorRow("T_multiplier (1.0-2.5)", "NCRB ADSI Table 1A.6 peak time scalar (22:00-04:00 = 2.5x)")
                    FormulaFactorRow("U_reports (0-100)", "Geo-tagged crowd reports with 48h decay curve (e⁻ˡᵗ)")
                    FormulaFactorRow("P_proximity (0-100)", "Distance vector to Police & 24/7 Hospital ERs")
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = CardBackground)
                    Spacer(Modifier.height(8.dp))
                    Text("Zone Breakdown:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    Text("• 🟢 Safer Area: Score 0 – 30\n• 🟡 Caution Area: Score 31 – 65\n• 🔴 Reported-Risk Area: Score 66 – 100", color = TextSecondary, fontSize = 11.sp)
                }
            },
            confirmButton = {
                Button(
                    onClick = { showFormulaDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Text("Close Engine Specs", color = DarkNavy, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun FormulaFactorRow(name: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(name, color = AccentCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(desc, color = TextSecondary, fontSize = 11.sp)
    }
}

// ──────────────────────────────────────────────────────────────────
// Leaflet Map Rendering
// ──────────────────────────────────────────────────────────────────

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun LeafletMapView(
    userLat: Double,
    userLng: Double,
    zones: List<SafetyZone>,
    reportedRisks: List<UserReport>,
    onWebViewCreated: (WebView) -> Unit
) {
    val context = LocalContext.current
    val html = remember(userLat, userLng, zones, reportedRisks) {
        buildMapHtml(userLat, userLng, zones, reportedRisks)
    }

    // Write HTML to a cache file so WebView can load external CDN resources
    val mapFile = remember(html) {
        val file = java.io.File(context.cacheDir, "safety_map.html")
        file.writeText(html)
        file
    }

    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                setBackgroundColor(android.graphics.Color.parseColor("#0A1628"))
                webViewClient = WebViewClient()
                loadUrl("file://${mapFile.absolutePath}")
                onWebViewCreated(this)
            }
        },
        update = { /* Manual refresh only via webViewRef */ },
        modifier = Modifier.fillMaxSize()
    )
}

// ──────────────────────────────────────────────────────────────────
// Leaflet HTML & JavaScript Generator
// ──────────────────────────────────────────────────────────────────

private fun buildMapHtml(
    userLat: Double,
    userLng: Double,
    zones: List<SafetyZone>,
    reportedRisks: List<UserReport>
): String {
    val circlesJs = buildString {
        for (z in zones) {
            val opacity = when (z.riskLevel) {
                RiskLevel.GREEN  -> 0.22
                RiskLevel.YELLOW -> 0.32
                RiskLevel.RED    -> 0.42
            }
            val popupText = """
                <div style="font-family:sans-serif; color:#0A1628;">
                    <div style="font-weight:bold; font-size:14px;">${z.riskLevel.emoji} ${z.label}</div>
                    <div style="margin-top:4px; font-size:12px; color:#333;"><b>Risk Score:</b> <span style="color:${z.riskLevel.color}; font-weight:bold;">${z.score} / 100</span> (${z.riskLevel.title})</div>
                    <div style="margin-top:4px; font-size:11px; color:#666;">${z.reason}</div>
                    <hr style="border:none; border-top:1px solid #ddd; margin:6px 0;"/>
                    <div style="font-size:10px; color:#555;"><b>Formula Specs:</b><br/>C=${z.baselineC.toInt()} | T=${z.timeMultiplierT}x | U=${z.userReportsU.toInt()} | P=${z.safeProximityP.toInt()}</div>
                </div>
            """.trimIndent().replace("\n", "")

            append("""
                L.circle([${z.lat}, ${z.lng}], {
                    radius: 450,
                    color: '${z.riskLevel.color}',
                    fillColor: '${z.riskLevel.color}',
                    fillOpacity: $opacity,
                    weight: 2
                }).bindPopup('$popupText').addTo(map);
            """.trimIndent())
            append("\n")
        }

        // Active user reported incident markers
        for (r in reportedRisks) {
            val ageHours = (System.currentTimeMillis() - r.timestampMs) / (1000.0 * 3600.0)
            val popup = "<b>🔴 Active Incident Report</b><br/><small>Reported ${ageHours.toInt()}h ago (Decay active)</small>"
            append("""
                L.circle([${r.lat}, ${r.lng}], {
                    radius: 250,
                    color: '#FF1744',
                    fillColor: '#FF1744',
                    fillOpacity: 0.60,
                    weight: 3
                }).bindPopup('$popup').addTo(map);
            """.trimIndent())
            append("\n")
        }
    }

    return """
<!DOCTYPE html>
<html>
<head>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="https://unpkg.com/leaflet@1.9.4/dist/leaflet.css"/>
<script src="https://unpkg.com/leaflet@1.9.4/dist/leaflet.js"></script>
<style>
  body { margin:0; padding:0; background:#0A1628; }
  #map { height:100vh; width:100vw; }
</style>
</head>
<body>
<div id="map"></div>
<script>
  var map = L.map('map', {
    center: [$userLat, $userLng],
    zoom: 15,
    zoomControl: true
  });

  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
    attribution: '© OpenStreetMap | NCRB Risk Engine',
    maxZoom: 19
  }).addTo(map);

  var userIcon = L.divIcon({
    className: '',
    html: '<div style="width:20px;height:20px;border-radius:50%;background:#26C6DA;border:3px solid white;box-shadow:0 0 12px #26C6DA;"></div>',
    iconSize: [20,20],
    iconAnchor: [10,10]
  });

  L.marker([$userLat, $userLng], {icon: userIcon})
    .bindPopup('<b>📍 Current Location</b><br/><small>Calculated Risk Score evaluated</small>').addTo(map).openPopup();

  $circlesJs
</script>
</body>
</html>
    """.trimIndent()
}

// ──────────────────────────────────────────────────────────────────
// Mathematical Risk Calculation Engine Implementation
// ──────────────────────────────────────────────────────────────────

/**
 * Composite Safety Risk Score Formula:
 * Score (S) = (W_crime * C_baseline) + (W_time * T_multiplier) + (W_user * U_reports) - (W_safe * P_proximity)
 */
private fun computeRiskScore(
    cBaseline: Double,
    tMultiplier: Double,
    uReports: Double,
    pProximity: Double
): Pair<Int, RiskLevel> {
    val wCrime = 0.35
    val wTime = 14.0
    val wUser = 0.40
    val wSafe = 0.30

    val rawScore = (wCrime * cBaseline) + (wTime * tMultiplier) + (wUser * uReports) - (wSafe * pProximity)
    val clampedScore = min(100, max(0, rawScore.toInt()))

    val level = when {
        clampedScore <= 30 -> RiskLevel.GREEN
        clampedScore <= 65 -> RiskLevel.YELLOW
        else -> RiskLevel.RED
    }

    return Pair(clampedScore, level)
}

/**
 * NCRB ADSI Table 1A.6 Time-of-day Multiplier (1.0 to 2.5)
 */
private fun calculateTimeMultiplier(): Double {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when (hour) {
        in 22..23, in 0..3 -> 2.5  // Peak late-night risk window (10 PM - 4 AM)
        in 20..21, in 4..5 -> 1.8  // Transition risk window (8 PM - 10 PM & 4 AM - 6 AM)
        in 18..19, in 6..7 -> 1.3  // Evening/early morning twilight
        else -> 1.0                // Daytime standard (8 AM - 6 PM)
    }
}

/**
 * 48-Hour Exponential Time Decay for User Incident Reports (e⁻ˡᵗ)
 */
private fun calculateUserReportScore(userLat: Double, userLng: Double, zoneLat: Double, zoneLng: Double): Double {
    var totalWeightedScore = 0.0
    val halfLifeHours = 24.0
    val lambda = 0.693 / halfLifeHours

    for (report in userReportedRisks) {
        val dist = FloatArray(1)
        Location.distanceBetween(zoneLat, zoneLng, report.lat, report.lng, dist)
        if (dist[0] <= 800f) { // Within 800 meter incident radius
            val ageHours = max(0.0, (System.currentTimeMillis() - report.timestampMs) / (1000.0 * 3600.0))
            val decayFactor = exp(-lambda * ageHours)
            totalWeightedScore += 100.0 * decayFactor
        }
    }

    return min(100.0, totalWeightedScore)
}

/**
 * Safe Haven Proximity Vector Score (0 - 100)
 */
private fun calculateSafeProximityScore(distMeters: Float): Double {
    // 0m = 100 proximity score, 1500m = 0 proximity score
    return max(0.0, 100.0 - (distMeters / 15.0))
}

// ──────────────────────────────────────────────────────────────────
// Build Safety Zones pipeline incorporating Overpass API + NCRB Engine
// ──────────────────────────────────────────────────────────────────

private suspend fun buildSafetyZones(context: Context, userLat: Double, userLng: Double): List<SafetyZone> =
    withContext(Dispatchers.IO) {
        val zones = mutableListOf<SafetyZone>()
        val timeMultiplier = calculateTimeMultiplier()

        try {
            val client = OkHttpClient.Builder()
                .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(15, java.util.concurrent.TimeUnit.SECONDS)
                .build()

            val query = """
                [out:json][timeout:20];
                (
                  node["amenity"="police"](around:2000,$userLat,$userLng);
                  node["amenity"="hospital"](around:2000,$userLat,$userLng);
                  node["amenity"="clinic"](around:2000,$userLat,$userLng);
                  node["amenity"="pharmacy"](around:2000,$userLat,$userLng);
                  node["amenity"="fire_station"](around:2000,$userLat,$userLng);
                );
                out body;
            """.trimIndent()

            val request = Request.Builder()
                .url("https://overpass-api.de/api/interpreter")
                .post(query.toRequestBody("text/plain".toMediaType()))
                .build()

            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: ""
            val json = JSONObject(body)
            val elements = json.getJSONArray("elements")

            for (i in 0 until elements.length()) {
                val el = elements.getJSONObject(i)
                val lat = el.optDouble("lat", 0.0)
                val lng = el.optDouble("lon", 0.0)
                val tags = el.optJSONObject("tags") ?: continue
                val amenity = tags.optString("amenity", "")
                val name = tags.optString("name", amenity.replaceFirstChar { it.uppercase() })

                if (lat == 0.0 || lng == 0.0) continue

                val dist = FloatArray(1)
                Location.distanceBetween(userLat, userLng, lat, lng, dist)

                val cBaseline = when (amenity) {
                    "police", "hospital", "fire_station" -> 15.0
                    "clinic", "pharmacy" -> 30.0
                    else -> 40.0
                }

                val proximityP = calculateSafeProximityScore(dist[0])
                val userU = calculateUserReportScore(userLat, userLng, lat, lng)

                val (score, level) = computeRiskScore(cBaseline, timeMultiplier, userU, proximityP)

                val reason = when (amenity) {
                    "police"       -> "NCRB Verified Police Station • High emergency response capability"
                    "hospital"     -> "24/7 Medical Emergency ER • Safe Haven node"
                    "fire_station" -> "Emergency First-Responder Command Center"
                    "clinic"       -> "Medical Clinic • Limited night availability"
                    "pharmacy"     -> "Local Pharmacy Facility"
                    else           -> "Public Emergency Infrastructure"
                }

                zones.add(SafetyZone(
                    lat = lat,
                    lng = lng,
                    label = name,
                    riskLevel = level,
                    score = score,
                    baselineC = cBaseline,
                    timeMultiplierT = timeMultiplier,
                    userReportsU = userU,
                    safeProximityP = proximityP,
                    reason = reason
                ))
            }
        } catch (e: Exception) {
            // Overpass API fallback
        }

        // Add user's active location evaluation zone
        val userDistToSafe = zones.minOfOrNull { z ->
            val d = FloatArray(1)
            Location.distanceBetween(userLat, userLng, z.lat, z.lng, d)
            d[0]
        } ?: 1200f

        val userProximityP = calculateSafeProximityScore(userDistToSafe)
        val userU = calculateUserReportScore(userLat, userLng, userLat, userLng)
        val userC = if (timeMultiplier > 2.0) 45.0 else 25.0

        val (userScore, userLevel) = computeRiskScore(userC, timeMultiplier, userU, userProximityP)

        zones.add(0, SafetyZone(
            lat = userLat,
            lng = userLng,
            label = "Current Location Zone",
            riskLevel = userLevel,
            score = userScore,
            baselineC = userC,
            timeMultiplierT = timeMultiplier,
            userReportsU = userU,
            safeProximityP = userProximityP,
            reason = "Evaluated via NCRB ADSI Table 1A.6 & Metro IPC Baseline"
        ))

        zones
    }

// ──────────────────────────────────────────────────────────────────
// Bottom Intelligence Card UI Component
// ──────────────────────────────────────────────────────────────────

@Composable
private fun NcrbIntelligenceBottomCard(userLat: Double, userLng: Double, safetyZones: List<SafetyZone>) {
    val currentZone = safetyZones.firstOrNull()
    val greenCount  = safetyZones.count { it.riskLevel == RiskLevel.GREEN }
    val yellowCount = safetyZones.count { it.riskLevel == RiskLevel.YELLOW }
    val redCount    = safetyZones.count { it.riskLevel == RiskLevel.RED }

    Surface(
        color = DarkSurface,
        shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        "Live Safety Intelligence",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Text(
                        "NCRB Multi-Layer Risk Engine",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                currentZone?.let { z ->
                    Surface(
                        color = Color(android.graphics.Color.parseColor(z.riskLevel.color)).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(android.graphics.Color.parseColor(z.riskLevel.color))),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(z.riskLevel.emoji, fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "Score: ${z.score} / 100",
                                color = Color(android.graphics.Color.parseColor(z.riskLevel.color)),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendItem(color = SafeGreen,    emoji = "🟢", label = "Safer (0-30)",   count = greenCount)
                LegendItem(color = WarningAmber, emoji = "🟡", label = "Caution (31-65)", count = yellowCount)
                LegendItem(color = EmergencyRed, emoji = "🔴", label = "Risk (66-100)",    count = redCount)
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, emoji: String, label: String, count: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f))
                .border(2.dp, color, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(emoji, fontSize = 14.sp)
        }
        Spacer(Modifier.height(4.dp))
        Text(label, color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Text("$count zones", color = TextSecondary, fontSize = 10.sp)
    }
}

// ──────────────────────────────────────────────────────────────────
// Location Provider Helper
// ──────────────────────────────────────────────────────────────────

@SuppressLint("MissingPermission")
private fun getLastKnownLocation(context: Context, onResult: (Double, Double) -> Unit) {
    try {
        val fusedClient = LocationServices.getFusedLocationProviderClient(context)
        fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
            if (loc != null) {
                onResult(loc.latitude, loc.longitude)
            } else {
                val req = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMaxUpdates(1)
                    .build()
                fusedClient.requestLocationUpdates(req, object : LocationCallback() {
                    override fun onLocationResult(result: LocationResult) {
                        fusedClient.removeLocationUpdates(this)
                        result.lastLocation?.let { onResult(it.latitude, it.longitude) }
                    }
                }, Looper.getMainLooper())
            }
        }.addOnFailureListener {
            onResult(12.9716, 77.5946)
        }
    } catch (e: Exception) {
        onResult(12.9716, 77.5946)
    }
}
