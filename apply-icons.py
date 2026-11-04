#!/usr/bin/env python3
"""Replace Capacitor's default Android launcher icon with the Sprintly logo.
Run AFTER `npx cap add android` and BEFORE gradle build:
    pip install pillow && python3 scripts/apply-icons.py
Uses icon-maskable-512.png (logo already sits inside the adaptive-icon safe zone)."""
import os, glob, shutil
from PIL import Image

SRC = next(p for p in ("www/icon-maskable-512.png", "icon-maskable-512.png") if os.path.exists(p))
RES = "android/app/src/main/res"
BG = "#2A3646"  # dark navy sampled from the logo background

img = Image.open(SRC).convert("RGB")
# legacy launcher sizes (px) and adaptive foreground sizes (108dp)
legacy = {"mdpi": 48, "hdpi": 72, "xhdpi": 96, "xxhdpi": 144, "xxxhdpi": 192}
adaptive = {k: int(v * 108 / 48) for k, v in legacy.items()}

for d, px in legacy.items():
    out = f"{RES}/mipmap-{d}"
    os.makedirs(out, exist_ok=True)
    sq = img.resize((px, px), Image.LANCZOS)
    sq.save(f"{out}/ic_launcher.png")
    sq.save(f"{out}/ic_launcher_round.png")
    img.resize((adaptive[d], adaptive[d]), Image.LANCZOS).save(f"{out}/ic_launcher_foreground.png")

# remove Capacitor's default vector foreground/background that would override ours
for p in glob.glob(f"{RES}/drawable*/ic_launcher_foreground.xml") + glob.glob(f"{RES}/drawable*/ic_launcher_background.xml"):
    os.remove(p)

os.makedirs(f"{RES}/values", exist_ok=True)
with open(f"{RES}/values/ic_launcher_background.xml", "w") as f:
    f.write(f'<?xml version="1.0" encoding="utf-8"?>\n<resources>\n    <color name="ic_launcher_background">{BG}</color>\n</resources>\n')

os.makedirs(f"{RES}/mipmap-anydpi-v26", exist_ok=True)
xml = '''<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background"/>
    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>
</adaptive-icon>
'''
for n in ("ic_launcher.xml", "ic_launcher_round.xml"):
    open(f"{RES}/mipmap-anydpi-v26/{n}", "w").write(xml)
print("Launcher icons applied from", SRC)
