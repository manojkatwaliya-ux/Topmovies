# TopMovies Android App

This is the exported Android Studio project for the TopMovies OTT app.

## Project configuration

- Android application ID: `com.topmovies.app`
- Backend root: `https://topmovies.info/`
- OxOO API config endpoint: `https://topmovies.info/rest-api/v130/config`
- API key: intentionally **not embedded** in the source archive.

## Open and build a debug APK

1. Install a current stable Android Studio and the Android SDK Platform 36.
2. Extract this ZIP to a normal writable folder.
3. In Android Studio choose **Open** and select the extracted `topmovies_work` project folder (the folder containing `settings.gradle.kts`).
4. Allow Gradle sync to finish and install any SDK components Android Studio requests.
5. Connect an Android phone with USB debugging enabled, or start an emulator.
6. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
7. Android Studio will show the generated debug APK location, normally `app/build/outputs/apk/debug/app-debug.apk`.

The exported archive contains the Gradle wrapper properties but not the wrapper launcher scripts/JAR. Android Studio can still import the Gradle project and resolve its configured Gradle distribution. If you want to build from a terminal, generate/add the Gradle Wrapper from Android Studio or with an installed Gradle version first.

## Connect the OxOO backend

1. Install/deploy the matching OxOO PHP Admin/API backend on `topmovies.info`.
2. In the Android app open **Profile / API Configuration** (or the API configuration control on Home).
3. Enter the API key created by your own admin panel, tap **Test Connection**, then **Save & Connect**.
4. The app tests `GET https://topmovies.info/rest-api/v130/config` and sends the key in the `API-KEY` and `X-API-KEY` request headers.

If the config endpoint returns 404, fix the PHP backend route and `.htaccess` rewrite rules first; changing the Android key will not fix a missing route. If it returns 401/403, verify that the key belongs to this exact backend/API version.

## Release signing

A release signing keystore is not included. To create a Play Store/release APK, configure your own upload keystore and the `KEYSTORE_PATH`, `STORE_PASSWORD`, and `KEY_PASSWORD` environment variables before building the `release` variant. Keep the keystore and passwords private.

## Important security notes

- Do not commit or share your API key in a public repository. A key stored in an Android app can be extracted from the APK; enforce rate limits and access controls on the server.
- API credentials are sent in HTTP headers, not query strings, and request-header logging has been removed to avoid logging the key.
- Only stream video content you own or are authorized to distribute.

## Verification status

This archive was statically reviewed and project XML files were checked for well-formedness. A full Android Gradle build and live backend/API test could not be run in this environment because outbound DNS/network access is unavailable. Build and playback still need verification in Android Studio against the deployed backend.
