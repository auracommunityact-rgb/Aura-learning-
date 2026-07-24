with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'r') as f:
    content = f.read()

content = content.replace("if (members.isEmpty()))", "if (members.isEmpty())")

with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'w') as f:
    f.write(content)
