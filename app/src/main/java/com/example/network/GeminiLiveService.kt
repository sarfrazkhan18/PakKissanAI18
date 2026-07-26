package com.example.network

import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.viewmodel.LanguageOption
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

sealed interface LiveConnectionState {
    object Disconnected : LiveConnectionState
    object Connecting : LiveConnectionState
    object Connected : LiveConnectionState
    object SetupComplete : LiveConnectionState
    data class Error(val message: String) : LiveConnectionState
}

class GeminiLiveService {

    private val tag = "GeminiLiveService"
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val _connectionState = MutableStateFlow<LiveConnectionState>(LiveConnectionState.Disconnected)
    val connectionState: StateFlow<LiveConnectionState> = _connectionState.asStateFlow()

    private val _incomingText = MutableSharedFlow<String>(replay = 0)
    val incomingText: SharedFlow<String> = _incomingText.asSharedFlow()

    private val _incomingAudio = MutableSharedFlow<ByteArray>(replay = 0)
    val incomingAudio: SharedFlow<ByteArray> = _incomingAudio.asSharedFlow()

    private val serviceScope = CoroutineScope(Dispatchers.IO)

    fun connect(language: LanguageOption) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            _connectionState.value = LiveConnectionState.Error("API Key is missing or default placeholder.")
            return
        }

        disconnect()
        _connectionState.value = LiveConnectionState.Connecting

        // Using standard Google generativelanguage websocket live toolkit endpoint
        // Using v1alpha.LiveConnect as standard for Multimodal Live/Bidi API
        val url = "wss://generativelanguage.googleapis.com/ws/google.ai.generativetoolkit.v1alpha.Pipelines?key=$apiKey"
        Log.d(tag, "Connecting to: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(tag, "WebSocket Opened: $response")
                _connectionState.value = LiveConnectionState.Connected
                sendSetupMessage(language)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(tag, "Received live message: $text")
                parseIncomingMessage(text)
            }

            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(tag, "WebSocket closing: $code / $reason")
                _connectionState.value = LiveConnectionState.Disconnected
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(tag, "WebSocket error: ${t.message}", t)
                _connectionState.value = LiveConnectionState.Error(t.message ?: "نیٹ ورک کا رابطہ منقطع ہو گیا ہے۔")
            }
        })
    }

    fun disconnect() {
        webSocket?.close(1000, "User disconnected")
        webSocket = null
        _connectionState.value = LiveConnectionState.Disconnected
    }

    private fun sendSetupMessage(language: LanguageOption) {
        try {
            // Adaptive custom Kisaan advisor instructions matching developer requirements
            val instructions = """
                You are Kisaan Dost (کسان دوست), a live audio translator and crop advisor.
                You are speaking directly to a Pakistani farmer in a real-time voice call.
                
                Mandatory guidelines:
                1. Always speak and reply in standard: ${language.displayName}.
                   - Urdu: write in phonetically beautiful and extremely polite Urdu.
                   - Punjabi: write in Shahmukhi script but friendly terms.
                   - Sindhi, Pashto, Balochi, Seraiki: write in standard regional expressions.
                2. Keep sentences short, comforting, clear, and agricultural-focused because the user is listening to you live!
                3. Always present crop advice in clear bite-sized points. Use native terms: Acre (ایکڑ), Maund (من), Bori (بوری), Nahri Paani, desilaaj (دیسی علاج).
                4. Do not offer lengthy generic explanations. Speak like a helpful neighbor.
            """.trimIndent()

            val setupJson = JSONObject().apply {
                put("setup", JSONObject().apply {
                    // Using modern preview model supported for Real-time audio & video tasks
                    put("model", "models/gemini-2.5-flash")
                    put("generationConfig", JSONObject().apply {
                        // Requesting audio as primary live output modality
                        val modalities = JSONArray().apply {
                            put("AUDIO")
                        }
                        put("responseModalities", modalities)
                        put("speechConfig", JSONObject().apply {
                            put("voiceConfig", JSONObject().apply {
                                put("prebuiltVoiceConfig", JSONObject().apply {
                                    put("voiceName", "Kore") // Aoede, Charon, Fenrir, Kore, Puck, etc.
                                })
                            })
                        })
                    })
                    put("systemInstruction", JSONObject().apply {
                        put("parts", JSONArray().apply {
                            put(JSONObject().apply {
                                put("text", instructions)
                            })
                        })
                    })
                })
            }

            val payload = setupJson.toString()
            Log.d(tag, "Sending setup message: $payload")
            webSocket?.send(payload)
        } catch (e: Exception) {
            Log.e(tag, "Error building setup JSON: ${e.message}", e)
            _connectionState.value = LiveConnectionState.Error("تیاری کا پیغام بھیجنے میں خرابی ہوئی۔")
        }
    }

    fun sendTextMessage(text: String) {
        if (_connectionState.value != LiveConnectionState.SetupComplete && 
            _connectionState.value != LiveConnectionState.Connected) {
            Log.w(tag, "WebSocket is not connected / setup yet.")
            return
        }

        try {
            val contentJson = JSONObject().apply {
                put("clientContent", JSONObject().apply {
                    put("turns", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", text)
                                })
                            })
                        })
                    })
                    put("turnComplete", true)
                })
            }

            val payload = contentJson.toString()
            Log.d(tag, "Sending text clientContent payload: $payload")
            webSocket?.send(payload)
        } catch (e: Exception) {
            Log.e(tag, "Error building text clientContent: ${e.message}", e)
        }
    }

    private fun parseIncomingMessage(jsonText: String) {
        try {
            val root = JSONObject(jsonText)

            // 1. Handle Setup Complete status
            if (root.has("setupComplete")) {
                Log.i(tag, "Live API Setup Complete received!")
                _connectionState.value = LiveConnectionState.SetupComplete
                return
            }

            // 2. Handle Server Content responses
            if (root.has("serverContent")) {
                val serverContent = root.getJSONObject("serverContent")
                if (serverContent.has("modelTurn")) {
                    val modelTurn = serverContent.getJSONObject("modelTurn")
                    if (modelTurn.has("parts")) {
                        val parts = modelTurn.getJSONArray("parts")
                        for (i in 0 until parts.length()) {
                            val part = parts.getJSONObject(i)

                            // Streaming text chunk
                            if (part.has("text")) {
                                val textChunk = part.getString("text")
                                serviceScope.launch {
                                    _incomingText.emit(textChunk)
                                }
                            }

                            // Streaming audio chunk
                            if (part.has("inlineData")) {
                                val inlineData = part.getJSONObject("inlineData")
                                if (inlineData.has("data")) {
                                    val base64Data = inlineData.getString("data")
                                    val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
                                    serviceScope.launch {
                                        _incomingAudio.emit(decodedBytes)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e(tag, "Error parsing incoming websocket json: ${e.message}", e)
        }
    }
}
