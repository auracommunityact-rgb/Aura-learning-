#!/bin/bash
echo "Retrieving best Gofile server..."
SERVERS_JSON=$(curl -s https://api.gofile.io/servers)
echo "Servers JSON response:"
echo "$SERVERS_JSON"

SERVER=$(echo "$SERVERS_JSON" | grep -oP '"name":"[^"]+"' | head -n 1 | cut -d':' -f2 | tr -d '"')

if [ -z "$SERVER" ]; then
    # fallback to a known storage server if parsing fails
    SERVER="store1"
fi

echo "Selected Server: $SERVER"

APK_PATH="./app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "Error: APK not found at $APK_PATH"
    exit 1
fi

echo "Uploading $APK_PATH to Gofile..."
UPLOAD_RESPONSE=$(curl -X POST \
  -H "Expect:" \
  -F "file=@$APK_PATH" \
  "https://${SERVER}.gofile.io/contents/uploadfile")

echo "Upload response:"
echo "$UPLOAD_RESPONSE"
