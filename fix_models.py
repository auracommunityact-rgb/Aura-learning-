import re

with open('app/src/main/java/com/example/data/models/Models.kt', 'r') as f:
    content = f.read()

# Fix Book
content = re.sub(r'    @Serializable\(with = SafeStringSerializer::class\)\n    val className: String = "",\n    val subject: String = "",\n', r'    @Serializable(with = SafeStringSerializer::class)\n    val subject: String = "",\n', content)

# Fix Video
# Wait, let's just do a simpler replace.
content = content.replace('    @Serializable(with = SafeStringSerializer::class)\n    val className: String = "",\n    val subject: String = "",\n', '    @Serializable(with = SafeStringSerializer::class)\n    val subject: String = "",\n')

# Fix FlashcardDeck
content = content.replace('    val subject: String = "",\n    val className: String = "",\n', '    val subject: String = "",\n')

with open('app/src/main/java/com/example/data/models/Models.kt', 'w') as f:
    f.write(content)
