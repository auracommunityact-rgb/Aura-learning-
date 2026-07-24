with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'r') as f:
    content = f.read()

import re

content = re.sub(
    r'val newDeck = if \(deck.id.isEmpty\(\)\) deck.copy\(createdAt = System.currentTimeMillis\(\)\) else deck\s+client.postgrest\["flashcard_decks"\].insert\(if \(deck.id.isEmpty\(\)\) getJsonWithoutId\(newDeck\) else newDeck\)\s+newDeckId',
    """val newDeck = if (deck.id.isEmpty()) deck.copy(createdAt = System.currentTimeMillis()) else deck
            val result = client.postgrest["flashcard_decks"].insert(if (deck.id.isEmpty()) getJsonWithoutId(newDeck) else newDeck) { select() }
            val inserted = result.decodeSingle<FlashcardDeck>()
            inserted.id""",
    content
)

with open('app/src/main/java/com/example/data/repository/AuraRepository.kt', 'w') as f:
    f.write(content)
