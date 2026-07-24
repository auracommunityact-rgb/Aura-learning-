import re

with open('app/src/main/java/com/example/ui/admin/AdminUserProfileScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("SupabaseService.client.from(\"notifications\").insert(newNotification)", "repository.addNotification(newNotification)")

with open('app/src/main/java/com/example/ui/admin/AdminUserProfileScreen.kt', 'w') as f:
    f.write(content)
