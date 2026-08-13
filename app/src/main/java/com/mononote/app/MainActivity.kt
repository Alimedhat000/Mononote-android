package com.mononote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.glance.appwidget.updateAll
import com.mononote.app.navigation.MononoteNavHost
import com.mononote.app.ui.theme.MononoteTheme
import com.mononote.app.widget.MononoteWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

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

    override fun onStop() {
        super.onStop()
        runBlocking(Dispatchers.Default) {
            MononoteWidget().updateAll(applicationContext)
        }
    }
}
