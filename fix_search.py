import re

with open('app/src/main/java/com/example/ui/home/GlobalSearchScreen.kt', 'r') as f:
    content = f.read()

# I will replace the broken `else if (selectedTab == 4)` block with a working one
broken_block_pattern = re.compile(r'\} else if \(selectedTab == 4\) \{\s*GoogleSearchCard\([^}]*\}\s*\)\s*\}', re.MULTILINE)

fixed_block = """} else if (selectedTab == 4) {
                                // Question Papers Tab
                                items(scoredQuestionPapers) { paper ->
                                    GoogleSearchCard(
                                        category = "Question Paper",
                                        categoryIcon = Icons.Default.MenuBook,
                                        displayPath = "Aura Learning > Question Papers > ${paper.subject}",
                                        title = paper.title,
                                        description = paper.description,
                                        thumbnail = paper.thumbnail,
                                        gradeText = "Grade ${paper.className} • ${paper.subject} • ${paper.year}",
                                        onClick = {
                                            rootNavController.navigate("pdf_viewer/${paper.id}")
                                        }
                                    )
                                }
                            }"""

# Wait, `scoredQuestionPapers` doesn't exist? In `build.log`: `Unresolved reference 'Course'` is from the top of the file where I didn't replace them properly.
# Let's see what happens.
