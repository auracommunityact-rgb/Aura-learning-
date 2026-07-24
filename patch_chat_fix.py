with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("))", ")))", 1) # wait, this might be risky. Let's do it precisely.

import re
content = re.sub(
    r'postgrest\["conversation_members"\]\.insert\(getJsonListWithoutId\(listOf\(\s*ConversationMember\(conversationId = convoId, userId = currentUserId\),\s*ConversationMember\(conversationId = convoId, userId = otherUserId\)\s*\)\)',
    r'postgrest["conversation_members"].insert(getJsonListWithoutId(listOf(\n            ConversationMember(conversationId = convoId, userId = currentUserId),\n            ConversationMember(conversationId = convoId, userId = otherUserId)\n        )))',
    content
)

with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'w') as f:
    f.write(content)
