import re

with open("app/build.gradle.kts", "r") as f:
    content = f.read()

content = content.replace("java.util.Base64.getDecoder()", "Base64.getDecoder()")

with open("app/build.gradle.kts", "w") as f:
    f.write(content)
