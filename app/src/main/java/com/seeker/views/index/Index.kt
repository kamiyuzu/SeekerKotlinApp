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
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil3.compose.AsyncImage
import com.seeker.external.services.AssetResult
import com.seeker.external.services.index
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.details.AssetView
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class IndexViewModel(): ViewModel() {
    var assets: List<AssetResult> = emptyList()
    var fetchedIndex = false

    suspend fun fetchIndex(username: String): List<AssetResult> {
        try {
            if (!fetchedIndex) assets = index(username)
            fetchedIndex = true
            return assets
        } catch (e: Exception) {
            // Handle exceptions (like network failure)
            Log.println(Log.INFO,"LoginViewModel/login", "Error ${e.stackTraceToString()}")
            return assets
        }
    }
}

fun fetchIndex(mainViewModel: MainViewModel, indexViewModel: IndexViewModel, coroutineScope: CoroutineScope) {
    coroutineScope.launch { indexViewModel.fetchIndex(mainViewModel.username) }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndexView(navController: NavHostController, mainViewModel: MainViewModel) {
    val context = LocalContext.current
    var hasRequestedPermission by rememberSaveable { mutableStateOf(false) }
    var permissionRequestCompleted by rememberSaveable { mutableStateOf(false) }
    val indexViewModel by remember { mutableStateOf(IndexViewModel()) }
    val coroutineScope = rememberCoroutineScope()
    if (!indexViewModel.fetchedIndex) fetchIndex(mainViewModel, indexViewModel, coroutineScope)
    val carouselMultiBrowseState = rememberCarouselState(itemCount = { indexViewModel.assets.size }, initialItem = 0)
    val heigth = LocalConfiguration.current.screenHeightDp

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
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        if (indexViewModel.assets.isNotEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().height((heigth/3).dp)) {
                HorizontalMultiBrowseCarousel(
                    state = carouselMultiBrowseState,
                    preferredItemWidth = 250.dp,
                    itemSpacing = 10.dp,
                    minSmallItemWidth = 50.dp,
                    maxSmallItemWidth = 100.dp,
                    contentPadding = PaddingValues(start = 10.dp),
                ) { index ->
                    val item = indexViewModel.assets[index]
                    AssetView(Modifier, item.latitude.toDouble(), item.longitude.toDouble(), item.set.toInt())
                    //Paging
                    LaunchedEffect(key1 = true) {
                        if (indexViewModel.assets.size - 1 == index) {
//                    pokemonListViewModel.requestToFetchPokemon(
//                        state.nextPage
//                    )
                        }
                    }
                }
            }
        }

        indexViewModel.assets.forEach { item ->
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
    val mainViewModel = MainViewModel()
    SeekerTheme {
        IndexView(navController = navController, mainViewModel = mainViewModel)
    }
}