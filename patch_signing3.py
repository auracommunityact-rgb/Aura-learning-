import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = "import java.util.Base64\n" + content

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
