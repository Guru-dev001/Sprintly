# Sprintly - build the signed Android update from GitHub

This repo contains the Sprintly v19 app plus two home-screen widgets (Today's routine 3x2, Notes 2x2).

## One-time setup: signing secrets (never put the key or password in the repo)
1. On your computer, turn your key into text:
   - Windows PowerShell: `[Convert]::ToBase64String([IO.File]::ReadAllBytes("sprintly-release.p12")) | Set-Clipboard`
   - Mac/Linux: `base64 -w0 sprintly-release.p12`
2. GitHub repo > Settings > Secrets and variables > Actions > New repository secret:
   - `SIGNING_KEYSTORE_BASE64` = the text from step 1
   - `SIGNING_KEYSTORE_PASSWORD` = your key password (alias is `sprintly`)

## Build
1. Upload everything in this folder to your repo (keep `.github`, `native`).
2. Repo > Actions > **Build Android APK** > Run workflow.
3. When green, download **Sprintly-apk** > `Sprintly-update.apk` and install it over v19.
   It uses the same key as v19, so your data stays.

## Widgets
Long-press the home screen > Widgets > Sprintly. Open the app once after updating so the widgets fill with your data.
Tick/untick a habit or delete a note on the widget; the app applies it the next time it opens.
