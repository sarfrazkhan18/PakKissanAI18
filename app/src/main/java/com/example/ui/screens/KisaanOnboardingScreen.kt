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
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.font.FontFamily
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

    // Steps 8, 9, 10: Farm challenges & premium value proposition
    var selectedChallenge by remember { mutableStateOf("") }
    var selectedPremiumTier by remember { mutableStateOf("Trial") }

    // TTS Speak Callback helper
    fun playVoiceGuidance(urduMsg: String, englishBackup: String) {
        if (isTtsInitialized && textToSpeech != null) {
            textToSpeech.language = Locale("ur")
            textToSpeech.speak(urduMsg, TextToSpeech.QUEUE_FLUSH, null, "OnboardingTTS")
        } else {
            Toast.makeText(context, urduMsg, Toast.LENGTH_SHORT).show()
        }
    }

    // Trigger initial welcoming audio greeting
    LaunchedEffect(Unit) {
        delay(600)
        playVoiceGuidance(
            UrduDictionary.VOICE_INTRO_GREETING,
            "Welcome to Kisaan Dost application. Please register and complete your farmer profile."
        )
    }

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
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "ur-PK")
            putExtra(RecognizerIntent.EXTRA_SUPPORTED_LANGUAGES, arrayOf("ur-PK", "ur", "en-US"))
            putExtra("android.speech.extra.EXTRA_ADDITIONAL_LANGUAGES", arrayOf("ur-PK", "ur", "en-US"))
            putExtra(RecognizerIntent.EXTRA_PROMPT, UrduDictionary.VOICE_DICTATION_HINT)
        }
        try {
            speechToTextLauncher.launch(intent)
        } catch (e: Exception) {
            try {
                // Try with default language fallback
                val fallbackIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PROMPT, UrduDictionary.VOICE_DICTATION_HINT)
                }
                speechToTextLauncher.launch(fallbackIntent)
            } catch (ex: Exception) {
                Toast.makeText(context, UrduDictionary.SPEECH_NOT_SUPPORTED, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF070B08))
    ) {
        // Real photo crop field as background with dark gradient overlay for readability
        Image(
            painter = painterResource(id = R.drawable.img_crop_field_1782639380166),
            contentDescription = "Background crop field",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 0.22f
        )

        // Overlay to keep text perfectly high-contrast and readable
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color(0xFF070B08).copy(alpha = 0.85f),
                            Color(0xFF070B08)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Progress indicators (Million Dollar style sleek design)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val totalSteps = if (isLoginMode) 3 else 11
                for (i in 0 until totalSteps) {
                    val progressColor = when {
                        currentStep == i -> Color(0xFF10B981) // active emerald
                        currentStep > i -> Color(0xFF1B5E20)  // completed
                        else -> Color(0xFF1F2420)             // pending
                    }
                    val weight = if (currentStep == i) 2f else 1f
                    Box(
                        modifier = Modifier
                            .weight(weight)
                            .height(6.dp)
                            .clip(CircleShape)
                            .background(progressColor)
                    )
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
                    if (isLoginMode) {
                        when (step) {
                            0 -> OnboardingStepWelcome(
                                onSelectRegister = {
                                    isLoginMode = false
                                    currentStep = 1
                                    playVoiceGuidance("نیا رجسٹریشن کرنے کے لیے اپنا موبائل نمبر درج کریں۔", "")
                                },
                                onSelectLogin = {
                                    isLoginMode = true
                                    currentStep = 1
                                    playVoiceGuidance("اپنے اکاؤنٹ میں داخل ہونے کے لیے اپنا موبائل نمبر درج کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance(UrduDictionary.VOICE_INTRO_GREETING, "")
                                }
                            )
                            1 -> OnboardingStepPhone(
                                phoneNumber = phoneNumber,
                                onPhoneChange = { phoneNumber = it },
                                isLogin = true,
                                onNext = {
                                    currentStep = 2
                                    playVoiceGuidance("اب اپنا خفیہ پاسورڈ درج کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("اپنا رجسٹرڈ موبائل نمبر درج کریں۔", "")
                                }
                            )
                            2 -> OnboardingStepPassword(
                                password = password,
                                onPasswordChange = { password = it },
                                isLogin = true,
                                isSubmitting = isSendingOtp,
                                onNext = {
                                    scope.launch {
                                        isSendingOtp = true
                                        val success = viewModel.attemptLocalLogin(phoneNumber, password)
                                        isSendingOtp = false
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
                                    playVoiceGuidance("اپنا خفیہ پاسورڈ درج کیجیے۔", "")
                                }
                            )
                        }
                    } else {
                        when (step) {
                            0 -> OnboardingStepWelcome(
                                onSelectRegister = {
                                    isLoginMode = false
                                    currentStep = 1
                                    playVoiceGuidance("نیا رجسٹریشن کرنے کے لیے اپنا موبائل نمبر درج کریں۔", "")
                                },
                                onSelectLogin = {
                                    isLoginMode = true
                                    currentStep = 1
                                    playVoiceGuidance("اپنے اکاؤنٹ میں داخل ہونے کے لیے اپنا موبائل نمبر درج کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance(UrduDictionary.VOICE_INTRO_GREETING, "")
                                }
                            )
                            1 -> OnboardingStepPhone(
                                phoneNumber = phoneNumber,
                                onPhoneChange = { phoneNumber = it },
                                isLogin = false,
                                onNext = {
                                    currentStep = 2
                                    playVoiceGuidance("اب اپنے اکاؤنٹ کے لیے ایک خفیہ پاسورڈ منتخب کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("اپنا موبائل نمبر درج کریں تاکہ ہم آپ کا اکاؤنٹ بنا سکیں۔", "")
                                }
                            )
                            2 -> OnboardingStepPassword(
                                password = password,
                                onPasswordChange = { password = it },
                                isLogin = false,
                                isSubmitting = false,
                                onNext = {
                                    isSendingOtp = true
                                    playVoiceGuidance(UrduDictionary.VOICE_OTP_SENT, "Sending verification OTP.")
                                    Toast.makeText(context, "کِسان دوست کوڈ: '1234' بھیج دیا گیا ہے", Toast.LENGTH_LONG).show()
                                    isVerificationSent = true
                                    isSendingOtp = false
                                    verificationCode = "1234"
                                    currentStep = 3
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("اپنا خفیہ پاسورڈ منتخب کیجیے۔", "")
                                }
                            )
                            3 -> OnboardingStepOtp(
                                code = verificationCode,
                                onCodeChange = { verificationCode = it },
                                onVerify = {
                                    if (verificationCode == "1234" || verificationCode == "7860") {
                                        isCodeVerified = true
                                        playVoiceGuidance(UrduDictionary.VOICE_OTP_VERIFIED, "Phone verified successfully.")
                                        currentStep = 4
                                    } else {
                                        Toast.makeText(context, "غلط کوڈ درج کیا گیا ہے! دوبارہ کوشش کریں۔", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("موصول شدہ تصدیقی کوڈ درج کریں۔", "")
                                }
                            )
                            4 -> OnboardingStepLanguage(
                                dialect = selectedDialect,
                                onDialectChange = {
                                    selectedDialect = it
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
                                onNext = {
                                    currentStep = 5
                                    playVoiceGuidance("اب اپنا نام درج کریں یا مائیکروفون کا بٹن دبا کر بولیں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("اپنی پسندیدہ مادری زبان منتخب کریں تاکہ ہم آپ کو اسی زبان میں معلومات فراہم کر سکیں۔", "")
                                }
                            )
                            5 -> OnboardingStepName(
                                name = farmerName,
                                onNameChange = { farmerName = it },
                                onVoiceToTextClick = { startSpeechRecognizer() },
                                onNext = {
                                    currentStep = 6
                                    playVoiceGuidance("اپنا صوبہ منتخب کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance(UrduDictionary.VOICE_STEP_NAME_HELP, "")
                                }
                            )
                            6 -> OnboardingStepRegion(
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
                                onNext = {
                                    currentStep = 7
                                    playVoiceGuidance("اپنی بنیادی فصل منتخب کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance(UrduDictionary.VOICE_STEP_REGION_HELP, "")
                                }
                            )
                            7 -> OnboardingStepCrops(
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
                                onNext = {
                                    currentStep = 8
                                    playVoiceGuidance("کسان بھائی، اب اپنی سب سے بڑی زرعی پریشانی یا مسئلہ منتخب کریں۔", "")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance(UrduDictionary.VOICE_STEP_CROPS_HELP, "")
                                }
                            )
                            8 -> OnboardingStepChallenge(
                                selectedChallenge = selectedChallenge,
                                onChallengeSelect = {
                                    selectedChallenge = it
                                    val guidance = when (it) {
                                        "Pests" -> "کیڑے مکوڑوں اور بیماریوں کا سستا دیسی اور سائنسی حل تلاش کرنے میں ہم آپ کی مدد کریں گے۔"
                                        "Water" -> "آبپاشی کا درست وقت اور پانی کی بچت کے بہترین طریقے پیش کریں گے۔"
                                        "Fertilizer" -> "کھاد اور اسپرے کے اخراجات میں تیس فیصد تک کمی لانے کا منصوبہ تیار ہے۔"
                                        "Rates" -> "روزانہ ملک بھر کی غلہ منڈیوں کے تازہ ترین ریٹ اب آپ کی جیب میں۔"
                                        else -> "جدید زرعی تحقیق سے آپ کی فصل کی پیداوار کو چالیس فیصد بڑھانے کا عزم۔"
                                    }
                                    playVoiceGuidance(guidance, "Farm challenge selected.")
                                },
                                onNext = {
                                    currentStep = 9
                                    val intro = when (selectedChallenge) {
                                        "Pests" -> "کسان بھائی، کیڑوں سے نجات پانے اور اسپرے کا خرچہ آدھا کرنے کا حل دیکھیں۔"
                                        "Water" -> "سیٹلائٹ اور موسم کی مدد سے پانی کی بچت اور بہترین پیداوار کا لائحہ عمل۔"
                                        "Fertilizer" -> "کھاد کی درست مقدار استعمال کر کے ہزاروں روپے بچانے کا منصوبہ۔"
                                        "Rates" -> "لائیو منڈی ریٹس کی مدد سے اپنی فصل سب سے مہنگی قیمت پر بیچنے کی گائیڈ۔"
                                        else -> "جیمنائی اسسٹنٹ کے ذریعے چالیس فیصد تک پیداوار بڑھانے کا کسان دوست فارمولا۔"
                                    }
                                    playVoiceGuidance(intro, "Proposal page.")
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("کسان بھائی، اپنی سب سے بڑی زرعی پریشانی یا مسئلہ منتخب کریں تاکہ ہم اس کا حل فراہم کریں۔", "")
                                }
                            )
                            9 -> OnboardingStepProposal(
                                challenge = selectedChallenge,
                                onNext = {
                                    currentStep = 10
                                    playVoiceGuidance("کسان بھائی، مبارک ہو! آپ کا پریمیم ممبر کارڈ تیار ہو گیا ہے۔ اسے فعال کر کے فوائد حاصل کریں۔", "")
                                },
                                onVoicePlay = {
                                    val intro = when (selectedChallenge) {
                                        "Pests" -> "ہم آپ کو اسپرے کے دیسی اور سستے طریقے بتائیں گے تاکہ آپ کا خرچہ بچے اور فصل بھی محفوظ رہے۔"
                                        "Water" -> "ہم موسم اور زمین کی نمی دیکھ کر پانی لگانے کا وقت بتائیں گے۔"
                                        "Fertilizer" -> "ہم آپ کی زمین کے مطابق کھاد کا درست حساب بتائیں گے۔"
                                        "Rates" -> "ہم روزانہ کی لائیو منڈی قیمتیں پیش کریں گے تاکہ آپ کا نقصان نہ ہو۔"
                                        else -> "ہم جیمنائی آرٹیفیشل انٹیلیجنس کے ذریعے پیداوار چالیس فیصد تک بڑھائیں گے۔"
                                    }
                                    playVoiceGuidance(intro, "")
                                }
                            )
                            10 -> OnboardingStepPaywall(
                                farmerName = farmerName,
                                phoneNumber = phoneNumber,
                                selectedTier = selectedPremiumTier,
                                onTierSelect = { selectedPremiumTier = it },
                                onFinish = {
                                    if (farmerName.isBlank()) farmerName = "کسان بھائی"
                                    viewModel.saveUserProfile(
                                        name = farmerName,
                                        phone = phoneNumber,
                                        region = selectedRegion.ifBlank { "Punjab" },
                                        primaryCrop = selectedCrop.ifBlank { "Wheat" },
                                        dialect = selectedDialect,
                                        passwordRaw = password,
                                        challenge = selectedChallenge,
                                        premium = selectedPremiumTier
                                    )
                                    playVoiceGuidance(
                                        "مبارک ہو $farmerName بھائی! کِسان دوست پریمیم کارڈ فعال ہو گیا ہے اور آپ کا خوش آمدید کارڈ تیار ہے۔",
                                        "Welcome complete!"
                                    )
                                    Toast.makeText(context, "$farmerName بھائی، آپ کا زرعی پریمیم ممبر کارڈ کامیابی سے فعال کر دیا گیا ہے!", Toast.LENGTH_LONG).show()
                                },
                                onVoicePlay = {
                                    playVoiceGuidance("اپنے پریمیم کارڈ کے فوائد دیکھیں اور فعال کرنے کا بٹن دبائیں۔", "")
                                }
                            )
                        }
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

                // Voice guidance helper button (Big green rounded speaker button)
                IconButton(
                    onClick = {
                        if (isLoginMode) {
                            when(currentStep) {
                                0 -> playVoiceGuidance(UrduDictionary.VOICE_INTRO_GREETING, "")
                                1 -> playVoiceGuidance("اپنا رجسٹرڈ موبائل نمبر درج کریں۔", "")
                                2 -> playVoiceGuidance("اپنا خفیہ پاسورڈ درج کیجیے۔", "")
                            }
                        } else {
                            when(currentStep) {
                                0 -> playVoiceGuidance(UrduDictionary.VOICE_INTRO_GREETING, "")
                                1 -> playVoiceGuidance("اپنا موبائل نمبر درج کریں تاکہ ہم آپ کا اکاؤنٹ بنا سکیں۔", "")
                                2 -> playVoiceGuidance("اپنا خفیہ پاسورڈ منتخب کیجیے۔", "")
                                3 -> playVoiceGuidance("موصول شدہ تصدیقی کوڈ درج کریں۔", "")
                                4 -> playVoiceGuidance("اپنی پسندیدہ مادری زبان منتخب کریں تاکہ ہم آپ کو اسی زبان میں معلومات فراہم کر سکیں۔", "")
                                5 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_NAME_HELP, "")
                                6 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_REGION_HELP, "")
                                7 -> playVoiceGuidance(UrduDictionary.VOICE_STEP_CROPS_HELP, "")
                            }
                        }
                    },
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(Color(0xFF10B981).copy(alpha = 0.15f))
                        .size(54.dp)
                ) {
                    val scale by rememberInfiniteTransition().animateFloat(
                        initialValue = 0.9f,
                        targetValue = 1.15f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1200, easing = FastOutSlowInEasing),
                            repeatMode = RepeatMode.Reverse
                        )
                    )
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = "Read Aloud",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(26.dp)
                    )
                }

                Spacer(modifier = Modifier.width(1.dp))
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun OnboardingStepWelcome(
    onSelectRegister: () -> Unit,
    onSelectLogin: () -> Unit,
    onVoicePlay: () -> Unit
) {
    var slideIndex by remember { mutableStateOf(0) }
    
    val slides = remember {
        listOf(
            Triple(
                "فصلوں کی سمارٹ نگرانی",
                "Advanced AI Crop Protection & Diagnostics",
                "جدید ترین جیمنائی آرٹیفیشل انٹیلیجنس کے ذریعے اپنی فصل کی بیماریوں کی تصویر لیں اور سیکنڈوں میں سستا اور مستند علاج معلوم کریں۔\n\nTake a photo of any crop disease to get instant expert remedies and chemical/organic protection advice powered by Gemini AI."
            ),
            Triple(
                "مال مویشی کا لائیو ڈاکٹر",
                "Modern Livestock Veterinary Assistance",
                "گائے، بھینس، بکریوں کی صحت اور دودھ کی پیداوار بڑھانے کے طریقے جانیں اور جانوروں کے کسی بھی مسئلے کا فوری دیسی یا سائنسی حل پائیں۔\n\nBoost milk production, track livestock health patterns, and receive instant veterinary diagnostic advice in your own language."
            ),
            Triple(
                "روزانہ تازہ ترین منڈی ریٹس",
                "Real-time Mandi Commodity Pricing",
                "پاکستان بھر کی غلہ منڈیوں کے تازہ ترین روزانہ ریٹس اب آپ کے موبائل پر۔ اپنی محنت کی کمائی کا بہترین اور پورا بھاؤ جانیں اور مڈل مین سے چھٹکارا پائیں۔\n\nAccess up-to-date commodity prices across main Punjab and Sindh markets to sell at optimal rates."
            ),
            Triple(
                "اردو اور علاقائی آواز کا جادو",
                "Urdu & Dialect Voice Companion",
                "لکھنے کی زحمت سے پاک! صرف ایک بٹن دبائیں اور اپنی مادری زبان (اردو، پنجابی، سندھی، سرائیکی) میں بول کر لائیو جواب اور آواز کے ذریعے رہنمائی حاصل کریں۔\n\nNo typing required. Simply speak in your native language for automated voice guides and real-time support."
            )
        )
    }

    val slideIcons = listOf(
        Icons.Default.Spa,
        Icons.Default.Agriculture,
        Icons.Default.TrendingUp,
        Icons.Default.Mic
    )

    val slideColors = listOf(
        Color(0xFF10B981), // Emerald
        Color(0xFFF5B041), // Warm Amber
        Color(0xFF3498DB), // Sky Blue
        Color(0xFFE74C3C)  // Crimson
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Slideshow Card Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF121814))
                .border(
                    BorderStroke(1.5.dp, slideColors[slideIndex].copy(alpha = 0.45f)),
                    RoundedCornerShape(24.dp)
                )
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Interactive dynamic header graphic representing the slide
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Underlay: cropped crop field background image
                    Image(
                        painter = painterResource(id = R.drawable.img_crop_field_1782639380166),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp)),
                        contentScale = ContentScale.Crop,
                        alpha = 0.35f
                    )

                    // Overlay gradient matching current slide accent color
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        slideColors[slideIndex].copy(alpha = 0.6f)
                                    )
                                )
                            )
                    )

                    // Floating Glowing Icon
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(slideColors[slideIndex].copy(alpha = 0.25f))
                            .border(2.dp, slideColors[slideIndex], CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = slideIcons[slideIndex],
                            contentDescription = null,
                            tint = slideColors[slideIndex],
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Features text layout
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    Text(
                        text = slides[slideIndex].first,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = slides[slideIndex].second,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = slideColors[slideIndex],
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = slides[slideIndex].third,
                        fontSize = 12.sp,
                        color = Color(0xFFE1E3E1).copy(alpha = 0.9f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // Horizontal Indicator dots
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(vertical = 4.dp)
        ) {
            for (i in 0 until 4) {
                val isActive = i == slideIndex
                val dotWidth by animateDpAsState(
                    targetValue = if (isActive) 24.dp else 8.dp,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )
                val dotColor = if (isActive) slideColors[slideIndex] else Color(0xFF1F2420)
                Box(
                    modifier = Modifier
                        .height(8.dp)
                        .width(dotWidth)
                        .clip(CircleShape)
                        .background(dotColor)
                        .clickable { slideIndex = i }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Dynamic Action Controls
        AnimatedContent(
            targetState = slideIndex == 3,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) with fadeOut(animationSpec = tween(220))
            }
        ) { isLastSlide ->
            if (isLastSlide) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onSelectRegister,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "نیا اکاؤنٹ بنائیں (Register)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    OutlinedButton(
                        onClick = onSelectLogin,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF10B981)),
                        border = BorderStroke(1.5.dp, Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Login,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "پہلے سے موجود اکاؤنٹ کھولیں (Login)",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = { slideIndex = 3 },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF888E89))
                    ) {
                        Text(
                            text = "چھوڑیں (Skip)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Button(
                        onClick = { slideIndex++ },
                        colors = ButtonDefaults.buttonColors(containerColor = slideColors[slideIndex]),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .widthIn(min = 140.dp)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "آگے بڑھیں (Next)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingStepPhone(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    isLogin: Boolean,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Phone,
            contentDescription = "Phone icon",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = if (isLogin) "موبائل نمبر درج کریں" else "اپنا موبائل نمبر فراہم کریں",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isLogin) "اپنے رجسٹرڈ اکاؤنٹ میں داخل ہونے کے لیے اپنا موبائل نمبر درج کریں۔" else "کسان دوست میں رجسٹریشن کے لیے اپنا درست موبائل نمبر درج کریں۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = phoneNumber,
            onValueChange = onPhoneChange,
            label = { Text(UrduDictionary.PHONE_LABEL) },
            placeholder = { Text(UrduDictionary.PHONE_PLACEHOLDER) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = phoneNumber.length >= 3,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepPassword(
    password: String,
    onPasswordChange: (String) -> Unit,
    isLogin: Boolean,
    isSubmitting: Boolean,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Password icon",
            tint = Color(0xFFF5B041),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = if (isLogin) "اپنا پاسورڈ درج کریں" else "ایک مضبوط پاسورڈ بنائیں",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = if (isLogin) "لاگ ان مکمل کرنے کے لیے اپنے اکاؤنٹ کا خفیہ پاسورڈ درج کریں۔" else "اپنے کسان دوست اکاؤنٹ کی حفاظت کے لیے کم از کم 4 ہندسوں کا خفیہ پاسورڈ درج کریں۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            label = { Text("خفیہ پاسورڈ (Password)") },
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = password.length >= 3 && !isSubmitting,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(if (isLogin) "لاگ ان کریں" else "آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun OnboardingStepOtp(
    code: String,
    onCodeChange: (String) -> Unit,
    onVerify: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Sms,
            contentDescription = "SMS OTP icon",
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "تصدیقی کوڈ درج کریں",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = "ہم نے آپ کے نمبر پر تصدیقی کوڈ بھیجا ہے۔ رجسٹریشن مکمل کرنے کے لیے کوڈ درج کریں۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onVerify,
            enabled = code.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("کوڈ تصدیق کریں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun BadgeItem(ur: String, en: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color(0xFF0F1411))
            .border(1.dp, Color(0xFF1F2922), RoundedCornerShape(10.dp))
            .padding(horizontal = 4.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = ur, fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold, maxLines = 1)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = en, fontSize = 8.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Medium, maxLines = 1)
        }
    }
}

@Composable
fun OnboardingStepLanguage(
    dialect: String,
    onDialectChange: (String) -> Unit,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Translate,
            contentDescription = "Language icon",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "اپنی مادری زبان منتخب کریں",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = "کِسان دوست آپ کی مادری زبان کے لہجے کے مطابق جوابات فراہم کرے گا۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        val dialects = listOf(
            DialectItem("Urdu", "اردو مادری زبان", Icons.Default.ChatBubbleOutline, Color(0xFF10B981)),
            DialectItem("Punjabi", "پنجابی (شاہ مکھی)", Icons.Default.Agriculture, Color(0xFFF5B041)),
            DialectItem("Sindhi", "سنڌی ٻولی", Icons.Default.WaterDrop, Color(0xFF3B82F6)),
            DialectItem("Pashto", "پښتو ژبه", Icons.Default.Nature, Color(0xFFEF4444)),
            DialectItem("Seraiki", "سرائیکی لہجہ", Icons.Default.Grass, Color(0xFF8B5CF6)),
            DialectItem("Balochi", "بلوچی زبان", Icons.Default.Terrain, Color(0xFFEC4899))
        )

        dialects.chunked(2).forEach { rowDialects ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowDialects.forEach { item ->
                    val isSelected = dialect == item.id
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) item.accent.copy(alpha = 0.15f) else Color(0xFF131A15))
                            .border(
                                width = if (isSelected) 2.2.dp else 1.dp,
                                color = if (isSelected) item.accent else Color(0xFF3E4A40),
                                shape = RoundedCornerShape(16.dp)
                            )
                            .clickable { onDialectChange(item.id) }
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) item.accent else Color(0xFFE1E3E1).copy(alpha = 0.5f),
                                modifier = Modifier.size(28.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = item.label,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else Color(0xFFE1E3E1).copy(alpha = 0.7f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = dialect.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepName(
    name: String,
    onNameChange: (String) -> Unit,
    onVoiceToTextClick: () -> Unit,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AccountCircle,
            contentDescription = "User Profile Icon",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = "اپنا نام درج کریں",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = "کِسان دوست میں اپنا نام درج کریں تاکہ ہم آپ کو احترام سے پکار سکیں۔ آپ بول کر بھی لکھ سکتے ہیں!",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
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
                modifier = Modifier.weight(1f)
            )

            // Huge Voice recording dictation button рядом с полем
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFF10B981))
                    .clickable(onClick = onVoiceToTextClick)
                    .size(54.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Mic,
                    contentDescription = "Voice Dictate Name",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Voice simulation fallback options
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "آواز متبادل (ٹیسٹ کرنے کے لیے نام پر کلک کریں):",
                fontSize = 11.sp,
                color = Color(0xFF10B981),
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 2.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("احمد علی", "محمد خان", "سرفراز احمد").forEach { testName ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF161B17), RoundedCornerShape(12.dp))
                            .border(BorderStroke(1.dp, Color(0xFF2E3B30)), RoundedCornerShape(12.dp))
                            .clickable { onNameChange(testName) }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(testName, fontSize = 13.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = name.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepRegion(
    selectedRegion: String,
    onRegionSelect: (String) -> Unit,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Map,
            contentDescription = "Map icon",
            tint = Color(0xFF3B82F6),
            modifier = Modifier.size(64.dp)
        )

        Text(
            text = UrduDictionary.REGION_TITLE,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.REGION_DESC,
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
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
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) item.color.copy(alpha = 0.12f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.color else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onRegionSelect(item.id) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(item.color.copy(alpha = 0.2f))
                                .size(40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(imageVector = item.icon, contentDescription = null, tint = item.color)
                        }

                        Column {
                            Text(
                                text = "صوبہ ${item.labelUrdu}",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                            Text(
                                text = item.detailsUrdu,
                                fontSize = 11.sp,
                                color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
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

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onNext,
            enabled = selectedRegion.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepCrops(
    selectedCrop: String,
    onCropSelect: (String) -> Unit,
    onNext: () -> Unit,
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
        Icon(
            imageVector = Icons.Default.Spa,
            contentDescription = "Crop icon",
            tint = Color(0xFF10B981),
            modifier = Modifier.size(54.dp)
        )

        Text(
            text = UrduDictionary.CROP_TITLE,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = UrduDictionary.CROP_DESC,
            fontSize = 11.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
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
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(140.dp)
        ) {
            items(crops) { item ->
                val isSelected = selectedCrop == item.id
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) item.color.copy(alpha = 0.15f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) item.color else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { onCropSelect(item.id) }
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = item.label,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (isSelected) Color.White else Color(0xFFE1E3E1).copy(alpha = 0.8f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Button(
            onClick = onNext,
            enabled = selectedCrop.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepChallenge(
    selectedChallenge: String,
    onChallengeSelect: (String) -> Unit,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = "Challenge icon",
            tint = Color(0xFFF5B041),
            modifier = Modifier.size(54.dp)
        )

        Text(
            text = "اپنی سب سے بڑی پریشانی منتخب کریں",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = "ہم اس پریشانی کا پریمیم حل فراہم کریں گے اور آپ کا منافع بڑھائیں گے۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        val challenges = listOf(
            ChallengeItem("Pests", "🐛 کیڑے مکوڑے اور بیماریاں", "فصل کو بیماریوں سے بچائیں اور اسپرے کا خرچہ آدھا کریں", Color(0xFFEF4444)),
            ChallengeItem("Water", "💧 پانی کی کمی اور آبپاشی", "سیٹلائٹ اور موسم کی مدد سے پانی لگانے کا بہترین وقت جانیں", Color(0xFF3B82F6)),
            ChallengeItem("Fertilizer", "🧪 مہنگی کھاد اور اسپرے کا حل", "زمین کے مطابق کھاد کا درست اور سستا حساب حاصل کریں", Color(0xFF10B981)),
            ChallengeItem("Rates", "📈 منڈی کے ریٹ معلوم نہ ہونا", "پاکستان کی تمام غلہ منڈیوں کے لائیو ریٹ حاصل کریں", Color(0xFFF5B041)),
            ChallengeItem("Yield", "🌾 کم پیداوار کا مسئلہ", "جدید تحقیق سے اپنی پیداوار میں چالیس فیصد اضافہ کریں", Color(0xFF8B5CF6))
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            challenges.forEach { item ->
                val isSelected = selectedChallenge == item.id
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isSelected) item.color.copy(alpha = 0.1f) else Color(0xFF131A15))
                        .border(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) item.color else Color(0xFF3E4A40),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onChallengeSelect(item.id) }
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.labelUrdu,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                        Text(
                            text = item.descUrdu,
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }

                    RadioButton(
                        selected = isSelected,
                        onClick = { onChallengeSelect(item.id) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = item.color,
                            unselectedColor = Color(0xFF3E4A40)
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onNext,
            enabled = selectedChallenge.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("آگے بڑھیں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepProposal(
    challenge: String,
    onNext: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f))
                .padding(18.dp)
        ) {
            Icon(
                imageVector = Icons.Default.TrendingUp,
                contentDescription = "Profit Trend icon",
                tint = Color(0xFF10B981),
                modifier = Modifier.size(64.dp)
            )
        }

        Text(
            text = "کِسان دوست حل اور منافع گائیڈ",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        Text(
            text = "ہم آپ کی فصل کو زیادہ منافع بخش بنانے کے لیے تیار ہیں۔",
            fontSize = 12.sp,
            color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Large Premium Highlight Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF131A15))
                .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(18.dp))
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "آپ کے مسئلے کا پریمیم کسان دوست حل:",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF10B981),
                    textAlign = TextAlign.Center
                )

                val solutionText = when (challenge) {
                    "Pests" -> "ہم آپ کی فصل کو سنڈیوں اور کیڑوں سے بچانے کے لیے لائیو مانیٹرنگ اور الرٹس فراہم کریں گے۔ دیسی اسپرے کے نسخوں سے آپ کے اسپرے کے اخراجات میں 30 فیصد کمی ہوگی اور پیداوار میں اضافہ ہوگا۔"
                    "Water" -> "سیٹلائٹ تصاویر اور لائیو موسمی معلومات کی مدد سے پانی لگانے کا بہترین دن معلوم کریں۔ اس سے پانی کی 20 فیصد بچت اور فصل کی صحت بہتر رہے گی۔"
                    "Fertilizer" -> "مٹی کی قسم اور فصل کے سائز کے مطابق کھاد کی بہترین اور سستی مقدار معلوم کریں۔ مہنگی کھاد کا ضیاع روکیں اور ہزاروں روپے بچائیں۔"
                    "Rates" -> "پاکستان کی تمام غلہ منڈیوں کے تازہ ترین ریٹ ہر گھنٹے اپ ڈیٹ دیکھیں۔ اپنی فصل کو نقصان سے بچائیں اور سب سے زیادہ قیمت پر فروخت کریں۔"
                    else -> "جیمنائی آرٹیفیشل انٹیلیجنس زرعی اسسٹنٹ کی بدولت جدید ٹیکنالوجی سے فصل کی دیکھ بھال کریں۔ اس سے مجموعی پیداوار میں 40 فیصد تک اضافہ متوقع ہے۔"
                }

                Text(
                    text = solutionText,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                HorizontalDivider(color = Color(0xFF3E4A40), thickness = 1.dp)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "اخراجات میں بچت", fontSize = 11.sp, color = Color(0xFFE1E3E1).copy(alpha = 0.6f))
                        Text(text = "30% تک کمی", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444))
                    }
                    Box(modifier = Modifier.width(1.dp).height(30.dp).background(Color(0xFF3E4A40)))
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "پیداوار میں اضافہ", fontSize = 11.sp, color = Color(0xFFE1E3E1).copy(alpha = 0.6f))
                        Text(text = "40% تک اضافہ", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onNext,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text("اپنا پریمیم کارڈ فعال کریں", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(6.dp))
            Icon(imageVector = Icons.Default.ArrowForward, contentDescription = null, modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun OnboardingStepPaywall(
    farmerName: String,
    phoneNumber: String,
    selectedTier: String,
    onTierSelect: (String) -> Unit,
    onFinish: () -> Unit,
    onVoicePlay: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "پریمیم کِسان ممبر شپ کارڈ",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD1E8D1),
            textAlign = TextAlign.Center
        )

        // VISUALLY STUNNING PREMIUM GRADIENT CARD
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1.6f)
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1B5E20), // Rich forest emerald
                            Color(0xFF111827), // Deep space slate
                            Color(0xFFD97706)  // Elegant warm gold
                        )
                    )
                )
                .border(2.dp, Color(0xFFF5B041), RoundedCornerShape(20.dp))
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header of Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = Color(0xFFF5B041),
                            modifier = Modifier.size(24.dp)
                        )
                        Text(
                            text = "KISAAN DOST",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFD1E8D1),
                            letterSpacing = 1.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFFF5B041).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFFF5B041), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "PREMIUM MEMBER",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF5B041)
                        )
                    }
                }

                // Middle of Card: Name & Chip
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (farmerName.isBlank()) "کسان دوست ممبر" else farmerName,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = phoneNumber.ifBlank { "+92 300 1234567" },
                            fontSize = 12.sp,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }

                    // Simulated Gold Chip
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFFF5B041), Color(0xFFF4D03F))
                                )
                            )
                            .size(36.dp, 28.dp)
                    )
                }

                // Footer of Card
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "MEMBERSHIP ID: KD-${System.currentTimeMillis() % 1000000}",
                        fontSize = 9.sp,
                        color = Color.White.copy(alpha = 0.5f),
                        fontFamily = FontFamily.Monospace
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                                .size(8.dp)
                        )
                        Text(
                            text = "ACTIVE",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF10B981)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Subscription Tier Options
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Option 1: Free Trial
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedTier == "Trial") Color(0xFF1B5E20).copy(alpha = 0.15f) else Color(0xFF131A15))
                    .border(
                        width = if (selectedTier == "Trial") 1.5.dp else 1.dp,
                        color = if (selectedTier == "Trial") Color(0xFF10B981) else Color(0xFF3E4A40),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onTierSelect("Trial") }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFF10B981))
                    Column {
                        Text(
                            text = "14 دن کا مفت ٹرائل شروع کریں",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "پہلے 14 دن تمام پریمیم خصوصیات بالکل مفت استعمال کریں",
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                }

                RadioButton(
                    selected = selectedTier == "Trial",
                    onClick = { onTierSelect("Trial") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFF10B981),
                        unselectedColor = Color(0xFF3E4A40)
                    )
                )
            }

            // Option 2: Full Premium Year
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedTier == "Premium") Color(0xFFD97706).copy(alpha = 0.15f) else Color(0xFF131A15))
                    .border(
                        width = if (selectedTier == "Premium") 1.5.dp else 1.dp,
                        color = if (selectedTier == "Premium") Color(0xFFF5B041) else Color(0xFF3E4A40),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onTierSelect("Premium") }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.CardMembership, contentDescription = null, tint = Color(0xFFF5B041))
                    Column {
                        Text(
                            text = "سالانہ پریمیم کسان ممبرشپ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "صرف 1500 روپے سالانہ میں لا محدود جیمنائی آواز اور مشورے پائیں",
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                }

                RadioButton(
                    selected = selectedTier == "Premium",
                    onClick = { onTierSelect("Premium") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFFF5B041),
                        unselectedColor = Color(0xFF3E4A40)
                    )
                )
            }

            // Option 3: Free Basic
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (selectedTier == "Free") Color(0xFF1F2420) else Color(0xFF131A15))
                    .border(
                        width = if (selectedTier == "Free") 1.5.dp else 1.dp,
                        color = if (selectedTier == "Free") Color(0xFFE1E3E1).copy(alpha = 0.5f) else Color(0xFF3E4A40),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .clickable { onTierSelect("Free") }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Person, contentDescription = null, tint = Color(0xFFE1E3E1).copy(alpha = 0.6f))
                    Column {
                        Text(
                            text = "بنیادی مفت اکاؤنٹ (Basic Free Account)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                        Text(
                            text = "محدود سروس کے ساتھ مفت آگے بڑھیں",
                            fontSize = 11.sp,
                            color = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                }

                RadioButton(
                    selected = selectedTier == "Free",
                    onClick = { onTierSelect("Free") },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = Color(0xFFE1E3E1).copy(alpha = 0.8f),
                        unselectedColor = Color(0xFF3E4A40)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            onClick = onFinish,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Icon(imageVector = Icons.Default.Check, contentDescription = "Activate Card")
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (selectedTier == "Free") "مفت اکاؤنٹ کے ساتھ مکمل کریں" else "پریمیم ممبر کارڈ فعال کریں",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

data class DialectItem(val id: String, val label: String, val icon: ImageVector, val accent: Color)
data class RegionCardItem(val id: String, val labelUrdu: String, val detailsUrdu: String, val icon: ImageVector, val color: Color)
data class CropItem(val id: String, val label: String, val color: Color)
data class ChallengeItem(val id: String, val labelUrdu: String, val descUrdu: String, val color: Color)
