import urllib.request
import urllib.parse
import json
import ssl

url = 'https://api.gofile.io/servers'
ctx = ssl.create_default_context()
ctx.check_hostname = False
ctx.verify_mode = ssl.CERT_NONE

req = urllib.request.Request(url, headers={'User-Agent': 'Mozilla/5.0'})
try:
    with urllib.request.urlopen(req, context=ctx) as response:
        data = json.loads(response.read().decode())
        server = data['data']['servers'][0]['name']
        print(f"Uploading to {server}...")
        
        # It's difficult to do multipart upload with raw urllib without requests library,
        # so let's try writing a small bash script that uses python for parsing JSON
except Exception as e:
    print(f"Error: {e}")
