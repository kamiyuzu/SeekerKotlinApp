package com.seeker.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.seeker.ui.theme.SeekerTheme
import com.seeker.views.main.MainView
import io.ktor.client.HttpClient

lateinit var client: HttpClient

class MainActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SeekerTheme {
                MainView(navController = rememberNavController(), this)
            }
        }
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStop() {
        super.onStop()
    }
}
