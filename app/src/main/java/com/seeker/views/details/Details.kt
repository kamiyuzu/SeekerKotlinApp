package com.seeker.views.details

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.google.android.gms.location.LocationServices
import com.seeker.location.RequestLocationPermission
import com.seeker.location.fusedLocationProviderClient
import com.seeker.location.getCurrentLocation
import com.seeker.location.getLastUserLocation
import com.seeker.ui.theme.LocalSnackbarHostState
import com.seeker.ui.theme.SeekerTheme
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.text.*

@Composable
fun DetailsView(navController: NavHostController, setId: Int?) {
    val context = LocalContext.current
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    val width = LocalConfiguration.current.screenWidthDp
    //val height = LocalConfiguration.current.screenHeightDp
    val borderWidth = 10.dp
    val rainbowColorsBrush = remember {
        Brush.sweepGradient(
            listOf(
                Color(0xFF9575CD),
                Color(0xFFBA68C8),
                Color(0xFFE57373),
                Color(0xFFFFB74D),
                Color(0xFFFFF176),
                Color(0xFFAED581),
                Color(0xFF4DD0E1),
                Color(0xFF9575CD)
            )
        )
    }

    // State variables to manage location information and permission result text
    var locationText by remember { mutableStateOf("No location obtained :(") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var showPermissionResultText by remember { mutableStateOf(false) }
    var permissionResultText by remember { mutableStateOf("Permission granted...") }
    val snackBarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    // Request location permission using a Compose function
    RequestLocationPermission(
        onPermissionGranted = {
            // Callback when permission is granted
            showPermissionResultText = true
            permissionResultText = "Permission granted..."
            // Attempt to get the last known user location
            getLastUserLocation(
                onGetLastLocationSuccess = {
                    latitude = it.first
                    longitude = it.second
                    Log.println(Log.DEBUG, "LocationServices", "Location using LAST-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}")
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Success getting last location ✅")
                    }
                    locationText =
                        "Location using LAST-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                },
                onGetLastLocationFailed = { exception ->
                    showPermissionResultText = true
                    Log.println(Log.DEBUG, "LocationServices", exception.localizedMessage ?: "Error getting last location")
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Error getting last location 🚧")
                    }
                    locationText =
                        exception.localizedMessage ?: "Error getting last location 🚧"
                },
                onGetLastLocationIsNull = {
                    // Attempt to get the current user location
                    getCurrentLocation(
                        onGetCurrentLocationSuccess = {
                            Log.println(Log.DEBUG, "LocationServices", "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}")
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Success getting current location ✅")
                            }
                            locationText =
                                "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                        },
                        onGetCurrentLocationFailed = {
                            showPermissionResultText = true
                            Log.println(Log.DEBUG, "LocationServices", it.localizedMessage ?: "Error Getting Current Location")
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error getting current location 🚧")
                            }
                            locationText = it.localizedMessage ?: "Error getting current location 🚧"
                        },
                        true,
                        context
                    )
                },
                context
            )
        },
        onPermissionDenied = {
            // Callback when permission is denied
            showPermissionResultText = true
            Log.println(Log.DEBUG, "LocationServices", "Permission denied 🥲")
            coroutineScope.launch {
                snackBarHostState.showSnackbar("Permission denied 🥲")
            }
            permissionResultText = "Permission denied 🥲"
        },
        onPermissionsRevoked = {
            // Callback when permission is revoked
            showPermissionResultText = true
            Log.println(Log.DEBUG, "LocationServices", "Permission revoked 😢")
            coroutineScope.launch {
                snackBarHostState.showSnackbar("Permission revoked 😢")
            }
            permissionResultText = "Permission revoked 😢"
        }
    )

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Column(
                verticalArrangement = Arrangement.Top,
                horizontalAlignment = Alignment.Start,
                modifier = Modifier.fillMaxSize()
            ) {
                AsyncImage(
                    model = "https://robohash.org/${String.format(Locale.getDefault(), "%.3f", latitude)},${String.format(Locale.getDefault(), "%.3f", longitude)}?set=set${setId}",
                    contentDescription = null,
                    modifier = Modifier
                        .size(width.dp)
//                        .border(
//                            BorderStroke(borderWidth, rainbowColorsBrush),
//                            CircleShape
//                        )
                        .padding(borderWidth)
                        .clip(CircleShape),
                    contentScale = ContentScale.Fit
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "Test name",
                        modifier = Modifier.padding(top = 8.dp),
                    )
                    Text(
                        text = "Test description",
                        modifier = Modifier.alpha(0.8f),
                    )
                    Text(
                        text = "Test about",
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .alpha(0.7f),
                        textAlign = TextAlign.Justify
                    )
                }
            }
        }
    }

}

@Preview(showBackground = true)
@Composable
fun DetailsViewPreview() {
    val navController = rememberNavController()
    SeekerTheme {
        DetailsView(navController = navController, setId = 1)
    }
}