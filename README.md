# Sprintly – Android APK

Your `index.html` wrapped with Capacitor. GitHub builds the APK for you.

## Steps
1. Create a new GitHub repository.
2. Upload ALL files from this folder (keep the `.github/workflows` folder and `www/` folder).
3. Go to the **Actions** tab -> **Build Android APK** -> wait ~5 minutes (it also runs automatically on every push).
4. Open the finished run -> **Artifacts** -> download **Sprintly-APK** (zip) -> unzip -> install `app-debug.apk` on your phone (allow "install unknown apps").

To change the app, edit `www/index.html` and commit; a new APK is built.
Note: the app needs internet (it loads Supabase and Google Fonts online).

## Permissions
The build runs `scripts/lock-permissions.py`, which limits the app to
**INTERNET** (sync with Supabase) and **VIBRATE** (alarms). Camera, location,
contacts, storage, microphone, etc. are explicitly removed. The only prompt
you may still see is the notification permission when you set an alarm.
