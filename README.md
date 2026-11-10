# dumgram

Unofficial Telegram client for Android, based on the [official Telegram for Android source code](https://github.com/DrKLO/Telegram).
Distributed as APK files only (no Google Play).

**dumgram is not affiliated with Telegram.**

- Developer: dum
- Package: `dev.dum.dumgram`
- Upstream version: 13.2.1 (7291)

## Building

1. Clone with submodules:
   ```bash
   git clone https://github.com/damnurmum/dumgram.git
   cd dumgram
   git submodule update --init --recursive
   ```
2. Get your own `api_id` and `api_hash` at [my.telegram.org](https://my.telegram.org) and set
   `APP_ID` / `APP_HASH` in `gradle.properties` (or `~/.gradle/gradle.properties` to keep them private).
3. Replace `TMessagesProj/config/release.keystore` with your own keystore and update
   `RELEASE_KEY_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_STORE_PASSWORD` in `gradle.properties`.
4. For push notifications, create your own Firebase project and replace `TMessagesProj_App/google-services.json`.
5. Build:
   ```bash
   ./gradlew :TMessagesProj_App:assembleAfatRelease
   ```
   Output: `TMessagesProj_App/build/outputs/apk/afat/release/dumgram-<version>-<abi>.apk`
   for `armeabi-v7a`, `arm64-v8a`, `x86`, `x86_64` and `universal`.

## CI

[`.github/workflows/build.yml`](.github/workflows/build.yml) builds and checks the APKs on every push to `main` and every PR.
Pushing a `v*` tag also creates a GitHub Release with the APKs attached.

Repository secrets (Settings > Secrets and variables > Actions):

| Secret | Value |
|---|---|
| `APP_ID`, `APP_HASH` | from my.telegram.org |
| `KEYSTORE_BASE64` | `base64 -i release.keystore` (required for releases) |
| `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` | keystore credentials |

## Syncing with upstream

```bash
git remote add upstream https://github.com/DrKLO/Telegram.git   # once
git fetch upstream && git merge upstream/master
```

## License

GPL v2, see [LICENSE](LICENSE). The source code of every distributed build must be published.

Telegram API docs: https://core.telegram.org/api
