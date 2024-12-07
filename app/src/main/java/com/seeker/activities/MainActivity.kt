package com.seeker.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.rememberNavController
import com.seeker.external.services.validateJWT
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.main.MainView
import com.seeker.views.main.MainViewModel
import io.ktor.client.HttpClient
import kotlinx.coroutines.launch

lateinit var client: HttpClient

class MainActivity: ComponentActivity() {
    private val mainViewModel by viewModels<MainViewModel>()
    private val mainActivity = this

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeekerTheme {
                MainView(navController = rememberNavController(), mainActivity, mainViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            if(::client.isInitialized) {
                if (mainViewModel.isLoggedIn) {
                    if (validateJWT() != "valid") {
                        enableEdgeToEdge()
                        setContent {
                            SeekerTheme {
                                MainView(
                                    navController = rememberNavController(),
                                    mainActivity,
                                    mainViewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
