package com.example.flashforge

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.json.JSONArray
import org.json.JSONObject

data class Flashcard(
    val id: Long,
    val question: String,
    val answer: String
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFF7F3FF)
                ) {
                    FlashcardApp(context = this)
                }
            }
        }
    }
}

@Composable
fun FlashcardApp(context: Context) {
    var showSplash by remember { mutableStateOf(true) }
    val flashcards = remember { mutableStateListOf<Flashcard>() }
    var currentIndex by remember { mutableIntStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }
    var questionText by remember { mutableStateOf("") }
    var answerText by remember { mutableStateOf("") }
    var editingId by remember { mutableStateOf<Long?>(null) }
    var message by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        val savedCards = loadFlashcards(context)
        flashcards.addAll(savedCards.ifEmpty { sampleFlashcards() })
        delay(3000)
        showSplash = false
    }

    if (showSplash) {
        SplashScreen()
        return
    }

    fun saveAll() {
        saveFlashcards(context, flashcards)
    }

    fun clearForm() {
        questionText = ""
        answerText = ""
        editingId = null
        message = ""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F3FF))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            HeaderSection()
        }

        item {
            if (flashcards.isNotEmpty()) {
                StudyCardSection(
                    card = flashcards[currentIndex],
                    currentIndex = currentIndex,
                    totalCards = flashcards.size,
                    showAnswer = showAnswer,
                    onToggleAnswer = { showAnswer = !showAnswer },
                    onPrevious = {
                        if (currentIndex > 0) {
                            currentIndex--
                            showAnswer = false
                        }
                    },
                    onNext = {
                        if (currentIndex < flashcards.lastIndex) {
                            currentIndex++
                            showAnswer = false
                        }
                    },
                    onEdit = {
                        val currentCard = flashcards[currentIndex]
                        questionText = currentCard.question
                        answerText = currentCard.answer
                        editingId = currentCard.id
                        message = "Editing current flashcard"
                    },
                    onDelete = {
                        flashcards.removeAt(currentIndex)
                        currentIndex = currentIndex.coerceAtMost((flashcards.size - 1).coerceAtLeast(0))
                        showAnswer = false
                        saveAll()
                        clearForm()
                    }
                )
            } else {
                EmptyDeckCard()
            }
        }

        item {
            EditorSection(
                questionText = questionText,
                answerText = answerText,
                editingId = editingId,
                message = message,
                onQuestionChange = { questionText = it },
                onAnswerChange = { answerText = it },
                onClear = { clearForm() },
                onSave = {
                    val question = questionText.trim()
                    val answer = answerText.trim()

                    if (question.isBlank() || answer.isBlank()) {
                        message = "Please enter both question and answer."
                        return@EditorSection
                    }

                    val idToEdit = editingId
                    if (idToEdit == null) {
                        flashcards.add(
                            Flashcard(
                                id = System.currentTimeMillis(),
                                question = question,
                                answer = answer
                            )
                        )
                        currentIndex = flashcards.lastIndex
                    } else {
                        val index = flashcards.indexOfFirst { it.id == idToEdit }
                        if (index != -1) {
                            flashcards[index] = Flashcard(idToEdit, question, answer)
                            currentIndex = index
                        }
                    }

                    showAnswer = false
                    saveAll()
                    clearForm()
                }
            )
        }

        item {
            DeckListSection(
                flashcards = flashcards,
                currentIndex = currentIndex,
                onSelectCard = { selectedIndex ->
                    currentIndex = selectedIndex
                    showAnswer = false
                }
            )
        }
    }
}

@Composable
fun SplashScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.linearGradient(
                    listOf(Color(0xFF2D1457), Color(0xFF6D28D9), Color(0xFFFFF7ED))
                )
            )
            .padding(28.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "DEVELOPED BY",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFEDE7F6)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "KUSUMANJALI DARA",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = "FlashForge - Kotlin Flashcard Quiz App",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFFFFF7ED)
        )
        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = "Connect with me",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Spacer(modifier = Modifier.height(12.dp))
        SocialCard(label = "Instagram", value = "@kusumanjalidara9")
        Spacer(modifier = Modifier.height(8.dp))
        SocialCard(label = "LinkedIn", value = "linkedin.com/in/kusumanjalidara")
        Spacer(modifier = Modifier.height(8.dp))
        SocialCard(label = "GitHub", value = "github.com/KUSUMANJALI-DARA")
    }
}

@Composable
fun SocialCard(label: String, value: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.18f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFF7ED)
            )
            Text(
                text = value,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                textAlign = TextAlign.Start
            )
        }
    }
}

@Composable
fun HeaderSection() {
    Column {
        Text(
            text = "FlashForge",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D1457)
        )
        Text(
            text = "Kotlin Flashcard Quiz App",
            style = MaterialTheme.typography.bodyLarge,
            color = Color(0xFF6D5A8D)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Developed by KUSUMANJALI DARA",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color(0xFF7C3AED)
        )
    }
}

@Composable
fun StudyCardSection(
    card: Flashcard,
    currentIndex: Int,
    totalCards: Int,
    showAnswer: Boolean,
    onToggleAnswer: () -> Unit,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Card ${currentIndex + 1} of $totalCards",
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF6D5A8D)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFEEE7FF), Color(0xFFFFF7ED))
                            )
                        )
                        .padding(22.dp)
                ) {
                    Text(
                        text = if (showAnswer) "Answer" else "Question",
                        color = Color(0xFF7C3AED),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (showAnswer) card.answer else card.question,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF24113D)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onPrevious,
                    enabled = currentIndex > 0
                ) {
                    Text("Previous")
                }

                OutlinedButton(
                    modifier = Modifier.weight(1f),
                    onClick = onNext,
                    enabled = currentIndex < totalCards - 1
                ) {
                    Text("Next")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onToggleAnswer,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9))
            ) {
                Text(if (showAnswer) "Hide Answer" else "Show Answer")
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(modifier = Modifier.weight(1f), onClick = onEdit) {
                    Text("Edit")
                }
                TextButton(modifier = Modifier.weight(1f), onClick = onDelete) {
                    Text("Delete", color = Color(0xFFB42318))
                }
            }
        }
    }
}

@Composable
fun EmptyDeckCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "No flashcards yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Add your first question and answer below.",
                color = Color(0xFF6D5A8D)
            )
        }
    }
}

@Composable
fun EditorSection(
    questionText: String,
    answerText: String,
    editingId: Long?,
    message: String,
    onQuestionChange: (String) -> Unit,
    onAnswerChange: (String) -> Unit,
    onClear: () -> Unit,
    onSave: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = if (editingId == null) "Add Flashcard" else "Edit Flashcard",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D1457)
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = questionText,
                onValueChange = onQuestionChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Question") },
                minLines = 2
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = answerText,
                onValueChange = onAnswerChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Answer") },
                minLines = 2
            )

            if (message.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = message, color = Color(0xFF6D28D9))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onSave,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6D28D9))
            ) {
                Text(if (editingId == null) "Add Card" else "Save Card")
            }

            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                onClick = onClear
            ) {
                Text("Clear")
            }
        }
    }
}

@Composable
fun DeckListSection(
    flashcards: List<Flashcard>,
    currentIndex: Int,
    onSelectCard: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Deck List",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D1457)
            )

            Spacer(modifier = Modifier.height(8.dp))

            flashcards.forEachIndexed { index, card ->
                TextButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onSelectCard(index) }
                ) {
                    Text(
                        text = "${index + 1}. ${card.question}",
                        modifier = Modifier.fillMaxWidth(),
                        color = if (index == currentIndex) Color(0xFF6D28D9) else Color(0xFF24113D),
                        fontWeight = if (index == currentIndex) FontWeight.Bold else FontWeight.Normal
                    )
                }

                if (index != flashcards.lastIndex) {
                    HorizontalDivider(color = Color(0xFFE7DDFB))
                }
            }
        }
    }
}

fun sampleFlashcards(): List<Flashcard> {
    return listOf(
        Flashcard(1, "What is Kotlin?", "Kotlin is a modern programming language used for Android development."),
        Flashcard(2, "What is Jetpack Compose?", "Jetpack Compose is Android's modern UI toolkit."),
        Flashcard(3, "What is a Composable function?", "A Composable function builds part of the app's user interface.")
    )
}

fun saveFlashcards(context: Context, flashcards: List<Flashcard>) {
    val jsonArray = JSONArray()

    flashcards.forEach { card ->
        val jsonObject = JSONObject()
        jsonObject.put("id", card.id)
        jsonObject.put("question", card.question)
        jsonObject.put("answer", card.answer)
        jsonArray.put(jsonObject)
    }

    context
        .getSharedPreferences("flashforge_storage", Context.MODE_PRIVATE)
        .edit()
        .putString("flashcards", jsonArray.toString())
        .apply()
}

fun loadFlashcards(context: Context): List<Flashcard> {
    val savedJson = context
        .getSharedPreferences("flashforge_storage", Context.MODE_PRIVATE)
        .getString("flashcards", null)
        ?: return emptyList()

    val jsonArray = JSONArray(savedJson)
    val cards = mutableListOf<Flashcard>()

    for (index in 0 until jsonArray.length()) {
        val item = jsonArray.getJSONObject(index)
        cards.add(
            Flashcard(
                id = item.getLong("id"),
                question = item.getString("question"),
                answer = item.getString("answer")
            )
        )
    }

    return cards
}
