with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

pairs = [
    ("paper", "newPaper"),
    ("section", "newSection"),
    ("website", "newWebsite"),
    ("note", "newNote"),
    ("deck", "newDeck"),
    ("card", "newCard"),
    ("quiz", "newQuiz"),
    ("result", "newResult"),
    ("board", "newBoard")
]

for orig, new_obj in pairs:
    old = f"if ({orig}.id.isEmpty()) getJsonWithoutId({new_obj}) else {new_obj}"
    new = f"if ({orig}.id.isEmpty() || {orig}.id.length > 20) getJsonWithoutId({new_obj}) else {new_obj}"
    content = content.replace(old, new)

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
