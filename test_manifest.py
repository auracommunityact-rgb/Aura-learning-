import xml.etree.ElementTree as ET
try:
    tree = ET.parse('app/src/main/AndroidManifest.xml')
    print("XML parsed successfully")
except Exception as e:
    print(e)
