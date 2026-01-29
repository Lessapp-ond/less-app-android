package com.lessapp.less.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Locale

// MARK: - Language
enum class Lang(val code: String) {
    FR("fr"),
    EN("en"),
    ES("es");

    companion object {
        fun fromDevice(): Lang {
            val locale = Locale.getDefault().language
            return when (locale) {
                "fr" -> FR
                "es" -> ES
                else -> EN
            }
        }

        fun fromCode(code: String): Lang {
            return values().find { it.code == code } ?: EN
        }
    }
}

// MARK: - Card
@Serializable
data class Card(
    val id: String,
    val topic: String,
    val difficulty: Int,
    @SerialName("created_at") val createdAt: String,
    val title: String,
    val hook: String,
    val bullets: List<String>,
    val why: String
) {
    companion object {
        fun sanitize(
            id: String,
            topic: String,
            difficulty: Int,
            createdAt: String,
            title: String?,
            hook: String?,
            bullets: List<String>?,
            why: String?
        ): Card? {
            val cleanTitle = title?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val cleanHook = hook?.trim()?.takeIf { it.isNotEmpty() } ?: return null
            val cleanBullets = bullets?.map { it.trim() }?.filter { it.isNotEmpty() }?.takeIf { it.isNotEmpty() } ?: return null
            val cleanWhy = why?.trim()?.takeIf { it.isNotEmpty() } ?: return null

            return Card(
                id = id,
                topic = topic,
                difficulty = difficulty,
                createdAt = createdAt,
                title = cleanTitle,
                hook = cleanHook,
                bullets = cleanBullets,
                why = cleanWhy
            )
        }
    }
}

// MARK: - System Card (Support Message)
data class SystemCard(
    val title: String,
    val hook: String,
    val bullets: List<String>,
    val supportTitle: String,
    val supportDescription: String,
    val watchVideoLabel: String,
    val donateLabel: String,
    val finePrint: String
) {
    companion object {
        fun forLang(lang: Lang): SystemCard {
            return when (lang) {
                Lang.FR -> SystemCard(
                    title = "Less reste gratuit",
                    hook = "On croit au savoir accessible pour tous.",
                    bullets = listOf(
                        "Pas d'abonnement, pas de paywall",
                        "Pas de pub intrusive",
                        "Respectueux de votre temps"
                    ),
                    supportTitle = "Soutenez-nous",
                    supportDescription = "Regardez une courte pub ou faites un don pour nous aider.",
                    watchVideoLabel = "Regarder une pub",
                    donateLabel = "Faire un don",
                    finePrint = "Merci de votre soutien !"
                )
                Lang.ES -> SystemCard(
                    title = "Less sigue siendo gratis",
                    hook = "Creemos en el conocimiento accesible para todos.",
                    bullets = listOf(
                        "Sin suscripción, sin paywall",
                        "Sin publicidad intrusiva",
                        "Respetuoso con tu tiempo"
                    ),
                    supportTitle = "Apóyanos",
                    supportDescription = "Mira un breve anuncio o haz una donación para ayudarnos.",
                    watchVideoLabel = "Ver un anuncio",
                    donateLabel = "Hacer una donación",
                    finePrint = "¡Gracias por tu apoyo!"
                )
                Lang.EN -> SystemCard(
                    title = "Less stays free",
                    hook = "We believe in knowledge accessible to all.",
                    bullets = listOf(
                        "No subscription, no paywall",
                        "No intrusive ads",
                        "Respectful of your time"
                    ),
                    supportTitle = "Support us",
                    supportDescription = "Watch a short ad or make a donation to help us.",
                    watchVideoLabel = "Watch an ad",
                    donateLabel = "Make a donation",
                    finePrint = "Thank you for your support!"
                )
            }
        }
    }
}

// MARK: - Feed Item
sealed class FeedItem {
    abstract val id: String

    data class Content(val card: Card) : FeedItem() {
        override val id: String get() = card.id
    }

    data class System(val card: SystemCard) : FeedItem() {
        override val id: String get() = "system_card"
    }
}

// MARK: - Review Item (Spaced Repetition)
@Serializable
data class ReviewItem(
    val stage: Int,
    val nextAt: Long, // Epoch millis
    val lastSeenAt: Long? = null
) {
    companion object {
        val stageDays = listOf(1, 3, 7, 14)

        fun create(): ReviewItem {
            val tomorrow = Instant.now().plusSeconds(24 * 60 * 60).toEpochMilli()
            return ReviewItem(stage = 0, nextAt = tomorrow)
        }
    }

    fun advance(): ReviewItem {
        val newStage = minOf(stage + 1, stageDays.size - 1)
        val daysToAdd = stageDays[newStage]
        val newNextAt = Instant.now().plusSeconds(daysToAdd * 24L * 60 * 60).toEpochMilli()
        return copy(stage = newStage, nextAt = newNextAt, lastSeenAt = Instant.now().toEpochMilli())
    }

    fun reschedule(hours: Int = 6): ReviewItem {
        val newNextAt = Instant.now().plusSeconds(hours * 60L * 60).toEpochMilli()
        return copy(nextAt = newNextAt, lastSeenAt = Instant.now().toEpochMilli())
    }

    val isDue: Boolean
        get() = Instant.now().toEpochMilli() >= nextAt
}

// MARK: - Feedback Item
@Serializable
data class FeedbackItem(
    val id: String,
    val cardId: String,
    val lang: String,
    val kind: String,
    val message: String,
    val createdAt: Long
) {
    enum class Kind(val value: String) {
        TYPO("typo"),
        WRONG("wrong"),
        UNCLEAR("unclear"),
        OTHER("other")
    }

    companion object {
        fun create(cardId: String, lang: Lang, kind: Kind, message: String): FeedbackItem {
            val timestamp = Instant.now().toEpochMilli()
            return FeedbackItem(
                id = "${timestamp}_${java.util.UUID.randomUUID()}",
                cardId = cardId,
                lang = lang.code,
                kind = kind.value,
                message = message,
                createdAt = timestamp
            )
        }
    }
}

// MARK: - UI Settings
@Serializable
data class UISettings(
    val lang: String = Lang.fromDevice().code,
    val listMode: String = ListMode.FEED.value,
    val textScale: String = TextScale.NORMAL.value,
    val focusMode: Boolean = false,
    val continuousReading: Boolean = false,
    val gesturesEnabled: Boolean = true,
    val helpSeen: Boolean = false,
    val gestureHintSeen: Boolean = false
)

// MARK: - List Mode
enum class ListMode(val value: String) {
    FEED("feed"),
    LEARNED("learned"),
    UNUSEFUL("unuseful"),
    REVIEW("review");

    companion object {
        fun fromValue(value: String): ListMode {
            return values().find { it.value == value } ?: FEED
        }
    }
}

// MARK: - Text Scale
enum class TextScale(val value: String, val factor: Float) {
    NORMAL("normal", 1.0f),
    LARGE("large", 1.12f);

    companion object {
        fun fromValue(value: String): TextScale {
            return values().find { it.value == value } ?: NORMAL
        }
    }
}

// MARK: - Cards Cache
@Serializable
data class CardsCache(
    val savedAt: Long,
    val cards: List<Card>
) {
    val isFresh: Boolean
        get() {
            val now = Instant.now().toEpochMilli()
            val hoursDiff = (now - savedAt) / (1000 * 60 * 60)
            return hoursDiff < 24
        }
}

// MARK: - Undo Toast
data class UndoToast(
    val message: String,
    val undoAction: () -> Unit
)
