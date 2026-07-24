with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'r') as f:
    content = f.read()

imports = """import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
"""
if "import kotlinx.serialization.json.Json" not in content:
    content = content.replace("class ChatRepository", imports + "\nclass ChatRepository")

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
    content = content.replace("class ChatRepository {", "class ChatRepository {\n" + helpers)

content = content.replace(".insert(message) { select() }", ".insert(if (message.id.isEmpty() || message.id.length > 20) getJsonWithoutId(message) else message) { select() }")
content = content.replace("postgrest[\"conversations\"].insert(newConvo)", "postgrest[\"conversations\"].insert(if (newConvo.id.isEmpty() || newConvo.id.length > 20) getJsonWithoutId(newConvo) else newConvo)")

# For conversation members, wait, they set the ID manually:
content = content.replace("ConversationMember(id = java.util.UUID.randomUUID().toString(), conversationId = convoId, userId = currentUserId)", "ConversationMember(conversationId = convoId, userId = currentUserId)")
content = content.replace("ConversationMember(id = java.util.UUID.randomUUID().toString(), conversationId = convoId, userId = otherUserId)", "ConversationMember(conversationId = convoId, userId = otherUserId)")

content = content.replace("postgrest[\"conversation_members\"].insert(listOf(", "postgrest[\"conversation_members\"].insert(getJsonListWithoutId(listOf(")

with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'w') as f:
    f.write(content)
