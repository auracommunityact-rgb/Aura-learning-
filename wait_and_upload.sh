#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"
while [ ! -f "$APK_PATH" ]; do
    sleep 5
done
echo "APK found! Uploading..."
SERVER=$(curl -s https://api.gofile.io/servers | grep -o '"name":"[^"]*"' | head -1 | cut -d '"' -f 4)
curl -s -F "file=@$APK_PATH" https://$SERVER.gofile.io/uploadFile > upload_result.json
