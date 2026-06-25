package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.RecognitionListener
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.R
import com.example.data.*
import com.example.viewmodel.*
import com.example.network.*
import com.example.utils.UrduDictionary
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainFarmersScreen(
    viewModel: FarmersViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sessions by viewModel.sessions.collectAsStateWithLifecycle()
    val activeSessionId by viewModel.currentSessionId.collectAsStateWithLifecycle()
    val messages by viewModel.currentMessages.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLanguage by viewModel.selectedLanguage.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()

    var inputQueryText by remember { mutableStateOf("") }
    var showSessionDrawer by remember { mutableStateOf(false) }
    var showMarketsDialog by remember { mutableStateOf(false) }
    var showSetupDialog by remember { mutableStateOf(false) }
    var showHelpGuideDialog by remember { mutableStateOf(false) }
    var activeTTSMessageId by remember { mutableStateOf<String?>(null) }

    var showCustomVoiceDialog by remember { mutableStateOf(false) }
    var showVoiceTextInputDialog by remember { mutableStateOf(false) }
    var lastClickedVoiceMode by remember { mutableStateOf("send") } // "send" or "input"
    var showLiveSessionDialog by remember { mutableStateOf(false) }

    val liveConnectionState by viewModel.liveConnectionState.collectAsStateWithLifecycle()
    val liveReceivedText by viewModel.liveReceivedText.collectAsStateWithLifecycle()

    var isHandsFreeActive by remember { mutableStateOf(false) }
    var triggerAutoMicLaunch by remember { mutableStateOf(false) }
    val messageTranslations by viewModel.messageTranslations.collectAsStateWithLifecycle()
    val translationLoadingIds by viewModel.translationLoadingIds.collectAsStateWithLifecycle()

    // TTS Setup
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var isTtsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                isTtsInitialized = true
            }
        }
        textToSpeech = tts
        onDispose {
            tts.stop()
            tts.shutdown()
        }
    }

    val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()

    if (userProfile == null || userProfile?.onboardingCompleted != true) {
        KisaanOnboardingScreen(
            viewModel = viewModel,
            textToSpeech = textToSpeech,
            isTtsInitialized = isTtsInitialized
        )
        return
    }

    // Speech-to-Text handle callback
    val speechToTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spokenResults?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                inputQueryText = text
                Toast.makeText(context, "آواز منتقل ہو گئی: \"$text\"", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Record audio permission launcher
    val microphonePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            if (lastClickedVoiceMode == "send") {
                showCustomVoiceDialog = true
            } else {
                showVoiceTextInputDialog = true
            }
        } else {
            Toast.makeText(context, "آواز ریکارڈ کرنے کی اجازت مسترد کر دی گئی۔ آپ ٹائپ کر کے بھی پوچھ سکتے ہیں۔", Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(messages) {
        val lastMessage = messages.lastOrNull()
        if (lastMessage != null && lastMessage.role == "model" && isHandsFreeActive) {
            if (activeTTSMessageId != lastMessage.id) {
                activeTTSMessageId = lastMessage.id
                speakOutLoud(
                    textToSpeech = textToSpeech,
                    text = lastMessage.text,
                    languageCode = selectedLanguage.audioLocale,
                    onDone = {
                        activeTTSMessageId = null
                        if (isHandsFreeActive) {
                            triggerAutoMicLaunch = true
                        }
                    }
                )
            }
        }
    }

    LaunchedEffect(triggerAutoMicLaunch) {
        if (triggerAutoMicLaunch) {
            triggerAutoMicLaunch = false
            lastClickedVoiceMode = "send"
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED

            if (hasPermission) {
                showCustomVoiceDialog = true
            } else {
                microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
            }
        }
    }

    // Filter messages by active category
    val filteredMessages = remember(messages, selectedCategory) {
        if (selectedCategory == "All") {
            messages
        } else {
            messages.filter { it.category.equals(selectedCategory, ignoreCase = true) }
        }
    }

    val listState = rememberLazyListState()
    LaunchedEffect(filteredMessages.size) {
        if (filteredMessages.isNotEmpty()) {
            listState.animateScrollToItem(filteredMessages.size - 1)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0A0C0B))
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (userProfile != null) "خوش آمدید، ${userProfile?.fullName}" else "کِسان دوست",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color(0xFFD1E8D1)
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        val pulseTransition = rememberInfiniteTransition(label = "pulse_green")
                        val blinkAlpha by pulseTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1.0f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "blink"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(blinkAlpha)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Text(
                            text = "آپ کا زرعی معاون حاضر ہے",
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.7f),
                            fontWeight = FontWeight.Light
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    userProfile?.let { prof ->
                        val cropEmoji = when (prof.primaryCrop) {
                            "Wheat" -> "🌾"
                            "Cotton" -> "☁️"
                            "Rice" -> "🍚"
                            "Sugarcane" -> "🎋"
                            "Livestock" -> "🐄"
                            else -> "🍎"
                        }
                        IconButton(
                            onClick = {
                                viewModel.logoutOrClearProfile()
                                Toast.makeText(context, "اکاؤنٹ تبدیل کیا جا رہا ہے...", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("profile_badge_button")
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                    .border(1.dp, Color(0xFF10B981), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(cropEmoji, fontSize = 16.sp)
                            }
                        }
                    }

                    LanguageMenuButton(
                        currentLanguage = selectedLanguage,
                        onLanguageSelected = { viewModel.setLanguage(it) }
                    )

                    IconButton(
                        onClick = { showLiveSessionDialog = true },
                        modifier = Modifier.testTag("live_translation_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            val pulseTransition = rememberInfiniteTransition(label = "pulse_live_icon")
                            val livePulseScale by pulseTransition.animateFloat(
                                initialValue = 0.95f,
                                targetValue = 1.15f,
                                animationSpec = infiniteRepeatable(
                                    animation = tween(800),
                                    repeatMode = RepeatMode.Reverse
                                ),
                                label = "live_icon_pulse"
                            )
                            Icon(
                                imageVector = Icons.Filled.RecordVoiceOver,
                                contentDescription = "Live Voice Translation Room",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(24.dp).scale(livePulseScale)
                            )
                            // Small red "live" badge indicator
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .align(Alignment.TopEnd)
                                    .background(Color(0xFFEF4444), CircleShape)
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF1F2420))
                            .border(BorderStroke(1.dp, Color(0xFF3E4A40)), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "User Profile",
                            tint = Color(0xFFE1E3E1),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.background,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                if (showHelpGuideDialog) {
                    OfflineFarmingGuide(
                        selectedLanguage = selectedLanguage,
                        onClose = { showHelpGuideDialog = false },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    // Category Filter Tabs
                    CategoryFilterBar(
                        selectedCategory = selectedCategory,
                        onCategorySelected = { viewModel.setCategoryFilter(it) }
                    )

                if (filteredMessages.isEmpty()) {
                    // Empty Conversation Page: Show Friendly Pakistani Agricultural Guidance Topics
                    EmptyStateGuide(
                        selectedLanguage = selectedLanguage,
                        onTopicSelected = { topicText, cat ->
                            inputQueryText = topicText
                            viewModel.sendMessage(topicText, cat)
                        },
                        onMicClick = {
                            val hasPermission = ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.RECORD_AUDIO
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasPermission) {
                                showCustomVoiceDialog = true
                            } else {
                                microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                            }
                        },
                        isHandsFreeActive = isHandsFreeActive,
                        onHandsFreeToggle = { isHandsFreeActive = it }
                    )
                } else {
                    // Chat Messages Feed
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredMessages, key = { it.id }) { msg ->
                            val isUser = msg.role == "user"
                            val isTtsPlaying = activeTTSMessageId == msg.id

                            ChatMessageBubble(
                                message = msg,
                                isUser = isUser,
                                isTtsPlaying = isTtsPlaying,
                                onSpeakClick = {
                                    if (isTtsPlaying) {
                                        textToSpeech?.stop()
                                        activeTTSMessageId = null
                                    } else {
                                        activeTTSMessageId = msg.id
                                        speakOutLoud(
                                            textToSpeech = textToSpeech,
                                            text = msg.text,
                                            languageCode = selectedLanguage.audioLocale,
                                            onDone = { activeTTSMessageId = null }
                                        )
                                    }
                                },
                                translationsMap = messageTranslations,
                                loadingIds = translationLoadingIds,
                                onTranslateSelected = { targetLang ->
                                    viewModel.translateMessageOnTheFly(msg.id, msg.text, targetLang)
                                },
                                speakTranslation = { text ->
                                    speakOutLoud(
                                        textToSpeech = textToSpeech,
                                        text = text,
                                        languageCode = selectedLanguage.audioLocale,
                                        onDone = {}
                                    )
                                }
                            )
                        }
                    }
                }

                // Interaction Action Panel at Bottom (Floating text field and Mic)
                BottomActionPanel(
                    queryText = inputQueryText,
                    onQueryChange = { inputQueryText = it },
                    uiState = uiState,
                    selectedLanguage = selectedLanguage,
                    onSendClick = {
                        if (inputQueryText.isNotBlank()) {
                            viewModel.sendMessage(inputQueryText, determineCategory(inputQueryText))
                            inputQueryText = ""
                        }
                    },
                    onMicClick = {
                        lastClickedVoiceMode = "send"
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            showCustomVoiceDialog = true
                        } else {
                            microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    onVoiceTextInputClick = {
                        lastClickedVoiceMode = "input"
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO
                        ) == PackageManager.PERMISSION_GRANTED

                        if (hasPermission) {
                            showVoiceTextInputDialog = true
                        } else {
                            microphonePermissionLauncher.launch(android.Manifest.permission.RECORD_AUDIO)
                        }
                    },
                    isHandsFreeActive = isHandsFreeActive,
                    onHandsFreeToggle = { isHandsFreeActive = it }
                )
                }

                // High fidelity bottom navigation bar from the Elegant Dark theme guidelines
                val activeNav = when {
                    showSessionDrawer -> "History"
                    showMarketsDialog -> "Markets"
                    showSetupDialog -> "Setup"
                    showHelpGuideDialog -> "Help"
                    else -> "Home"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1F2420))
                        .padding(vertical = 4.dp)
                        .navigationBarsPadding(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    NavTabItem(
                        icon = Icons.Default.Home,
                        labelUrdu = "مرکزی صفحہ",
                        isActive = activeNav == "Home",
                        onClick = {
                            viewModel.setCategoryFilter("All")
                            Toast.makeText(context, "مرکزی صفحہ ری سیٹ ہو گیا", Toast.LENGTH_SHORT).show()
                            showSessionDrawer = false
                            showMarketsDialog = false
                            showSetupDialog = false
                            showHelpGuideDialog = false
                        }
                    )

                    NavTabItem(
                        icon = Icons.Default.Help,
                        labelUrdu = "زرعی معلومات",
                        isActive = activeNav == "Help",
                        onClick = {
                            showHelpGuideDialog = true
                            showSessionDrawer = false
                            showMarketsDialog = false
                            showSetupDialog = false
                        }
                    )

                    NavTabItem(
                        icon = Icons.Default.History,
                        labelUrdu = "پرانی گفتگو",
                        isActive = activeNav == "History",
                        onClick = {
                            showSessionDrawer = !showSessionDrawer
                            showMarketsDialog = false
                            showSetupDialog = false
                            showHelpGuideDialog = false
                        }
                    )

                    NavTabItem(
                        icon = Icons.Default.Assessment,
                        labelUrdu = "منڈی ریٹ",
                        isActive = activeNav == "Markets",
                        onClick = {
                            showMarketsDialog = true
                            showSessionDrawer = false
                            showSetupDialog = false
                            showHelpGuideDialog = false
                        }
                    )

                    NavTabItem(
                        icon = Icons.Default.Settings,
                        labelUrdu = "سیٹنگز",
                        isActive = activeNav == "Setup",
                        onClick = {
                            showSetupDialog = true
                            showSessionDrawer = false
                            showMarketsDialog = false
                            showHelpGuideDialog = false
                        }
                    )
                }
            }

            // Markets Dialog Overlay
            if (showMarketsDialog) {
                AlertDialog(
                    onDismissRequest = { showMarketsDialog = false },
                    title = {
                        Text(
                            text = "فصلوں کی تازہ ترین منڈی ریٹ • Market Rates",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFD1E8D1)
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = "پنجاب اور سندھ کی غلہ منڈیاں (rates per 40 Kg):",
                                fontSize = 12.sp,
                                color = Color(0xFFE1E3E1).copy(alpha = 0.7f),
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            listOf(
                                "گندم (Wheat)" to "Rs. 3,850",
                                "کپاس (Cotton)" to "Rs. 7,400",
                                "باسمتی چاول (Rice)" to "Rs. 6,800",
                                "گنا (Sugarcane)" to "Rs. 425",
                                "مکئی (Maize)" to "Rs. 2,200",
                                "سرصوں (Mustard)" to "Rs. 8,100"
                            ).forEach { (crop, price) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = crop, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = Color(0xFFE1E3E1))
                                    Text(text = price, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFFD1E8D1))
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showMarketsDialog = false }) {
                            Text(text = "ٹھیک ہے (OK)", color = Color(0xFFD1E8D1))
                        }
                    },
                    containerColor = Color(0xFF1F2420),
                    textContentColor = Color(0xFFE1E3E1)
                )
            }

            // Setup Dialog Overlay
            if (showSetupDialog) {
                var selectedModelSpeed by remember { mutableStateOf(0.7f) }
                AlertDialog(
                    onDismissRequest = { showSetupDialog = false },
                    title = {
                        Text(
                            text = "معاون کی ترتیبات • Assistant Setup",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color(0xFFD1E8D1)
                        )
                    },
                    text = {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "آواز کی رفتار اور اسسٹنٹ ماڈل سیٹ اپ کریں:",
                                fontSize = 12.sp,
                                color = Color(0xFFE1E3E1).copy(alpha = 0.7f)
                            )

                            Text(
                                text = "آواز کی رفتار (Speech Speed): ${(selectedModelSpeed * 2.0).toString().take(4)}x",
                                fontSize = 13.sp,
                                color = Color(0xFFE1E3E1)
                            )

                            Slider(
                                value = selectedModelSpeed,
                                onValueChange = { selectedModelSpeed = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = Color(0xFFD1E8D1),
                                    activeTrackColor = Color(0xFF10B981)
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = UrduDictionary.VOICE_ASSISTANT_ACTIVATE, fontSize = 13.sp, color = Color(0xFFE1E3E1))
                                Switch(
                                    checked = true,
                                    onCheckedChange = {},
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF0A0C0B),
                                        checkedTrackColor = Color(0xFFD1E8D1)
                                    )
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "صرف مستند زرعی اور موسماتی ڈیٹا کے لیے Gemini 3.5 Flash انجن استعمال کیا جا رہا ہے۔",
                                fontSize = 10.sp,
                                color = Color(0xFFE1E3E1).copy(alpha = 0.5f),
                                lineHeight = 14.sp
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showSetupDialog = false }) {
                            Text(text = "محفوظ کریں (Save)", color = Color(0xFFD1E8D1))
                        }
                    },
                    containerColor = Color(0xFF1F2420),
                    textContentColor = Color(0xFFE1E3E1)
                )
            }

            // Chat Session Navigation Drawer
            if (showSessionDrawer) {
                SessionDrawerOverlay(
                    sessions = sessions,
                    activeSessionId = activeSessionId,
                    onSessionSelect = {
                        viewModel.selectSession(it)
                        showSessionDrawer = false
                    },
                    onSessionDelete = { viewModel.deleteSession(it) },
                    onNewSessionClick = {
                        viewModel.createNewSession("نئی گفتگو - ${SimpleDateFormat("dd MMM", Locale.getDefault()).format(Date())}")
                        showSessionDrawer = false
                    },
                    onDismiss = { showSessionDrawer = false }
                )
            }
        }
    }

    if (showCustomVoiceDialog) {
        CustomVoiceListeningDialog(
            language = selectedLanguage,
            onSpeechResult = { text ->
                if (text.isNotBlank()) {
                    inputQueryText = text
                    viewModel.sendMessage(text, determineCategory(text))
                    inputQueryText = ""
                }
                showCustomVoiceDialog = false
            },
            onDismiss = {
                showCustomVoiceDialog = false
            }
        )
    }

    if (showVoiceTextInputDialog) {
        CustomVoiceListeningDialog(
            language = selectedLanguage,
            onSpeechResult = { text ->
                if (text.isNotBlank()) {
                    inputQueryText = text
                    Toast.makeText(context, "آواز تبدیل کر دی گئی", Toast.LENGTH_SHORT).show()
                }
                showVoiceTextInputDialog = false
            },
            onDismiss = {
                showVoiceTextInputDialog = false
            }
        )
    }

    if (showLiveSessionDialog) {
        FarmersLiveSessionDialog(
            onDismiss = { showLiveSessionDialog = false },
            viewModel = viewModel,
            selectedLanguage = selectedLanguage,
            liveConnectionState = liveConnectionState,
            liveReceivedText = liveReceivedText
        )
    }

    // Handle security and extracted keys warnings as per guidelines
    LaunchedEffect(Unit) {
        Toast.makeText(context, UrduDictionary.VOICE_ASSISTANT_WELCOME, Toast.LENGTH_LONG).show()
    }
}

// Language Selector DropDown Menu
@Composable
fun LanguageMenuButton(
    currentLanguage: LanguageOption,
    onLanguageSelected: (LanguageOption) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Button(
            onClick = { expanded = true },
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            modifier = Modifier.height(36.dp).testTag("language_selector")
        ) {
            Icon(
                imageVector = Icons.Filled.Language,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = currentLanguage.displayName.split(" ").firstOrNull() ?: "Urdu",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            LanguageOption.values().forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayName,
                            fontWeight = if (option == currentLanguage) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    onClick = {
                        onLanguageSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

// Category Filter bar for farming topics
@Composable
fun CategoryFilterBar(
    selectedCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        CategoryTab("All", "سب معلومات", Icons.Outlined.Widgets),
        CategoryTab("Crops", "فصلیں", Icons.Outlined.Spa),
        CategoryTab("Pest", "کیڑے اور بیماری", Icons.Outlined.BugReport),
        CategoryTab("Weather", "موسم اور پانی", Icons.Outlined.WbSunny),
        CategoryTab("Livestock", "مال مویشی", Icons.Outlined.Pets)
    )

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 4.dp),
        contentPadding = PaddingValues(horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { cat ->
            val isSelected = cat.id == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(cat.id) },
                label = { Text(text = cat.urduName) },
                leadingIcon = {
                    Icon(
                        imageVector = cat.icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

data class CategoryTab(val id: String, val urduName: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// Farmers Chat speech bubbles
@Composable
fun ChatMessageBubble(
    message: ChatMessage,
    isUser: Boolean,
    isTtsPlaying: Boolean,
    onSpeakClick: () -> Unit,
    translationsMap: Map<String, String>,
    loadingIds: Set<String>,
    onTranslateSelected: (LanguageOption) -> Unit,
    speakTranslation: (String) -> Unit
) {
    val speechDirection = if (isUser) TextDirection.Rtl else TextDirection.Ltr
    val themeCardColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.85f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    }

    val alignment = if (isUser) Alignment.End else Alignment.Start
    val shape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalAlignment = alignment
    ) {
        // Tag Category helper
        if (!isUser && message.category != "General") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.padding(bottom = 2.dp, start = 4.dp)
            ) {
                Text(
                    text = translateCategoryUrdu(message.category),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(0.95f),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Bottom
        ) {
            if (!isUser) {
                // Dynamic pulsing speaker icon for TTS reading
                IconButton(
                    onClick = onSpeakClick,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            if (isTtsPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f),
                            CircleShape
                        ).testTag("tts_play_${message.id}")
                ) {
                    Icon(
                        imageVector = if (isTtsPlaying) Icons.Filled.VolumeUp else Icons.Outlined.VolumeUp,
                        contentDescription = "سنائیے (Speak)",
                        tint = if (isTtsPlaying) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Message Body Text block
            Box(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .clip(shape)
                    .background(themeCardColor)
                    .border(
                        BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        shape
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Column {
                    // Dynamic Agentic Diagnostic Card if !isUser
                    if (!isUser) {
                        val parsedCrop = remember(message.text) {
                            val text = message.text.lowercase()
                            when {
                                text.contains("گندم") || text.contains("gandum") || text.contains("wheat") -> "گندم"
                                text.contains("کپاس") || text.contains("kapas") || text.contains("cotton") -> "کپاس"
                                text.contains("چاول") || text.contains("chawal") || text.contains("rice") -> "چاول"
                                text.contains("گنا") || text.contains("kamand") || text.contains("sugarcane") || text.contains("کماند") -> "گنا"
                                text.contains("مکئی") || text.contains("makai") || text.contains("maize") -> "مکئی"
                                text.contains("آم") || text.contains("aam") || text.contains("mango") -> "آم (آم کی کاشت)"
                                text.contains("پیاز") || text.contains("onion") -> "پیاز"
                                text.contains("ٹماٹر") || text.contains("tomato") -> "ٹماٹر"
                                text.contains("مالٹا") || text.contains("citrus") -> "کینو / مالٹا"
                                text.contains("پانی") || text.contains("paani") || text.contains("water") || text.contains("irrigation") -> "پانی کا نظام"
                                text.contains("کھاد") || text.contains("khaad") || text.contains("fertilizer") -> "کھاد اور غذائیت"
                                text.contains("مرغی") || text.contains("murgi") || text.contains("poultry") || text.contains("بھینس") || text.contains("گائے") || text.contains("animal") || text.contains("livestock") -> "مال مویشی اور مرغیاں"
                                else -> "عام رہنمائی"
                            }
                        }
                        
                        val isHighUrgency = remember(message.text) {
                            val text = message.text
                            text.contains("شدید") || text.contains("فوری") || text.contains("نقصان") || text.contains("بیماری") || text.contains("حملہ") || text.contains("کیڑا") || text.contains("pest") || text.contains("attack") || text.contains("disease")
                        }
                        
                        val urgencyText = if (isHighUrgency) "فوری توجہ" else "عمومی مشورہ"
                        val urgencyColor = if (isHighUrgency) Color(0xFFEF4444) else Color(0xFF10B981)

                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1210)),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color(0xFF223628)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(11.dp)
                                        )
                                    }
                                    
                                    Text(
                                        text = "کِسان دوست نظامِ تجزیہ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFFD1E8D1)
                                    )
                                    
                                    Spacer(modifier = Modifier.weight(1f))
                                    
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                            .padding(horizontal = 5.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "تصدیق شدہ",
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFF10B981)
                                        )
                                    }
                                }
                                
                                HorizontalDivider(
                                    color = Color(0xFF223628), 
                                    thickness = 0.5.dp, 
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                                
                                Row(
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column {
                                        Text(
                                            text = "مضمون / فصل",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD1E8D1).copy(alpha = 0.5f)
                                        )
                                        Text(
                                            text = parsedCrop,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFE1E3E1),
                                            modifier = Modifier.padding(top = 1.dp)
                                        )
                                    }
                                    
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "اہمیت کا درجہ",
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color(0xFFD1E8D1).copy(alpha = 0.5f)
                                        )
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                                            modifier = Modifier.padding(top = 1.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .clip(CircleShape)
                                                    .background(urgencyColor)
                                            )
                                            Text(
                                                text = urgencyText,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = urgencyColor
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = message.text,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        color = if (isUser) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        textAlign = if (isUser) TextAlign.Right else TextAlign.Left,
                        style = LocalTextStyle.current.copy(
                            textDirection = TextDirection.ContentOrLtr
                        )
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date(message.timestamp)),
                            fontSize = 9.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )

                        if (!isUser) {
                            var showTranslationMenu by remember { mutableStateOf(false) }
                            Box {
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .clickable { showTranslationMenu = true }
                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Language,
                                        contentDescription = "Translate",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(10.dp)
                                    )
                                    Text(
                                        text = "زبان تبدیل کریں",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }

                                DropdownMenu(
                                    expanded = showTranslationMenu,
                                    onDismissRequest = { showTranslationMenu = false }
                                ) {
                                    LanguageOption.values().forEach { opt ->
                                        DropdownMenuItem(
                                            text = { Text(opt.displayName, fontSize = 12.sp) },
                                            onClick = {
                                                onTranslateSelected(opt)
                                                showTranslationMenu = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // On-the-fly translated response card
                    if (!isUser) {
                        val translatedText = translationsMap[message.id]
                        val isLoading = loadingIds.contains(message.id)

                        if (isLoading) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                    .padding(6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 1.dp)
                                Text("ترجمہ تیار کیا جا رہا ہے...", fontSize = 10.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        } else if (translatedText != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp)
                                    .background(Color(0xFF0F1210), RoundedCornerShape(8.dp))
                                    .padding(8.dp)
                                ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "ترجمہ شدہ مشورہ",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981)
                                    )
                                    IconButton(
                                        onClick = { speakTranslation(translatedText) },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.VolumeUp,
                                            contentDescription = "Speak translation",
                                            tint = Color(0xFF10B981),
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = translatedText,
                                    fontSize = 13.sp,
                                    lineHeight = 18.sp,
                                    color = Color(0xFFD1E8D1),
                                    textAlign = TextAlign.Right,
                                    modifier = Modifier.fillMaxWidth(),
                                    style = LocalTextStyle.current.copy(
                                        textDirection = TextDirection.ContentOrLtr
                                    )
                                )
                            }
                        }
                    }
                }
            }

            if (isUser) {
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp).align(Alignment.Top)
                )
            }
        }
    }
}

// Farm advice empty state pre-filled helper cards
@Composable
fun EmptyStateGuide(
    selectedLanguage: LanguageOption,
    onTopicSelected: (String, String) -> Unit,
    onMicClick: () -> Unit,
    isHandsFreeActive: Boolean,
    onHandsFreeToggle: (Boolean) -> Unit
) {
    val guides = listOf(
        GuideItem("فصلوں کی دیکھ بھال", "گندم کی اچھی پیداوار حاصل کرنے کے لیے کھاد کا صحیح شیڈول کیا ہے؟", "Crops", Icons.Outlined.Spa),
        GuideItem("بیماریاں اور کیڑے", "کپاس کے پتوں پر سفید مکھی کے حملے کا دیسی اور سستا علاج بتائیں۔", "Pest", Icons.Outlined.BugReport),
        GuideItem("موسم اور پانی", "کیا اس مہینے بارانِ رحمت سے نہری پانی کے استعمال میں کمی کرنی چاہیے؟", "Weather", Icons.Outlined.WbSunny),
        GuideItem("مال مویشی", "بھینس کا دودھ بڑھانے کے لیے کونسا دیسی ونڈا بہترین اور سستا ہے؟", "Livestock", Icons.Outlined.Pets)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Aesthetic concentric decorative rings with centralized mic and bilingual speak prompt
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Elegant Concentric Web Mock Circles
            Box(
                modifier = Modifier
                    .size(230.dp)
                    .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.15f)), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(160.dp)
                    .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.10f)), CircleShape)
            )
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .border(BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.05f)), CircleShape)
            )

            // Inner focus content
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "موبائل سے بات کریں",
                    fontWeight = FontWeight.Bold,
                    fontSize = 19.sp,
                    color = Color(0xFFE1E3E1),
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "زراعت اور فصلوں کا کوئی بھی سوال پوچھیے",
                    fontSize = 12.sp,
                    color = Color(0xFFD1E8D1).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Light,
                    modifier = Modifier.padding(top = 2.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                // Elegant physical dark circle mic button with customized drop shadowglow
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .shadow(
                            elevation = 24.dp,
                            shape = CircleShape,
                            spotColor = Color(0xFFD1E8D1).copy(alpha = 0.35f)
                        )
                        .background(Color(0xFFD1E8D1), CircleShape)
                        .clickable(onClick = onMicClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "Speak voice query",
                        tint = Color(0xFF0A0C0B),
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Decorative wave equalizer bars at the bottom
                Row(
                    modifier = Modifier.padding(top = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(width = 3.dp, height = 12.dp).background(Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(width = 3.dp, height = 20.dp).background(Color(0xFF10B981).copy(alpha = 0.6f), RoundedCornerShape(2.dp)))
                    Box(modifier = Modifier.size(width = 3.dp, height = 14.dp).background(Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(2.dp)))
                }
            }
        }

        // State-driven hands-free toggler on the empty state guide
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 12.dp)
                .background(Color(0xFF1F2420), RoundedCornerShape(16.dp))
                .border(BorderStroke(1.dp, if (isHandsFreeActive) Color(0xFF10B981) else Color(0xFF3E4A40)), RoundedCornerShape(16.dp))
                .clickable { onHandsFreeToggle(!isHandsFreeActive) }
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = null,
                    tint = if (isHandsFreeActive) Color(0xFF10B981) else Color(0xFFD1E8D1).copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
                Column {
                    Text(
                        text = UrduDictionary.VOICE_AUTOMATIC_MODE,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE1E3E1)
                    )
                    Text(
                        text = "معاون خود بخود جواب سنائے گا اور آپ کا سوال سنے گا",
                        fontSize = 10.sp,
                        color = Color(0xFFE1E3E1).copy(alpha = 0.5f)
                    )
                }
            }
            Switch(
                checked = isHandsFreeActive,
                onCheckedChange = onHandsFreeToggle,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF0A0C0B),
                    checkedTrackColor = Color(0xFF10B981),
                    uncheckedThumbColor = Color(0xFFD1E8D1).copy(alpha = 0.5f),
                    uncheckedTrackColor = Color(0xFF1F2420)
                ),
                modifier = Modifier.scale(0.85f).testTag("handsfree_welcome_switch")
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Section with "Latest Advice" from Design Guidelines:
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .border(BorderStroke(1.dp, Color(0xFF3E4A40)), RoundedCornerShape(24.dp)),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2420)),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "LATEST ADVICE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E8D1).copy(alpha = 0.6f)
                    )
                    Text(
                        text = "2 mins ago",
                        fontSize = 9.sp,
                        color = Color(0xFFD1E8D1).copy(alpha = 0.4f)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "گندم کی کٹائی کے لیے موسم موزوں ہے۔ اگلے تین دن تک بارش کا کوئی امکان نہیں ہے۔",
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                    color = Color(0xFFE1E3E1),
                    textAlign = TextAlign.Right,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clickable {
                            onTopicSelected("گندم کی اچھی پیداوار حاصل کرنے کے لیے کھاد کا صحیح شیڈول کیا ہے؟", "Crops")
                        },
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Details",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E8D1)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFFD1E8D1),
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Grid of interactive cards
        guides.forEach { guide ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable { onTopicSelected(guide.prompt, guide.category) },
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2420)),
                border = BorderStroke(0.5.dp, Color(0xFF3E4A40)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = guide.icon,
                        contentDescription = null,
                        tint = Color(0xFFD1E8D1),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = guide.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = Color(0xFFE1E3E1)
                        )
                        Text(
                            text = guide.prompt,
                            fontSize = 11.sp,
                            maxLines = 1,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color(0xFFD1E8D1).copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}

data class GuideItem(val title: String, val prompt: String, val category: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

// Bottom control board with big pulsing voice capturing button
@Composable
fun BottomActionPanel(
    queryText: String,
    onQueryChange: (String) -> Unit,
    uiState: FarmersUiState,
    selectedLanguage: LanguageOption,
    onSendClick: () -> Unit,
    onMicClick: () -> Unit,
    onVoiceTextInputClick: () -> Unit,
    isHandsFreeActive: Boolean,
    onHandsFreeToggle: (Boolean) -> Unit
) {
    var isRecordingPulse by remember { mutableStateOf(false) }
    val isProgressActive = uiState is FarmersUiState.Loading

    // Pulse animation logic for microphone button
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isRecordingPulse) 1.25f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Elegant hands free subtitle/toggle row in input panel
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val pulseTransition = rememberInfiniteTransition(label = "pulse_handsfree")
                    val pulseAlpha by pulseTransition.animateFloat(
                        initialValue = 0.5f,
                        targetValue = 1.0f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1250),
                            repeatMode = RepeatMode.Reverse
                        ),
                        label = "pulse_alpha"
                    )
                    
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isHandsFreeActive) Color(0xFF10B981) else Color(0xFFE1E3E1).copy(alpha = 0.4f),
                        modifier = Modifier
                            .size(16.dp)
                            .scale(if (isHandsFreeActive) pulseAlpha else 1f)
                    )
                    
                    Text(
                        text = if (isHandsFreeActive) "ہینڈز فری موڈ فعال ہے • Hands-Free ON" else "ہینڈز فری گفتگو • Hands-Free Mode",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isHandsFreeActive) Color(0xFF10B981) else Color(0xFFE1E3E1).copy(alpha = 0.5f)
                    )
                }

                Switch(
                    checked = isHandsFreeActive,
                    onCheckedChange = onHandsFreeToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color(0xFF0A0C0B),
                        checkedTrackColor = Color(0xFF10B981),
                        uncheckedThumbColor = Color(0xFFD1E8D1).copy(alpha = 0.5f),
                        uncheckedTrackColor = Color(0xFF1F2420)
                    ),
                    modifier = Modifier.scale(0.80f).testTag("handsfree_toggle_switch")
                )
            }

            if (isProgressActive) {
                // High fidelity responsive multi-step AI Agent Query Analyser Timeline
                var agentStep by remember { mutableStateOf(0) }
                LaunchedEffect(Unit) {
                    while (true) {
                        kotlinx.coroutines.delay(1800)
                        agentStep = (agentStep + 1) % 4
                    }
                }
                
                val stepTextUrdu = when (agentStep) {
                    0 -> "🔍 سوال کا تجزیہ اور فصل کی شناخت جاری ہے..."
                    1 -> "🌾 مٹی، بیج اور مقامی زراعتی ڈیٹا بیس کا جائزہ..."
                    2 -> "☀️ موسمی حالات اور پانی کے نظام کا موازنہ..."
                    else -> "🌱 کسان لائحہ عمل اور حل تیار کیا جا رہا ہے..."
                }
                
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF131A15)),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF1F2E22)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color(0xFF10B981)
                            )
                            Text(
                                text = "کِسان دوست اسسٹنٹ تجزیہ",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(6.dp))
                        
                        Text(
                            text = stepTextUrdu,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE1E3E1),
                            style = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl)
                        )
                        
                        // Small workflow indicator bar
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (i in 0..3) {
                                val isPassed = i <= agentStep
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(1.5.dp))
                                        .background(if (isPassed) Color(0xFF10B981) else Color(0xFF223628))
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Pulsing Mic Button for voice recording
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .scale(pulseScale)
                        .clickable {
                            isRecordingPulse = true
                            onMicClick()
                            isRecordingPulse = false
                        }
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                )
                            ),
                            CircleShape
                        ).testTag("mic_input_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Mic,
                        contentDescription = "آواز سے پوچھیں (Voice Menu)",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                // Standard bilingual farm-centric input text-field
                TextField(
                    value = queryText,
                    onValueChange = onQueryChange,
                    placeholder = {
                        Text(
                            text = "بولیے یا اردو میں لکھیے...",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(max = 120.dp)
                        .testTag("text_input_field"),
                    shape = RoundedCornerShape(24.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    ),
                    maxLines = 4,
                    trailingIcon = {
                        if (queryText.isNotBlank()) {
                            IconButton(onClick = onSendClick, modifier = Modifier.testTag("send_query_button")) {
                                Icon(
                                    imageVector = Icons.Filled.Send,
                                    contentDescription = "ارسال کریں",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            IconButton(
                                onClick = onVoiceTextInputClick,
                                modifier = Modifier.testTag("voice_text_input_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Mic,
                                    contentDescription = "آواز سے لکھیں (Voice-to-Text Input)",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

// Session drawer modal to list past assistance session streams
@Composable
fun SessionDrawerOverlay(
    sessions: List<ChatSession>,
    activeSessionId: String?,
    onSessionSelect: (String) -> Unit,
    onSessionDelete: (String) -> Unit,
    onNewSessionClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.4f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.75f)
                .background(MaterialTheme.colorScheme.surface)
                .clickable(enabled = false) {}
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "گفتگو کا تاریخچہ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "بند کریں")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onNewSessionClick,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth().testTag("new_session_button"),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "نئی گفتگو شروع کریں")
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sessions, key = { it.id }) { s ->
                    val isActive = s.id == activeSessionId
                    val itemBgColor = if (isActive) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSessionSelect(s.id) },
                        colors = CardDefaults.cardColors(containerColor = itemBgColor),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1.0f)) {
                                Text(
                                    text = s.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = "زبان: ${s.language}",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (sessions.size > 1) {
                                IconButton(
                                    onClick = { onSessionDelete(s.id) },
                                    modifier = Modifier.size(24.dp).testTag("delete_session_${s.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Delete,
                                        contentDescription = "حذف کریں",
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Helper Utilities
private fun startVoiceActivity(
    context: Context,
    language: LanguageOption,
    launcher: androidx.activity.result.ActivityResultLauncher<Intent>
) {
    try {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            // Pin to ur-PK on both extras so the recognizer cannot drift to hi-IN.
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.sttPrimary)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.sttPrimary)
            putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
            putExtra(RecognizerIntent.EXTRA_PROMPT, "اب بولیے... (${language.displayName})")
        }
        launcher.launch(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "گوگل وائس سروس دستیاب نہیں ہے۔ معلومات ٹائپ کریں۔", Toast.LENGTH_LONG).show()
    }
}

private fun speakOutLoud(
    textToSpeech: TextToSpeech?,
    text: String,
    languageCode: String,
    onDone: () -> Unit
) {
    textToSpeech?.let { tts ->
        tts.language = Locale(languageCode)
        // Strip markdown and styling characters to enhance TTS synthesis
        val stripped = text
            .replace("*", "")
            .replace("#", "")
            .replace("-", "")
            .replace("`", "")
            .replace("🚜", "")
            .replace("🌾", "")
            .replace("🐛", "")
            .replace("☀️", "")
            .replace("🌧️", "")
            .trim()
        
        tts.speak(stripped, TextToSpeech.QUEUE_FLUSH, null, "KisaanDostTTS")
        
        // Listen to when speaking ends to reset mic pulse indicators
        tts.setOnUtteranceProgressListener(object : android.speech.tts.UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}
            override fun onDone(utteranceId: String?) { onDone() }
            override fun onError(utteranceId: String?) { onDone() }
        })
    } ?: onDone()
}

private fun determineCategory(text: String): String {
    val urduText = text.lowercase()
    return when {
        urduText.contains("کھاد") || urduText.contains("مٹی") || urduText.contains("زمین") || urduText.contains("fertilizer") || urduText.contains("soil") -> "Crops"
        urduText.contains("کیڑا") || urduText.contains("بیماری") || urduText.contains("پیسٹ") || urduText.contains("سپرے") || urduText.contains("insect") || urduText.contains("pest") -> "Pest"
        urduText.contains("بارش") || urduText.contains("پانی") || urduText.contains("موسم") || urduText.contains("weather") || urduText.contains("rain") || urduText.contains("canal") -> "Weather"
        urduText.contains("بھینس") || urduText.contains("گائے") || urduText.contains("بکری") || urduText.contains("دودھ") || urduText.contains("livestock") || urduText.contains("cow") -> "Livestock"
        else -> "Crops"
    }
}

private fun translateCategoryUrdu(category: String): String {
    return when (category) {
        "Crops" -> "فصلیں اور کھاد"
        "Pest" -> "کیڑے مار علاج"
        "Weather" -> "موسم اور پانی"
        "Livestock" -> "مال مویشی صحت"
        else -> "عام معلومات"
    }
}

@Composable
fun CustomVoiceListeningDialog(
    language: LanguageOption,
    onSpeechResult: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var statusText by remember { mutableStateOf("تیار ہے، بولنا شروع کریں...") }
    var subStatusText by remember { mutableStateOf("Listening for ${language.displayName}...") }
    var parsedText by remember { mutableStateOf("") }
    var rmsValue by remember { mutableStateOf(0f) }

    DisposableEffect(Unit) {
        var speechRecognizer: SpeechRecognizer? = null
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: android.os.Bundle?) {
                            statusText = "بولیں، کسان دوست سن رہا ہے..."
                            subStatusText = "Speak now, Kisaan Dost is listening..."
                        }

                        override fun onBeginningOfSpeech() {
                            statusText = "آواز ریکارڈ ہو رہی ہے..."
                            subStatusText = "Recording your voice..."
                        }

                        override fun onRmsChanged(rmsdB: Float) {
                            rmsValue = rmsdB
                        }

                        override fun onBufferReceived(buffer: ByteArray?) {}

                        override fun onEndOfSpeech() {
                            statusText = "تبدیل کیا جا رہا ہے..."
                            subStatusText = "Processing speech with Gemini engine..."
                        }

                        override fun onError(error: Int) {
                            val errorMsgUrdu = when (error) {
                                SpeechRecognizer.ERROR_AUDIO -> "آڈیو ریکارڈنگ کی غلطی (Audio Error)"
                                SpeechRecognizer.ERROR_CLIENT -> "کنکشن کا مسئلہ (Client Error)"
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "مائیک کی اجازت درکار ہے"
                                SpeechRecognizer.ERROR_NETWORK -> "انٹرنیٹ کا مسئلہ (Network connection)"
                                SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "نیٹ ورک ٹائم آؤٹ (Network Timeout)"
                                SpeechRecognizer.ERROR_NO_MATCH -> "آواز سمجھ نہیں آسکی (No speech matched)"
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "سسٹم مصروف ہے (Recognizer busy)"
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "آواز موصول نہیں ہوئی (Speech Timeout)"
                                else -> "کوشش ناکام رہی (Speech failed)"
                            }
                            statusText = "معذرت: $errorMsgUrdu"
                            subStatusText = "Please try again or type your question."
                        }

                        override fun onResults(results: android.os.Bundle?) {
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                parsedText = text
                                onSpeechResult(text)
                            } else {
                                statusText = "کوئی لفظ سمجھ میں نہیں آیا۔"
                            }
                        }

                        override fun onPartialResults(partialResults: android.os.Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull() ?: ""
                            if (text.isNotBlank()) {
                                parsedText = text
                            }
                        }

                        override fun onEvent(eventType: Int, params: android.os.Bundle?) {}
                    })

                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                        // Use sttPrimary (ur-PK for all PK languages) — keeps streaming partials
                        // in Urdu/Shahmukhi script instead of hi-IN Devanagari/Hindi fallback.
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.sttPrimary)
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.sttPrimary)
                        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                        putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
                    }
                    startListening(intent)
                }
            } else {
                statusText = "آواز کی سروس دستیاب نہیں ہے۔"
                subStatusText = "Speech recognizer is unavailable on this device."
            }
        } catch (e: Exception) {
            statusText = "رابطے کی غلطی: ${e.message}"
        }

        onDispose {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
                speechRecognizer?.destroy()
            } catch (e: Exception) {
                // Ignore destruction issues
            }
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .border(BorderStroke(2.dp, Color(0xFF10B981)), RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F1210))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = Color(0xFF0F1210),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "کسان دوست آواز • PakKissanAI",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E8D1),
                        fontSize = 15.sp,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(160.dp)
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val animatedRms = remember { Animatable(1.0f) }
                    LaunchedEffect(rmsValue) {
                        val targetScale = (1.0f + (rmsValue.coerceIn(-2f, 12f) + 2f) / 7f).coerceIn(1.0f, 2.5f)
                        animatedRms.animateTo(targetScale, animationSpec = tween(120, easing = LinearOutSlowInEasing))
                    }

                    Box(
                        modifier = Modifier
                            .size(120.dp)
                            .scale(animatedRms.value)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.12f))
                    )
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(animatedRms.value * 0.8f)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.18f))
                    )

                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .shadow(16.dp, CircleShape, spotColor = Color(0xFF10B981))
                            .background(Color(0xFF10B981), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Mic,
                            contentDescription = "Listening",
                            tint = Color(0xFF090B09),
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = statusText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE1E3E1),
                    textAlign = TextAlign.Center,
                    lineHeight = 24.sp
                )

                Text(
                    text = subStatusText,
                    fontSize = 11.sp,
                    color = Color(0xFFD1E8D1).copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp),
                    fontWeight = FontWeight.Light
                )

                if (parsedText.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp)
                            .background(Color(0xFF1F2420), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFF3E4A40)), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = parsedText,
                            fontSize = 14.sp,
                            color = Color(0xFFD1E8D1),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth(),
                            style = LocalTextStyle.current.copy(textDirection = TextDirection.ContentOrRtl)
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val waveHeight1 = remember { Animatable(14f) }
                    val waveHeight2 = remember { Animatable(24f) }
                    val waveHeight3 = remember { Animatable(18f) }
                    
                    LaunchedEffect(rmsValue) {
                        val baseVal = rmsValue.coerceIn(0f, 10f)
                        waveHeight1.animateTo(8f + baseVal * 1.5f, tween(100))
                        waveHeight2.animateTo(12f + baseVal * 2.5f, tween(100))
                        waveHeight3.animateTo(10f + baseVal * 1.8f, tween(100))
                    }
                    Box(modifier = Modifier.size(width = 4.dp, height = waveHeight1.value.dp).background(Color(0xFF10B981), RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(width = 4.dp, height = waveHeight2.value.dp).background(Color(0xFF10B981), RoundedCornerShape(3.dp)))
                    Box(modifier = Modifier.size(width = 4.dp, height = waveHeight3.value.dp).background(Color(0xFF10B981), RoundedCornerShape(3.dp)))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Divider(color = Color(0xFF2C3E2F), thickness = 1.dp)
                Spacer(modifier = Modifier.height(12.dp))

                // Beautiful Regional Language Question Shortcuts
                Text(
                    text = "مقبول کسان سوالات • Try Local Voice Shortcut:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    modifier = Modifier.align(Alignment.Start).padding(bottom = 6.dp)
                )

                val quickQueries = remember(language) {
                    when (language) {
                        LanguageOption.URDU -> listOf(
                            "گندم کی پیداوار بڑھانے کا طریقہ کیا ہے؟",
                            "ڈی اے پی اور یوریا کھاد کا متوازن استعمال کیسے کریں؟",
                            "کپاس کے پتوں پر سفید مکھی کے حملے کا دیسی علاج کیا ہے؟"
                        )
                        LanguageOption.PUNJABI -> listOf(
                            "پانی لانے دا صحیح وقت کڑا اے؟",
                            "کنک دی بیجائی لئی کیڑا وقت چنگا اے؟",
                            "کھاد پاون دا سہی طریقہ کی اے؟"
                        )
                        LanguageOption.SINDHI -> listOf(
                            "ڪڻڪ جي پوکيءَ لاءِ بھترين وقت ڪھڙو آھي؟",
                            "پکيءَ ۽ کیڙن کان بچاءَ جو علاج ڪھڙو آھي؟",
                            "پاڻي ڏيڻ جا نازڪ مرحلا ٻڌايو؟"
                        )
                        LanguageOption.PASHTO -> listOf(
                            "د غنمو د فصل دپاره کوه ښه ده؟",
                            "د ډي اے پي سرې صحیح استعمال څنګه وکړو؟",
                            "کپاس دپاره د سپین مچ دیسی علاج نشته؟"
                        )
                        LanguageOption.SERAIKI -> listOf(
                            "کپاہ کوں کیڑے توں بچاونڑ دا طریقہ ڈساؤ۔",
                            "گندم کوں پہلا پانی کڈھنڑ لاونڑاں چائیدا اے؟",
                            "ڈی اے پی کھاد پاونڑ دا صحیح وقت ڈساؤ۔"
                        )
                        LanguageOption.BALOCHI -> listOf(
                            "مئے زمین ءَ پہ آپ جنگ ءَ چے کنگی انت؟",
                            "گندم ءِ پیدوار گیش کنگ ءِ کار پئیما بیت؟",
                            "کپاس سفید ماھیگ قدرتی گاجان چونی انت؟"
                        )
                        LanguageOption.ENGLISH -> listOf(
                            "How to deal with cotton whiteflies pest attack?",
                            "What are the critical growth stages for wheat irrigation?",
                            "How to prepare chemical-free organic pest sprays?"
                        )
                    }
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    quickQueries.forEach { query ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onSpeechResult(query)
                                },
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B17)),
                            border = BorderStroke(1.dp, Color(0xFF2C3E2F))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.RecordVoiceOver,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = query,
                                        fontSize = 12.sp,
                                        color = Color(0xFFE1E3E1),
                                        style = LocalTextStyle.current.copy(textDirection = TextDirection.ContentOrRtl)
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = "پوچھیں Ask Kisaan AI",
                                    tint = Color(0xFF10B981).copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2B1F20),
                        contentColor = Color(0xFFFC8181)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "روکیں (Cancel & Close)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
fun FarmersLiveSessionDialog(
    onDismiss: () -> Unit,
    viewModel: FarmersViewModel,
    selectedLanguage: LanguageOption,
    liveConnectionState: LiveConnectionState,
    liveReceivedText: String
) {
    val context = LocalContext.current
    var inputMessageText by remember { mutableStateOf("") }

    // Start live connection and streams collection once dialog is opened
    LaunchedEffect(Unit) {
        viewModel.startLiveSession()
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.stopLiveSession()
        }
    }

    val sttLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = spokenResults?.firstOrNull() ?: ""
            if (spokenText.isNotBlank()) {
                viewModel.sendLiveTextMessage(spokenText)
                Toast.makeText(context, "آواز موصول ہوئی: $spokenText", Toast.LENGTH_SHORT).show()
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        confirmButton = {},
        containerColor = Color(0xFF0F1310),
        tonalElevation = 6.dp,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = UrduDictionary.VOICE_LIVE_MEETING,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E8D1)
                    )
                    
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close session dialog",
                            tint = Color(0xFFFC8181)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Connection indicator status
                val statusText: String
                val statusColor: Color
                val isPulsing: Boolean

                when (liveConnectionState) {
                    is LiveConnectionState.Disconnected -> {
                        statusText = "رابطہ بند ہے (Disconnected)"
                        statusColor = Color.Gray
                        isPulsing = false
                    }
                    is LiveConnectionState.Connecting -> {
                        statusText = "رابطہ قائم ہو رہا ہے... (Connecting)"
                        statusColor = Color(0xFFFBBF24)
                        isPulsing = true
                    }
                    is LiveConnectionState.Connected -> {
                        statusText = "رابطہ قائم ہو گیا! (Connected)"
                        statusColor = Color(0xFF3B82F6)
                        isPulsing = true
                    }
                    is LiveConnectionState.SetupComplete -> {
                        statusText = "لائیو مترجم فعال ہے (Live Translation Active)"
                        statusColor = Color(0xFF10B981)
                        isPulsing = true
                    }
                    is LiveConnectionState.Error -> {
                        statusText = "خرابی: ${liveConnectionState.message}"
                        statusColor = Color(0xFFEF4444)
                        isPulsing = false
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .background(statusColor.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    if (isPulsing) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse_live_state")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.7f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(900),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "state_badge"
                        )
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .scale(scale)
                                .background(statusColor, CircleShape)
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(statusColor, CircleShape)
                        )
                    }
                    Text(
                        text = statusText,
                        fontSize = 11.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Avatar Orb Visual with audio pulsing
                Box(
                    modifier = Modifier
                        .size(110.dp)
                        .background(Color(0xFF1E2820), CircleShape)
                        .border(
                            BorderStroke(
                                2.dp,
                                Brush.linearGradient(
                                    listOf(Color(0xFF10B981), Color(0xFF059669))
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPulsing) {
                        val infiniteTransition = rememberInfiniteTransition(label = "avatar_pulse_live")
                        val sizeMultiplier by infiniteTransition.animateFloat(
                            initialValue = 0.9f,
                            targetValue = 1.25f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1200, easing = LinearOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulse_avatar_ring"
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(sizeMultiplier)
                                .background(Color(0xFF10B981).copy(alpha = 0.12f), CircleShape)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Hearing,
                        contentDescription = "Realtime Sound Listening Assistant",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Real-time voice stream / response display board
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(Color(0xFF161C17), RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, Color(0xFF2C3B2E)), RoundedCornerShape(16.dp))
                        .padding(14.dp)
                ) {
                    if (liveReceivedText.isBlank()) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "کِسان دوست لائیو مترجم کی آواز کا انتظار ہے...",
                                color = Color.White.copy(alpha = 0.4f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "نیچے بٹن دبا کر پوچھیے یا ٹائپ کیجیے، جواب فورا بولا جائے گا۔",
                                color = Color.White.copy(alpha = 0.25f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            item {
                                Text(
                                    text = liveReceivedText,
                                    color = Color(0xFFE1E3E1),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Typing row panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = inputMessageText,
                        onValueChange = { inputMessageText = it },
                        placeholder = {
                            Text(
                                "اپنی مقامی زبان میں لکھیے...",
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.35f)
                            )
                        },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFF161C17),
                            unfocusedContainerColor = Color(0xFF161C17),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedIndicatorColor = Color(0xFF10B981),
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )

                    // Keyboard clientContent send button
                    IconButton(
                        onClick = {
                            if (inputMessageText.isNotBlank()) {
                                viewModel.sendLiveTextMessage(inputMessageText)
                                inputMessageText = ""
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF1F2A21), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFF374E3B)), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send text directly to stream",
                            tint = Color(0xFF10B981)
                        )
                    }

                    // Floating microphone action item
                    IconButton(
                        onClick = {
                            try {
                                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, selectedLanguage.sttPrimary)
                                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, selectedLanguage.sttPrimary)
                                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, false)
                                }
                                sttLauncher.launch(intent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "کھیلیں: گوگل اسپیچ سروس بند ہے۔", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .background(Color(0xFF10B981), RoundedCornerShape(12.dp))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Speak live to translation session",
                            tint = Color.Black
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun NavTabItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    labelUrdu: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(if (isActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color.Transparent)
                .padding(horizontal = 14.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = labelUrdu,
                tint = if (isActive) Color(0xFF10B981) else Color(0xFFE1E3E1).copy(alpha = 0.5f),
                modifier = Modifier.size(22.dp)
            )
        }
        Text(
            text = labelUrdu,
            fontSize = 11.sp,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            color = if (isActive) Color(0xFFD1E8D1) else Color(0xFFE1E3E1).copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 2.dp)
        )
    }
}

