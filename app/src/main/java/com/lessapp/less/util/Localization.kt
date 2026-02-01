package com.lessapp.less.util

import com.lessapp.less.data.model.Lang

class L10n(val lang: Lang) {

    private fun t(fr: String, en: String, es: String): String {
        return when (lang) {
            Lang.FR -> fr
            Lang.EN -> en
            Lang.ES -> es
        }
    }

    // Common
    val close get() = t("Fermer", "Close", "Cerrar")
    val cancel get() = t("Annuler", "Cancel", "Cancelar")
    val send get() = t("Envoyer", "Send", "Enviar")
    val ok get() = t("OK", "OK", "OK")

    // Card Actions
    val learned get() = t("J'ai appris quelque chose", "I learned something", "Aprendí algo")
    val notUseful get() = t("Pas utile", "Not useful", "No útil")
    val reviewLater get() = t("À revoir", "Review later", "Repasar después")
    val report get() = t("Signaler", "Report", "Reportar")

    // List Modes
    val feed get() = t("Feed", "Feed", "Feed")
    val daily get() = t("Daily", "Daily", "Daily")
    val learnedMode get() = t("Appris", "Learned", "Aprendido")
    val unusefulMode get() = t("Pas utile", "Not useful", "No útil")
    val review get() = t("À revoir", "Review", "Repasar")

    // Daily Mode
    val dailyComplete get() = t("Rituel terminé !", "Ritual complete!", "¡Ritual completado!")

    // Settings
    val language get() = t("Langue", "Language", "Idioma")
    val reading get() = t("Lecture", "Reading", "Lectura")
    val focus get() = t("Focus", "Focus", "Enfoque")
    val continuous get() = t("Continu", "Continuous", "Continuo")
    val gestures get() = t("Gestes", "Gestures", "Gestos")
    val darkMode get() = t("Mode sombre", "Dark mode", "Modo oscuro")
    val help get() = t("Aide", "Help", "Ayuda")

    // Help
    val helpReadTitle get() = t("Lire", "Read", "Leer")
    val helpReadContent get() = t(
        "Chaque carte contient une idée essentielle. Scroll pour découvrir.",
        "Each card contains a key idea. Scroll to discover.",
        "Cada tarjeta contiene una idea clave. Desplázate para descubrir."
    )
    val helpActionsTitle get() = t("Actions", "Actions", "Acciones")
    val helpActionsContent get() = t(
        "\"J'ai appris\" archive la carte. \"Pas utile\" la cache. \"À revoir\" planifie une révision.",
        "\"I learned\" archives the card. \"Not useful\" hides it. \"Review\" schedules a review.",
        "\"Aprendí\" archiva la tarjeta. \"No útil\" la oculta. \"Repasar\" programa una revisión."
    )
    val helpModesTitle get() = t("Modes", "Modes", "Modos")
    val helpModesContent get() = t(
        "Basculez entre Feed, Appris, Pas utile et À revoir dans le menu.",
        "Switch between Feed, Learned, Not useful and Review in the menu.",
        "Cambia entre Feed, Aprendido, No útil y Repasar en el menú."
    )
    val helpPrivacyTitle get() = t("Vie privée", "Privacy", "Privacidad")
    val helpPrivacyContent get() = t(
        "Pas de compte requis. Tout est stocké localement sur votre appareil.",
        "No account required. Everything is stored locally on your device.",
        "No se requiere cuenta. Todo se almacena localmente en tu dispositivo."
    )

    // Feedback
    val feedbackTitle get() = t("Signaler un problème", "Report an issue", "Reportar un problema")
    val feedbackSubtitle get() = t(
        "Aidez-nous à améliorer cette carte",
        "Help us improve this card",
        "Ayúdanos a mejorar esta tarjeta"
    )
    val feedbackPlaceholder get() = t("Décrivez le problème...", "Describe the issue...", "Describe el problema...")
    val feedbackTypo get() = t("Faute de frappe", "Typo", "Error tipográfico")
    val feedbackWrong get() = t("Information fausse", "Wrong info", "Info incorrecta")
    val feedbackUnclear get() = t("Pas clair", "Unclear", "No claro")
    val feedbackOther get() = t("Autre", "Other", "Otro")

    // States
    val slowConnection get() = t("Connexion lente...", "Slow connection...", "Conexión lenta...")
    val noCards get() = t("Aucune carte", "No cards", "Sin tarjetas")
    val nothingToReview get() = t("Rien à revoir", "Nothing to review", "Nada que repasar")
    val offline get() = t("Hors ligne", "Offline", "Sin conexión")
    val loading get() = t("Chargement...", "Loading...", "Cargando...")

    // Undo Toast
    val undoLearned get() = t("Carte archivée", "Card archived", "Tarjeta archivada")
    val undoUnuseful get() = t("Carte masquée", "Card hidden", "Tarjeta oculta")
    val undoReview get() = t("Ajoutée aux révisions", "Added to reviews", "Añadida a repasar")
    val undo get() = t("Annuler", "Undo", "Deshacer")

    // Ads
    val adsLoading get() = t("Chargement de la pub...", "Loading ad...", "Cargando anuncio...")
    val adsLoaded get() = t("Pub prête", "Ad ready", "Anuncio listo")
    val adsEarned get() = t("Merci pour votre soutien !", "Thank you for your support!", "¡Gracias por tu apoyo!")
    val adsError get() = t("Erreur de pub", "Ad error", "Error de anuncio")

    // Why It Matters
    val whyItMatters get() = t("Pourquoi c'est important", "Why it matters", "Por qué importa")

    // Difficulty
    fun difficulty(level: Int): String {
        return when (level) {
            1 -> "●○○"
            2 -> "●●○"
            else -> "●●●"
        }
    }

    // Support
    val supportTitle get() = t("Soutenir Less", "Support Less", "Apoyar Less")
    val supportSubtitle get() = t(
        "Chaque don nous aide à créer plus de contenu et améliorer l'app.",
        "Every donation helps us create more content and improve the app.",
        "Cada donación nos ayuda a crear más contenido y mejorar la app."
    )
    val restorePurchases get() = t("Restaurer les achats", "Restore purchases", "Restaurar compras")
    val thankYou get() = t(
        "Votre soutien nous aide à garder Less gratuit et sans publicité intrusive.",
        "Your support helps us keep Less free and without intrusive ads.",
        "Tu apoyo nos ayuda a mantener Less gratis y sin anuncios intrusivos."
    )
    val retry get() = t("Réessayer", "Retry", "Reintentar")
    val noOptionsAvailable get() = t("Aucune option disponible", "No options available", "Sin opciones disponibles")
}
