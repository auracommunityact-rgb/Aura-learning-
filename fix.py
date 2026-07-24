with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'r') as f:
    lines = f.readlines()

new_lines = [lines[2], lines[0], lines[1]] + lines[3:]
with open('app/src/main/java/com/example/data/repository/ChatRepository.kt', 'w') as f:
    f.writelines(new_lines)
