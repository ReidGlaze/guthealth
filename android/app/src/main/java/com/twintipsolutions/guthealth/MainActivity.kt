package com.twintipsolutions.guthealth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.twintipsolutions.guthealth.navigation.AppNavigation
import com.twintipsolutions.guthealth.ui.theme.GutHealthTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GutHealthTheme {
                AppNavigation()
            }
        }
    }
}
