#!/bin/bash
APK_PATH="app/build/outputs/apk/release/app-release.apk"

SERVER=$(python3 -c "import urllib.request, json, ssl; ctx = ssl.create_default_context(); ctx.check_hostname = False; ctx.verify_mode = ssl.CERT_NONE; req = urllib.request.Request('https://api.gofile.io/servers', headers={'User-Agent': 'Mozilla/5.0'}); print(json.loads(urllib.request.urlopen(req, context=ctx).read().decode())['data']['servers'][0]['name'])")

echo "Uploading to $SERVER..."
RESULT=$(curl -s -F file=@"$APK_PATH" "https://$SERVER.gofile.io/contents/uploadfile")

echo "Result:"
python3 -c "import sys, json; data = json.loads(sys.stdin.read()); print(data.get('data', {}).get('downloadPage', 'Failed to parse url: ' + str(data)))" <<< "$RESULT"
