import re

with open("app/src/main/java/com/example/ui/home/HomeScreen.kt", "r") as f:
    content = f.read()

# Remove Daily Study Goal section
goal_pattern = r"\s*// Daily Study Goal\s*item \{\s*DailyStudyGoalCard\(\)\s*\}"
content = re.sub(goal_pattern, "", content)

# Remove unused import if any
content = re.sub(r"^import com\.example\.ui\.home\.DailyStudyGoalCard\n", "", content, flags=re.MULTILINE)

with open("app/src/main/java/com/example/ui/home/HomeScreen.kt", "w") as f:
    f.write(content)
