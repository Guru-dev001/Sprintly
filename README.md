# Sprintly - GitHub setup

## 1) Put the app online (GitHub Pages)
1. Create a new GitHub repository (public is simplest) and upload everything in this folder
   (keep the `.github` folder and `.nojekyll` file).
2. Repo > Settings > Pages > Build and deployment > Source: **GitHub Actions**.
3. Push to `main`. After ~1 minute your app is live at
   `https://YOUR-USERNAME.github.io/YOUR-REPO/`

## 2) Supabase (needed for login to work on the new address)
Supabase dashboard > Authentication > URL Configuration: add your GitHub Pages address
to **Site URL** / **Redirect URLs**.

## 3) Build the APK
Repo > Actions > **Build Android APK** > Run workflow. When it finishes, download
**Sprintly-apk** from the run page, unzip it, and install `app-debug.apk` on your phone
(allow "install unknown apps").

## 4) Updating the app later
Edit `index.html` (or any file) and push. GitHub Pages redeploys, and the APK shows the new
version the next time it is opened. You only rebuild the APK if you change the app name,
icon or package id.

## Notes
- The APK is a debug build, fine for your own phone. For Play Store you need a signed release build.
- If you already have your own `sw.js` / `manifest.webmanifest` with push logic, keep yours.
- Keep the same web address, because the APK points at it.
