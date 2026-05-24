package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FashionConstants
import com.example.data.SavedLook
import com.example.data.WardrobeItem
import com.example.ui.theme.*

enum class AtelierTab {
    CONSULTANT,
    WARDROBE,
    LOOKBOOK
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FashionAtelierScreen(
    viewModel: FashionViewModel,
    modifier: Modifier = Modifier
) {
    var activeTab by remember { mutableStateOf(AtelierTab.CONSULTANT) }
    
    val wardrobeItems by viewModel.wardrobeItems.collectAsState(initial = emptyList())
    val savedLooks by viewModel.savedLooks.collectAsState(initial = emptyList())
    val chatHistory by viewModel.chatHistory.collectAsState(initial = emptyList())
    val consultationState by viewModel.consultationState.collectAsState()
    val isKeyConfigured by viewModel.isKeyConfigured.collectAsState()

    var showAddItemDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            AtelierHeader()
        },
        bottomBar = {
            AtelierPillNavigation(
                activeTab = activeTab,
                onTabSelected = { activeTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = activeTab,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "AtelierTabAnimation"
            ) { tab ->
                when (tab) {
                    AtelierTab.CONSULTANT -> {
                        AtelierConsultantView(
                            chatHistory = chatHistory,
                            consultationState = consultationState,
                            isKeyConfigured = isKeyConfigured,
                            onSendMessage = { viewModel.sendMessageToAurelia(it) },
                            onClearChat = { viewModel.clearChat() }
                        )
                    }
                    AtelierTab.WARDROBE -> {
                        AtelierWardrobeView(
                            wardrobeItems = wardrobeItems,
                            onRemoveItem = { viewModel.removeWardrobeItem(it) },
                            onRestoreDefaults = { viewModel.restoreDefaultWardrobe() },
                            onAddItemClick = { showAddItemDialog = true }
                        )
                    }
                    AtelierTab.LOOKBOOK -> {
                        AtelierLookbookView(
                            savedLooks = savedLooks,
                            wardrobeItems = wardrobeItems,
                            consultationState = consultationState,
                            isKeyConfigured = isKeyConfigured,
                            onDeleteLook = { viewModel.removeSavedLook(it) },
                            onGenerateNewLook = { category ->
                                viewModel.generateLookFromCurrentWardrobe(category) { title, desc, items ->
                                    viewModel.saveLook(title, desc, items, category)
                                }
                            }
                        )
                    }
                }
            }

            // Bottom gradient padding to prevent list content from touching edge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .align(Alignment.BottomCenter)
                    .background(MaterialTheme.colorScheme.background)
            )
        }
    }

    if (showAddItemDialog) {
        AddWardrobeItemDialog(
            onDismiss = { showAddItemDialog = false },
            onConfirm = { name, category, color, material, notes ->
                viewModel.addWardrobeItem(name, category, color, material, notes)
                showAddItemDialog = false
            }
        )
    }
}

@Composable
fun AtelierHeader() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Official Consultant",
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = BrandSage,
                        letterSpacing = 4.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Aurelia",
                    style = MaterialTheme.typography.headlineLarge.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Italic,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandObsidian
                    )
                )
            }
            // User Avatar matching Natural Tones layout
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .border(1.dp, BrandSand, CircleShape)
                    .background(Color.White)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "User Profile",
                    tint = BrandSage,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
        Divider(
            color = BrandBorder,
            thickness = 1.dp,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

@Composable
fun AtelierPillNavigation(
    activeTab: AtelierTab,
    onTabSelected: (AtelierTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(100.dp))
                .border(1.dp, Color.White, RoundedCornerShape(100.dp))
                .background(Color.White.copy(alpha = 0.9f))
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            AtelierNavPill(
                icon = Icons.Default.Home,
                label = "Consultant",
                isSelected = activeTab == AtelierTab.CONSULTANT,
                onClick = { onTabSelected(AtelierTab.CONSULTANT) }
            )
            AtelierNavPill(
                icon = Icons.Default.Menu,
                label = "Wardrobe",
                isSelected = activeTab == AtelierTab.WARDROBE,
                onClick = { onTabSelected(AtelierTab.WARDROBE) }
            )
            AtelierNavPill(
                icon = Icons.Default.Star,
                label = "Lookbook",
                isSelected = activeTab == AtelierTab.LOOKBOOK,
                onClick = { onTabSelected(AtelierTab.LOOKBOOK) }
            )
        }
    }
}

@Composable
fun AtelierNavPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) BrandSage else Color.Transparent
    val contentColor = if (isSelected) Color.White else BrandSage

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(100.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(20.dp)
        )
        if (isSelected) {
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = contentColor
                )
            )
        }
    }
}

// --- VIEW 1: CONSULTANT CHAT ---

@Composable
fun AtelierConsultantView(
    chatHistory: List<ChatMessage>,
    consultationState: ConsultationUiState,
    isKeyConfigured: Boolean,
    onSendMessage: (String) -> Unit,
    onClearChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val keyboardController = LocalSoftwareKeyboardController.current

    val suggestions = listOf(
        "Build a timeless look with Navy and Charcoal",
        "Suggest a versatile capsule for city travels",
        "How should I layer olive and off-white beige?"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        if (!isKeyConfigured) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Key Error",
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "GEMINI_API_KEY is not configured. Please use the build platform's Secrets Panel to set GEMINI_API_KEY.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }

        // Chat Message Log
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                // Aurelia Welcome Statement Panel
                AtelierWelcomePanel()
            }

            items(chatHistory) { msg ->
                if (msg.sender == Sender.AURELIA && msg.text.contains("Atelier. I am Aurelia")) {
                    // Suppress duplicate showing of initial welcome card since we render the banner
                } else {
                    ChatBubble(message = msg)
                }
            }

            if (consultationState is ConsultationUiState.Loading) {
                item {
                    AtelierConsultingTypingBubble()
                }
            }
        }

        // Suggestions deck and input controls pinned at bottom of the content column
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.background)
                .padding(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (chatHistory.size <= 2) {
                // Show recommendations buttons to trigger conversations
                Text(
                    text = "Aesthetic Prompts:",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandSage
                    ),
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    suggestions.forEach { prompt ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, BrandBorder, RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .clickable {
                                    onSendMessage(prompt)
                                }
                                .padding(10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = prompt,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontFamily = FontFamily.Serif,
                                    fontStyle = FontStyle.Italic,
                                    fontSize = 11.sp,
                                    color = BrandObsidian
                                )
                            )
                        }
                    }
                }
            }

            // Input Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = {
                        Text(
                            text = "Consult Aurelia...",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = BrandCharcoal.copy(alpha = 0.5f)
                            )
                        )
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        unfocusedBorderColor = BrandBorder,
                        focusedBorderColor = BrandSage
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                    keyboardActions = KeyboardActions(onSend = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    })
                )

                IconButton(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onSendMessage(textInput)
                            textInput = ""
                            keyboardController?.hide()
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(BrandSage, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Consultation Query",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                if (chatHistory.size > 1) {
                    IconButton(
                        onClick = onClearChat,
                        modifier = Modifier
                            .size(48.dp)
                            .border(1.dp, BrandBorder, CircleShape)
                            .background(Color.White)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear Session Catalog",
                            tint = BrandObsidian,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AtelierWelcomePanel() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(20.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Star,
                    contentDescription = "Atelier Core Style",
                    tint = BrandSage,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "TAILORED AESTHETIC DIRECTIVE",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = BrandSage,
                        letterSpacing = 1.sp
                    )
                )
            }
            Text(
                text = "\"For your seasonal capsule layout, I propose an architectural layering of solid shades with fine tactile contrasts—a testament to timeless grace and absolute minimalism.\"",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontStyle = FontStyle.Italic,
                    fontSize = 17.sp,
                    lineHeight = 24.sp,
                    color = BrandObsidian
                )
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == Sender.USER
    val alignment = if (isUser) Alignment.End else Alignment.Start

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 290.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .background(if (isUser) BrandSage else Color.White)
                .border(
                    width = 1.dp,
                    color = if (isUser) Color.Transparent else BrandBorder,
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp,
                        bottomStart = if (isUser) 20.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 20.dp
                    )
                )
                .padding(14.dp)
        ) {
            Text(
                text = message.text,
                style = if (isUser) {
                    MaterialTheme.typography.bodyMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontStyle = FontStyle.Normal,
                        color = BrandObsidian,
                        lineHeight = 20.sp
                    )
                }
            )
        }
    }
}

@Composable
fun AtelierConsultingTypingBubble() {
    Row(
        modifier = Modifier
            .padding(top = 4.dp, bottom = 4.dp)
            .background(Color.White, RoundedCornerShape(20.dp))
            .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "Consulting style records...",
            style = MaterialTheme.typography.bodySmall.copy(
                fontStyle = FontStyle.Italic,
                color = BrandSage
            )
        )
        LinearProgressIndicator(
            color = BrandSage,
            trackColor = BrandBorder,
            modifier = Modifier
                .width(40.dp)
                .height(2.dp)
        )
    }
}


// --- VIEW 2: CAPSULE WARDROBE ---

@Composable
fun AtelierWardrobeView(
    wardrobeItems: List<WardrobeItem>,
    onRemoveItem: (WardrobeItem) -> Unit,
    onRestoreDefaults: () -> Unit,
    onAddItemClick: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All") + FashionConstants.WardrobeCategories

    val filteredList = if (selectedCategory == "All") {
        wardrobeItems
    } else {
        wardrobeItems.filter { it.category == selectedCategory }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Top Action Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your Capsule Wardrobe",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandObsidian
                )
            )
            Text(
                text = "${wardrobeItems.size} Pieces",
                style = MaterialTheme.typography.labelMedium.copy(
                    color = BrandSage,
                    fontWeight = FontWeight.Bold
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Horizontal Category Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { category ->
                val isSelected = selectedCategory == category
                val outlineColor = if (isSelected) BrandSage else BrandBorder
                val cardColor = if (isSelected) BrandSage else Color.White
                val textColor = if (isSelected) Color.White else BrandCharcoal

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, outlineColor, RoundedCornerShape(12.dp))
                        .background(cardColor)
                        .clickable { selectedCategory = category }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = category,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Grid-List view of Capsule Items
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Empty",
                            tint = BrandSand,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            text = "No pieces categorized here yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = BrandCharcoal.copy(alpha = 0.6f)
                            )
                        )
                        Button(
                            onClick = onRestoreDefaults,
                            colors = ButtonDefaults.buttonColors(containerColor = BrandSage)
                        ) {
                            Text("Restock Studio Originals")
                        }
                    }
                }
            } else {
                items(filteredList) { item ->
                    WardrobeItemRow(item = item, onRemove = { onRemoveItem(item) })
                }
            }
        }

        // Pinned Bottom Add Button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onAddItemClick,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = BrandSage),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom item",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Introduce Capsule Piece")
            }

            IconButton(
                onClick = onRestoreDefaults,
                modifier = Modifier
                    .size(48.dp)
                    .border(1.dp, BrandBorder, CircleShape)
                    .background(Color.White)
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restore Default Closet",
                    tint = BrandObsidian
                )
            }
        }
    }
}

@Composable
fun WardrobeItemRow(
    item: WardrobeItem,
    onRemove: () -> Unit
) {
    // Dynamic Color Palette Swatch Lookup
    val swatchColor = when (item.color.lowercase().trim()) {
        "black" -> Color(0xFF1E1E1E)
        "white" -> Color(0xFFFAFAFA)
        "off-white/beige" -> Color(0xFFEDE9E1)
        "navy blue" -> Color(0xFF1F2F46)
        "charcoal grey" -> Color(0xFF4D4F53)
        "olive green" -> Color(0xFF5A5C43)
        else -> Color(0xFF8A8276) // Default
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, BrandBorder, RoundedCornerShape(20.dp))
            .background(Color.White)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Visual Swatch indicator
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(swatchColor)
                    .border(1.dp, BrandSand, CircleShape)
            )

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = BrandObsidian
                        )
                    )
                    if (item.isDefault) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .background(BrandLinenMatte, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "CORE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 7.sp,
                                    color = BrandSage,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
                Text(
                    text = "${item.category}  •  ${item.color}  •  ${item.material}",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandCharcoal.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                )
                if (item.notes.isNotBlank()) {
                    Text(
                        text = item.notes,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = BrandCharcoal.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            IconButton(
                onClick = onRemove,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Remove Item",
                    tint = BrandSand,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}


// --- VIEW 3: SAVED LOOKS (LOOKBOOK) ---

@Composable
fun AtelierLookbookView(
    savedLooks: List<SavedLook>,
    wardrobeItems: List<WardrobeItem>,
    consultationState: ConsultationUiState,
    isKeyConfigured: Boolean,
    onDeleteLook: (SavedLook) -> Unit,
    onGenerateNewLook: (String) -> Unit
) {
    var activeCategorySelection by remember { mutableStateOf("Smart Casual") }
    val occasionChoices = listOf("Smart Casual", "Minimalist Lounge", "Gala Reception", "Business Tailor")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        // Style engine card at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .border(2.dp, BrandSand, RoundedCornerShape(24.dp))
                .background(Color.White)
                .padding(20.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Aesthetic Synthesis Engine",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = FontFamily.Serif,
                        fontWeight = FontWeight.SemiBold,
                        color = BrandObsidian
                    )
                )
                Text(
                    text = "Aurelia will compose a tailored look utilizing only your actual wardrobe elements.",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = BrandCharcoal.copy(alpha = 0.7f),
                        fontStyle = FontStyle.Italic
                    )
                )

                // occasion tabs inside synthesis panel
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    occasionChoices.take(2).forEach { choice ->
                        val isSel = activeCategorySelection == choice
                        OutlinedButton(
                            onClick = { activeCategorySelection = choice },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSel) BrandSage else BrandBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSel) BrandLinenMatte else Color.Transparent
                            )
                        ) {
                            Text(
                                text = choice,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) BrandSage else BrandCharcoal
                                )
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    occasionChoices.drop(2).forEach { choice ->
                        val isSel = activeCategorySelection == choice
                        OutlinedButton(
                            onClick = { activeCategorySelection = choice },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, if (isSel) BrandSage else BrandBorder),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSel) BrandLinenMatte else Color.Transparent
                            )
                        ) {
                            Text(
                                text = choice,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) BrandSage else BrandCharcoal
                                )
                            )
                        }
                    }
                }

                // Call Action Button
                val isSynthesizing = consultationState is ConsultationUiState.Loading
                Button(
                    onClick = { onGenerateNewLook(activeCategorySelection) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = BrandSage),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSynthesizing && wardrobeItems.isNotEmpty()
                ) {
                    if (isSynthesizing) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Drafting Combination...")
                    } else {
                        Text("Generate Timeless Look")
                    }
                }

                if (wardrobeItems.isEmpty()) {
                    Text(
                        text = "Kindly add pieces to your closet before generating looks.",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // History Saved List Header
        Text(
            text = "Timeless Pairings Library",
            style = MaterialTheme.typography.titleMedium.copy(
                fontFamily = FontFamily.Serif,
                fontWeight = FontWeight.SemiBold,
                color = BrandObsidian
            ),
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Saved List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            if (savedLooks.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Empty Library",
                            tint = BrandSand,
                            modifier = Modifier.size(32.dp)
                        )
                        Text(
                            text = "No styling layouts registered yet.",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontStyle = FontStyle.Italic,
                                color = BrandCharcoal.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            } else {
                items(savedLooks) { look ->
                    LookbookCard(look = look, onDelete = { onDeleteLook(look) })
                }
            }
        }
    }
}

@Composable
fun LookbookCard(
    look: SavedLook,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .border(1.dp, BrandBorder, RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(18.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .background(BrandLinenMatte, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = look.categoryHint.uppercase(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandSage
                        )
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Erase Look",
                        tint = BrandSand,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Text(
                text = look.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.SemiBold,
                    color = BrandObsidian
                )
            )

            Text(
                text = look.description,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontStyle = FontStyle.Italic,
                    color = BrandCharcoal.copy(alpha = 0.8f)
                )
            )

            Divider(color = BrandBorder, modifier = Modifier.padding(vertical = 4.dp))

            // Split items list and draw nice circular capsules
            val parts = look.items.split(",")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                parts.forEach { part ->
                    val trimmed = part.trim()
                    if (trimmed.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .border(1.dp, BrandBorder, RoundedCornerShape(8.dp))
                                .background(BrandLinen)
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = trimmed,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    color = BrandObsidian
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}


// --- DIALOGS & SHEET CONTROL ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWardrobeItemDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, category: String, color: String, material: String, notes: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(FashionConstants.WardrobeCategories.first()) }
    var selectedColor by remember { mutableStateOf(FashionConstants.ClassicColors.first()) }
    var material by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var expandedCat by remember { mutableStateOf(false) }
    var expandedColor by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Add Minimalist Piece",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontFamily = FontFamily.Serif,
                    fontWeight = FontWeight.Bold,
                    color = BrandObsidian
                )
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Garment Name Example: Wool Knit Sweater") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedCat = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Category: $selectedCategory",
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrandCharcoal)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedCat,
                        onDismissRequest = { expandedCat = false }
                    ) {
                        FashionConstants.WardrobeCategories.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    selectedCategory = cat
                                    expandedCat = false
                                }
                            )
                        }
                    }
                }

                // Color Dropdown
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { expandedColor = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Color: $selectedColor",
                            style = MaterialTheme.typography.bodyMedium.copy(color = BrandCharcoal)
                        )
                    }
                    DropdownMenu(
                        expanded = expandedColor,
                        onDismissRequest = { expandedColor = false }
                    ) {
                        FashionConstants.ClassicColors.forEach { col ->
                            DropdownMenuItem(
                                text = { Text(col) },
                                onClick = {
                                    selectedColor = col
                                    expandedColor = false
                                }
                            )
                        }
                    }
                }

                // Material
                OutlinedTextField(
                    value = material,
                    onValueChange = { material = it },
                    label = { Text("Material Example: 100% Merino Wool, Heavy Canvas") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Styling remarks") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && material.isNotBlank()) {
                        onConfirm(name, selectedCategory, selectedColor, material, notes)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = BrandSage),
                enabled = name.isNotBlank() && material.isNotBlank()
            ) {
                Text("Incorporate")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = BrandObsidian)
            ) {
                Text("Cancel")
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}
