import re
with open("pdf_tool_content.txt", "r") as f:
    lines = f.readlines()

out = []
for line in lines:
    if line.startswith("The following code has been modified") or line.startswith("Showing lines") or line.startswith("Total Lines") or line.startswith("Total Bytes") or line.startswith("File Path:") or line.startswith("The above content shows the entire"):
        continue
    # remove leading line number (e.g. "123: ")
    m = re.match(r"^\d+:\s(.*)", line)
    if m:
        out.append(m.group(1) + "\n")
    else:
        # just append the line if it doesn't match
        pass

with open("app/src/main/java/com/example/ui/pdf/screens/PdfToolScreen.kt", "w") as f:
    f.writelines(out)
