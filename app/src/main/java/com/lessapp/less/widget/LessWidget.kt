package com.lessapp.less.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.lessapp.less.MainActivity
import com.lessapp.less.R
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import kotlin.math.abs

@Serializable
data class WidgetCard(
    val id: String,
    val title: String,
    val topic: String,
    val hook: String
)

class LessWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // First widget added
    }

    override fun onDisabled(context: Context) {
        // Last widget removed
    }

    companion object {
        private const val PREFS_NAME = "less_widget_prefs"
        private const val KEY_CARDS = "widget_cards_cache"
        private const val KEY_LANG = "widget_lang"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_less)

            // Load card data
            val card = loadTodayCard(context)

            if (card != null) {
                views.setTextViewText(R.id.widget_topic, card.topic.uppercase())
                views.setTextViewText(R.id.widget_title, card.title)
                views.setTextViewText(R.id.widget_footer, getFooterText(context))
            } else {
                views.setTextViewText(R.id.widget_topic, "LESS")
                views.setTextViewText(R.id.widget_title, "Ouvre l'app pour charger les cartes")
                views.setTextViewText(R.id.widget_footer, "")
            }

            // Create intent to open app in daily mode
            val intent = Intent(context, MainActivity::class.java).apply {
                action = "OPEN_DAILY"
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        private fun loadTodayCard(context: Context): WidgetCard? {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val cardsJson = prefs.getString(KEY_CARDS, null) ?: return null
            val lang = prefs.getString(KEY_LANG, "en") ?: "en"

            return try {
                val cards = Json.decodeFromString<List<WidgetCard>>(cardsJson)
                if (cards.isEmpty()) return null

                // Select today's card using deterministic seed
                val today = LocalDate.now(ZoneOffset.UTC)
                val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
                val seed = "$dateString-$lang"
                val hash = abs(seed.hashCode())
                val index = hash % cards.size

                cards[index]
            } catch (e: Exception) {
                null
            }
        }

        private fun getFooterText(context: Context): String {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            return when (prefs.getString(KEY_LANG, "en")) {
                "fr" -> "💡 Tap pour apprendre"
                "es" -> "💡 Toca para aprender"
                else -> "💡 Tap to learn"
            }
        }

        // Called by the app to save cards for widget
        fun saveCardsForWidget(context: Context, cards: List<WidgetCard>, lang: String) {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().apply {
                putString(KEY_CARDS, Json.encodeToString(ListSerializer(WidgetCard.serializer()), cards))
                putString(KEY_LANG, lang)
                apply()
            }

            // Trigger widget update for all widget instances
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = android.content.ComponentName(context, LessWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)

            if (appWidgetIds.isNotEmpty()) {
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId)
                }
            }
        }
    }
}
