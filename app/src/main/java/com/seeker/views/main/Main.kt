package com.seeker.views.main

import android.util.Log
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.InstallStatus
import com.seeker.activities.appUpdateManager
import com.seeker.activities.listener
import com.seeker.data.NavigationItems
import com.seeker.datastores.PASSWORD_PREFERENCE_KEY
import com.seeker.datastores.USERNAME_PREFERENCE_KEY
import com.seeker.datastores.storePreference
import com.seeker.ui.theme.LocalSnackbarHostState
import com.seeker.views.categories.CategoriesView
import com.seeker.views.details.DetailsView
import com.seeker.views.index.IndexView
import com.seeker.views.login.LoginView
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.qrscanner.QrScannerView
import com.seeker.views.screens.Screens
import com.seeker.views.topbar.AppBar
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    var isLoggedIn = false
    var username = ""
    var password = ""
}

private fun getCurrentScreen(backStackEntry: NavBackStackEntry?): Screens {
    if(backStackEntry?.destination?.route?.contains("/") == true) {
        val routeList = backStackEntry.destination.route?.split("/")
        val result = Screens.valueOf( routeList?.first() ?: Screens.Login.name)
        Log.println(Log.DEBUG, "getCurrentScreen", "$result")
        return result
    }
    else {
        val result = Screens.valueOf(backStackEntry?.destination?.route ?: Screens.Login.name)
        Log.println(Log.DEBUG, "getCurrentScreen", "$result")
        return result
    }
}

@Composable
fun MainView(navController: NavHostController = rememberNavController()) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = getCurrentScreen(backStackEntry)
    val snackbarHostState = LocalSnackbarHostState.current
    val mainViewModel by remember { mutableStateOf(MainViewModel()) }
    val startDestination = if(!mainViewModel.isLoggedIn) Screens.Login.name else Screens.Index.name
    val context = LocalContext.current
    val width = LocalConfiguration.current.screenWidthDp

    ///List of Navigation Items that will be clicked
    val items = listOf(
        NavigationItems(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Screens.Index.title,
            onClick = {
                if (!mainViewModel.isLoggedIn)
                    if (currentScreen.title != Screens.Login.title)
                        navController.navigate(Screens.Login.name)
                else if (currentScreen.title == Screens.Login.title)
                    navController.navigateAndReplaceStartRoute(Screens.Login.name)
                else {
                    if (currentScreen.title != Screens.Index.title)
                        navController.navigate(Screens.Index.name)
                }
            }
        ),
//        NavigationItems(
//            title = "Info",
//            selectedIcon = Icons.Filled.Info,
//            unselectedIcon = Icons.Outlined.Info
//        ),
//        NavigationItems(
//            title = "Edit",
//            selectedIcon = Icons.Filled.Edit,
//            unselectedIcon = Icons.Outlined.Edit,
//            badgeCount = 105
//        ),
        NavigationItems(
            title = "Log out",
            selectedIcon = Icons.AutoMirrored.Filled.Logout,
            unselectedIcon = Icons.AutoMirrored.Outlined.Logout,
            route = 0,
            onClick = {
                mainViewModel.username = ""
                mainViewModel.password = ""
                mainViewModel.isLoggedIn = false
                storePreference(context, "", PASSWORD_PREFERENCE_KEY)
                storePreference(context, "", USERNAME_PREFERENCE_KEY)
                navController.navigateAndReplaceStartRoute(Screens.Login.name)
            }
        )
    )

    //Remember Clicked index state
    var selectedItemIndex by remember { mutableIntStateOf(0) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val snackBarHostState = LocalSnackbarHostState.current
    val coroutineScope = rememberCoroutineScope()

    // Create a listener to track request state updates.
    listener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADING) {
            val bytesDownloaded = state.bytesDownloaded()
            val totalBytesToDownload = state.totalBytesToDownload()
            Log.println(Log.DEBUG, "inAppUpdates", "bytesDownloaded $bytesDownloaded / $totalBytesToDownload")
            // Update UI to show download progress.
        } else if (state.installStatus() == InstallStatus.DOWNLOADED) {
            Log.println(Log.DEBUG, "inAppUpdates","Update is downloaded and ready to install")
            // Notify the user and request installation.
            coroutineScope.launch {
                snackBarHostState.showSnackbar("An update has just been downloaded.")
            }
        } else if (state.installStatus() == InstallStatus.INSTALLING) {
            Log.println(Log.DEBUG, "inAppUpdates","Update is being installed")
            // Update UI to show installation progress.
        } else if (state.installStatus() == InstallStatus.INSTALLED) {
            Log.println(Log.DEBUG, "inAppUpdates","Update is installed")
            // Notify the user and perform any necessary actions.
            coroutineScope.launch {
                snackBarHostState.showSnackbar("Update is installed")
            }
        } else if (state.installStatus() == InstallStatus.FAILED) {
            Log.println(Log.DEBUG, "inAppUpdates","Update failed to install")
            // Notify the user and handle the error.
            coroutineScope.launch {
                snackBarHostState.showSnackbar("Update failed to install")
            }
        }
    }

    // Before starting an update, register a listener for updates.
    appUpdateManager.registerListener(listener)

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width((width/1.35).dp)
            ) {
                Spacer(modifier = Modifier.padding().statusBarsPadding()) //space (margin) from top
                items.forEachIndexed { index, item ->
                    NavigationDrawerItem(
                        label = { Text(text = item.title) },
                        selected = currentScreen.title == item.route,
                        // index == selectedItemIndex,
                        onClick = {
                            item.onClick.invoke()
                            selectedItemIndex = index
                            scope.launch {
                                drawerState.close()
                            }
                        },
                        icon = {
                            Icon(
                                imageVector = item.unselectedIcon,
                                // if (index == selectedItemIndex) {
                                //    item.selectedIcon
                                //} else item.unselectedIcon,
                                contentDescription = item.title
                            )
                        },
                        badge = {  // Show Badge
                            //item.badgeCount?.let {
                            //    Text(text = item.badgeCount.toString())
                            //}
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding) //padding between items
                    )
                }

            }
        },
        gesturesEnabled = true
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            topBar = {
                AppBar(
                    mainViewModel = mainViewModel,
                    canNavigateBack = navController.previousBackStackEntry != null,
                    navigateUp = { navController.navigateUp() },
                    scope = scope,
                    drawerState = drawerState,
                )
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = startDestination,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(route = Screens.Login.name) {
                    LoginView(navController = navController, mainViewModel = mainViewModel)
                }
                composable(route = Screens.Index.name) {
                    IndexView(navController = navController)
                }
                composable(route = Screens.QR.name) {
                    QrScannerView(navController = navController)
                }
                composable(route = "${Screens.Details.name}/{setId}", arguments = listOf(
                    navArgument("setId") {
                        type = NavType.IntType
                    }
                )) { navBackStackEntry ->
                    /* Extracting the id from the route */
                    val setId = navBackStackEntry.arguments?.getInt("setId")
                    DetailsView(navController = navController, setId = setId)
                }
                composable(route = Screens.Categories.name) {
                    CategoriesView(navController = navController)
                }
            }
        }
    }
}