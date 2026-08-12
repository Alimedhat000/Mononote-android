package com.mononote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.mononote.app.navigation.MononoteNavHost
import com.mononote.app.ui.theme.MononoteTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MononoteTheme {
                MononoteNavHost()
            }
        }
    }
}
