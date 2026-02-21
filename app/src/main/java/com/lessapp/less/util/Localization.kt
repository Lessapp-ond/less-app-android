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
    val topics get() = t("Catégories", "Topics", "Categorías")
    val clearFilter get() = t("Effacer", "Clear", "Borrar")

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
    val noFavorites get() = t("Aucun favori", "No favorites", "Sin favoritos")
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
    val comingSoon get() = t("Bientôt disponible", "Coming soon", "Próximamente")
    val comingSoonSubtitle get() = t(
        "Cette fonctionnalité sera disponible dans une prochaine mise à jour.",
        "This feature will be available in an upcoming update.",
        "Esta función estará disponible en una próxima actualización."
    )

    // Notifications
    val notifications get() = t("Rappel quotidien", "Daily reminder", "Recordatorio diario")
    val notificationsOn get() = t("Activé", "On", "Activado")
    val notificationsOff get() = t("Désactivé", "Off", "Desactivado")
    val notificationTime get() = t("Heure", "Time", "Hora")

    // Settings Page
    val settings get() = t("Réglages", "Settings", "Ajustes")
    val appearance get() = t("Apparence", "Appearance", "Apariencia")
    val support get() = t("Soutenir", "Support", "Apoyar")
    val supportUs get() = t("Regarder une vidéo", "Watch a video", "Ver un video")
    val done get() = t("OK", "Done", "Listo")

    // Statistics
    val statistics get() = t("Statistiques", "Statistics", "Estadísticas")
    val cardsLearned get() = t("Cartes apprises", "Cards learned", "Tarjetas aprendidas")
    val maxStreak get() = t("Streak max", "Max streak", "Racha máxima")
    val topTopic get() = t("Sujet préféré", "Top topic", "Tema favorito")
    val days get() = t("jours", "days", "días")
    val noTopicYet get() = t("Aucun pour l'instant", "None yet", "Ninguno aún")

    // Onboarding
    val onboardingNext get() = t("Suivant", "Next", "Siguiente")
    val onboardingSkip get() = t("Passer", "Skip", "Saltar")
    val onboardingStart get() = t("C'est parti !", "Let's go!", "¡Vamos!")
    val onboardingWelcome get() = t(
        "Apprends quelque chose de nouveau chaque jour",
        "Learn something new every day",
        "Aprende algo nuevo cada día"
    )
    val onboardingHowTitle get() = t("Comment ça marche", "How it works", "Cómo funciona")
    val onboardingHow1 get() = t("1 carte = 20-30 secondes", "1 card = 20-30 seconds", "1 tarjeta = 20-30 segundos")
    val onboardingHow2 get() = t("Scroll pour passer à la suivante", "Scroll to continue", "Desliza para continuar")
    val onboardingHow3 get() = t("Le feed s'adapte à toi", "The feed adapts to you", "El feed se adapta a ti")
    val onboardingActionsTitle get() = t("Tes actions", "Your actions", "Tus acciones")
    val onboardingAction1 get() = t("J'ai appris → archive la carte", "Learned → archives the card", "Aprendido → archiva la tarjeta")
    val onboardingAction2 get() = t("Pas utile → masque la carte", "Not useful → hides the card", "No útil → oculta la tarjeta")
    val onboardingAction3 get() = t("À revoir → revient plus tard", "Review → comes back later", "Repasar → vuelve más tarde")
    val onboardingLangTitle get() = t("Ta langue", "Your language", "Tu idioma")
    val onboardingLangSubtitle get() = t(
        "Tu pourras la changer dans les réglages",
        "You can change it later in settings",
        "Puedes cambiarlo en ajustes"
    )
}
