with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

import re

content = content.replace("client.postgrest[\"notifications\"].insert(notification)", "client.postgrest[\"notifications\"].insert(if (notification.id.isEmpty() || notification.id.length > 20) getJsonWithoutId(notification) else notification)")

# wait, if notification.id is a UUID, its length is 36. So length > 20 captures it.
# but what if it's already an empty string? We should strip it if it's empty or a UUID.

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
