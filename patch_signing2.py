import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

# Fix the debug signingConfig in buildTypes
content = re.sub(r"signingConfig\s*=\s*signingConfigs\.findByName\(\"debug\"\)", "", content)

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
