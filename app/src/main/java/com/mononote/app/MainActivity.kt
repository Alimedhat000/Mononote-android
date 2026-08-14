package com.mononote.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.glance.appwidget.updateAll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mononote.app.data.FontFamilyOption
import com.mononote.app.data.FontSizeOption
import com.mononote.app.data.ThemeMode
import com.mononote.app.navigation.MononoteNavHost
import com.mononote.app.ui.theme.LocalMononoteColors
import com.mononote.app.ui.theme.MononoteTheme
import com.mononote.app.ui.theme.bodyTextSize
import com.mononote.app.ui.theme.composeFamily
import com.mononote.app.ui.theme.resolveDarkTheme
import com.mononote.app.widget.MononoteWidget
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

class MainActivity : ComponentActivity() {
    private val app: MononoteApp get() = application as MononoteApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by app.settingsDataStore.themeMode.collectAsStateWithLifecycle(
                initialValue = ThemeMode.SYSTEM,
            )
            val fontFamily by app.settingsDataStore.fontFamily.collectAsStateWithLifecycle(
                initialValue = FontFamilyOption.DEFAULT,
            )
            val fontSize by app.settingsDataStore.fontSize.collectAsStateWithLifecycle(
                initialValue = FontSizeOption.MEDIUM,
            )
            MononoteTheme(
                darkTheme = themeMode.resolveDarkTheme(androidx.compose.foundation.isSystemInDarkTheme()),
                fontFamily = fontFamily.composeFamily,
                bodyTextSize = fontSize.bodyTextSize,
            ) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .background(LocalMononoteColors.current.background),
                ) {
                    MononoteNavHost()
                }
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
