with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("client.postgrest[\"feedback\"].insert(feedback)", "client.postgrest[\"feedback\"].insert(if (feedback.id.isEmpty() || feedback.id.length > 20) getJsonWithoutId(feedback) else feedback)")

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
