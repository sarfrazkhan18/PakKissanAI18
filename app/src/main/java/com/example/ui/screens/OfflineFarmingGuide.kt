package com.example.ui.screens

import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.LanguageOption
import java.util.Locale

data class FarmingTip(
    val id: String,
    val titleUrdu: String,
    val titleEnglish: String,
    val category: String, // "crops", "fertilizer", "water", "pests"
    val contentUrdu: String,
    val contentEnglish: String,
    val stepsUrdu: List<String>,
    val stepsEnglish: List<String>,
    val iconEmoji: String
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OfflineFarmingGuide(
    selectedLanguage: LanguageOption,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryTab by remember { mutableStateOf("All") }
    var expandedTipId by remember { mutableStateOf<String?>(null) }
    
    // Text to Speech integration for offline reading helper
    var textToSpeech by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsPlayingId by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        textToSpeech = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech?.language = Locale("ur", "PK")
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        }
    }

    fun speakTip(tip: FarmingTip) {
        textToSpeech?.let { tts ->
            if (ttsPlayingId == tip.id) {
                tts.stop()
                ttsPlayingId = null
            } else {
                ttsPlayingId = tip.id
                val speakText = if (selectedLanguage == LanguageOption.ENGLISH) {
                    "${tip.titleEnglish}. ${tip.contentEnglish}. Key steps: " + tip.stepsEnglish.joinToString(". ")
                } else {
                    "${tip.titleUrdu}۔ ${tip.contentUrdu}۔ اہم اقدامات: " + tip.stepsUrdu.joinToString("۔ ")
                }
                
                tts.language = if (selectedLanguage == LanguageOption.ENGLISH) Locale.ENGLISH else Locale("ur", "PK")
                tts.speak(speakText, TextToSpeech.QUEUE_FLUSH, null, "TipSpeak_${tip.id}")
            }
        }
    }

    val staticTips = remember { getStaticFarmingTips() }

    // Filter logic
    val filteredTips = remember(searchQuery, selectedCategoryTab) {
        staticTips.filter { tip ->
            val matchesCategory = selectedCategoryTab == "All" || tip.category.equals(selectedCategoryTab, ignoreCase = true)
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                tip.titleUrdu.contains(searchQuery, ignoreCase = true) ||
                        tip.titleEnglish.contains(searchQuery, ignoreCase = true) ||
                        tip.contentUrdu.contains(searchQuery, ignoreCase = true) ||
                        tip.contentEnglish.contains(searchQuery, ignoreCase = true)
            }
            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0C0B))
            .padding(top = 8.dp)
    ) {
        // Welcoming header card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF161B17)),
            border = BoxDefaults.cardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF10B981))
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "آف لائن زرعی رہنمائی • Offline Smart Guide",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color(0xFF10B981)
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "انٹرنیٹ کے بغیر بھی بہترین کاشتکاری کے طریقے سیکھیں اور اپنی پیداوار دگنی کریں۔",
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = Color(0xFFE1E3E1).copy(alpha = 0.8f),
                        lineHeight = 16.sp
                    )
                }
                
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(32.dp)
                        .background(Color(0xFF2C3E2F), CircleShape)
                        .testTag("close_help_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "قریب کریں Close Guide",
                        tint = Color(0xFFD1E8D1),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // Live Search Bar with modern layout
        TextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, Color(0xFF323F34), RoundedCornerShape(12.dp))
                .testTag("help_guide_search_bar"),
            placeholder = {
                Text(
                    text = "فصل کا نام، کھاد، پانی یا کیڑے مکوڑے تلاش کریں...",
                    fontSize = 12.sp,
                    color = Color(0xFFE1E3E1).copy(alpha = 0.5f)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "تلاش کریں Search",
                    tint = Color(0xFF10B981)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "صاف کریں۔ Clear Search",
                            tint = Color(0xFFE1E3E1).copy(alpha = 0.6f)
                        )
                    }
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFF111412),
                unfocusedContainerColor = Color(0xFF111412),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                focusedTextColor = Color(0xFFE1E3E1),
                unfocusedTextColor = Color(0xFFE1E3E1)
            ),
            singleLine = true
        )

        // Custom Category Selector bar
        val categories = listOf(
            Triple("All", "سب معلومات", "📚"),
            Triple("crops", "فصلیں", "🌾"),
            Triple("fertilizer", "کھادیں", "🧪"),
            Triple("water", "پانی", "💧"),
            Triple("pests", "بیماریاں", "🐛")
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { (catKey, catName, emoji) ->
                val isSelected = selectedCategoryTab == catKey
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) Color(0xFF10B981) else Color(0xFF161B17))
                        .border(
                            width = 1.dp,
                            color = if (isSelected) Color(0xFF10B981) else Color(0xFF2C3E2F),
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { selectedCategoryTab = catKey }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = emoji, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = catName,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.Black else Color(0xFFD1E8D1)
                        )
                    }
                }
            }
        }

        // List of offline guide tips
        if (filteredTips.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "کوئی معلومات نہیں ملی",
                        tint = Color(0xFF10B981).copy(alpha = 0.5f),
                        modifier = Modifier.size(54.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "کوئی رہنمائی نہیں ملی",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color(0xFFD1E8D1)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "کچھ اور تلاش کرنے کی کوشش کریں جیسے کہ 'گندم' یا 'پانی' تاکہ درست متبادل ملے۔",
                        fontSize = 11.sp,
                        color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredTips, key = { it.id }) { tip ->
                    val isExpanded = expandedTipId == tip.id
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .animateItemPlacement()
                            .clickable {
                                expandedTipId = if (isExpanded) null else tip.id
                            }
                            .testTag("tip_card_${tip.id}"),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isExpanded) Color(0xFF1F2420) else Color(0xFF111412)
                        ),
                        border = BoxDefaults.cardBorder(isExpanded)
                    ) {
                        Column {
                            // Header row of the card
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Rounded elegant emoji container
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF161B17)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = tip.iconEmoji, fontSize = 20.sp)
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = tip.titleUrdu,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp,
                                        color = Color(0xFFD1E8D1)
                                    )
                                    Text(
                                        text = tip.titleEnglish,
                                        fontWeight = FontWeight.Normal,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE1E3E1).copy(alpha = 0.5f)
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    // Audio reader aid button
                                    IconButton(
                                        onClick = { speakTip(tip) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (ttsPlayingId == tip.id) Icons.Default.VolumeUp else Icons.Default.VolumeMute,
                                            contentDescription = "آواز سنیں Speak Out Loud",
                                            tint = if (ttsPlayingId == tip.id) Color(0xFF10B981) else Color(0xFFD1E8D1).copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "تبدیل کریں Expand Details",
                                        tint = Color(0xFFD1E8D1).copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }

                            // Expandable detailed guide panel
                            AnimatedVisibility(
                                visible = isExpanded,
                                enter = expandVertically() + fadeIn(),
                                exit = shrinkVertically() + fadeOut()
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFF0F1210))
                                        .padding(14.dp)
                                ) {
                                    Divider(color = Color(0xFF2C3E2F), modifier = Modifier.padding(bottom = 10.dp))
                                    
                                    // Summary explanation in Urdu
                                    Text(
                                        text = tip.contentUrdu,
                                        fontSize = 13.sp,
                                        color = Color(0xFFD1E8D1),
                                        lineHeight = 20.sp,
                                        modifier = Modifier.padding(bottom = 4.dp)
                                    )
                                    
                                    // English subtitle translation
                                    Text(
                                        text = tip.contentEnglish,
                                        fontSize = 11.sp,
                                        color = Color(0xFFE1E3E1).copy(alpha = 0.7f),
                                        lineHeight = 16.sp,
                                        modifier = Modifier.padding(bottom = 12.dp)
                                    )

                                    Text(
                                        text = "اہم رہنما اصول • Key Guidelines & Steps:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF10B981),
                                        modifier = Modifier.padding(bottom = 8.dp)
                                    )

                                    // Display steps elegantly
                                    tip.stepsUrdu.forEachIndexed { index, stepUrdu ->
                                        val stepEnglish = tip.stepsEnglish.getOrNull(index) ?: ""
                                        
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.Top
                                        ) {
                                            Icon(
                                                imageVector = Icons.Outlined.CheckCircle,
                                                contentDescription = "شامل کریں Done",
                                                tint = Color(0xFF10B981),
                                                modifier = Modifier
                                                    .size(16.dp)
                                                    .padding(top = 2.dp)
                                            )
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(
                                                    text = stepUrdu,
                                                    fontSize = 12.sp,
                                                    color = Color(0xFFE1E3E1),
                                                    lineHeight = 18.sp
                                                )
                                                if (stepEnglish.isNotBlank()) {
                                                    Text(
                                                        text = stepEnglish,
                                                        fontSize = 10.sp,
                                                        color = Color(0xFFE1E3E1).copy(alpha = 0.6f),
                                                        lineHeight = 14.sp
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
            }
        }
    }
}

// Helpers for consistent styling
object BoxDefaults {
    @Composable
    fun cardBorder(isHighlighted: Boolean = false): androidx.compose.foundation.BorderStroke {
        return androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isHighlighted) Color(0xFF10B981) else Color(0xFF2C3E2F)
        )
    }
}

fun getStaticFarmingTips(): List<FarmingTip> {
    return listOf(
        FarmingTip(
            id = "wheat_time",
            titleUrdu = "گندم کی بوائی کا وقت",
            titleEnglish = "Wheat Sowing Best Time",
            category = "crops",
            contentUrdu = "پنجاب اور سندھ میں گندم کی بوائی کے لیے بہترین وقت یکم نومبر سے ٢٠ نومبر تک ہے۔ دیر سے بوائی کی صورت میں پیداوار ہر ہفتے کم ہو جاتی ہے۔",
            contentEnglish = "The optimum timing for wheat sowing in Punjab & Sindh is Nov 1st to Nov 20th. Delayed sowing can reduce yields by 1% per day.",
            stepsUrdu = listOf(
                "اچھی پیداوار کے لیے یکم سے ٢٠ نومبر تک بوائی لازمی مکمل کریں۔",
                "صرف منظور شدہ بیماریوں سے پاک بیج ہی فصل کے لیے استعمال کریں۔",
                "بیج کی مناسب مقدار ٤٥ تا ٥٠ کلوگرام فی ایکڑ رکھیں۔",
                "بیج کو مٹی میں ڈالنے سے پہلے پھپھوندی کش زہر لگوانا یقینی بنائیں۔"
            ),
            stepsEnglish = listOf(
                "Ensure sowing is completed between November 1st and November 20th for premium yields.",
                "Always use high-quality certified seeds approved by agriculture departments.",
                "Maintain uniform seed rate of 45-50 kg per acre across your field.",
                "Treat the seeds with certified fungicide products before sowing."
            ),
            iconEmoji = "🌾"
        ),
        FarmingTip(
            id = "dap_urea_balance",
            titleUrdu = "ڈی اے پی اور یوریا کھاد کا متوازن استعمال",
            titleEnglish = "DAP & Urea Fertilizer Balance",
            category = "fertilizer",
            contentUrdu = "ڈی اے پی کھاد ہمیشہ بوائی کے وقت دیں تاکہ پودے کی جڑیں مضبوط ہوں۔ یوریا کھاد کو پہلے اور دوسرے پانی کے وقت قسطوں میں دینا چاہیے۔",
            contentEnglish = "Phosphatic fertilizers (DAP) must be applied entirely at sowing time to build deep roots. Nitrogen (Urea) should be split between first and second irrigation cycles.",
            stepsUrdu = listOf(
                "١ بوری ڈی اے پی بوائی کے وقت زمین کی تیاری میں یکساں ڈالیں۔",
                "پہلی قسط یوریا (آدھی بوری) پہلے پانی پر اچھی طرح دیں۔",
                "دوسری قسط یوریا (آدھی بوری) دوسرے پانی پر بوائی کے ٤٠ سے ٤٥ دن بعد دیں۔",
                "یوریا کو دوپہر کی شدید دھوپ میں ڈالنے سے گریز کریں۔"
            ),
            stepsEnglish = listOf(
                "Apply 1 bag of DAP per acre uniformly during seedbed preparation.",
                "Apply first split of Urea (half bag) during first irrigation cycle.",
                "Apply second split of Urea (half bag) during second irrigation cycle (40-45 days).",
                "Avoid broadcasting Urea in intense midday heat to prevent nitrogen loss."
            ),
            iconEmoji = "🧪"
        ),
        FarmingTip(
            id = "wheat_irrigation",
            titleUrdu = "آبپاشی کے نازک مراحل",
            titleEnglish = "Critical Irrigation Stages",
            category = "water",
            contentUrdu = "گندم کی فصل کے دوران پانی کے ٣ نازک مراحل ہوتے ہیں جب پانی کی کمی نہیں ہونی چاہیے۔ ان مراحل پر پانی دینے سے پیداوار دگنی ہو سکتی ہے۔",
            contentEnglish = "Wheat requires moisture most critically during three growth stages. Proper irrigation at these stages saves water while preserving yield.",
            stepsUrdu = listOf(
                "پہلا پانی: بوائی کے ٢٠ سے ٢٢ دن بعد (جب جڑیں نکل رہی ہوں یا شاخیں بننا شروع ہوں)۔",
                "دوسرا پانی: بوائی کے ٨٠ سے ٩٠ دن بعد (جب گندم کا سٹا نکل رہا ہو یا پھول آنے کا وقت ہو)۔",
                "تیسرا پانی: بوائی کے ١٢٠ دن بعد (جب دانہ بن رہا ہو یا دودھیا مرحلہ ہو)۔",
                "تیز ہوا کے دوران کھیت کو پانی مت دیں تاکہ فصل نیچے نہ گرے۔"
            ),
            stepsEnglish = listOf(
                "First watering: 20-22 days after sowing (Crown Root Initiation stage).",
                "Second watering: 80-90 days after sowing (Heading/Booting stage).",
                "Third watering: 120 days after sowing (Milky/Grain filling stage).",
                "Avoid watering the crops during high winds to prevent lodging."
            ),
            iconEmoji = "💧"
        ),
        FarmingTip(
            id = "natural_pest",
            titleUrdu = "کیمیائی زرعی ادویات کے بغیر کیڑوں کا تدارک",
            titleEnglish = "Natural Organic Pest Control",
            category = "pests",
            contentUrdu = "گھر کے دیسی طریقوں سے ماحول دوست سپرے تیار کر کے زہریلی ادویات کے بغیر کیڑوں (جیسے سست تیلا) پر قابو پایا جا سکتا ہے۔",
            contentEnglish = "Eco-friendly, chemical-free organic sprays prepared at home can safely control pests like aphids and protect beneficial predatory insects.",
            stepsUrdu = listOf(
                "نیم کے خشک پتے پانی میں اچھے سے ابال کر اس کا خالص عرق سپرے پمپ میں بھریں۔",
                "نیم گرم پانی میں تھوڑا سا برتن دھونے کا مائع صابن اور نیم کا تیل ملا کر پودوں پر اسپری کریں۔",
                "یہ دیسی سپرے سست تیلے اور سفید مکھی کی آمد کو روکنے میں انتہائی کارگر ہے۔",
                "یہ سپرے صبح سویرے یا شام کو سورج غروب ہوتے وقت کریں۔"
            ),
            stepsEnglish = listOf(
                "Boil fresh neem leaves in pure water and strain the concentrated liquid for spraying.",
                "Mix a small amount of liquid dishwashing soap and neem oil into lukewarm water.",
                "This organic spray effectively handles green aphids and cotton whitefly without toxic chemicals.",
                "Always apply spray in early morning or late evening when temperature is mild."
            ),
            iconEmoji = "🐛"
        ),
        FarmingTip(
            id = "rice_nursery",
            titleUrdu = "چاول کی پنیری اور منتقلی",
            titleEnglish = "Rice Nursery & Transplantation",
            category = "crops",
            contentUrdu = "چاول کی صحت مند پنیری کی کاشت مئی کے آخر سے جون کے وسط تک ہونی چاہیے۔ پنیری کی منتقلی کے وقت پودوں کے درمیان مناسب فاصلہ رکھیں۔",
            contentEnglish = "Healthy rice nurseries should be sown from late May to mid-June. Maintain uniform distance between seedling hills during transplantation.",
            stepsUrdu = listOf(
                "چاول کی پنیری کی کاشت ٢٠ مئی سے ٢٠ جون تک لازمی مکمل کریں۔",
                "منتقل کرتے وقت پودے کی عمر ٢٥ سے ٣٠ دن سے زیادہ نہیں ہونی چاہئے۔",
                "ہر سوراخ یا شگاف میں ٢ دیسی صحت مند شتھل ( seedling) لگائیں۔",
                "پہلے ٢ ہفتے تک کھیت میں پانی ہمیشہ کھڑا رکھنا ضروری ہے۔"
            ),
            stepsEnglish = listOf(
                "Sow the nursery beds from May 20th to June 20th depending on specific variety.",
                "Seedlings should ideally be transplanted within 25-30 days of sowing.",
                "Ensure transplanting 2 healthy seedlings per hill with equal spacing.",
                "Keep 2-3 inches of standing water fields for 15 days to suppress weed growth."
            ),
            iconEmoji = "🍚"
        ),
        FarmingTip(
            id = "soil_test",
            titleUrdu = "مٹی کے نمونے لینے کی رہنمائی",
            titleEnglish = "Soil Testing & Sampling Guide",
            category = "fertilizer",
            contentUrdu = "فصل کی کاشت سے پہلے مٹی کا ٹیسٹ کروانا مٹی کی اصل زرخیزی کو جاننے اور فالتو کھاد کے خرچ کو روکنے کا بہترین طریقہ ہے۔",
            contentEnglish = "Soil sampling prior to crop cultivation is essential to understand nutrient statuses and avoid excessive, redundant fertilizer costs.",
            stepsUrdu = listOf(
                "اپنے کھیت کے ٧ سے ٨ مختلف مقامات سے 'V' شکل کا ٦ انچ گہرا گڑھا کھودیں۔",
                "اوپری گھاس اور فالتو مٹی ہٹا کر سائیڈ سے یکساں مٹی کا تراشہ لیں۔",
                "ان تمام نمونوں کو آپس میں اچھی طرح مکس کر کے آدھا کلو مٹی صاف تھیلی میں الگ کریں۔",
                "مٹی کے لفافے پر کسان کا نام، اپنے کھیت کا نمبر اور پچھلی کاشت لکھیں۔"
            ),
            stepsEnglish = listOf(
                "Dig a 'V' shaped pit of about 6 inches depth across 7-8 different random spots of the field.",
                "Clear top weeds, then scrap soil uniformly from the slice profile.",
                "Mix all specimens thoroughly in a bucket, retaining half-kg of homogeneous soil in a clean bag.",
                "Label the packet clearly with your name, farm id, and list of prior crops."
            ),
            iconEmoji = "🌱"
        ),
        FarmingTip(
            id = "drip_irrigation",
            titleUrdu = "جدید قطرہ قطرہ آبپاشی (ڈرپ)",
            titleEnglish = "Modern Drip Irrigation Tech",
            category = "water",
            contentUrdu = "ڈرپ اریگیشن سسٹم کے ذریعے پانی اور کھاد پودے کی جڑوں تک براہِ راست پہنچتی ہے جس سے ٥٠ فیصد پانی کی بچت ہوتی ہے۔",
            contentEnglish = "Drip technology delivers water and soluble nutrients directly to plant roots, leading to 50% water savings and double fertilizer uptake efficiency.",
            stepsUrdu = listOf(
                "پائپ لائن نیٹ ورک کھیت میں بچھائیں تا کہ پانی ہر پودے کے پاس قطرہ قطرہ گرے۔",
                "پانی کے ساتھ کھاد (فرٹیگیشن) دینے سے کھاد ضائع ہونے سے بچتی ہے۔",
                "یہ خشک اور ریگستانی مٹیوں میں باغات کے لیے بہترین سسٹم ہے۔",
                "محکمہ زراعت کی سبسڈی اسکیموں سے ڈرپ کی لاگت میں رعایت لیں۔"
            ),
            stepsEnglish = listOf(
                "Layout a pipeline network so water drips consistently and slowly right around the root base.",
                "Introduce fertilizer through irrigation water (fertigation) to minimize deep percolation leaks.",
                "Best suited for orchards, high-value vegetables, and sandy lands.",
                "Utilize Government subsidy programs for drip installation support."
            ),
            iconEmoji = "⛲"
        ),
        FarmingTip(
            id = "whitefly_cotton",
            titleUrdu = "کپاس کی سفید مکھی کا دیسی و کیمیائی علاج",
            titleEnglish = "Whitefly Control in Cotton",
            category = "pests",
            contentUrdu = "کپاس کی فصل میں سفید مکھی پتوں کا رس چوستی ہے جس سے پودا کالا ہو جاتا ہے۔ اس کے تدارک کے لیے بروقت اور درست اقدام ضروری ہے۔",
            contentEnglish = "The silverleaf whitefly drains cotton sap and encourages black mold growth. Scout fields weekly to determine precise action levels.",
            stepsUrdu = listOf(
                "کپاس کے کھیت میں ہمیشہ باقاعدہ پیسٹ سکاؤٹنگ (ہفتے میں دو بار) کریں۔",
                "سفید مکھی کی تعداد معاشی نقصان کی حد (٥ سفید مکھی فی پتا) ہونے پر سپرے کریں۔",
                "دوست کیڑے (جیسے چڑی اور لیڈی برڈ) کے شکار کا انتظار کریں تاکہ سفید مکھی قدرتی طور پر ختم ہو۔",
                "کھیت کی مینڈھوں اور اطراف کی جڑی بوٹیوں کو ہمیشہ صاف رکھیں جو ان کا مسکن بنتی ہیں۔"
            ),
            stepsEnglish = listOf(
                "Perform diligent pest scouting in your cotton field twice every week.",
                "Apply chemical sprays only when whitefly population reaches 5 insects per leaf threshold.",
                "Conserve ladybug beetles and chrysopa lacewings that hunt whitefly nymphs.",
                "Maintain weed-free levees since agricultural weeds host overwintering whiteflies."
            ),
            iconEmoji = "☁️"
        )
    )
}
