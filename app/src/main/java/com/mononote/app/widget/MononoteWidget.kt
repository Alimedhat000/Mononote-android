package com.mononote.app.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceComposable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.mononote.app.MainActivity
import com.mononote.app.MononoteApp
import com.mononote.app.R
import com.mononote.app.ui.theme.DarkMononoteColors
import com.mononote.app.ui.theme.LightMononoteColors
import kotlinx.coroutines.flow.first

class MononoteWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(
        context: Context,
        id: GlanceId,
    ) {
        val settings = (context.applicationContext as MononoteApp).settingsDataStore
        val snapshot = settings.activeNoteSnapshot.first()
        provideContent {
            MononoteWidgetContent(noteText = snapshot)
        }
    }
}

@Composable
@GlanceComposable
private fun MononoteWidgetContent(noteText: String) {
    val context = LocalContext.current
    val size = LocalSize.current
    val openApp = actionStartActivity(Intent(context, MainActivity::class.java))
    val displayText =
        noteText.ifBlank { context.getString(R.string.placeholder_text) }
    val textColor =
        if (noteText.isBlank()) WidgetPalette.secondaryText else WidgetPalette.primaryText

    Box(
        modifier =
            GlanceModifier
                .fillMaxSize()
                .background(WidgetPalette.cardSurface)
                .cornerRadius(24.dp)
                .padding(all = widgetContentPadding)
                .clickable(openApp),
    ) {
        Text(
            text = displayText,
            style =
                TextStyle(
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Normal,
                ),
            maxLines = noteMaxLines(size),
        )
    }
}

private fun noteMaxLines(size: DpSize): Int {
    val availableHeight = size.height - (widgetContentPadding * 2)
    return (availableHeight / noteLineHeight).toInt().coerceAtLeast(1)
}

private object WidgetPalette {
    val cardSurface =
        ColorProvider(LightMononoteColors.cardSurface, DarkMononoteColors.cardSurface)
    val primaryText =
        ColorProvider(LightMononoteColors.primaryText, DarkMononoteColors.primaryText)
    val secondaryText =
        ColorProvider(LightMononoteColors.secondaryText, DarkMononoteColors.secondaryText)
}

private val widgetContentPadding = 16.dp
private val noteLineHeight = 26.dp
