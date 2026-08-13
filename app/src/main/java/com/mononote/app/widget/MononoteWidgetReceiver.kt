package com.mononote.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Broadcast receiver that links the home-screen widget to [MononoteWidget].
 * The launcher notifies it on APPWIDGET_UPDATE; Glance then runs the widget's
 * composition and sends the resulting remote views to the AppWidgetManager.
 */
class MononoteWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MononoteWidget()
}
