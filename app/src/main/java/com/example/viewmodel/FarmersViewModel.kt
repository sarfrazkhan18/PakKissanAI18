package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import com.example.network.*
import com.example.utils.PasswordHasher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FarmersViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = KisaanRepository(database.kisaanDao())

    private val prefs = application.getSharedPreferences("kisaan_prefs", Context.MODE_PRIVATE)

    // Global text-scale multiplier ("بڑا سائز"). Applied via LocalDensity so it enlarges
    // every sp in the app — including hardcoded sizes — for farmers with weak eyesight.
    private val _textScale = MutableStateFlow(prefs.getFloat("text_scale", 1.0f))
    val textScale: StateFlow<Float> = _textScale.asStateFlow()

    fun toggleTextScale() {
        val next = if (_textScale.value >= 1.3f) 1.0f else 1.3f
        _textScale.value = next
        prefs.edit().putFloat("text_scale", next).apply()
    }

    // Theme mode. Defaults to LIGHT (false) because farmers mostly use the app outdoors in
    // bright sunlight where a dark UI on a cheap LCD is unreadable. Manual ☀️/🌙 toggle.
    private val _darkMode = MutableStateFlow(prefs.getBoolean("dark_mode", false))
    val darkMode: StateFlow<Boolean> = _darkMode.asStateFlow()

    fun toggleDarkMode() {
        val next = !_darkMode.value
        _darkMode.value = next
        prefs.edit().putBoolean("dark_mode", next).apply()
    }

    // WebSocket Gemini Live Service Properties
    private val liveService = GeminiLiveService()
    val liveConnectionState: StateFlow<LiveConnectionState> = liveService.connectionState
    
    // Live received text stream accumulator
    private val _liveReceivedText = MutableStateFlow("")
    val liveReceivedText: StateFlow<String> = _liveReceivedText.asStateFlow()
    
    private val audioPlayer = PcmAudioPlayer()
    
    fun startLiveSession() {
        liveService.connect(_selectedLanguage.value)
        _liveReceivedText.value = ""
        viewModelScope.launch {
            // Collect live incoming streaming text
            liveService.incomingText.collect { chunk ->
                val cleanChunk = chunk.replace("*", "")
                _liveReceivedText.value = _liveReceivedText.value + cleanChunk
            }
        }
        viewModelScope.launch {
            // Collect and play incoming raw audio stream
            audioPlayer.start()
            liveService.incomingAudio.collect { pcmChunk ->
                audioPlayer.write(pcmChunk)
            }
        }
    }
    
    fun stopLiveSession() {
        liveService.disconnect()
        audioPlayer.stop()
    }
    
    fun sendLiveTextMessage(text: String) {
        if (text.isNotBlank()) {
            _liveReceivedText.value = "" // Reset for new answer
            liveService.sendTextMessage(text)
        }
    }

    val userProfile: StateFlow<UserProfile?> = repository.userProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allKnowledge: StateFlow<List<AgriKnowledge>> = repository.allKnowledge
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // All active chat companion sessions dynamically reactive to the active farmer
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val sessions: StateFlow<List<ChatSession>> = userProfile
        .flatMapLatest { profile ->
            if (profile != null) {
                repository.getSessionsForFarmer(profile.phoneNumber)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUserProfile(name: String, phone: String, region: String, primaryCrop: String, dialect: String, passwordRaw: String) {
        viewModelScope.launch {
            // First deactivate other active accounts
            repository.deactivateAllProfiles()
            
            val profile = UserProfile(
                id = phone, // Use unique phone number as ID to scale to 1M+ farmers
                fullName = name,
                phoneNumber = phone,
                region = region,
                primaryCrop = primaryCrop,
                selectedDialect = dialect,
                passwordHash = PasswordHasher.hash(passwordRaw), // Salted PBKDF2, never plaintext
                onboardingCompleted = true,
                isActive = true // Mark this register session active on device
            )
            repository.saveProfile(profile)
            
            // set language option based on name match
            val langOption = LanguageOption.values().find { 
                it.displayName.contains(dialect, ignoreCase = true) 
            } ?: LanguageOption.URDU
            setLanguage(langOption)
        }
    }

    // Local authentications to scale to millions of farmers on shared center/personal devices
    suspend fun attemptLocalLogin(phone: String, passwordRaw: String): Boolean {
        val profile = repository.getProfileByPhone(phone)
        if (profile != null && PasswordHasher.verify(passwordRaw, profile.passwordHash)) {
            repository.deactivateAllProfiles()
            // Upgrade any legacy plaintext passcode to a salted hash on first successful login.
            val upgradedHash = if (PasswordHasher.isLegacyPlaintext(profile.passwordHash)) {
                PasswordHasher.hash(passwordRaw)
            } else {
                profile.passwordHash
            }
            val activated = profile.copy(isActive = true, passwordHash = upgradedHash)
            repository.saveProfile(activated)
            
            val dialect = activated.selectedDialect
            val langOption = LanguageOption.values().find { 
                it.displayName.contains(dialect, ignoreCase = true) 
            } ?: LanguageOption.URDU
            setLanguage(langOption)
            return true
        }
        return false
    }

    suspend fun isPhoneAlreadyRegistered(phone: String): Boolean {
        return repository.getProfileByPhone(phone) != null
    }

    /** Persist the My Farm profile fields (P2.1/P2.2) onto the active farmer's profile. */
    fun saveFarmDetails(
        district: String,
        primaryCrop: String,
        cropVariety: String,
        landArea: String,
        areaUnit: String,
        sowingDateMillis: Long,
        irrigationSource: String
    ) {
        viewModelScope.launch {
            val current = userProfile.value ?: return@launch
            val updated = current.copy(
                district = district,
                primaryCrop = primaryCrop.ifBlank { current.primaryCrop },
                cropVariety = cropVariety,
                landArea = landArea,
                areaUnit = areaUnit,
                sowingDateMillis = sowingDateMillis,
                irrigationSource = irrigationSource
            )
            repository.saveProfile(updated)
        }
    }

    fun logoutOrClearProfile() {
        viewModelScope.launch {
            repository.clearProfile()
        }
    }

    // Selected session ID state
    private val _currentSessionId = MutableStateFlow<String?>(null)
    val currentSessionId: StateFlow<String?> = _currentSessionId.asStateFlow()

    // Message list for the selected session
    val currentMessages: StateFlow<List<ChatMessage>> = _currentSessionId
        .flatMapLatest { sessionId ->
            if (sessionId != null) {
                repository.getMessagesForSession(sessionId)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // UI state states
    private val _uiState = MutableStateFlow<FarmersUiState>(FarmersUiState.Idle)
    val uiState: StateFlow<FarmersUiState> = _uiState.asStateFlow()

    // Selected language for assistant voice recognizer and translation
    private val _selectedLanguage = MutableStateFlow(LanguageOption.URDU)
    val selectedLanguage: StateFlow<LanguageOption> = _selectedLanguage.asStateFlow()

    // Category filter for the past messages
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        // Seed agricultural knowledge. The compiled base set seeds once; the expandable JSON
        // pack (assets/agri_knowledge.json) is upserted every launch so content updates land
        // for existing installs too (P2.3).
        viewModelScope.launch {
            try {
                if (repository.getKnowledgeCount() == 0) {
                    repository.insertKnowledge(AgriKnowledgeSeeder.getInitialKnowledge())
                }
                val jsonPack = AgriKnowledgeLoader.loadFromAssets(getApplication())
                if (jsonPack.isNotEmpty()) {
                    repository.insertKnowledge(jsonPack)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Automatically create a default session if sessions list is empty
        viewModelScope.launch {
            sessions.collect { list ->
                if (list.isEmpty() && _currentSessionId.value == null) {
                    val activePhone = userProfile.value?.phoneNumber ?: ""
                    val defaultSession = ChatSession(
                        title = "کِسان دوست گفتگو (Main Conversation)",
                        language = _selectedLanguage.value.displayName,
                        farmerPhoneNumber = activePhone
                    )
                    repository.createSession(defaultSession)
                    _currentSessionId.value = defaultSession.id
                } else if (_currentSessionId.value == null && list.isNotEmpty()) {
                    _currentSessionId.value = list.first().id
                }
            }
        }
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }

    fun setLanguage(language: LanguageOption) {
        _selectedLanguage.value = language
        viewModelScope.launch {
            val currentId = _currentSessionId.value
            if (currentId != null) {
                // Update language of current session for persistence context
                sessions.value.find { it.id == currentId }?.let { session ->
                    repository.updateSession(session.copy(language = language.displayName))
                }
            }
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun createNewSession(title: String) {
        viewModelScope.launch {
            val sessionTitle = title.ifBlank { "گفتگو - ${System.currentTimeMillis() % 10000}" }
            val activePhone = userProfile.value?.phoneNumber ?: ""
            val session = ChatSession(
                title = sessionTitle,
                language = _selectedLanguage.value.displayName,
                farmerPhoneNumber = activePhone
            )
            repository.createSession(session)
            _currentSessionId.value = session.id
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = sessions.value.find { it.id != sessionId }?.id
            }
        }
    }

    fun sendMessage(userText: String, category: String = "General") {
        if (userText.isBlank()) return
        val sessionId = _currentSessionId.value ?: return

        viewModelScope.launch {
            // Capture the prior conversation BEFORE inserting this turn.
            // currentMessages is a Room-backed Flow that updates asynchronously, so reading
            // it after the insert races: the new user message may or may not be present yet.
            // Snapshotting here guarantees history excludes the current prompt, which
            // executeGeminiQuery appends exactly once (fixes the duplicate-turn bug).
            val chatHistory = currentMessages.value.takeLast(8) // cap history for token usage

            // Save User Message
            val userMsg = ChatMessage(
                sessionId = sessionId,
                role = "user",
                text = userText,
                category = category
            )
            repository.insertMessage(userMsg)

            _uiState.value = FarmersUiState.Loading

            try {
                // Offline-first: when there's no connection, answer from the local verified
                // knowledge base instead of failing. Farmers in the field lose signal
                // constantly — a saved-guidance answer beats a blank error screen.
                val result = if (isOnline()) {
                    executeGeminiQuery(userText, chatHistory)
                } else {
                    buildOfflineAnswer(userText)
                }

                val botMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    text = result.text,
                    category = category,
                    usedVerifiedSource = result.usedVerified
                )
                repository.insertMessage(botMsg)

                _uiState.value = FarmersUiState.Success(result.text)
            } catch (e: Exception) {
                // A network call that started online but failed mid-flight still falls back
                // to local guidance rather than a dead error.
                val fallback = buildOfflineAnswer(userText)
                val botMsg = ChatMessage(
                    sessionId = sessionId,
                    role = "model",
                    text = fallback.text,
                    category = category,
                    usedVerifiedSource = fallback.usedVerified
                )
                repository.insertMessage(botMsg)
                _uiState.value = FarmersUiState.Success(fallback.text)
            }
        }
    }

    /** True when the device currently has an internet-capable network. Fails open. */
    private fun isOnline(): Boolean {
        val cm = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return true
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Search the local verified knowledge base for entries relevant to the prompt.
     * Shared by the online RAG path and the offline fallback so both stay consistent.
     */
    private suspend fun searchLocalKnowledge(userPrompt: String): List<AgriKnowledge> {
        val matches = mutableListOf<AgriKnowledge>()
        try {
            matches.addAll(repository.searchKnowledge(userPrompt.trim()))
            // Split into words to catch individual keywords (gandum, whitefly, cotton, ...)
            val words = userPrompt.split(Regex("[\\s,?.۔،]+"))
            for (word in words) {
                if (word.length >= 3 && matches.size < 3) {
                    for (wm in repository.searchKnowledge(word)) {
                        if (matches.none { it.id == wm.id }) matches.add(wm)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return matches
    }

    /** Build an answer purely from local verified data, clearly marked as offline. */
    private suspend fun buildOfflineAnswer(userPrompt: String): QueryResult {
        val isEnglish = _selectedLanguage.value == LanguageOption.ENGLISH
        val matches = searchLocalKnowledge(userPrompt)
        if (matches.isEmpty()) {
            val msg = if (isEnglish) {
                "📴 You are offline. I couldn't find this in the saved guide. " +
                    "Please reconnect to the internet and ask again."
            } else {
                "📴 انٹرنیٹ دستیاب نہیں ہے۔ یہ سوال محفوظ شدہ رہنمائی میں نہیں ملا۔ " +
                    "براہ کرم انٹرنیٹ آنے پر دوبارہ پوچھیں۔"
            }
            return QueryResult(msg, usedVerified = false)
        }
        val header = if (isEnglish) {
            "📴 No internet — showing saved verified guidance:\n\n"
        } else {
            "📴 انٹرنیٹ نہیں ہے — یہ محفوظ شدہ تصدیق شدہ معلومات ہیں:\n\n"
        }
        val body = matches.take(2).joinToString("\n\n") { m ->
            val title = if (isEnglish) m.titleEn else m.titleUr
            val details = if (isEnglish) m.detailsEn else m.detailsUr
            "🌾 $title\n$details"
        }.replace("*", "")
        return QueryResult(header + body, usedVerified = true)
    }

    /** Record the farmer's 👍/👎 on an answer (quality signal + future training set). */
    fun setMessageFeedback(messageId: String, value: Int) {
        viewModelScope.launch {
            repository.setMessageFeedback(messageId, value)
        }
    }

    // Translation Cache Map to display translated versions on the fly
    private val _messageTranslations = MutableStateFlow<Map<String, String>>(emptyMap())
    val messageTranslations: StateFlow<Map<String, String>> = _messageTranslations.asStateFlow()

    private val _translationLoadingIds = MutableStateFlow<Set<String>>(emptySet())
    val translationLoadingIds: StateFlow<Set<String>> = _translationLoadingIds.asStateFlow()

    fun translateMessageOnTheFly(messageId: String, textToTranslate: String, targetLanguage: LanguageOption) {
        if (_translationLoadingIds.value.contains(messageId)) return
        
        viewModelScope.launch {
            _translationLoadingIds.value = _translationLoadingIds.value + messageId
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    _messageTranslations.value = _messageTranslations.value + (messageId to "غلطی: API کلید غائب ہے۔")
                    return@launch
                }

                val prompt = """
                    You are an expert machine translation system customized for agricultural dialects of Pakistan.
                    Task: Translate this agriculture advisory advice to ${targetLanguage.displayName}.
                    
                    If Urdu: write standard Urdu formatting.
                    If Punjabi: write in Shahmukhi Punjabi script.
                    If Sindhi: write in standard Sindhi script.
                    If Pashto: write in standard Pashto script.
                    If Seraiki: write in Seraiki script or simple local Punjabi.
                    If Balochi: write in Balochi text or simple local Urdu.
                    
                    Rules: Ensure crop names, rates (per 40 Kg, etc.) and specific numeric guides remain completely unchanged and clear. 
                    Format the output with helpful emojis matching the content.
                    Respond ONLY with the direct translation text. No headers, introductory phrases, or pleasantries.
                    
                    Text to Translate:
                    "$textToTranslate"
                """.trimIndent()

                val request = GenerateContentRequest(
                    contents = listOf(
                        Content(parts = listOf(Part(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = 0.2f
                    )
                )

                val response = withContext(Dispatchers.IO) {
                    RetrofitClient.service.generateContent(apiKey, request)
                }
                
                val rawTranslation = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    ?: "ترجمہ دستیاب نہیں ہے۔"
                val translation = rawTranslation.replace("*", "")
                
                _messageTranslations.value = _messageTranslations.value + (messageId to translation)
            } catch (e: Exception) {
                _messageTranslations.value = _messageTranslations.value + (messageId to "ترجمہ کے دوران نیٹ ورک کا مسئلہ پیش آیا: ${e.message}")
            } finally {
                _translationLoadingIds.value = _translationLoadingIds.value - messageId
            }
        }
    }

    // Decide whether a query needs live Google Search grounding. Only time-sensitive
    // questions (market/mandi rates, weather, government subsidies) do. Everything else is
    // served from the model + the local verified knowledge base without a paid search.
    // Matches English, Roman-Urdu and Urdu-script phrasings farmers actually use.
    private fun needsLiveSearch(prompt: String): Boolean {
        val p = prompt.lowercase()
        val liveKeywords = listOf(
            // English / Roman-Urdu
            "rate", "rates", "price", "prices", "mandi", "bhao", "bhav", "bhaao",
            "today", "aaj", "kal", "weather", "mausam", "forecast", "rain", "barish",
            "subsidy", "subsidies", "kisan card", "kissan card", "market", "bazar",
            // Urdu script
            "ریٹ", "بھاؤ", "قیمت", "قیمتیں", "منڈی", "موسم", "بارش", "آج",
            "سبسڈی", "کسان کارڈ", "نرخ", "بازار", "پیشگوئی"
        )
        return liveKeywords.any { p.contains(it) }
    }

    private suspend fun executeGeminiQuery(userPrompt: String, history: List<ChatMessage>): QueryResult = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext QueryResult("معذرت، AI سروس کی چابی (Gemini API Key) غائب ہے۔ براہ کرم AI Studio کے Secrets پینل میں اپنی چابی درج کریں۔", usedVerified = false)
        }

        // Construct Content parts
        val profile = userProfile.value
        val personalizationIntro = if (profile != null) {
            // Build a specific farm context so advice is right for THIS farmer's district, crop
            // stage, and water source — not generic (P2.1/P2.2). Today's date lets the model
            // reason about timing relative to the sowing date (e.g. "when do I irrigate?").
            val locationLine = if (profile.district.isNotBlank()) {
                "${profile.district} district of ${profile.region}"
            } else {
                profile.region
            }
            val farmDetails = buildList {
                if (profile.cropVariety.isNotBlank()) add("crop variety: ${profile.cropVariety}")
                if (profile.landArea.isNotBlank()) add("farm size: ${profile.landArea} ${profile.areaUnit}")
                if (profile.irrigationSource.isNotBlank()) add("water source: ${profile.irrigationSource}")
                if (profile.sowingDateMillis > 0L) {
                    val fmt = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.ENGLISH)
                    add("sowing date: ${fmt.format(java.util.Date(profile.sowingDateMillis))}")
                }
            }.joinToString("; ")
            val today = java.text.SimpleDateFormat("d MMMM yyyy", java.util.Locale.ENGLISH)
                .format(java.util.Date())
            buildString {
                append("The farmer's name is ${profile.fullName}. They farm in $locationLine, ")
                append("with their primary crop being ${profile.primaryCrop}. ")
                if (farmDetails.isNotEmpty()) append("Farm details — $farmDetails. ")
                append("Today's date is $today. ")
                append("Always greet them warmly as '${profile.fullName} Bhai' in Urdu/local language. ")
                append("Customize every answer to the specific soil, pest, climate and canal/tubewell ")
                append("conditions of their district, and to their crop's current growth stage inferred ")
                append("from the sowing date and today's date. When they ask about timing (irrigation, ")
                append("spraying, fertiliser), reason from the sowing date and season, not generic advice.")
            }
        } else {
            "No profile setup completed yet. Respond friendly to the generic farmer."
        }

        // Search local database for verified Pakistani agricultural guidelines (RAG)
        val matches = searchLocalKnowledge(userPrompt)

        val verifiedContext = if (matches.isNotEmpty()) {
            val contextText = matches.joinToString("\n\n") { match ->
                """
                    ### VERIFIED LOCAL DATA FOR ${match.titleEn} (${match.titleUr}):
                    Category: ${match.category}
                    - English Verified Guidelines:
                    ${match.detailsEn}
                    - Urdu Verified Guidelines:
                    ${match.detailsUr}
                """.trimIndent()
            }
            """
                CRITICAL LOCALIZED KNOWLEDGE SOURCE:
                Below are verified regional agricultural guidelines retrieved from Pakistan's official agricultural database matching the user's question.
                You MUST use this specific local information to answer the farmer's question. Prioritize these regional details (such as seed rates, critical water cycles, local pest treatment etc.) over general generic global training data:
                
                $contextText
            """.trimIndent()
        } else {
            ""
        }

        // System Instructions in Urdu / Punjabi / Pushto adaptive context
        val systemDirective = """
            You are Kisaan Dost (کسان دوست), a friendly, expert agricultural advisor for Pakistani farmers. 
            Your goal is to guide farmers with highly practical, cost-effective, climate-resilient, and localized agricultural solutions in Pakistan.
            
            $personalizationIntro
            
            $verifiedContext
            
            Key Rules:
            1. You must respond in the farmer's preferred language option: ${_selectedLanguage.value.displayName}.
               - If Urdu: write in proper, phonetic, and clear Urdu script (اردو لکھائی).
               - If Punjabi: write in clear Shahmukhi Punjabi (پنجابی لکھائی) or easy Urdu with a Punjabi touch.
               - If Pushto, Sindhi, Balochi, Seraiki: write in local text or easy Urdu with friendly dialect expressions.
            2. Present advice in clean, bulleted, bite-sized steps so it is easily understandable.
            3. Use native Pakistani farming terms: Gandum (گندم), Kapas (کپاس), Chawal (چاول), Kamand (کماد/گنا), Maund (من), Acre (ایکڑ), Bori (بوری), Nahri paani (نہری پانی).
            4. Provide cost-effective and natural home remedies (دیسی حل) for pest control and animal care alongside scientific names if necessary.
            5. Always maintain an encouraging, polite, and rural-friendly tone ('Aap', 'Bhai', 'Kisaan Dost').
            6. Keep emojis helpful and contextual (🚜, 🌾, 🐛, ☀️, 🌧️). Keep sentences moderately short because the farmer will listen to these answers via Text-to-Speech (موبائل بول کر سنائے گا).
            7. **LIVE SEARCH GROUNDING & ACCURACY (MANDI RATES & WEATHER)**: 
               - When the user asks about daily market prices (mandi rates), regional weather, current pest forecasts, or government agriculture subsidies, you MUST utilize the integrated live Google Search tool.
               - Look specifically for verified and official regional sources, such as the Punjab Agricultural Marketing Information Service (AMIS) at `amis.pk`, `zaraimandi.com`, or official provincial departments of agriculture.
               - **STRICTLY PROHIBITED TO HALLUCINATE OR ESTIMATE IMAGINARY RATES**: If the live search does not return explicit, high-confidence, real-time market rates for that crop, market, or district today, DO NOT make up or guess any numbers. Politely inform the farmer in their chosen language that the live rate for that commodity/market is currently not updated on the official regional portals today, and offer to give them the nearest regional market average that is officially verified instead. Fully cite the reference source and date when rates are successfully found.
        """.trimIndent()

        // Build the contents structure of previous conversation flow
        val contentList = mutableListOf<Content>()
        
        // Add previous message logs for multi-turn coherence
        history.forEach { msg ->
            contentList.add(
                Content(
                    role = if (msg.role == "model") "model" else "user",
                    parts = listOf(Part(text = msg.text))
                )
            )
        }

        // Add current user prompt
        contentList.add(
            Content(
                role = "user",
                parts = listOf(Part(text = userPrompt))
            )
        )

        // Only pay for Google Search grounding when the question actually needs live data
        // (mandi rates, weather, subsidies). Grounding is billed per query (~$14/1000), so
        // enabling it on every turn — including questions already answered by the local
        // knowledge base — wastes 70-80% of the search spend. See needsLiveSearch().
        val liveTools = if (needsLiveSearch(userPrompt)) listOf(Tool(googleSearch = emptyMap())) else null

        val request = GenerateContentRequest(
            contents = contentList,
            generationConfig = GenerationConfig(
                temperature = 0.5f,
                topP = 0.95f
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemDirective))
            ),
            tools = liveTools
        )

        val response = RetrofitClient.service.generateContent(apiKey, request)
        val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
        val cleanText = text?.replace("*", "") ?: "معذرت، کوئی جواب موصول نہیں ہوا۔"
        // Verified when the answer was grounded in the local knowledge base.
        QueryResult(cleanText, usedVerified = matches.isNotEmpty())
    }

    /** Result of an advisory query: the answer text and whether it was grounded in verified data. */
    private data class QueryResult(val text: String, val usedVerified: Boolean)

    override fun onCleared() {
        super.onCleared()
        stopLiveSession()
    }
}

sealed interface FarmersUiState {
    object Idle : FarmersUiState
    object Loading : FarmersUiState
    data class Success(val response: String) : FarmersUiState
    data class Error(val message: String) : FarmersUiState
}

enum class LanguageOption(
    val displayName: String,
    val bcp47Code: String,
    val sampleQuestion: String,
    val audioLocale: String,
    // Whether to offer this option in the picker. Languages without a working speech
    // voice are hidden until a real TTS/STT is available — a Pashto answer read by an
    // Urdu engine is unintelligible, which is worse than not offering it (defect D7).
    val selectable: Boolean = true,
    // Honest disclosure shown in the picker when text and voice differ.
    val voiceNote: String = ""
) {
    URDU("اردو (Urdu)", "ur-PK", "گندم کی پیداوار بڑھانے کا طریقہ کیا ہے؟", "ur"),
    // Text in Punjabi/Seraiki, but voiced by the Urdu engine — labelled as such.
    PUNJABI("پنجابی (Punjabi)", "ur-PK", "پانی لانے دا صحیح وقت کڑا اے؟", "ur",
        voiceNote = "پنجابی متن، اردو آواز"),
    SERAIKI("سرائیکی (Seraiki)", "ur-PK", "کپاہ کوں کیڑے توں بچاونڑ دا طریقہ ڈساؤ۔", "ur",
        voiceNote = "سرائیکی متن، اردو آواز"),
    ENGLISH("انگریزی (English)", "en-PK", "How to deal with cotton whiteflies pest attack?", "en"),
    // Hidden until a real regional voice exists — no working STT/TTS today.
    SINDHI("سنڌي (Sindhi)", "ur-PK", "ڪڻڪ جي پوکيءَ لاءِ بھترين وقت ڪھڙو آھي؟", "ur",
        selectable = false),
    PASHTO("پښتو (Pashto)", "ur-PK", "د غنمو د فصل دپاره کوه ښه ده؟", "ur",
        selectable = false),
    BALOCHI("بلوچی (Balochi)", "ur-PK", "مئے زمین ءَ پہ آپ جنگ ءَ چے کنگی انت؟", "ur",
        selectable = false);

    companion object {
        /** Languages currently offered in the picker (those with a working voice). */
        val selectableOptions: List<LanguageOption> get() = entries.filter { it.selectable }
    }
}
