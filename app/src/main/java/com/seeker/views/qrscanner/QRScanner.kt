package com.seeker.views.qrscanner

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.seeker.R
import com.seeker.ui.theme.LocalSnackbarHostState
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import qrscanner.CameraLens
import qrscanner.QrScanner

@Composable
fun QrScannerView(navController: NavHostController, mainViewModel: MainViewModel) {
    var qrCodeURL by rememberSaveable { mutableStateOf("") }
    var flashlightOn by rememberSaveable { mutableStateOf(false) }
    var openImagePicker by rememberSaveable { mutableStateOf(value = false) }
    val snackBarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()
    val clipboardManager: ClipboardManager = LocalClipboardManager.current
    val width = LocalConfiguration.current.screenWidthDp
    var cameraLensFront by remember { mutableStateOf(false) }
    var cameraLens by remember { mutableStateOf(CameraLens.Back) }

    LaunchedEffect(mainViewModel) {
        if (!mainViewModel.isLoggedIn) navController.navigateAndReplaceStartRoute(Screens.Login.name)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                //.background(Color(0xFF1D1C22))
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(width.dp)
                    .clip(shape = RoundedCornerShape(size = 14.dp))
                    .clipToBounds()
                    .padding((width / 10).dp)
                    .border(
                        2.dp,
                        MaterialTheme.colorScheme.primaryContainer,
                        RoundedCornerShape(size = 14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                QrScanner(
                    modifier = Modifier
                        .clipToBounds()
                        .clip(shape = RoundedCornerShape(size = 14.dp)),
                    flashlightOn = flashlightOn,
                    openImagePicker = openImagePicker,
                    onCompletion = {
                        Log.println(Log.DEBUG,"QrScanner/qrCodeURL", it)
                        if (it.matches(Regex("\\d+"))) {
                            qrCodeURL = it
                            coroutineScope.launch {
                                navController.navigate("${Screens.Details.name}/${it}")
                            }
                        }
                    },
                    imagePickerHandler = {
                        openImagePicker = it
                    },
                    onFailure = {
                        coroutineScope.launch {
                            if (it.isEmpty()) {
                                snackBarHostState.showSnackbar("Invalid qr code")
                            } else {
                                snackBarHostState.showSnackbar(it)
                            }
                        }
                    },
                    cameraLens = cameraLens,
                )
            }

            Box(
                modifier = Modifier
                    .padding(start = 20.dp, end = 20.dp, top = 20.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(25.dp)
                    )
                    .height(35.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = if (flashlightOn) Icons.Filled.FlashOn else Icons.Filled.FlashOff,
                        "flash",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                flashlightOn = !flashlightOn
                            },
                        tint = if (flashlightOn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    )

                    VerticalDivider(
                        modifier = Modifier,
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Icon(
                        imageVector = Icons.Filled.Cameraswitch,
                        "flash",
                        modifier = Modifier
                            .size(20.dp)
                            .clickable {
                                cameraLensFront = !cameraLensFront
                                if (!cameraLensFront) cameraLens = CameraLens.Back
                                if (cameraLensFront) cameraLens = CameraLens.Front
                            },
                        tint = if (cameraLensFront) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onPrimary,
                    )

                    VerticalDivider(
                        modifier = Modifier,
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Image(
                        painter = painterResource(R.drawable.ic_gallery_icon),
                        contentDescription = "gallery",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { openImagePicker = true }
                    )
                }
            }
        }

        if (qrCodeURL.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .padding(bottom = 22.dp)
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = qrCodeURL,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .weight(1f),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary,
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )

                Icon(
                    Icons.Filled.CopyAll,
                    "CopyAll",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            clipboardManager.setText(AnnotatedString((qrCodeURL)))
                        },
                    tint = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun QrScannerViewPreview() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val mainViewModel = MainViewModel(context)
    SeekerTheme {
        QrScannerView(navController = navController, mainViewModel = mainViewModel)
    }
}