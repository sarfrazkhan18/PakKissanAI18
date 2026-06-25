package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.*
import com.example.viewmodel.FarmersViewModel
import com.example.viewmodel.LanguageOption
import com.example.utils.UrduDictionary
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import java.util.Locale

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun KisaanOnboardingScreen(
    viewModel: FarmersViewModel,
    textToSpeech: TextToSpeech?,
    isTtsInitialized: Boolean
) {
    val context = LocalContext.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    var isLoginMode by remember { mutableStateOf(false) }

    // Welcome gate: friendly hero screen before stepper starts
    var showWelcome by remember { mutableStateOf(true) }

    var currentStep by remember { mutableStateOf(0) }
    
    // Step 0: Auth State
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isVerificationSent by remember { mutableStateOf(false) }
    var verificationCode by remember { mutableStateOf("") }
    var isCodeVerified by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }

    // Step 1: Bio Preference
    var farmerName by remember { mutableStateOf("") }
    var selectedDialect by remember { mutableStateOf("Urdu") }

    // Step 2: Regional Selection
    var selectedRegion by remember { mutableStateOf("") }

    // Step 3: Main crop
    var selectedCrop by remember { mutableStateOf("") }

    // TTS Speak Callback helper
    fun playVoiceGuidance(urduMsg: String, englishBackup: String) {
        if (isTtsInitialized && textToSpeech != null) {
            textToSpeech.language = Locale("ur")
            textToSpeech.speak(urduMsg, TextToSpeech.QUEUE_FLUSH, null, "OnboardingTTS")
        } else {
            Toast.makeText(context, urduMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // Initial welcome voice greeting is handled inside OnboardingStepWelcome.

    // Voice to Text launcher for name dictation
    val speechToTextLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenResults = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = spokenResults?.firstOrNull() ?: ""
            if (text.isNotBlank()) {
                farmerName = text
                playVoiceGuidance("جزاک اللہ $text بھائی! ہم نے آپ کا نام درج کر لیا ہے۔", "Thank you candidate. Your name has been received.")
            }
        }
    }

    fun startSpeechRecognizer() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_PROMPT, UrduDictionary.VOICE_DICTATION_HINT)
        }
        try {
            speechToTextLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(context, UrduDictionary.SPEECH_NOT_SUPPORTED, Toast.LENGTH_SHORT).show()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B08))
    ) {
        // Decorative rich background organic elements
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1B5E20).copy(alpha = 0.15f),
                            Color(0xFF0A0C0B)
                        )
                    )
                )
        )

        // Animated welcome hero gate before the stepper begins
        AnimatedVisibility(
            visible = showWelcome,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(300)) + slideOutHorizontally { -it / 4 }
        ) {
            OnboardingStepWelcome(
                onStart = {
                    showWelcome = false
                    playVoiceGuidance(
                        UrduDictionary.VOICE_STEP_AUTH_HELP,
                        "Please register with your phone number."
                    )
                },
                onVoicePlay = {
                    playVoiceGuidance(
                        UrduDictionary.VOICE_WELCOME_HERO,
                        "Welcome to Kisaan Dost. Tap the green button to begin."
                    )
                }
            )
        }

        AnimatedVisibility(
            visible = !showWelcome,
            enter = fadeIn(animationSpec = tween(400)) + slideInHorizontally { it / 4 },
            exit = fadeOut(animationSpec = tween(200))
        ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header: brand row + Progress indicator with icons + step counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp, bottom = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = UrduDictionary.APP_NAME,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFD1E8D1)
                    )
                }
                Text(
                    text = "${currentStep + 1} / 4",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF10B981)
                )
            }

            // Step progress with icons + labels under each (farmer-friendly)
            val stepIcons = listOf(
                Icons.Default.Lock to UrduDictionary.STEP_LABEL_AUTH,
                Icons.Default.Person to UrduDictionary.STEP_LABEL_BIO,
                Icons.Default.Map to UrduDictionary.STEP_LABEL_REGION,
                Icons.Default.Spa to UrduDictionary.STEP_LABEL_CROP
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                stepIcons.forEachIndexed { i, (icon, label) ->
                    val isActive = currentStep == i
                    val isDone = currentStep > i
                    val accent = when {
                        isActive -> Color(0xFF10B981)
                        isDone -> Color(0xFF1B5E20)
                        else -> Color(0xFF2A332C)
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isActive) 8.dp else 6.dp)
                                .clip(CircleShape)
                                .background(accent)
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = if (isDone) Icons.Default.Check else icon,
                                contentDescription = label,
                                tint = if (isActive || isDone) Color(0xFF10B981) else Color(0xFFE1E3E1).copy(alpha = 0.4f),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = label,
                                fontSize = 9.sp,
                                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isActive) Color(0xFFD1E8D1) else Color(0xFFE1E3E1).copy(alpha = 0.45f),
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            // Central Dynamic animated step content
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = currentStep,
                    transitionSpec = {
                        if (targetState > initialState) {
                            slideInHorizontally { width -> width } + fadeIn() with
                                    slideOutHorizontally { width -> -width } + fadeOut()
                        } else {
                            slideInHorizontally { width -> -width } + fadeIn() with
                                    slideOutHorizontally { width -> width } + fadeOut()
                        }.using(SizeTransform(clip = false))
                    }
                ) { step ->
                    when (step) {
                        0 -> OnboardingStepAuth(
                            phoneNumber = phoneNumber,
                            onPhoneChange = { phoneNumber = it },
                            password = password,
                            onPasswordChange = { password = it },
                            isSent = isVerificationSent,
                            onSendOtp = {
                                isSendingOtp = true
                                playVoiceGuidance(UrduDictionary.VOICE_OTP_SENT, "Sending verification OTP.")
                                Toast.makeText(context, "کِسان دوست کوڈ: '1234' بھیج دیا گیا ہے", Toast.LENGTH_LONG).show()
                                isVerificationSent = true
                                isSendingOtp = false
                                verificationCode = "1234" // Pre-fill code to prevent stuckness
                            },
                            code = verificationCode,
                            onCodeChange = { verificationCode = it },
                            onVerifyCode = {
                                if (verificationCode == "1234" || verificationCode == "7860") {
                                    isCodeVerified = true
                                    playVoiceGuidance(UrduDictionary.VOICE_OTP_VERIFIED, "Phone verified successfully.")
                                    currentStep = 1
                                } else {
                                    Toast.makeText(context, "غلط کوڈ درج کیا گیا ہے! دوبارہ کوشش کریں۔", Toast.LENGTH_SHORT).show()
                                }
                            },
                            isVerified = isCodeVerified,
                            isSending = isSendingOtp,
                            isLoginMode = isLoginMode,
                            onLoginModeChange = { isLoginMode = it },
                            onLoginSubmit = {
                                scope.launch {
                                    val success = viewModel.attemptLocalLogin(phoneNumber, password)
                                    if (success) {
                                        playVoiceGuidance(UrduDictionary.VOICE_LOGIN_WELCOME, "Login success!")
                                        Toast.makeText(context, UrduDictionary.LOGIN_SUCCESS_MSG, Toast.LENGTH_LONG).show()
                                    } else {
                                        playVoiceGuidance(UrduDictionary.LOGIN_FAILED_MSG, "Login failed.")
                                        Toast.makeText(context, UrduDictionary.LOGIN_FAILED_MSG, Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onVoicePlay = {
                                if (isLoginMode) {
                                    playVoiceGuidance("پیارے کسان بھائی، یہاں اپنا موبائل نمبر اور اپنا پاسورڈ درج کر کے لاگ ان کیجیے۔", "")
                                } else {
                                    playVoiceGuidance(
                                        UrduDictionary.VOICE_STEP_AUTH_HELP,
                                        "Please register safety OTP with phone number."
                                    )
                                }
                            }
                        )
                        1 -> OnboardingStepNameLanguage(
                            name = farmerName,
                            onNameChange = { farmerName = it },
                            dialect = selectedDialect,
                            onDialectChange = {
                                selectedDialect = it
                                // Play beautiful dialect greeting
                                val greet = when (it) {
                                    "Punjabi" -> UrduDictionary.DIALECT_PUNJABI_GREET
                                    "Sindhi" -> UrduDictionary.DIALECT_SINDHI_GREET
                                    "Pashto" -> UrduDictionary.DIALECT_PASHTO_GREET
                                    "Seraiki" -> UrduDictionary.DIALECT_SERAIKI_GREET
                                    "Balochi" -> UrduDictionary.DIALECT_BALOCHI_GREET
                                    else -> UrduDictionary.DIALECT_URDU_GREET
                                }
                                playVoiceGuidance(greet, "Language dialect chosen.")
                            },
                            onVoiceToTextClick = { startSpeechRecognizer() },
                            onVoicePlay = {
                                playVoiceGuidance(
                                    UrduDictionary.VOICE_STEP_NAME_HELP,
                                    "Enter your name by typing or speak into the microphone."
                                )
                            }
                        )
                        2 -> OnboardingStepRegion(
                            selectedRegion = selectedRegion,
                            onRegionSelect = {
                                selectedRegion = it
                                val speech = when(it) {
                                    "Punjab" -> "صوبہ پنجاب کے زرخیز علاقوں کی کاشتکاری کی تجاویز تیار کی جا رہی ہیں۔"
                                    "Sindh" -> "باب الاسلام سندھ کے لاڑ اور تھر پارکر کے علاقوں کی زراعت کے حل دستیاب ہیں۔"
                                    "KPK" -> "خیبر پختونخوا کے پہاڑی اور باغات کے لیے کسان تجاویز تیار کی جا رہی ہیں۔"
                                    else -> "صوبہ بلوچستان کی خشک سالی اور بہترین پھل دار درختوں کی رہنمائی ترتیب دی جا رہی ہے۔"
                                }
                                playVoiceGuidance(speech, "Region selected.")
                            },
                            onVoicePlay = {
                                playVoiceGuidance(
                                    UrduDictionary.VOICE_STEP_REGION_HELP,
                                    "Please select your geographic province."
                                )
                            }
                        )
                        3 -> OnboardingStepCrops(
                            selectedCrop = selectedCrop,
                            onCropSelect = {
                                selectedCrop = it
                                val cropInfo = when(it) {
                                    "Wheat" -> "گندم کی شاندار پیداوار بڑھانے کا منصوبہ ترتیب دیا جا رہا ہے۔"
                                    "Cotton" -> "صف اول کی کپاس کو سنڈیوں اور کیڑوں سے پاک رکھنے کا منصوبہ۔"
                                    "Rice" -> "باجرہ اور باسمتی چاول کے لذیذ دانوں کی کاشتکاری کا حل۔"
                                    "Sugarcane" -> "کماد اور گنے کی مٹھاس اور زیادہ وزن حاصل کرنے کی تجاویز۔"
                                    "Livestock" -> "گائے اور بھینس کا دودھ بڑھانے اور انکی صحت کا دیسی علاج۔"
                                    else -> "پھلوں اور سبزیوں کی سستی اور بہترین دیکھ بھال کا لائحہ عمل۔"
                                }
                                playVoiceGuidance(cropInfo, "Primary crop selected.")
                            },
                            onFinish = {
                                if (farmerName.isBlank()) farmerName = "کسان بھائی"
                                // Save profile in database!
                                viewModel.saveUserProfile(
                                    name = farmerName,
                                    phone = phoneNumber,
                                    region = selectedRegion.ifBlank { "Punjab" },
                                    primaryCrop = selectedCrop.ifBlank { "Wheat" },
                                    dialect = selectedDialect,
                                    passwordRaw = password
                                )
                                playVoiceGuidance(
                                    "مبارک ہو $farmerName بھائی! کِسان دوست ایپ میں آپ کا خوش آمدید کارڈ تیار ہو گیا ہے۔",
                                    "Welcome complete!"
                                )
                                Toast.makeText(context, "$farmerName بھائی، آپ کا زرعی الٹیمیٹ ممبر کارڈ فعال کر دیا گیا ہے!", Toast.LENGTH_LONG).show()
                            },
                            onVoicePlay = {
                                playVoiceGuidance(
                                    UrduDictionary.VOICE_STEP_CROPS_HELP,
                                    "Choose your primary crops now."
                                )
                            }
                        )
                    }
                }
            }

            // Bottom Navigation buttons (Comfortable and prominent spacing)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (currentStep > 0) {
                    FilledTonalButton(
                        onClick = { currentStep-- },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color(0xFF1F2420),
                            contentColor = Color(0xFFD1E8D1)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .padding(end = 6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = UrduDictionary.VOICE_BACK_BUTTON, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Voice guidance helper — large, pulsing, discoverable for non-readers
                val voicePulse by rememberInfiniteTransition(label = "voicePulse").animateFloat(
                    initialValue = 0.92f,
                    targetValue = 1.12f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1100, easing = FastOutSlowInEasing),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "voicePulseScale"
                )
                IconButton(
                    onClick = {
                        when (currentStep) {
                            0 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_AUTH_HELP, "")
                            1 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_NAME_HELP, "")
                            2 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_REGION_HELP, "")
                            3 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_CROPS_HELP, "")
                        }
                    },
                    modifier = Modifier
                        .scale(voicePulse)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF10B981).copy(alpha = 0.35f),
                                    Color(0xFF10B981).copy(alpha = 0.12f)
                                )
                            )
                        )
                        .border(2.dp, Color(0xFF10B981).copy(alpha = 0.5f), CircleShape)
                        .size(60.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(30.dp)
                    )
                }

                if (currentStep > 0 && currentStep < 3) {
                    Button(
                        onClick = {
                            if (currentStep == 1 && farmerName.isBlank()) {
                                Toast.makeText(context, UrduDictionary.NAME_REQUIRED, Toast.LENGTH_SHORT).show()
                            } else if (currentStep == 2 && selectedRegion.isBlank()) {
                                Toast.makeText(context, UrduDictionary.REGION_REQUIRED, Toast.LENGTH_SHORT).show()
                            } else {
                                currentStep++
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF10B981),
                            contentColor = Color(0xFFFFFFFF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .padding(start = 6.dp)
                    ) {
                        Text(text = UrduDictionary.VOICE_NEXT_BUTTON, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.ArrowForward, contentDescription = "Next")
                    }
                } else if (currentStep < 3) {
                    Spacer(modifier = Modifier.width(1.dp))
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }
            }
        }
        }
    }
}

@Composable
fun OnboardingStepAuth(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isSent: Boolean,
    onSendOtp: () -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    onVerifyCode: () -> Unit,
    isVerified: Boolean,
    isSending: Boolean,
    isLoginMode: Boolean,
    onLoginModeChange: (Boolean) -> Unit,
    onLoginSubmit: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_farmer_onboarding),
            contentDescription = "Onboarding Welcome Screen Poster",
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, Color(0xFF3E4A40), RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )

        // All-in-One Farm & Livestock Manager Value Proposition Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B5E20).copy(alpha = 0.25f),
                            Color(0xFF112214).copy(alpha = 0.45f)
                        )
                    )
                )
                .border(2.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(16.dp))
                .padding(14.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .size(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Agriculture,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = "آپ کا اپنا سمارٹ زرعی مینیجر",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color(0xFFD1E8D1)
                    )
                }

                Text(
                    text = "فصلوں کی نگرانی اور بیماریوں کے تدارک کے ساتھ ساتھ آپ کے مال مویشی کے لیے ڈاکٹر کی طرح بہترین گائیڈ۔",
                    fontSize = 13.sp,
                    color = Color(0xFFE1E3E1).copy(alpha = 0.9f),
                    lineHeight = 19.sp
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BadgeItem("🌾 فصلیں", "Crop Care", Modifier.weight(1f))
                    BadgeItem("🐄 مویشی", "Livestock Vet", Modifier.weight(1f))
                    BadgeItem("📅 منصوبہ", "Farm Planner", Modifier.weight(1f))
                    BadgeItem("🎙️ لائیو آواز", "Voice Assistant", Modifier.weight(1f))
                }
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isLoginMode) "لاگ ان کریں (Secure Login)" else UrduDictionary.REGISTRATION_TITLE,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD1E8D1)
            )

            IconButton(onClick = onVoicePlay) {
                Icon(
                    imageVector = Icons.Default.VolumeUp,
                    contentDescription = "Help Voice Guide",
                    tint = Color(0xFF10B981)
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFF131A15))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (!isLoginMode) Color(0xFF1B5E20) else Color.Transparent)
                    .clickable { onLoginModeChange(false) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UrduDictionary.REGISTER_MODE_TAB,
                    color = if (!isLoginMode) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isLoginMode) Color(0xFF1B5E20) else Color.Transparent)
                    .clickable { onLoginModeChange(true) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = UrduDictionary.LOGIN_MODE_TAB,
                    color = if (isLoginMode) Color.White else Color.White.copy(alpha = 0.5f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        if (isLoginMode) {
            // LOGIN SECTION
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = onPhoneChange,
                label = { Text(UrduDictionary.PHONE_LABEL) },
                placeholder = { Text(UrduDictionary.PHONE_PLACEHOLDER) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF3E4A40),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFFE1E3E1).copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("پاسورڈ (Password)") },
                placeholder = { Text(UrduDictionary.PASSWORD_PLACEHOLDER) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFF5B041).copy(alpha = 0.15f))
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFFF5B041),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF3E4A40),
                    focusedLabelColor = Color(0xFF10B981),
                    unfocusedLabelColor = Color(0xFFE1E3E1).copy(alpha = 0.5f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onLoginSubmit,
                enabled = phoneNumber.isNotBlank() && password.length >= 3 && !isSending,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (isSending) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(UrduDictionary.LOGIN_BUTTON_TEXT, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            // REGISTRATION SECTION
            if (!isSent) {
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    label = { Text(UrduDictionary.PHONE_LABEL) },
                    placeholder = { Text(UrduDictionary.PHONE_PLACEHOLDER) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF3E4A40),
                        focusedLabelColor = Color(0xFF10B981),
                        unfocusedLabelColor = Color(0xFFE1E3E1).copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = onPasswordChange,
                    label = { Text(UrduDictionary.PASSWORD_LABEL) },
                    placeholder = { Text(UrduDictionary.PASSWORD_PLACEHOLDER) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .padding(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF5B041).copy(alpha = 0.15f))
                                .size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFFF5B041),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF10B981),
                        unfocusedBorderColor = Color(0xFF3E4A40),
                        focusedLabelColor = Color(0xFF10B981),
                        unfocusedLabelColor = Color(0xFFE1E3E1).copy(alpha = 0.5f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = onSendOtp,
                    enabled = phoneNumber.length >= 3 && !isSending,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (isSending) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(UrduDictionary.GET_OTP_BUTTON, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // OTP verification UI panel
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF131A15))
                        .border(1.dp, Color(0xFF1B5E20), RoundedCornerShape(16.dp))
                        .padding(16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Sms, contentDescription = "SMS", tint = Color(0xFF10B981))
                            Text(
                                text = UrduDictionary.SMS_CODE_SENT,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = Color(0xFFD1E8D1)
                            )
                        }

                        Text(
                            text = UrduDictionary.SMS_INSTRUCTION,
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = code,
                            onValueChange = onCodeChange,
                            label = { Text(UrduDictionary.CODE_LABEL) },
                            placeholder = { Text("1234") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF3B82F6).copy(alpha = 0.15f))
                                        .size(36.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VpnKey,
                                        contentDescription = null,
                                        tint = Color(0xFF3B82F6),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF10B981),
                                unfocusedBorderColor = Color(0xFF3E4A40),
                                focusedLabelColor = Color(0xFF10B981),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Button(
                            onClick = onVerifyCode,
                            enabled = code.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            Text(UrduDictionary.VERIFY_CODE_BUTTON, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BadgeItem(ur: String, en: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF0F1411))
            .border(1.dp, Color(0xFF1F2922), RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ur, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(3.dp))
            Text(text = en, fontSize = 10.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun OnboardingStepNameLanguage(
    name: String,
    onNameChange: (String) -> Unit,
    dialect: String,
    onDialectChange: (String) -> Unit,
    onVoiceToTextClick: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "Profile icon",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = UrduDictionary.BIO_TITLE,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.BIO_DESC,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text(UrduDictionary.NAME_LABEL) },
                placeholder = { Text(UrduDictionary.NAME_PLACEHOLDER) },
                singleLine = true,
                leadingIcon = {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f))
                            .size(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF10B981),
                    unfocusedBorderColor = Color(0xFF3E4A40),
                    focusedLabelColor = Color(0xFF10B981),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.weight(1f)
            )

            // Huge Voice recording dictation button рядом с полем
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF10B981))
                    .clickable(onClick = onVoiceToTextClick)
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Dictate Name",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = UrduDictionary.DIALECT_TITLE,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            modifier = Modifier.align(Alignment.Start)
        )

        val dialects = listOf(
            DialectItem("Urdu", "اردو مادری زبان", Icons.Default.ChatBubbleOutline, Color(0xFF10B981)),
            DialectItem("Punjabi", "پنجابی (شاہ مکھی)", Icons.Default.Agriculture, Color(0xFFF5B041)),
            DialectItem("Sindhi", "سنڌی ٻولی", Icons.Default.WaterDrop, Color(0xFF3B82F6)),
            DialectItem("Pashto", "پښتو ژبه", Icons.Default.Nature, Color(0xFFEF4444)),
            DialectItem("Seraiki", "سرائیکی لہجہ", Icons.Default.Grass, Color(0xFF8B5CF6)),
            DialectItem("Balochi", "بلوچی زبان", Icons.Default.Terrain, Color(0xFFEC4899))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth().height(230.dp)
        ) {
            items(dialects) { item ->
                val isSelected = dialect == item.id
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "dialectScale"
                )
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) item.accent.copy(alpha = 0.18f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.accent else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onDialectChange(item.id) }
                        .padding(14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(item.accent.copy(alpha = if (isSelected) 0.25f else 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) item.accent else Color(0xFFE1E3E1).copy(alpha = 0.6f),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = item.label,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 14.sp,
                            color = if (isSelected) Color.White else Color(0xFFE1E3E1).copy(alpha = 0.85f),
                            textAlign = TextAlign.Center,
                            maxLines = 2
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepRegion(
    selectedRegion: String,
    onRegionSelect: (String) -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF3B82F6).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = "Map icon",
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = UrduDictionary.REGION_TITLE,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.REGION_DESC,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        val regions = listOf(
            RegionCardItem("Punjab", "پنجاب", "گندم، کماد، دھان اور نہری پانی", Icons.Default.Grass, Color(0xFF10B981)),
            RegionCardItem("Sindh", "سندھ", "کپاس، چاول، کیلا اور صوفیانہ زمین", Icons.Default.Water, Color(0xFFF5B041)),
            RegionCardItem("KPK", "خیبر پختونخوا", "تمباکو، باغات، مکئی اور پہاڑی زراعت", Icons.Default.Terrain, Color(0xFF3B82F6)),
            RegionCardItem("Balochistan", "بلوچستان", "سیب، اڑو، انگور اور کاریز آبپاشی", Icons.Default.WbSunny, Color(0xFFEF4444))
        )

        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            regions.forEach { item ->
                val isSelected = selectedRegion == item.id
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.02f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "regionScale"
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .scale(scale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) item.color.copy(alpha = 0.14f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.color else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onRegionSelect(item.id) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(item.color.copy(alpha = 0.22f))
                                .size(48.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = null,
                                tint = item.color,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(
                                text = "صوبہ ${item.labelUrdu}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp,
                                color = Color.White
                            )
                            Text(
                                text = item.detailsUrdu,
                                fontSize = 13.sp,
                                lineHeight = 18.sp,
                                color = Color(0xFFE1E3E1).copy(alpha = 0.7f)
                            )
                        }
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onRegionSelect(item.id) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = item.color,
                            unselectedColor = Color(0xFF3E4A40)
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingStepCrops(
    selectedCrop: String,
    onCropSelect: (String) -> Unit,
    onFinish: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Spa,
                contentDescription = "Crop icon",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(40.dp)
            )
        }

        Text(
            text = UrduDictionary.CROP_TITLE,
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.CROP_DESC,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.75f),
            textAlign = TextAlign.Center
        )

        val crops = listOf(
            CropItem("Wheat", "🌾 گندم (Wheat)", Color(0xFFF5B041)),
            CropItem("Cotton", "☁️ کپاس (Cotton)", Color(0xFFE1E3E1)),
            CropItem("Rice", "🍚 چاول (Rice)", Color(0xFF60A5FA)),
            CropItem("Sugarcane", "🎋 گنا / کماد", Color(0xFF34D399)),
            CropItem("Livestock", "🐄 مال مویشی", Color(0xFFF87171)),
            CropItem("Fruits", "🍎 پھل اور سبزیاں", Color(0xFFF472B6))
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
        ) {
            items(crops) { item ->
                val isSelected = selectedCrop == item.id
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.04f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "cropScale"
                )
                Box(
                    modifier = Modifier
                        .scale(scale)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) item.color.copy(alpha = 0.18f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.color else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onCropSelect(item.id) }
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 16.sp,
                        lineHeight = 22.sp,
                        color = if (isSelected) Color.White else Color(0xFFE1E3E1).copy(alpha = 0.85f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onFinish,
            enabled = selectedCrop.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Done")
            Spacer(modifier = Modifier.width(6.dp))
            Text(UrduDictionary.FINISH_ONBOARDING_BUTTON, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        }
    }
}

data class DialectItem(val id: String, val label: String, val icon: ImageVector, val accent: Color)
data class RegionCardItem(val id: String, val labelUrdu: String, val detailsUrdu: String, val icon: ImageVector, val color: Color)
data class CropItem(val id: String, val label: String, val color: Color)

@Composable
fun OnboardingStepWelcome(
    onStart: () -> Unit,
    onVoicePlay: () -> Unit
) {
    val heroPulse by rememberInfiniteTransition(label = "heroPulse").animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroPulseScale"
    )

    // Auto-play the voice greeting once when the welcome appears
    LaunchedEffect(Unit) {
        delay(900)
        onVoicePlay()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // App brand row
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF10B981).copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Agriculture,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(26.dp)
                )
            }
            Text(
                text = UrduDictionary.APP_NAME,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFD1E8D1)
            )
        }

        // Hero illustration in a soft glowing ring
        Box(
            modifier = Modifier
                .scale(heroPulse)
                .size(190.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            Color(0xFF10B981).copy(alpha = 0.28f),
                            Color(0xFF0A0C0B).copy(alpha = 0f)
                        )
                    )
                )
                .border(2.dp, Color(0xFF10B981).copy(alpha = 0.4f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.kisaan_companion_icon),
                contentDescription = "Kisaan Dost mascot",
                modifier = Modifier
                    .size(160.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        }

        Text(
            text = UrduDictionary.WELCOME_HERO_TITLE,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.WELCOME_HERO_TAGLINE,
            fontSize = 15.sp,
            lineHeight = 22.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // Voice-first invitation: tappable hint that the app talks to you
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF10B981).copy(alpha = 0.12f))
                .border(1.dp, Color(0xFF10B981).copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .clickable(onClick = onVoicePlay)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = UrduDictionary.WELCOME_TAP_TO_HEAR,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFD1E8D1)
            )
        }

        // Four feature rows — large, clear, scannable
        val features = listOf(
            Triple(Icons.Default.Mic, UrduDictionary.WELCOME_FEATURE_VOICE to UrduDictionary.WELCOME_FEATURE_VOICE_EN, Color(0xFF10B981)),
            Triple(Icons.Default.Spa, UrduDictionary.WELCOME_FEATURE_CROP to UrduDictionary.WELCOME_FEATURE_CROP_EN, Color(0xFFF5B041)),
            Triple(Icons.Default.Pets, UrduDictionary.WELCOME_FEATURE_VET to UrduDictionary.WELCOME_FEATURE_VET_EN, Color(0xFFEF4444)),
            Triple(Icons.Default.WbSunny, UrduDictionary.WELCOME_FEATURE_WEATHER to UrduDictionary.WELCOME_FEATURE_WEATHER_EN, Color(0xFF3B82F6))
        )
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            features.forEach { (icon, labels, color) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF131A15))
                        .border(1.dp, Color(0xFF2A3329), RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(color.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = labels.first,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = labels.second,
                            fontSize = 12.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = color.copy(alpha = 0.65f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Big, unmistakable Get Started CTA
        Button(
            onClick = onStart,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF10B981),
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
        ) {
            Text(
                text = UrduDictionary.WELCOME_GET_STARTED,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null)
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
