package com.seeker.views.index

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.seeker.database.entities.AssetEntity
import com.seeker.external.services.AssetResult
import com.seeker.external.services.index
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.details.AssetView
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapConcat
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IndexViewModel(): ViewModel() {
    private var _assetList = MutableStateFlow(emptyList<AssetResult>())
    var assetList = _assetList.asStateFlow()

    suspend fun fetchIndex(mainViewModel: MainViewModel) {
        try {
            val assets = index(mainViewModel.username)
            Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "Assets ${assets}")
            assets.forEach { asset ->
                Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "asset $asset")
                val insertResponse = mainViewModel.repo.insert(AssetEntity(id = asset.id.toInt(), username = asset.username, latitude = asset.latitude, longitude = asset.longitude, set = asset.set))
                Log.println(Log.DEBUG,"IndexViewModel/fetchIndex", "insert_response $insertResponse")
            }
            _assetList.tryEmit(assets)
        } catch (e: Exception) {
            // Handle exceptions (like network failure)
            Log.println(Log.INFO,"IndexViewModel/fetchIndex", "Error ${e.stackTraceToString()}")
                mainViewModel.repo.getAllAssets().flowOn(IO).collect {
                    val assetResultList = it.map { assetIt ->
                         AssetResult(assetIt.id.toString(), assetIt.username, assetIt.set, assetIt.latitude, assetIt.longitude)
                    }
                    _assetList.value = assetResultList
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexView(navController: NavHostController, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }
    val indexViewModel by remember { mutableStateOf(IndexViewModel()) }
    val assets: List<AssetResult> by indexViewModel.assetList.collectAsState()
    LaunchedEffect(key1 = true, block = {
        // we will get the student details when ever the screen is created
        // Launched effect is a side effect
        indexViewModel.fetchIndex(mainViewModel)
    })
//    val carouselMultiBrowseState = rememberCarouselState(itemCount = { indexViewModel.assets.size }, initialItem = 0)
//    val heigth = LocalConfiguration.current.screenHeightDp

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        if (it) {
            Log.println(Log.INFO,"DataStore", "Hashed password was present")
            Toast.makeText(context, "Permission Granted", Toast.LENGTH_SHORT).show()
            //cameraLauncher.launch(uri)
            navController.navigate(Screens.QR.name)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        permissionRequestCompleted = true
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
//            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))
//        if (assets.isNotEmpty()) {
//            Row(modifier = Modifier.fillMaxWidth().height((heigth/3).dp)) {
//                HorizontalMultiBrowseCarousel(
//                    state = carouselMultiBrowseState,
//                    preferredItemWidth = 250.dp,
//                    itemSpacing = 10.dp,
//                    minSmallItemWidth = 50.dp,
//                    maxSmallItemWidth = 100.dp,
//                    contentPadding = PaddingValues(start = 10.dp),
//                ) { index ->
//                    val item = assets[index]
//                    AssetView(Modifier, item.latitude.toDouble(), item.longitude.toDouble(), item.set.toInt())
//                    //Paging
//                    LaunchedEffect(key1 = true) {
//                        if (indexViewModel.assets.size - 1 == index) {
////                    pokemonListViewModel.requestToFetchPokemon(
////                        state.nextPage
////                    )
//                        }
//                    }
//                }
//            }
//        }

        LazyColumn {
            items(assets){ item ->
                Spacer(modifier = Modifier.height(10.dp))
                Row(modifier = Modifier
                    .clickable(
                        onClick = {
                            navController.navigate("${Screens.Details.name}/${item.set}")
                        },
                    )
                ){
                    AssetView(Modifier, item.latitude.toDouble(), item.longitude.toDouble(), item.set.toInt())
                }
            }
        }
    }
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(10.dp),
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.Absolute.Right
    ) {
        FloatingActionButton(
            onClick = {
                val permissionCheckResult =
                    ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                if (permissionCheckResult == PackageManager.PERMISSION_GRANTED) {
                    //cameraLauncher.launch(uri)
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

@Preview(showBackground = true)
@Composable
fun IndexViewPreview() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel = MainViewModel(context)
    SeekerTheme {
        IndexView(navController = navController, mainViewModel = mainViewModel)
    }
}