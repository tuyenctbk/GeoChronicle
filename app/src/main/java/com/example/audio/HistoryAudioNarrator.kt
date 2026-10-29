package com.example.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.model.HistoricalPlace
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

sealed interface AudioNarrationState {
    data object Idle : AudioNarrationState
    data class Speaking(
        val placeId: Long,
        val title: String,
        val text: String,
        val speechRate: Float = 1.0f,
        val isPaused: Boolean = false
    ) : AudioNarrationState
    data class Error(val message: String) : AudioNarrationState
}

class HistoryAudioNarrator(context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = TextToSpeech(context.applicationContext, this)
    private var isInitialized = false

    private val _state = MutableStateFlow<AudioNarrationState>(AudioNarrationState.Idle)
    val state: StateFlow<AudioNarrationState> = _state.asStateFlow()

    private var currentRate = 1.0f
    private var activePlace: HistoricalPlace? = null
    private var currentLangCode: String = "en"

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts?.let { engine ->
                applyLocale(engine, currentLangCode)
                engine.setSpeechRate(currentRate)
                engine.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        val current = activePlace
                        if (current != null) {
                            _state.value = AudioNarrationState.Speaking(
                                placeId = current.pageId,
                                title = current.title,
                                text = current.extract.ifBlank { current.description },
                                speechRate = currentRate,
                                isPaused = false
                            )
                        }
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.value = AudioNarrationState.Idle
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = AudioNarrationState.Idle
                    }
                })
                isInitialized = true
            }
        } else {
            _state.value = AudioNarrationState.Error("Text-to-speech engine failed to initialize")
        }
    }

    fun setLanguage(langCode: String) {
        currentLangCode = langCode
        tts?.let { engine ->
            applyLocale(engine, langCode)
        }
    }

    private fun applyLocale(engine: TextToSpeech, langCode: String) {
        val locale = when (langCode) {
            "en" -> Locale.US
            "es" -> Locale("es")
            "fr" -> Locale.FRENCH
            "de" -> Locale.GERMAN
            "it" -> Locale.ITALIAN
            "ja" -> Locale.JAPANESE
            "zh" -> Locale.SIMPLIFIED_CHINESE
            "pt" -> Locale("pt")
            "vi" -> Locale("vi")
            "ar" -> Locale("ar")
            else -> Locale.getDefault()
        }
        val result = engine.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            engine.language = Locale.getDefault()
        }
    }

    fun speak(place: HistoricalPlace) {
        if (!isInitialized) return
        stop()
        activePlace = place

        val speechText = buildString {
            append(place.title)
            append(". ")
            if (place.description.isNotBlank()) {
                append(place.description)
                append(". ")
            }
            if (place.extract.isNotBlank()) {
                append(place.extract)
            }
        }

        _state.value = AudioNarrationState.Speaking(
            placeId = place.pageId,
            title = place.title,
            text = speechText,
            speechRate = currentRate,
            isPaused = false
        )

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "place_${place.pageId}")
        }
        tts?.speak(speechText, TextToSpeech.QUEUE_FLUSH, params, "place_${place.pageId}")
    }

    fun stop() {
        tts?.stop()
        activePlace = null
        _state.value = AudioNarrationState.Idle
    }

    fun togglePlayPause(place: HistoricalPlace) {
        val current = _state.value
        if (current is AudioNarrationState.Speaking && current.placeId == place.pageId) {
            if (current.isPaused) {
                speak(place)
            } else {
                tts?.stop()
                _state.value = current.copy(isPaused = true)
            }
        } else {
            speak(place)
        }
    }

    fun cycleSpeechRate(): Float {
        currentRate = when (currentRate) {
            1.0f -> 1.25f
            1.25f -> 1.5f
            1.5f -> 0.85f
            else -> 1.0f
        }
        tts?.setSpeechRate(currentRate)
        val current = _state.value
        if (current is AudioNarrationState.Speaking) {
            _state.value = current.copy(speechRate = currentRate)
        }
        return currentRate
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
        tts = null
    }
}
