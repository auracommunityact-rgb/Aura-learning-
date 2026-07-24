import re

with open('app/src/main/java/com/example/ui/admin/AdminWebsiteUploadScreen.kt', 'r') as f:
    content = f.read()

content = content.replace("SupabaseService.client.from(\"websites\").insert(website)", "com.example.data.repository.AuraRepository().addWebsite(website)")

with open('app/src/main/java/com/example/ui/admin/AdminWebsiteUploadScreen.kt', 'w') as f:
    f.write(content)
