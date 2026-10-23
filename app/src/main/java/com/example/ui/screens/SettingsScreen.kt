package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AvatarImage
import com.example.ui.theme.AfricanGold
import com.example.ui.theme.MintAccent

enum class SettingsSubSection {
    ACCOUNT,
    PRIVACY,
    CHATS,
    NOTIFICATIONS,
    STORAGE,
    LANGUAGE,
    HELP
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onToggleDarkTheme: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var activeSubSection by remember { mutableStateOf<SettingsSubSection?>(null) }
    var showQrDialog by remember { mutableStateOf(false) }
    var showEditProfileDialog by remember { mutableStateOf(false) }

    // User Profile state
    var userName by remember { mutableStateOf("Abdoulaye Diallo (Moi)") }
    var userBio by remember { mutableStateOf("Disponible • Sama wakh, sama kër 🇸🇳") }
    var userPhone by remember { mutableStateOf("+221 77 890 12 34") }

    // Privacy toggles
    var readReceiptsEnabled by remember { mutableStateOf(true) }
    var lastSeenChoice by remember { mutableStateOf("Mes contacts") }
    var disappearingTimerChoice by remember { mutableStateOf("24 heures") }
    var protectIpInCalls by remember { mutableStateOf(true) }

    // Chat toggles
    var enterIsSend by remember { mutableStateOf(false) }
    var mediaVisibility by remember { mutableStateOf(true) }
    var selectedWallpaper by remember { mutableStateOf("Motif Traditionnel Vert") }

    // Notifications toggles
    var conversationTones by remember { mutableStateOf(true) }
    var highPriorityNotifs by remember { mutableStateOf(true) }
    var callRingtone by remember { mutableStateOf("Kora Moderne (Sénégal)") }

    // Storage toggles
    var reduceDataInCalls by remember { mutableStateOf(true) }
    var autoDownloadWifi by remember { mutableStateOf("Tous les médias") }
    var autoDownloadMobile by remember { mutableStateOf("Photos uniquement") }

    // Language
    var currentLanguage by remember { mutableStateOf("Français (Par défaut)") }

    Scaffold(
        modifier = Modifier.testTag("settings_screen_scaffold"),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (activeSubSection) {
                            SettingsSubSection.ACCOUNT -> "Compte"
                            SettingsSubSection.PRIVACY -> "Confidentialité"
                            SettingsSubSection.CHATS -> "Discussions"
                            SettingsSubSection.NOTIFICATIONS -> "Notifications"
                            SettingsSubSection.STORAGE -> "Stockage et données"
                            SettingsSubSection.LANGUAGE -> "Langue de l'application"
                            SettingsSubSection.HELP -> "Aide"
                            null -> "Paramètres"
                        },
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (activeSubSection != null) activeSubSection = null else onBack()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour", tint = Color.White)
                    }
                },
                actions = {
                    if (activeSubSection == null) {
                        IconButton(onClick = { showQrDialog = true }) {
                            Icon(Icons.Default.QrCode, contentDescription = "Mon QR Code", tint = Color.White)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when (activeSubSection) {
                null -> {
                    // Main Settings Menu
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        // User Profile Header Card
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { showEditProfileDialog = true }
                                    .padding(horizontal = 16.dp, vertical = 14.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp),
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box {
                                        AvatarImage(
                                            url = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
                                            seed = "abdoulaye_me",
                                            size = 62,
                                            isOnline = true
                                        )
                                        Box(
                                            modifier = Modifier
                                                .size(22.dp)
                                                .clip(CircleShape)
                                                .background(MaterialTheme.colorScheme.primary)
                                                .align(Alignment.BottomEnd),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(16.dp))

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(userName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            userBio,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(userPhone, fontSize = 12.sp, color = MintAccent, fontWeight = FontWeight.Medium)
                                    }

                                    IconButton(onClick = { showQrDialog = true }) {
                                        Icon(Icons.Default.QrCode2, contentDescription = "QR Code", tint = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                        }

                        // WhatsApp Settings Sections List
                        item {
                            SettingsMenuItem(
                                icon = Icons.Default.Key,
                                title = "Compte",
                                subtitle = "Notifications de sécurité, changer de numéro, clés d'accès",
                                onClick = { activeSubSection = SettingsSubSection.ACCOUNT }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.Lock,
                                title = "Confidentialité",
                                subtitle = "Dernière présence, accusés de lecture, verrouillage biométrique",
                                onClick = { activeSubSection = SettingsSubSection.PRIVACY }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.Chat,
                                title = "Discussions",
                                subtitle = "Thème, fonds d'écran, sauvegarde des discussions",
                                onClick = { activeSubSection = SettingsSubSection.CHATS }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.Notifications,
                                title = "Notifications",
                                subtitle = "Tonalités des messages, groupes et sonneries d'appels",
                                onClick = { activeSubSection = SettingsSubSection.NOTIFICATIONS }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.DataUsage,
                                title = "Stockage et données",
                                subtitle = "Utilisation réseau, téléchargement automatique, proxy",
                                onClick = { activeSubSection = SettingsSubSection.STORAGE }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.Language,
                                title = "Langue de l'application",
                                subtitle = currentLanguage,
                                onClick = { activeSubSection = SettingsSubSection.LANGUAGE }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.HelpOutline,
                                title = "Aide",
                                subtitle = "Centre d'aide, conditions générales, infos sur l'application",
                                onClick = { activeSubSection = SettingsSubSection.HELP }
                            )

                            SettingsMenuItem(
                                icon = Icons.Default.GroupAdd,
                                title = "Inviter des amis",
                                subtitle = "Partager Samawakhtane avec vos proches",
                                onClick = {
                                    Toast.makeText(context, "Lien d'invitation copié dans le presse-papiers !", Toast.LENGTH_SHORT).show()
                                }
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            // E2EE Footer badge
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Shield, contentDescription = null, tint = AfricanGold, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Samawakhtane pour Android", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                                Text("Chiffrement de bout en bout AES-256 GCM & WebRTC", fontSize = 11.sp, color = Color.Gray)
                                Text("Version 1.0.0 (Build officiel Dakar)", fontSize = 10.sp, color = Color.Gray)
                            }
                        }
                    }
                }

                // COMPTE SUB-SECTION
                SettingsSubSection.ACCOUNT -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SettingsSubHeader("Sécurité & Clés")
                            SettingsTileWithArrow(
                                icon = Icons.Default.Security,
                                title = "Notifications de sécurité",
                                subtitle = "Recevoir une notification si le code de sécurité d'un contact change",
                                onClick = { Toast.makeText(context, "Notifications de sécurité activées", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Fingerprint,
                                title = "Clés d'accès (Passkeys)",
                                subtitle = "Connexion sécurisée par empreinte digitale ou verrouillage d'écran",
                                onClick = { Toast.makeText(context, "Clé d'accès configurée dans AndroidKeyStore", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Password,
                                title = "Vérification en deux étapes",
                                subtitle = "Code PIN à 6 chiffres pour un niveau de protection supérieur",
                                onClick = { Toast.makeText(context, "Code PIN 2FA configuré : 1234", Toast.LENGTH_SHORT).show() }
                            )

                            SettingsSubHeader("Gestion du compte")
                            SettingsTileWithArrow(
                                icon = Icons.Default.PhoneForwarded,
                                title = "Changer de numéro",
                                subtitle = "Transférer votre historique, vos groupes et réglages",
                                onClick = { Toast.makeText(context, "Assistant de migration de numéro", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Assignment,
                                title = "Demander les infos du compte",
                                subtitle = "Obtenir un rapport chiffré sur vos données personnelles",
                                onClick = { Toast.makeText(context, "Rapport demandé. Préparation en cours...", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.DeleteForever,
                                title = "Supprimer mon compte",
                                subtitle = "Effacer l'historique et retirer ce numéro de Samawakhtane",
                                onClick = { Toast.makeText(context, "Suppression de compte sécurisée", Toast.LENGTH_SHORT).show() },
                                titleColor = Color.Red
                            )
                        }
                    }
                }

                // CONFIDENTIALITE SUB-SECTION
                SettingsSubSection.PRIVACY -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SettingsSubHeader("Qui peut voir mes informations")
                            SettingsTileWithChoice(
                                title = "Présence en ligne & Dernière présence",
                                currentChoice = lastSeenChoice,
                                onClick = {
                                    lastSeenChoice = if (lastSeenChoice == "Mes contacts") "Personne" else "Mes contacts"
                                }
                            )
                            SettingsTileWithChoice(
                                title = "Photo de profil",
                                currentChoice = "Mes contacts",
                                onClick = {}
                            )
                            SettingsTileWithChoice(
                                title = "Actus / Statuts",
                                currentChoice = "Mes contacts uniquement",
                                onClick = {}
                            )

                            SettingsSwitchTile(
                                title = "Confirmations de lecture",
                                subtitle = "Si cette option est désactivée, vous n'enverrez ni ne recevrez de coches bleues",
                                checked = readReceiptsEnabled,
                                onCheckedChange = { readReceiptsEnabled = it }
                            )

                            SettingsSubHeader("Messages éphémères")
                            SettingsTileWithChoice(
                                title = "Délai par défaut des messages",
                                currentChoice = disappearingTimerChoice,
                                onClick = {
                                    disappearingTimerChoice = when (disappearingTimerChoice) {
                                        "24 heures" -> "7 jours"
                                        "7 jours" -> "90 jours"
                                        else -> "24 heures"
                                    }
                                }
                            )

                            SettingsSubHeader("Sécurité des discussions & Appels")
                            SettingsTileWithArrow(
                                icon = Icons.Default.Fingerprint,
                                title = "Verrouillage par empreinte digitale",
                                subtitle = "Déverrouiller avec la biométrie (Immédiatement)",
                                onClick = { Toast.makeText(context, "Biométrie active", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Lock,
                                title = "Discussions verrouillées",
                                subtitle = "Gérer les conversations sous coffre-fort biométrique",
                                onClick = { Toast.makeText(context, "Coffre-fort à jour", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Block,
                                title = "Contacts bloqués",
                                subtitle = "0 contact bloqué",
                                onClick = { Toast.makeText(context, "Aucun contact bloqué", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsSwitchTile(
                                title = "Protéger l'adresse IP lors des appels",
                                subtitle = "Masque votre adresse IP en relayant les flux WebRTC via les serveurs sécurisés",
                                checked = protectIpInCalls,
                                onCheckedChange = { protectIpInCalls = it }
                            )
                        }
                    }
                }

                // DISCUSSIONS SUB-SECTION
                SettingsSubSection.CHATS -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SettingsSubHeader("Affichage")
                            SettingsTileWithArrow(
                                icon = Icons.Default.Brightness4,
                                title = "Thème",
                                subtitle = if (isDarkTheme) "Mode sombre (Dakar Night)" else "Mode clair (Teranga)",
                                onClick = onToggleDarkTheme
                            )
                            SettingsTileWithChoice(
                                title = "Fond d'écran des discussions",
                                currentChoice = selectedWallpaper,
                                onClick = {
                                    selectedWallpaper = if (selectedWallpaper.contains("Vert")) "Émeraude & Or Foncé" else "Motif Traditionnel Vert"
                                }
                            )

                            SettingsSubHeader("Paramètres des discussions")
                            SettingsSwitchTile(
                                title = "Touche Entrée pour envoyer",
                                subtitle = "La touche Entrée de votre clavier envoie le message",
                                checked = enterIsSend,
                                onCheckedChange = { enterIsSend = it }
                            )
                            SettingsSwitchTile(
                                title = "Visibilité des médias",
                                subtitle = "Afficher les médias téléchargés dans la galerie de votre téléphone",
                                checked = mediaVisibility,
                                onCheckedChange = { mediaVisibility = it }
                            )

                            SettingsSubHeader("Sauvegarde & Historique")
                            SettingsTileWithArrow(
                                icon = Icons.Default.Backup,
                                title = "Sauvegarde des discussions",
                                subtitle = "Sauvegarde chiffrée AES-256 (Dernière : aujourd'hui à 04:00)",
                                onClick = { Toast.makeText(context, "Sauvegarde locale chiffrée réussie !", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.History,
                                title = "Historique des discussions",
                                subtitle = "Exporter, archiver ou effacer toutes les discussions",
                                onClick = { Toast.makeText(context, "Historique disponible", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }

                // NOTIFICATIONS SUB-SECTION
                SettingsSubSection.NOTIFICATIONS -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SettingsSubHeader("Messages")
                            SettingsSwitchTile(
                                title = "Tonalités de conversation",
                                subtitle = "Émettre un son pour les messages entrants et sortants",
                                checked = conversationTones,
                                onCheckedChange = { conversationTones = it }
                            )
                            SettingsTileWithChoice(
                                title = "Sonnerie de notification",
                                currentChoice = "Bulle Douce (Samawakhtane)",
                                onClick = {}
                            )
                            SettingsTileWithChoice(
                                title = "Vibreur",
                                currentChoice = "Par défaut (Vibration moyenne)",
                                onClick = {}
                            )
                            SettingsTileWithChoice(
                                title = "Voyant lumineux",
                                currentChoice = "Vert émeraude",
                                onClick = {}
                            )
                            SettingsSwitchTile(
                                title = "Notifications de haute priorité",
                                subtitle = "Afficher un aperçu en haut de l'écran",
                                checked = highPriorityNotifs,
                                onCheckedChange = { highPriorityNotifs = it }
                            )

                            SettingsSubHeader("Appels WebRTC")
                            SettingsTileWithChoice(
                                title = "Sonnerie d'appel",
                                currentChoice = callRingtone,
                                onClick = {
                                    callRingtone = if (callRingtone.contains("Kora")) "Djembé Moderne" else "Kora Moderne (Sénégal)"
                                }
                            )
                            SettingsTileWithChoice(
                                title = "Vibration des appels",
                                currentChoice = "Par défaut",
                                onClick = {}
                            )
                        }
                    }
                }

                // STOCKAGE ET DONNEES SUB-SECTION
                SettingsSubSection.STORAGE -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            // Storage Bar Chart
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 1.dp
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("Gérer le stockage", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("1,8 Go utilisé", color = MintAccent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("58,2 Go libre", color = Color.Gray, fontSize = 13.sp)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { 0.18f },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(8.dp)
                                            .clip(RoundedCornerShape(4.dp)),
                                        color = MintAccent,
                                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    OutlinedButton(
                                        onClick = { Toast.makeText(context, "340 Mo de cache temporaire nettoyés !", Toast.LENGTH_SHORT).show() },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Nettoyer le cache multimédia")
                                    }
                                }
                            }

                            SettingsSubHeader("Téléchargement automatique des médias")
                            SettingsTileWithChoice(
                                title = "En données mobiles",
                                currentChoice = autoDownloadMobile,
                                onClick = {
                                    autoDownloadMobile = if (autoDownloadMobile.contains("Photos")) "Aucun média" else "Photos uniquement"
                                }
                            )
                            SettingsTileWithChoice(
                                title = "En Wi-Fi",
                                currentChoice = autoDownloadWifi,
                                onClick = {}
                            )

                            SettingsSubHeader("Qualité du réseau et appels")
                            SettingsSwitchTile(
                                title = "Utiliser moins de données pour les appels",
                                subtitle = "Compresse les codecs Opus et VP8/VP9 lors des appels WebRTC mobiles",
                                checked = reduceDataInCalls,
                                onCheckedChange = { reduceDataInCalls = it }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.VpnLock,
                                title = "Configuration du proxy",
                                subtitle = "Désactivé (Connexion directe P2P)",
                                onClick = { Toast.makeText(context, "Serveur Proxy sécurisé", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }

                // LANGUE SUB-SECTION
                SettingsSubSection.LANGUAGE -> {
                    val languages = listOf(
                        Pair("Français", "Langue par défaut"),
                        Pair("Wolof", "Sama Wakhtane (Sénégal)"),
                        Pair("English", "International"),
                        Pair("Pulaar / Fulfulde", "Afrique de l'Ouest"),
                        Pair("العربية (Arabe)", "Langue arabe"),
                        Pair("Mandinka", "Afrique de l'Ouest")
                    )

                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(languages) { lang ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        currentLanguage = lang.first
                                        Toast.makeText(context, "Langue définie : ${lang.first}", Toast.LENGTH_SHORT).show()
                                        activeSubSection = null
                                    }
                                    .padding(horizontal = 20.dp, vertical = 14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(lang.first, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                                    Text(lang.second, fontSize = 12.sp, color = Color.Gray)
                                }
                                if (currentLanguage.startsWith(lang.first)) {
                                    Icon(Icons.Default.Check, contentDescription = "Sélectionné", tint = MintAccent)
                                }
                            }
                            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                        }
                    }
                }

                // AIDE SUB-SECTION
                SettingsSubSection.HELP -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        item {
                            SettingsSubHeader("Assistance")
                            SettingsTileWithArrow(
                                icon = Icons.Default.HelpCenter,
                                title = "Centre d'aide & FAQ",
                                subtitle = "Guides d'utilisation, sécurité et fonctionnement E2EE",
                                onClick = { Toast.makeText(context, "Ouverture du centre d'aide", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Mail,
                                title = "Nous contacter",
                                subtitle = "Des questions ? Parlez au support technique",
                                onClick = { Toast.makeText(context, "Support : contact@samawakhtane.sn", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Gavel,
                                title = "Conditions et politique de confidentialité",
                                subtitle = "Engagements sur la non-conservation des clés et données",
                                onClick = { Toast.makeText(context, "Politique de confidentialité conforme RGPD & CDP Sénégal", Toast.LENGTH_SHORT).show() }
                            )
                            SettingsTileWithArrow(
                                icon = Icons.Default.Info,
                                title = "Informations sur l'application",
                                subtitle = "Samawakhtane v1.0.0 • Sécurisé par AndroidKeyStore",
                                onClick = { Toast.makeText(context, "Samawakhtane - Fait avec fierté en Afrique", Toast.LENGTH_SHORT).show() }
                            )
                        }
                    }
                }
            }
        }
    }

    // QR Code Dialog
    if (showQrDialog) {
        AlertDialog(
            onDismissRequest = { showQrDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.QrCode2, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Mon Code QR Samawakhtane", fontWeight = FontWeight.Bold, fontSize = 17.sp)
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "Vos amis peuvent scanner ce code QR pour lancer une discussion sécurisée de bout en bout avec vous.",
                        fontSize = 13.sp,
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Surface(
                        color = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(200.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.QrCode,
                                    contentDescription = "QR Code",
                                    tint = Color.Black,
                                    modifier = Modifier.size(150.dp)
                                )
                                Text("SAMAWAKHTANE-E2EE", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.DarkGray)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(userName, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(userPhone, color = MintAccent, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                }
            },
            confirmButton = {
                Button(onClick = {
                    showQrDialog = false
                    Toast.makeText(context, "Code QR partagé !", Toast.LENGTH_SHORT).show()
                }) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Partager mon code")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQrDialog = false }) {
                    Text("Fermer")
                }
            }
        )
    }

    // Edit Profile Dialog
    if (showEditProfileDialog) {
        var tempName by remember { mutableStateOf(userName) }
        var tempBio by remember { mutableStateOf(userBio) }
        var tempPhone by remember { mutableStateOf(userPhone) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = { Text("Modifier mon profil", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = tempName,
                        onValueChange = { tempName = it },
                        label = { Text("Nom complet") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempBio,
                        onValueChange = { tempBio = it },
                        label = { Text("Actu / Biographie") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = tempPhone,
                        onValueChange = { tempPhone = it },
                        label = { Text("Numéro de téléphone") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    userName = tempName
                    userBio = tempBio
                    userPhone = tempPhone
                    showEditProfileDialog = false
                    Toast.makeText(context, "Profil mis à jour !", Toast.LENGTH_SHORT).show()
                }) {
                    Text("Enregistrer")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text("Annuler")
                }
            }
        )
    }
}

@Composable
fun SettingsMenuItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(20.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun SettingsSubHeader(title: String) {
    Text(
        text = title.uppercase(),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 20.dp, top = 20.dp, bottom = 8.dp)
    )
}

@Composable
fun SettingsTileWithArrow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    titleColor: Color = Color.Unspecified
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(18.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp, color = if (titleColor != Color.Unspecified) titleColor else MaterialTheme.colorScheme.onSurface)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun SettingsTileWithChoice(
    title: String,
    currentChoice: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(currentChoice, fontSize = 13.sp, color = MintAccent, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun SettingsSwitchTile(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Medium, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = MintAccent)
        )
    }
}
