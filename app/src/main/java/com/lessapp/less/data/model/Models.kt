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
    val why: String,
    val supportTitle: String,
    val supportText: String,
    val ctaVideo: String,
    val ctaDonate: String
) {
    companion object {
        fun forLang(lang: Lang): SystemCard {
            return when (lang) {
                Lang.FR -> SystemCard(
                    title = "Ton cerveau a fait sa part",
                    hook = "Tu viens de parcourir plusieurs cartes. Prends une pause méritée.",
                    bullets = listOf(
                        "1 carte = 1 micro-apprentissage",
                        "Ton feed s'adapte à ta lecture"
                    ),
                    why = "LESS est gratuit et sans compte. Pour continuer, tu peux nous soutenir.",
                    supportTitle = "Soutenir LESS",
                    supportText = "Aide-nous à garder l'app gratuite et sans pub intrusive.",
                    ctaVideo = "Regarder une courte vidéo",
                    ctaDonate = "Faire un don"
                )
                Lang.ES -> SystemCard(
                    title = "Tu cerebro ya hizo su parte",
                    hook = "Acabas de ver varias tarjetas. Toma un descanso merecido.",
                    bullets = listOf(
                        "1 tarjeta = 1 micro-aprendizaje",
                        "Tu feed se adapta a tu lectura"
                    ),
                    why = "LESS es gratis y sin cuenta. Para continuar, puedes apoyarnos.",
                    supportTitle = "Apoyar LESS",
                    supportText = "Ayúdanos a mantener la app gratis y sin publicidad intrusiva.",
                    ctaVideo = "Ver un video corto",
                    ctaDonate = "Hacer una donación"
                )
                Lang.EN -> SystemCard(
                    title = "Your brain has done its part",
                    hook = "You've just browsed several cards. Take a well-deserved break.",
                    bullets = listOf(
                        "1 card = 1 micro-learning",
                        "Your feed adapts to your reading"
                    ),
                    why = "LESS is free and account-free. To keep going, you can support us.",
                    supportTitle = "Support LESS",
                    supportText = "Help us keep the app free and without intrusive ads.",
                    ctaVideo = "Watch a short video",
                    ctaDonate = "Make a donation"
                )
            }
        }
    }
}

// MARK: - Opening Card (Daily ritual)
data class OpeningCard(
    val title: String,
    val message: String,
    val footer: String
) {
    companion object {
        const val ID = "daily_opening"

        fun forLang(lang: Lang): OpeningCard {
            return when (lang) {
                Lang.FR -> OpeningCard(
                    title = "Un moment pour toi",
                    message = "5 cartes. Pas de score. Pas de pression.\n\nPrends quelques secondes pour te poser.\nCe qui compte, c'est d'être là.",
                    footer = "Respire. Découvre. Avance."
                )
                Lang.EN -> OpeningCard(
                    title = "A moment for you",
                    message = "5 cards. No score. No pressure.\n\nTake a few seconds to settle in.\nWhat matters is being here.",
                    footer = "Breathe. Discover. Move forward."
                )
                Lang.ES -> OpeningCard(
                    title = "Un momento para ti",
                    message = "5 tarjetas. Sin puntuación. Sin presión.\n\nTómate unos segundos para asentarte.\nLo que importa es estar aquí.",
                    footer = "Respira. Descubre. Avanza."
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

    data class Opening(val card: OpeningCard) : FeedItem() {
        override val id: String get() = OpeningCard.ID
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
    val gestureHintSeen: Boolean = false,
    val darkMode: Boolean = false
)

// MARK: - List Mode
enum class ListMode(val value: String) {
    FEED("feed"),
    DAILY("daily"),
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
