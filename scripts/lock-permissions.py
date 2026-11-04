#!/usr/bin/env python3
"""Rewrite android/app/src/main/AndroidManifest.xml so the app asks for
ONLY the permissions in ALLOWED, and explicitly removes anything a plugin
or library tries to merge in."""
import re, sys

PATH = "android/app/src/main/AndroidManifest.xml"
ALLOWED = ["android.permission.INTERNET", "android.permission.VIBRATE"]
REMOVE = [
    "ACCESS_NETWORK_STATE", "ACCESS_WIFI_STATE", "ACCESS_FINE_LOCATION",
    "ACCESS_COARSE_LOCATION", "CAMERA", "RECORD_AUDIO", "READ_CONTACTS",
    "WRITE_CONTACTS", "READ_EXTERNAL_STORAGE", "WRITE_EXTERNAL_STORAGE",
    "READ_MEDIA_IMAGES", "READ_MEDIA_VIDEO", "READ_MEDIA_AUDIO",
    "READ_PHONE_STATE", "POST_NOTIFICATIONS", "WAKE_LOCK",
    "RECEIVE_BOOT_COMPLETED", "SYSTEM_ALERT_WINDOW", "REQUEST_INSTALL_PACKAGES",
]

xml = open(PATH, encoding="utf-8").read()

# drop every existing <uses-permission .../>
xml = re.sub(r"\s*<uses-permission[^>]*/>", "", xml)

# make sure the tools namespace exists
if "xmlns:tools" not in xml:
    xml = xml.replace("<manifest ", '<manifest xmlns:tools="http://schemas.android.com/tools" ', 1)

lines = [f'    <uses-permission android:name="{p}" />' for p in ALLOWED]
lines += [
    f'    <uses-permission android:name="android.permission.{p}" tools:node="remove" />'
    for p in REMOVE
]
block = "\n" + "\n".join(lines) + "\n"

# insert just before </manifest>
xml = xml.replace("</manifest>", block + "</manifest>", 1)
open(PATH, "w", encoding="utf-8").write(xml)
print("Manifest locked. Allowed:", ", ".join(ALLOWED))
