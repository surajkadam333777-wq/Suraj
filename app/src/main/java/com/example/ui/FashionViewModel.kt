package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.api.GeminiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: Sender,
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

enum class Sender {
    USER,
    AURELIA
}

sealed interface ConsultationUiState {
    object Idle : ConsultationUiState
    object Loading : ConsultationUiState
    data class Success(val response: String) : ConsultationUiState
    data class Error(val error: String) : ConsultationUiState
}

class FashionViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = WardrobeRepository(db.wardrobeDao())

    // --- Wardrobe State ---
    private val _wardrobeItems = MutableStateFlow<List<WardrobeItem>>(emptyList())
    val wardrobeItems: StateFlow<List<WardrobeItem>> = _wardrobeItems.asStateFlow()

    // --- Saved Looks State ---
    private val _savedLooks = MutableStateFlow<List<SavedLook>>(emptyList())
    val savedLooks: StateFlow<List<SavedLook>> = _savedLooks.asStateFlow()

    // --- Chat State ---
    private val _chatHistory = MutableStateFlow<List<ChatMessage>>(listOf(
        ChatMessage(
            sender = Sender.AURELIA,
            text = "Welcome to the Aurelia Atelier. I am Aurelia, your style consultant. How may I assist you today in curating an enduring, elegant capsule layout?"
        )
    ))
    val chatHistory: StateFlow<List<ChatMessage>> = _chatHistory.asStateFlow()

    private val _consultationState = MutableStateFlow<ConsultationUiState>(ConsultationUiState.Idle)
    val consultationState: StateFlow<ConsultationUiState> = _consultationState.asStateFlow()

    // Key validation check state (to display nice warning in the UI if key is missing)
    private val _isKeyConfigured = MutableStateFlow(true)
    val isKeyConfigured: StateFlow<Boolean> = _isKeyConfigured.asStateFlow()

    init {
        // Collect Wardrobe Items from DB
        viewModelScope.launch {
            repository.allWardrobeItems.collectLatest {
                _wardrobeItems.value = it
            }
        }

        // Collect Saved Looks from DB
        viewModelScope.launch {
            repository.allSavedLooks.collectLatest {
                _savedLooks.value = it
            }
        }
    }

    // --- Wardrobe Actions ---
    fun addWardrobeItem(name: String, category: String, color: String, material: String, notes: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val newItem = WardrobeItem(
                name = name,
                category = category,
                color = color,
                material = material,
                notes = notes,
                isDefault = false
            )
            repository.insertWardrobeItem(newItem)
        }
    }

    fun removeWardrobeItem(item: WardrobeItem) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteWardrobeItem(item)
        }
    }

    fun restoreDefaultWardrobe() {
        viewModelScope.launch(Dispatchers.IO) {
            // Delete all and insert default ones
            for (item in _wardrobeItems.value) {
                repository.deleteWardrobeItem(item)
            }
            for (item in FashionConstants.DefaultItems) {
                repository.insertWardrobeItem(item)
            }
        }
    }

    // --- Saved Looks Actions ---
    fun saveLook(title: String, description: String, items: List<String>, categoryHint: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val look = SavedLook(
                title = title,
                description = description,
                items = items.joinToString(", "),
                categoryHint = categoryHint
            )
            repository.insertSavedLook(look)
        }
    }

    fun removeSavedLook(look: SavedLook) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteSavedLook(look)
        }
    }

    // --- Consultation Actions ---
    fun sendMessageToAurelia(messageText: String) {
        if (messageText.isBlank()) return

        // Append user's message
        val userMsg = ChatMessage(sender = Sender.USER, text = messageText)
        _chatHistory.value = _chatHistory.value + userMsg
        _consultationState.value = ConsultationUiState.Loading

        viewModelScope.launch {
            val systemInstruction = getAureliaSystemInstruction()
            val prompt = buildConsultationPrompt(messageText)

            val apiResponse = GeminiClient.consult(
                prompt = prompt,
                systemInstruction = systemInstruction
            )

            if (apiResponse.startsWith("CONFIG_ERROR:")) {
                _isKeyConfigured.value = false
                _consultationState.value = ConsultationUiState.Error("API Key is missing. Please add Your GEMINI_API_KEY inside the Secrets panel.")
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    sender = Sender.AURELIA,
                    text = "I apologize, but my style engine requires configuration. Please ensure the GEMINI_API_KEY is configured securely."
                )
            } else if (apiResponse.startsWith("ERROR:")) {
                _consultationState.value = ConsultationUiState.Error(apiResponse)
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    sender = Sender.AURELIA,
                    text = "I encountered an error connecting to my style archives. Pardon the inconvenience, please check your network connection."
                )
            } else {
                _isKeyConfigured.value = true
                _consultationState.value = ConsultationUiState.Success(apiResponse)
                _chatHistory.value = _chatHistory.value + ChatMessage(
                    sender = Sender.AURELIA,
                    text = apiResponse
                )
            }
        }
    }

    fun clearChat() {
        _chatHistory.value = listOf(
            ChatMessage(
                sender = Sender.AURELIA,
                text = "Welcome back. How may I advise your wardrobe choices today?"
            )
        )
        _consultationState.value = ConsultationUiState.Idle
    }

    // --- Gemini Look Generation Engine ---
    fun generateLookFromCurrentWardrobe(categoryHint: String, onLookGenerated: (title: String, desc: String, items: List<String>) -> Unit) {
        _consultationState.value = ConsultationUiState.Loading
        viewModelScope.launch {
            val itemsInWardrobe = _wardrobeItems.value
            val wardrobeDescription = if (itemsInWardrobe.isEmpty()) {
                "The user's wardrobe is empty except for basic classic garments."
            } else {
                itemsInWardrobe.joinToString("; ") { "${it.name} (${it.category} in ${it.color}, ${it.material})" }
            }

            val prompt = """
                Formulate a single elegant minimalist outfit look for the category: "$categoryHint".
                You must compose this look USING ONLY the client's current wardrobe items. If they have insufficient items to make an ensemble, recommend the addition of classic starter garments from our catalog.
                
                Current Wardrobe available:
                $wardrobeDescription
                
                Strict formatting: Respond ONLY with a valid compact JSON block. Do not wrap in markdown quotes. The JSON structure must match this exactly:
                {
                  "title": "Minimalist Workwear / Summer Promenade / etc",
                  "description": "Sophisticated styling description mentioning draping, texture, and why these pieces harmonize flawlessly. Use a premium, understated, polite tone.",
                  "items": ["Item Name 1", "Item Name 2", "Item Name 3"]
                }
            """.trimIndent()

            val systemInstruction = getAureliaSystemInstruction() + "\nYou are a structure generator. You must respond 100% with a valid outer level JSON object and nothing else."

            val apiResponse = GeminiClient.consult(
                prompt = prompt,
                systemInstruction = systemInstruction,
                isJson = true
            )

            try {
                // Parse simple JSON or fall back
                val cleanResponse = apiResponse.trim().removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                val moshi = com.squareup.moshi.Moshi.Builder()
                    .addLast(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory())
                    .build()
                val adapter = moshi.adapter(GeneratedLookJson::class.java)
                val parsed = adapter.fromJson(cleanResponse)
                if (parsed != null) {
                    _consultationState.value = ConsultationUiState.Idle
                    onLookGenerated(parsed.title, parsed.description, parsed.items)
                } else {
                    throw Exception("Parsing returned null object")
                }
            } catch (e: Exception) {
                // Return safe default look based on available items
                val defaultTitle = "Atelier Style Layout - $categoryHint"
                val defaultDesc = "A refined composition of tailored essentials chosen for effortless elegance and cohesive structure."
                val defaultItemsChosen = itemsInWardrobe.take(3).map { it.name }.ifEmpty { listOf("Classic White Tee", "Charcoal Wool Trousers") }
                _consultationState.value = ConsultationUiState.Idle
                onLookGenerated(defaultTitle, defaultDesc, defaultItemsChosen)
            }
        }
    }

    // --- Private Helper Prompts & Instructions ---
    private fun getAureliaSystemInstruction(): String {
        return """
            You are Aurelia, the elegant, official AI Style expert for a premium, minimalist fashion house.
            The house's absolute design guidelines are:
            1. ONLY recommend clothing with clean cuts, solid patterns, and timeless colors: Black, White, Off-White/Beige, Navy Blue, Charcoal Grey, and Olive Green. Never recommend busy prints, shiny buttons, neons, logos, or flashy styling.
            2. Speak in a highly sophisticated, calm, extremely polite, helpful, and premium brand consultant persona. Be formal, objective, and appreciative of classic silhouettes and textures.
            3. Emphasize versatility, capsule layout theory (doing more with fewer, higher quality products), and the architectural beauty of linen, high-weight cotton, merino wool, cashmere, and clean leather.
            4. Keep responses concise, tailored, and beautifully expressed. Avoid excessive exclamation marks, self-praise, or sales pitches. Treat the client with noble respect.
        """.trimIndent()
    }

    private fun buildConsultationPrompt(userMessage: String): String {
        // Enforce state context
        val currentWardrobeList = _wardrobeItems.value.joinToString(", ") { "${it.name} (${it.color})" }
        return """
            Client asks: "$userMessage"
            
            Context of client's current wardrobe items:
            [$currentWardrobeList]
            
            Respond politely and helpfully matching Aurelia's brand requirements. Keep your recommendation focused on wardrobe streamlining, solid clean colors, and versatile styling layers.
        """.trimIndent()
    }
}

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class GeneratedLookJson(
    val title: String,
    val description: String,
    val items: List<String>
)
