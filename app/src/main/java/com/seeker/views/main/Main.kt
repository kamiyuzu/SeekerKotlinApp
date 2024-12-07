package com.seeker.views.main

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
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
import androidx.navigation.navArgument
import androidx.room.Room
import com.seeker.activities.MainActivity
import com.seeker.activities.client
import com.seeker.data.NavigationItems
import com.seeker.database.AssetDatabase
import com.seeker.database.repositories.AssetRepositoryImpl
import com.seeker.datastores.PASSWORD_PREFERENCE_KEY
import com.seeker.datastores.USERNAME_PREFERENCE_KEY
import com.seeker.datastores.storePreference
import com.seeker.ui.theme.LocalSnackbarHostState
import com.seeker.views.details.DetailsView
import com.seeker.views.index.IndexView
import com.seeker.views.login.LoginView
import com.seeker.views.login.navigateAndReplaceStartRoute
import com.seeker.views.qrscanner.QrScannerView
import com.seeker.views.screens.Screens
import com.seeker.views.topbar.AppBar
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.launch

class MainViewModel: ViewModel() {
    var isLoggedIn = false
    var username = ""
    var password = ""
}

class DBViewModel(context: Context): ViewModel() {
    private var assetDB = Room.databaseBuilder(context, AssetDatabase::class.java, "AssetDatabase").build()
    var repo = AssetRepositoryImpl(assetDB.dao)
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

private fun navigateItems(mainViewModel: MainViewModel, navController: NavHostController, currentScreen: Screens, screen: String){
    if (!mainViewModel.isLoggedIn) {
        if (currentScreen.title != Screens.Login.title)
            navController.navigateAndReplaceStartRoute(Screens.Login.name)
        else if (currentScreen.name != screen && mainViewModel.isLoggedIn) navController.navigate(screen)
    }
    else if (currentScreen.name != screen) navController.navigate(screen)
}

@Composable
fun MainView(navController: NavHostController, mainContext: MainActivity, mainViewModel: MainViewModel) {
    // Get current back stack entry
    val backStackEntry by navController.currentBackStackEntryAsState()
    // Get the name of the current screen
    val currentScreen = getCurrentScreen(backStackEntry)
    val snackbarHostState = LocalSnackbarHostState.current
    val context = LocalContext.current
    val dBViewModel by remember { mutableStateOf(DBViewModel(context)) }
    val startDestination = if(!mainViewModel.isLoggedIn) Screens.Login.name else Screens.Index.name
    val width = LocalConfiguration.current.screenWidthDp

    client = HttpClient(CIO){
        install(Logging) {
            level = LogLevel.ALL
        }
        install(ContentNegotiation){
            json()
        }
        HttpResponseValidator {
            validateResponse { response ->
                if (response.status.value == 403) {
                    Log.println(Log.DEBUG, "HttpResponseValidator", response.toString())
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(mainContext, "JWT has expired, log in again", Toast.LENGTH_SHORT).show()
                    }
                    mainViewModel.password = ""
                    mainViewModel.isLoggedIn = false
                    mainViewModel.username = ""
                    navController.navigate(Screens.Login.name)
                    navController.navigateAndReplaceStartRoute(Screens.Login.name)
                }
            }
        }
        defaultRequest {
            header("jwt", mainViewModel.password)
        }
    }

    ///List of Navigation Items that will be clicked
    val items = listOf(
        NavigationItems(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
            route = Screens.Index.title,
            onClick = {
                navigateItems(mainViewModel, navController, currentScreen, Screens.Index.name)
            }
        ),
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
                    IndexView(navController = navController, mainViewModel = mainViewModel, dBViewModel = dBViewModel)
                }
                composable(route = Screens.QR.name) {
                    QrScannerView(navController = navController, mainViewModel = mainViewModel)
                }
                composable(route = "${Screens.Details.name}/{setId}", arguments = listOf(
                    navArgument("setId") {
                        type = NavType.IntType
                    }
                )) { navBackStackEntry ->
                    /* Extracting the id from the route */
                    val setId = navBackStackEntry.arguments?.getInt("setId")
                    DetailsView(navController = navController, mainViewModel = mainViewModel, setId = setId)
                }
            }
        }
    }
}