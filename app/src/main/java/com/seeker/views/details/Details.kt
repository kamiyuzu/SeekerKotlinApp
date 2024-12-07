package com.seeker.views.details

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.Bitmap
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import coil3.toBitmap
import com.google.android.gms.location.LocationServices
import com.seeker.external.services.AssetResult
import com.seeker.external.services.assetPost
import com.seeker.location.RequestLocationPermission
import com.seeker.location.fusedLocationProviderClient
import com.seeker.location.getCurrentLocation
import com.seeker.location.getLastUserLocation
import com.seeker.sharemanager.MimeType
import com.seeker.sharemanager.ShareFileModel
import com.seeker.sharemanager.rememberShareManager
import com.seeker.ui.theme.LocalSnackbarHostState
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Locale
import java.util.Random

class DetailsViewModel(): ViewModel() {
    suspend fun assetDetailsPost(username: String, latitude: Double, longitude: Double, set: String): AssetResult {
        try {
            val latitudeParsed = String.format(Locale.getDefault(), "%.3f", latitude)
            val longitudeParsed = String.format(Locale.getDefault(), "%.3f", longitude)
            return assetPost(username, latitudeParsed, longitudeParsed, set)
        } catch (e: Exception) {
            // Handle exceptions (like network failure)
            Log.println(Log.DEBUG,"LoginViewModel/login", "Error ${e.stackTraceToString()}")
            return AssetResult("", "", "", "", "", "", "")
        }
    }
}

@Composable
fun Share(text: String, context: Context, scope: CoroutineScope, imgBitmap: Bitmap?) {
    val randNo = Random().nextInt(100000)
    val cacheFile = File.createTempFile("IMG_$randNo", ".png", context.cacheDir)
    Log.println(Log.DEBUG, "Share", "Created" + cacheFile.path)
    val shareManager = rememberShareManager()

    Button(onClick = {
        scope.launch {
            val stream = ByteArrayOutputStream()
            imgBitmap?.compress(android.graphics.Bitmap.CompressFormat.PNG, 100, stream)
            val imageByteArray = stream.toByteArray()
            cacheFile.writeBytes(imageByteArray)
            Log.println(Log.DEBUG, "Share", "Writing image into " + cacheFile.path + "...")
            val shared = ShareFileModel(
                uri = FileProvider.getUriForFile(context, context.packageName + ".provider", cacheFile),
                mime = MimeType.IMAGE,
                text = text,
            )
            shareManager.shareFile(shared)
        }
    }) {
        Icon(imageVector = Icons.Default.Share, contentDescription = null)
        Text("Share", modifier = Modifier.padding(start = 8.dp))
    }
}

private fun setToCategoryName(setId: Int?): String {
    var categoryName = ""
    if (setId == 1) categoryName = "Robot whole body"
    if (setId == 2) categoryName = "Monster whole body"
    if (setId == 3) categoryName = "Robot head"
    if (setId == 4) categoryName = "Cat"
    if (setId == 5) categoryName = "Human technician"
    return categoryName
}

private fun setSharableText(setId: Int?, name: String, description: String): String {
    return "The asset: $name from category: ${setToCategoryName(setId)}.\n\nDescription: $description"
}

@Composable
fun AssetView(modifier: Modifier = Modifier, latitude: Double, longitude: Double, setId: Int?, name: String, description: String) {
    val width = LocalConfiguration.current.screenWidthDp
    val borderWidth = 10.dp
    var imgBitmap by remember { mutableStateOf<Bitmap?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.Start,
            modifier = modifier.fillMaxSize()
        ) {
            Row(modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.Absolute.Right
            ) {
                Share(
                    setSharableText(setId, name, description),
                    LocalContext.current,
                    coroutineScope,
                    imgBitmap
                )
            }
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("https://robohash.org/${String.format(Locale.getDefault(), "%.3f", latitude)},${String.format(Locale.getDefault(), "%.3f", longitude)}?set=set${setId}")
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .size(width.dp)
                    .padding(borderWidth)
                    .clip(CircleShape),
                contentScale = ContentScale.Fit,
                loading = {
                    CircularProgressIndicator()
                },
                onSuccess = { item ->
                    imgBitmap = item.result.image.toBitmap().asImageBitmap().asAndroidBitmap()
                }
            )
            Column(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = name,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Text(
                    text = setToCategoryName(setId),
                    modifier = Modifier.alpha(0.8f),
                )
                Text(
                    text = description,
                    modifier = Modifier
                        .padding(vertical = 12.dp)
                        .alpha(0.7f),
                    textAlign = TextAlign.Justify
                )
            }
        }
    }
}

@Composable
fun DetailsView(navController: NavHostController, mainViewModel: MainViewModel, setId: Int?) {
    val context = LocalContext.current
    fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)

    // State variables to manage location information and permission result text
    var locationText by remember { mutableStateOf("No location obtained :(") }
    var latitude by remember { mutableDoubleStateOf(0.0) }
    var longitude by remember { mutableDoubleStateOf(0.0) }
    var showPermissionResultText by remember { mutableStateOf(false) }
    var permissionResultText by remember { mutableStateOf("Permission granted...") }
    val snackBarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val detailsViewModel by remember { mutableStateOf(DetailsViewModel()) }
    var name by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        if (!mainViewModel.isLoggedIn) navController.navigateAndReplaceStartRoute(Screens.Login.name)
    }

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
                    coroutineScope.launch(Dispatchers.IO) {
                        val result = detailsViewModel.assetDetailsPost(mainViewModel.username, latitude, longitude, "$setId")
                        name = result.name
                        description = result.description
                        snackBarHostState.showSnackbar("Success getting current location ✅")
                    }
                    locationText = "Location using LAST-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                },
                onGetLastLocationFailed = { exception ->
                    showPermissionResultText = true
                    Log.println(Log.DEBUG, "LocationServices", exception.localizedMessage ?: "Error getting last location")
                    coroutineScope.launch {
                        snackBarHostState.showSnackbar("Error getting current location 🚧")
                    }
                    locationText = exception.localizedMessage ?: "Error getting current location 🚧"
                },
                onGetLastLocationIsNull = {
                    // Attempt to get the current user location
                    getCurrentLocation(
                        onGetCurrentLocationSuccess = {
                            Log.println(Log.DEBUG, "LocationServices", "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}")
                            coroutineScope.launch(Dispatchers.IO) {
                                val result = detailsViewModel.assetDetailsPost(mainViewModel.username, latitude, longitude, "$setId")
                                name = result.name
                                description = result.description
                                snackBarHostState.showSnackbar("Success getting last location ✅")
                            }
                            locationText = "Location using CURRENT-LOCATION: LATITUDE: ${it.first}, LONGITUDE: ${it.second}"
                        },
                        onGetCurrentLocationFailed = {
                            showPermissionResultText = true
                            Log.println(Log.DEBUG, "LocationServices", it.localizedMessage ?: "Error Getting Current Location")
                            coroutineScope.launch {
                                snackBarHostState.showSnackbar("Error getting last location 🚧")
                            }
                            locationText = it.localizedMessage ?: "Error getting last location 🚧"
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
        if (name.isNotEmpty()) {
            Spacer(modifier = Modifier.height(10.dp))
            AssetView(Modifier, latitude, longitude, setId, name, description)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DetailsViewPreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    SeekerTheme {
        DetailsView(navController = navController, mainViewModel = mainViewModel, setId = 1)
    }
}