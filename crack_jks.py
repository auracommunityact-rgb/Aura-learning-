import subprocess
passwords = ['android', 'password', 'auralearning', 'aura123', 'aura1234', 'aura12345', 'Auralearning123', 'Auralearning', 'admin123', '123456', '12345678', 'auralearning2026', 'auralearning2025', 'aura_learning']

for p in passwords:
    try:
        output = subprocess.check_output(f"keytool -list -keystore aura-learning.jks -storepass '{p}'", shell=True, stderr=subprocess.STDOUT)
        print(f"FOUND PASSWORD: {p}")
        break
    except subprocess.CalledProcessError:
        pass
else:
    print("Not found")
