#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ ! -f "$APK_PATH" ]; then
    echo "APK not found at $APK_PATH"
    exit 1
fi
SERVER=$(curl -s https://api.gofile.io/servers | grep -o '"name":"[^"]*"' | head -1 | cut -d '"' -f 4)
echo "Uploading to $SERVER.gofile.io..."
curl -F "file=@$APK_PATH" https://$SERVER.gofile.io/uploadFile
