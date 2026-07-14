#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"

echo "Fetching GoFile server..."
SERVER=$(curl -s "https://api.gofile.io/servers" | jq -r '.data.servers[0].name')

if [ -z "$SERVER" ] || [ "$SERVER" == "null" ]; then
    echo "Failed to get GoFile server"
    exit 1
fi

echo "Uploading to $SERVER..."
RESULT=$(curl -s -F file=@"$APK_PATH" "https://$SERVER.gofile.io/contents/uploadfile")

echo "Result:"
echo "$RESULT" | jq -r '.data.downloadPage'
