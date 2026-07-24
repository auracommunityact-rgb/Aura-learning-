import re

with open('app/src/main/java/com/example/utils/HapticHelper.kt', 'r') as f:
    content = f.read()

imports = """import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
"""

if "import kotlinx.serialization.json.Json" not in content:
    content = content.replace("object HapticHelper {", imports + "\nobject HapticHelper {")

helpers = """    private val lenientJson = Json { encodeDefaults = true; ignoreUnknownKeys = true }

    private inline fun <reified T : Any> getJsonWithoutId(item: T): Map<String, kotlinx.serialization.json.JsonElement> {
        val map = lenientJson.encodeToJsonElement(item).jsonObject.toMutableMap()
        map.remove("id")
        return map
    }
"""

if "getJsonWithoutId" not in content:
    content = content.replace("object HapticHelper {", "object HapticHelper {\n" + helpers)

content = content.replace("SupabaseService.client.from(\"haptic_logs\").insert(log)", "SupabaseService.client.from(\"haptic_logs\").insert(getJsonWithoutId(log))")

with open('app/src/main/java/com/example/utils/HapticHelper.kt', 'w') as f:
    f.write(content)
