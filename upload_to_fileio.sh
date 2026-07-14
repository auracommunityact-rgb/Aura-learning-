#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"

echo "Uploading to file.io..."
curl -F "file=@$APK_PATH" https://file.io
