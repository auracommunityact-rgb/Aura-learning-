#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"

echo "Uploading to bashupload.com..."
curl -F "file=@$APK_PATH" https://bashupload.com/
