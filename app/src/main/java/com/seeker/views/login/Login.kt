package com.seeker.views.login

import android.content.Context
import android.graphics.drawable.AdaptiveIconDrawable
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.seeker.R
import com.seeker.data.Credentials
import com.seeker.datastores.PASSWORD_PREFERENCE_KEY
import com.seeker.datastores.USERNAME_PREFERENCE_KEY
import com.seeker.datastores.readPreference
import com.seeker.datastores.storePreference
import com.seeker.external.services.login
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.main.MainViewModel
import com.seeker.views.screens.Screens
import kotlinx.coroutines.launch
import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
private fun String.hashedWithSha256() =
    MessageDigest.getInstance("SHA-256")
        .digest(toByteArray())
        .toHexString()

class LoginViewModel(): ViewModel() {
    suspend fun login(creds: Credentials): String {
        try {
            return login(creds.user, creds.pwd.hashedWithSha256())
        } catch (e: Exception) {
            // Handle exceptions (like network failure)
            Log.println(Log.INFO,"LoginViewModel/login", "Error ${e.stackTraceToString()}")
            return ""
        }
    }
}

private suspend fun checkCredentials(creds: Credentials, context: Context, navController: NavHostController, mainViewModel: MainViewModel, loginViewModel: LoginViewModel): Boolean {
    Log.println(Log.INFO,"Credentials", "User ${creds.user}")
    Log.println(Log.INFO,"Credentials", "Pass ${creds.pwd.hashedWithSha256()}")
    val result = loginViewModel.login(creds)
    if (creds.isNotEmpty() && result != "" && !mainViewModel.isLoggedIn) {
        if (creds.remember) {
            Log.println(Log.INFO,"Credentials", "Saving password and username")
            storePreference(context, result, PASSWORD_PREFERENCE_KEY)
            storePreference(context, creds.user, USERNAME_PREFERENCE_KEY)
        }
        mainViewModel.isLoggedIn = true
        mainViewModel.username = creds.user
        mainViewModel.password = result

        navController.navigate(Screens.Index.name)

        //context.startActivity(Intent(context, MainActivity::class.java))
        //(context as Activity).finish()
        return true
    } else {
        Toast.makeText(context, "Wrong Credentials", Toast.LENGTH_SHORT).show()
        return false
    }
}

fun NavHostController.navigateAndReplaceStartRoute(newHomeRoute: String) {
    addOnDestinationChangedListener { controller, _, _ ->
        val routes = controller
            .currentBackStack.value
            .map { it.destination.route }
            .joinToString(", ")

        Log.println(Log.DEBUG,"BackStackLog", "BackStack: $routes")
    }

    navigate(newHomeRoute) {
        // Pop up to the start destination of the graph to
        // avoid building up a large stack of destinations
        // on the back stack as users select items
        popUpTo(graph.findStartDestination().navigatorName) {
            saveState = true
        }
        // Avoid multiple copies of the same destination when
        // reselecting the same item
        launchSingleTop = true
        // Restore state when reselecting a previously selected item
        restoreState = true
    }

    //if(popUp) popBackStack(graph.startDestinationId, inclusive) else popBackStack()
    //graph.setStartDestination(newHomeRoute)
    //navigate(newHomeRoute)
}

private fun checkLoggedUser(context: Context, navController: NavHostController, currentScreen: Screens, mainViewModel: MainViewModel) {
    Log.println(Log.INFO,"Credentials", "Checking if user has already logged in")
    if (currentScreen.title == Screens.Login.title) {
        val pass = readPreference(context, PASSWORD_PREFERENCE_KEY)
        val user = readPreference(context, USERNAME_PREFERENCE_KEY)
        if (pass != "" && user != "") {
            Log.println(Log.INFO, "Credentials", "The user is already logged in")
            mainViewModel.password = readPreference(context, PASSWORD_PREFERENCE_KEY)
            mainViewModel.isLoggedIn = true
            mainViewModel.username = readPreference(context, USERNAME_PREFERENCE_KEY)
            navController.navigateAndReplaceStartRoute(Screens.Index.name)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun LoginView(navController: NavHostController, mainViewModel: MainViewModel) {
    var credentials by remember { mutableStateOf(Credentials()) }
    val context = LocalContext.current
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentScreen = Screens.valueOf(backStackEntry?.destination?.route ?: Screens.Login.name)
    LaunchedEffect(Unit) {
        checkLoggedUser(context, navController, currentScreen, mainViewModel)
    }
    val coroutineScope = rememberCoroutineScope()
    val loginViewModel by remember { mutableStateOf(LoginViewModel()) }

    val res = context.resources
    val theme = context.theme
    val adaptiveIcon = remember { ResourcesCompat.getDrawable(res, R.mipmap.ic_launcher_round, theme) as? AdaptiveIconDrawable }
    val width = LocalConfiguration.current.screenWidthDp
    val height = LocalConfiguration.current.screenHeightDp

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp)
    ) {
        adaptiveIcon?.toBitmap(width = width, height = height/2)?.let { BitmapPainter(it.asImageBitmap()) }?.let {
            Image(
                modifier = Modifier.fillMaxWidth(),
                painter = it,
                contentDescription = "App icon",
            )
        }
        Spacer(modifier = Modifier.height((height/12).dp))
        LoginField(
            value = credentials.user,
            onChange = { data -> credentials = credentials.copy(user = data) },
            modifier = Modifier.fillMaxWidth()
        )
        PasswordField(
            value = credentials.pwd,
            onChange = { data -> credentials = credentials.copy(pwd = data) },
            submit = {
                coroutineScope.launch {
                    if (!checkCredentials(credentials, context, navController, mainViewModel, loginViewModel)) credentials = Credentials()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(10.dp))
        LabeledCheckbox(
            label = "Remember me",
            onCheckChanged = {
                credentials = credentials.copy(remember = !credentials.remember)
            },
            isChecked = credentials.remember
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                coroutineScope.launch {
                    if (!checkCredentials(credentials, context, navController, mainViewModel, loginViewModel)) credentials = Credentials()
                }
            },
            enabled = credentials.isNotEmpty(),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth(0.5f),
        ) {
            Text("Login")
        }
    }

}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginViewPreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    SeekerTheme {
        LoginView(navController = navController, mainViewModel = mainViewModel)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginViewDarkPreview() {
    val navController = rememberNavController()
    val mainViewModel = MainViewModel()
    SeekerTheme(darkTheme = true) {
        LoginView(navController = navController, mainViewModel = mainViewModel)
    }
}
