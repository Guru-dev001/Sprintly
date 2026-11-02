# Sprintly - build the Android APK from GitHub

No GitHub Pages or hosting needed. The app (index.html) is packed inside the APK.

1. Upload everything in this folder to your GitHub repo (keep the `.github` folder).
2. Repo > Actions > **Build Android APK**. It runs by itself on every push to `main`,
   or press **Run workflow**.
3. When the run is green, open it and download **Sprintly-apk** (bottom of the page).
   Unzip it and install `app-debug.apk` on your phone (allow "install unknown apps").

## Changing the app
Edit `index.html` and commit. A new APK is built automatically. Install the new APK over
the old one (the app updates itself only when you install the new APK).

## Login e-mails (Supabase)
Inside the APK the app has no web address, so confirmation / reset e-mail links open
your Supabase **Site URL** (Authentication > URL Configuration). Set that to any page you
own (even a simple one). After tapping the link, go back to the app and sign in.

## Notes
- This is a debug APK, fine for your own phone. Play Store needs a signed release build.
- Do not add a `pages.yml` workflow; it is not needed.
