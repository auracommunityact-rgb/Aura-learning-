with open('app/src/main/java/com/example/AuraApplication.kt', 'r') as f:
    content = f.read()

import re

new_cache_logic = """
        // Clean up WebView Code Cache directories recursively
        try {
            val codeCacheDir = java.io.File(cacheDir, "WebView/Default/HTTP Cache/Code Cache")
            if (codeCacheDir.exists()) {
                codeCacheDir.deleteRecursively()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
"""

content = re.sub(r'// Clean up WebView Code Cache directories if they were created.*?\} catch \(e: Exception\) \{\n\s*e\.printStackTrace\(\)\n\s*\}', new_cache_logic, content, flags=re.DOTALL)

with open('app/src/main/java/com/example/AuraApplication.kt', 'w') as f:
    f.write(content)
