package com.example.myapplication.ui.screens.safepoints

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.myapplication.service.RealPlaceFetcher
import com.example.myapplication.ui.theme.*
import com.google.android.gms.location.LocationServices
import java.util.Locale

data class RealSafePointUiItem(
    val id: String,
    val name: String,
    val category: String, // "Police", "Hospital", "Helpline", "Safe Haven", "Pharmacy"
    val address: String,
    val searchQuery: String,
    val phone: String,
    val isOpen24x7: Boolean = true,
    val distanceKmStr: String = "Nearby"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SafePointsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedCategory by remember { mutableStateOf("All") }

    // Real GPS coordinates state
    var currentLat by remember { mutableDoubleStateOf(0.0) }
    var currentLng by remember { mutableDoubleStateOf(0.0) }
    var locationName by remember { mutableStateOf("Detecting GPS location...") }
    var isFetchingPlaces by remember { mutableStateOf(true) }

    var fetchedPlaces by remember { mutableStateOf<List<RealSafePointUiItem>>(emptyList()) }

    // Fetch real GPS location and call RealPlaceFetcher
    LaunchedEffect(Unit) {
        val hasPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            try {
                val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        currentLat = location.latitude
                        currentLng = location.longitude

                        // Reverse geocode to resolve neighborhood address
                        try {
                            val geocoder = Geocoder(context, Locale.getDefault())
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                geocoder.getFromLocation(location.latitude, location.longitude, 1) { addresses ->
                                    if (addresses.isNotEmpty()) {
                                        val addr = addresses[0]
                                        locationName = addr.subLocality ?: addr.locality ?: addr.adminArea ?: "GPS Active"
                                    }
                                }
                            } else {
                                @Suppress("DEPRECATION")
                                val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                                if (!addresses.isNullOrEmpty()) {
                                    val addr = addresses[0]
                                    locationName = addr.subLocality ?: addr.locality ?: addr.adminArea ?: "GPS Active"
                                }
                            }
                        } catch (e: Exception) {
                            locationName = "GPS (${String.format(Locale.US, "%.2f", currentLat)}, ${String.format(Locale.US, "%.2f", currentLng)})"
                        }
                    }
                }
            } catch (e: Exception) {
                locationName = "Location Standby"
            }
        } else {
            locationName = "Location Permission Required"
        }
    }

    // Reactive place fetching when Lat/Lng update
    LaunchedEffect(currentLat, currentLng) {
        isFetchingPlaces = true
        fetchedPlaces = RealPlaceFetcher.fetchNearbyRealSafePoints(currentLat, currentLng)
        isFetchingPlaces = false
    }

    val filteredPoints = if (selectedCategory == "All") {
        fetchedPlaces
    } else {
        fetchedPlaces.filter { it.category == selectedCategory }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Place, contentDescription = null, tint = SafeGreen)
                            Spacer(Modifier.width(8.dp))
                            Column {
                                Text("Real Safe Points Nearby", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("📍 $locationName", color = AccentTeal, fontSize = 11.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkNavy)
                )

                // Category Filter Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("All", "Police", "Hospital", "Helpline", "Safe Haven", "Pharmacy").forEach { cat ->
                        val isSelected = selectedCategory == cat
                        Surface(
                            onClick = { selectedCategory = cat },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) AccentTeal else CardBackground,
                            border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, TextSecondary.copy(alpha = 0.3f))
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) Color.Black else TextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        },
        containerColor = DarkNavy
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Live Google Maps Search Banner
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E3A2B)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            openGoogleMapsSearch(context, "emergency police hospital safe places", currentLat, currentLng)
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                "Open Live Google Maps Search",
                                color = SafeGreen,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                "View full interactive map with real-time turn-by-turn navigation",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, tint = SafeGreen, modifier = Modifier.size(18.dp))
                    }
                }
            }

            if (isFetchingPlaces) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(color = AccentTeal, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(12.dp))
                            Text("Fetching real nearby safe points around your GPS...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else {
                items(filteredPoints) { point ->
                    RealSafePointCard(
                        point = point,
                        onCall = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${point.phone}"))
                            context.startActivity(intent)
                        },
                        onDirections = {
                            openGoogleMapsSearch(context, point.searchQuery, currentLat, currentLng)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun RealSafePointCard(
    point: RealSafePointUiItem,
    onCall: () -> Unit,
    onDirections: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBackground),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Category Tag
                Surface(
                    color = when (point.category) {
                        "Police" -> EmergencyRed.copy(alpha = 0.2f)
                        "Hospital" -> SafeGreen.copy(alpha = 0.2f)
                        "Pharmacy" -> Color(0xFF2196F3).copy(alpha = 0.2f)
                        "Helpline" -> AccentCyan.copy(alpha = 0.2f)
                        else -> WarningAmber.copy(alpha = 0.2f)
                    },
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = point.category.uppercase(),
                        color = when (point.category) {
                            "Police" -> EmergencyRed
                            "Hospital" -> SafeGreen
                            "Pharmacy" -> Color(0xFF64B5F6)
                            "Helpline" -> AccentCyan
                            else -> WarningAmber
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Text(
                    text = "📍 ${point.distanceKmStr}",
                    color = AccentTeal,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(8.dp))

            Text(
                text = point.name,
                color = TextPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = point.address,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Call ${point.phone}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = onDirections,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentTeal),
                    border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Directions, contentDescription = null, tint = AccentTeal, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Google Maps", color = AccentTeal, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

/**
 * Opens Google Maps app or browser centered on real GPS coordinates with query.
 */
private fun openGoogleMapsSearch(context: Context, query: String, lat: Double, lng: Double) {
    val uriString = if (lat != 0.0 && lng != 0.0) {
        "geo:$lat,$lng?q=${Uri.encode(query)}"
    } else {
        "geo:0,0?q=${Uri.encode(query)}"
    }

    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uriString)).apply {
        setPackage("com.google.android.apps.maps")
    }

    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to web browser if Maps app isn't installed
        val webUrl = if (lat != 0.0 && lng != 0.0) {
            "https://www.google.com/maps/search/${Uri.encode(query)}/@$lat,$lng,15z"
        } else {
            "https://www.google.com/maps/search/${Uri.encode(query)}"
        }
        val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        context.startActivity(browserIntent)
    }
}
