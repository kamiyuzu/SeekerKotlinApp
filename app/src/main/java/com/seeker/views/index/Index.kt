package com.seeker.views.index

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.seeker.database.entities.AssetEntity
import com.seeker.external.services.AssetResult
import com.seeker.external.services.index
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.details.AssetView
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.main.DBViewModel
import com.seeker.views.main.JWTViewModel
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class IndexViewModel(mainViewModel: MainViewModel, dBViewModel: DBViewModel): ViewModel() {
    private var _assetList = MutableStateFlow(emptyList<AssetResult>())
    var assetList = _assetList.asStateFlow()

    init {
        viewModelScope.launch {
            fetchIndex(mainViewModel, dBViewModel)
        }
    }

    private fun fetchIndex(mainViewModel: MainViewModel, dBViewModel: DBViewModel) {
        viewModelScope.launch {
            try {
                val assets = index(mainViewModel.username)
                Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "Assets ${assets}")
                assets.forEach { asset ->
                    Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "asset $asset")
                    val insertResponse = dBViewModel.repo.insert(AssetEntity(id = asset.id.toInt(), username = asset.username, latitude = asset.latitude, longitude = asset.longitude, set = asset.set))
                    Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "insert_response $insertResponse")
                }
                _assetList.tryEmit(assets)
            } catch (e: Exception) {
                // Handle exceptions (like network failure)
                Log.println(Log.INFO,"IndexViewModel/fetchIndex", "Error ${e.stackTraceToString()}")
                dBViewModel.repo.getAllAssets().flowOn(IO).collect {
                    val assetResultList = it.map { assetIt ->
                        AssetResult(assetIt.id.toString(), assetIt.username, assetIt.set, assetIt.latitude, assetIt.longitude, assetIt.name, assetIt.description, assetIt.tag)
                    }
                    _assetList.value = assetResultList
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Composable
fun IndexView(navController: NavHostController, mainViewModel: MainViewModel, dBViewModel: DBViewModel) {
    val context = LocalContext.current
    var hasRequestedPermission by remember { mutableStateOf(false) }
    var hasRequestedNotificaionPermission by remember { mutableStateOf(false) }
    var permissionRequestCompleted by remember { mutableStateOf(false) }
    var permissionNotificationRequestCompleted by remember { mutableStateOf(false) }

    val assets: List<AssetResult> by IndexViewModel(mainViewModel, dBViewModel).assetList.collectAsState()
    val height = LocalConfiguration.current.screenHeightDp
    val jWTViewModel by remember { mutableStateOf(JWTViewModel(context, mainViewModel)) }

    val permissionNotificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Notification Permission Denied", Toast.LENGTH_SHORT).show()
        }
        permissionNotificationRequestCompleted = true
    }

    LaunchedEffect(Unit) {
        if (!mainViewModel.isLoggedIn) navController.navigateAndReplaceStartRoute(Screens.Login.name)
        val permissionCheckResult =
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
        if (permissionCheckResult != PackageManager.PERMISSION_GRANTED) {
            // Request a permission
            permissionNotificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            hasRequestedNotificaionPermission = true
        }
        jWTViewModel.validateJWT()
        mainViewModel.latitude = 0.0
        mainViewModel.longitude = 0.0
        mainViewModel.name = ""
        mainViewModel.description = ""
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Toast.makeText(context, "Camera Permission Granted", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.QR.name)
        } else {
            Toast.makeText(context, "Camera Permission Denied", Toast.LENGTH_SHORT).show()
            navController.navigate(Screens.QR.name)
        }
        permissionRequestCompleted = true
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(assets){ item ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier
                    .clickable(
                        onClick = {
                            mainViewModel.latitude = item.latitude.replace(",", ".").toDouble()
                            mainViewModel.longitude = item.longitude.replace(",", ".").toDouble()
                            mainViewModel.name = item.name
                            mainViewModel.description = item.description
                            mainViewModel.tag = item.tag
                            mainViewModel.id = item.id
                            navController.navigate("${Screens.Details.name}/${item.set}")
                        },
                    ).fillMaxSize()
                ) {
                    AssetView(Modifier.height((height/8).dp), item.id, item.latitude.replace(",", ".").toDouble(), item.longitude.replace(",", ".").toDouble(), item.set.toInt(), item.name, item.description, mainViewModel, false)
                }
            }
        }
    }
    Row(modifier = Modifier.fillMaxSize().padding(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        FloatingActionButton(
            onClick = {
                val permissionCheckResult = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    navController.navigate(Screens.QR.name)
                } else {
                    // Request a permission
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                    hasRequestedPermission = true
                }
            },
            shape = CircleShape,
        ) {
            Icon(Icons.Filled.Add, "Floating action button.")
        }
    }
}

@RequiresApi(Build.VERSION_CODES.TIRAMISU)
@Preview(showBackground = true)
@Composable
fun IndexViewPreview() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel = MainViewModel()
    val dBViewModel = DBViewModel(context)
    SeekerTheme {
        IndexView(navController = navController, mainViewModel = mainViewModel, dBViewModel= dBViewModel)
    }
}