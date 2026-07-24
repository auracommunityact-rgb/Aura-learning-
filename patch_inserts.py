import re

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

# Add imports
imports = """import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
"""
content = content.replace("class AuraRepository {", imports + "\nclass AuraRepository {")

# Add helper functions
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
content = content.replace("class AuraRepository {", "class AuraRepository {\n" + helpers)

# Now, we need to replace instances where `id = UUID.randomUUID().toString()` is assigned.
# For example: `val newBook = if (book.id.isEmpty()) book.copy(id = UUID.randomUUID().toString()) else book`
# should become: `val newBook = if (book.id.isEmpty()) book else book`
# Wait, actually, if the book is new (id is empty), we should NOT send the ID.
# But what if the book is being updated? We don't use insert for updates!
# So for inserts, we can ALWAYS use getJsonWithoutId (if we want Postgres to auto-generate the ID).
# If the id is not empty (which could happen if they copy an item?), Postgres will ignore it if we omit it,
# OR we can just check: if id is empty, use getJsonWithoutId(item). If id is not empty, use item?
# Wait! In Supabase, if you do `.insert()` and provide the `id`, and it's a BIGINT IDENTITY, it might fail if the ID is explicitly specified unless overriding system values is allowed. But usually, you don't supply it.
# So I can just modify the insert calls directly:

# Pattern: `val newSomething = if (something.id.isEmpty()) something.copy(id = UUID.randomUUID().toString()...) else something`
# We can change it to: `val newSomething = if (something.id.isEmpty()) something.copy(...) else something`
# And then `client.postgrest["..."]`.insert(getJsonWithoutId(newSomething))

content = re.sub(
    r'val (\w+) = if \(([^.]+)\.id\.isEmpty\(\)\) \2\.copy\(id = UUID\.randomUUID\(\)\.toString\(\)(.*?)\) else \2',
    r'val \1 = if (\2.id.isEmpty()) \2.copy(\3) else \2',
    content
)

# Wait, the regex needs to handle cases where there are no other arguments in copy()
# If copy() becomes copy(), we should remove it.
content = content.replace(".copy()", "")

# Let's fix specific lines instead of regex to be safe.
