import re

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

imports = """import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
"""
if "import kotlinx.serialization.json.Json" not in content:
    content = content.replace("class AuraRepository {", imports + "\nclass AuraRepository {")

helpers = """    private val lenientJson = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private inline fun <reified T : Any> getJsonWithoutId(item: T): Map<String, kotlinx.serialization.json.JsonElement> {
        val map = lenientJson.encodeToJsonElement(item).jsonObject.toMutableMap()
        map.remove("id")
        return map
    }

    private inline fun <reified T : Any> getJsonListWithoutId(items: List<T>): List<Map<String, kotlinx.serialization.json.JsonElement>> {
        return items.map { getJsonWithoutId(it) }
    }
"""
if "getJsonWithoutId" not in content:
    content = content.replace("class AuraRepository {", "class AuraRepository {\n" + helpers)

# Replace UUID generations
content = content.replace("val newBook = if (book.id.isEmpty()) book.copy(id = UUID.randomUUID().toString()) else book", "val newBook = book")
content = content.replace("client.postgrest[\"books\"].insert(newBook)", "client.postgrest[\"books\"].insert(if (newBook.id.isEmpty()) getJsonWithoutId(newBook) else newBook)")

content = content.replace("val newVideo = if (video.id.isEmpty()) video.copy(id = UUID.randomUUID().toString()) else video", "val newVideo = video")
content = content.replace("client.postgrest[\"videos\"].insert(newVideo)", "client.postgrest[\"videos\"].insert(if (newVideo.id.isEmpty()) getJsonWithoutId(newVideo) else newVideo)")

content = content.replace("val newBanner = if (banner.id.isEmpty()) banner.copy(id = UUID.randomUUID().toString()) else banner", "val newBanner = banner")
content = content.replace("client.postgrest[\"banners\"].insert(newBanner)", "client.postgrest[\"banners\"].insert(if (newBanner.id.isEmpty()) getJsonWithoutId(newBanner) else newBanner)")

content = content.replace("val newPaper = if (paper.id.isEmpty()) paper.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else paper", "val newPaper = if (paper.id.isEmpty()) paper.copy(createdAt = System.currentTimeMillis()) else paper")
content = content.replace("client.postgrest[\"question_papers\"].insert(newPaper)", "client.postgrest[\"question_papers\"].insert(if (paper.id.isEmpty()) getJsonWithoutId(newPaper) else newPaper)")

content = content.replace("val newSection = if (section.id.isEmpty()) section.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else section", "val newSection = if (section.id.isEmpty()) section.copy(createdAt = System.currentTimeMillis()) else section")
content = content.replace("client.postgrest[\"question_paper_sections\"].insert(newSection)", "client.postgrest[\"question_paper_sections\"].insert(if (section.id.isEmpty()) getJsonWithoutId(newSection) else newSection)")

content = content.replace("val newWebsite = if (website.id.isEmpty()) website.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else website", "val newWebsite = if (website.id.isEmpty()) website.copy(createdAt = System.currentTimeMillis()) else website")
content = content.replace("client.postgrest[\"websites\"].insert(newWebsite)", "client.postgrest[\"websites\"].insert(if (website.id.isEmpty()) getJsonWithoutId(newWebsite) else newWebsite)")

content = content.replace("val newNote = if (note.id.isEmpty()) note.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else note", "val newNote = if (note.id.isEmpty()) note.copy(createdAt = System.currentTimeMillis()) else note")
content = content.replace("client.postgrest[\"notes\"].insert(newNote)", "client.postgrest[\"notes\"].insert(if (note.id.isEmpty()) getJsonWithoutId(newNote) else newNote)")

content = content.replace("val newDeckId = UUID.randomUUID().toString()", "")
content = content.replace("val newDeck = deck.copy(id = newDeckId, createdAt = System.currentTimeMillis())", "val newDeck = if (deck.id.isEmpty()) deck.copy(createdAt = System.currentTimeMillis()) else deck")
content = content.replace("client.postgrest[\"flashcard_decks\"].insert(newDeck)", "client.postgrest[\"flashcard_decks\"].insert(if (deck.id.isEmpty()) getJsonWithoutId(newDeck) else newDeck)")
# Wait, flashcard decks ID is used later?
# Let's check: grep -A 5 "val newDeckId" 

content = content.replace("val newCard = if (card.id.isEmpty()) card.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else card", "val newCard = if (card.id.isEmpty()) card.copy(createdAt = System.currentTimeMillis()) else card")
content = content.replace("client.postgrest[\"flashcards\"].insert(newCard)", "client.postgrest[\"flashcards\"].insert(if (card.id.isEmpty()) getJsonWithoutId(newCard) else newCard)")

content = content.replace("client.postgrest[\"video_progress\"].insert(progress.copy(id = UUID.randomUUID().toString(), lastWatchedAt = System.currentTimeMillis()))", "client.postgrest[\"video_progress\"].insert(getJsonWithoutId(progress.copy(lastWatchedAt = System.currentTimeMillis())))")
content = content.replace("client.postgrest[\"book_progress\"].insert(progress.copy(id = UUID.randomUUID().toString(), lastReadAt = System.currentTimeMillis()))", "client.postgrest[\"book_progress\"].insert(getJsonWithoutId(progress.copy(lastReadAt = System.currentTimeMillis())))")

content = content.replace("val newId = if (quiz.id.isEmpty()) UUID.randomUUID().toString() else quiz.id", "val newId = quiz.id")
content = content.replace("val newQuiz = quiz.copy(id = newId, createdAt = System.currentTimeMillis())", "val newQuiz = if (quiz.id.isEmpty()) quiz.copy(createdAt = System.currentTimeMillis()) else quiz")
content = content.replace("client.postgrest[\"quizzes\"].insert(newQuiz)", "client.postgrest[\"quizzes\"].insert(if (quiz.id.isEmpty()) getJsonWithoutId(newQuiz) else newQuiz)")

content = content.replace("val newQuestions = questions.map { if (it.id.isEmpty()) it.copy(id = UUID.randomUUID().toString(), quizId = quizId) else it.copy(quizId = quizId) }", "val newQuestions = questions.map { it.copy(quizId = quizId) }")
content = content.replace("client.postgrest[\"quiz_questions\"].insert(newQuestions)", "client.postgrest[\"quiz_questions\"].insert(getJsonListWithoutId(newQuestions))")

content = content.replace("val newResult = result.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis())", "val newResult = if(result.id.isEmpty()) result.copy(createdAt = System.currentTimeMillis()) else result")
content = content.replace("client.postgrest[\"quiz_results\"].insert(newResult)", "client.postgrest[\"quiz_results\"].insert(if (result.id.isEmpty()) getJsonWithoutId(newResult) else newResult)")

content = content.replace("val newBoard = if (board.id.isEmpty()) board.copy(id = UUID.randomUUID().toString(), createdAt = System.currentTimeMillis()) else board", "val newBoard = if (board.id.isEmpty()) board.copy(createdAt = System.currentTimeMillis()) else board")
content = content.replace("client.postgrest[\"exam_boards\"].insert(newBoard)", "client.postgrest[\"exam_boards\"].insert(if (board.id.isEmpty()) getJsonWithoutId(newBoard) else newBoard)")

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
