<p align="center">
  <picture>
    <source media="(prefers-color-scheme: dark)" srcset="branding/banner-dark.svg">
    <img alt="dumGram" src="branding/banner-light.svg" width="480">
  </picture>
</p>

<p align="center">
  Unofficial Telegram client for Android with a few things the official one is missing.
</p>

<p align="center">
  <a href="https://github.com/damnurmum/dumGram/actions/workflows/build.yml"><img alt="Build" src="https://img.shields.io/github/actions/workflow/status/damnurmum/dumGram/build.yml?branch=main&label=build&logo=githubactions&logoColor=white&color=A62C2C&labelColor=E83030"></a>
  <a href="LICENSE"><img alt="License: GPL v2" src="https://img.shields.io/badge/license-GPL%20v2-A62C2C?labelColor=E83030"></a>
  <img alt="Android 6.0+" src="https://img.shields.io/badge/Android-6.0%2B-A62C2C?logo=android&logoColor=white&labelColor=E83030">
  <a href="https://github.com/DrKLO/Telegram"><img alt="Based on Telegram 13.2.1" src="https://img.shields.io/badge/based%20on-Telegram%2013.2.1-A62C2C?logo=telegram&logoColor=white&labelColor=E83030"></a>
</p>

---

## Features

| | |
|---|---|
| **TG WS Proxy** | Built-in proxy that reaches Telegram over WebSocket, the way the web version does. Works on networks that block Telegram, no VPN or server needed. Falls back to Cloudflare when even that is blocked. |
| **Proxy names** | Give any proxy a name instead of staring at IP addresses. |
| **ID and data center** | User, group and channel profiles show the ID and DC. Tap to copy. |
| **Seconds in time** | Optional `14:05:37` under messages. |
| **Hide your number** | Keeps your phone number off Settings and your profile when you share the screen. |
| **Bottom tabs** | Hide Contacts, Settings/Calls or Profile. Chats always stays. |
| **No dead ends** | Features that can't work in a fork (Play purchases, passkeys, Android Auto, Wear OS, Chromecast) are switched off or removed instead of failing. |

All of it lives in **Settings > dumGram**.

## Download

Grab an APK from [Releases](https://github.com/damnurmum/dumGram/releases) or the latest [build](https://github.com/damnurmum/dumGram/actions/workflows/build.yml):

| APK | For |
|---|---|
| `arm64-v8a` | almost every phone from the last years |
| `armeabi-v7a` | old 32-bit phones |
| `x86_64`, `x86` | emulators and Chromebooks |
| `universal` | not sure? this one runs everywhere, it's just bigger |

dumGram installs next to the official Telegram, they don't conflict.

## Building

1. Clone with submodules:
   ```bash
   git clone https://github.com/damnurmum/dumGram.git
   cd dumGram
   git submodule update --init --recursive
   ```
2. Get your own `api_id` and `api_hash` at [my.telegram.org](https://my.telegram.org) and set
   `APP_ID` / `APP_HASH` in `~/.gradle/gradle.properties` (keeps them out of git) or in `gradle.properties`.
3. Replace `TMessagesProj/config/release.keystore` with your own keystore and update
   `RELEASE_KEY_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_STORE_PASSWORD` in `gradle.properties`.
4. For push notifications, create your own Firebase project and replace `TMessagesProj_App/google-services.json`.
5. Build:
   ```bash
   ./gradlew :TMessagesProj_App:assembleAfatRelease
   ```
   APKs land in `TMessagesProj_App/build/outputs/apk/afat/release/` as `dumGram-<version>-<abi>.apk`.

The TG WS Proxy core ships prebuilt in `TMessagesProj/src/main/jniLibs`. To rebuild it you need Rust and the NDK:
`TMessagesProj/jni/prebuild/build_tg_ws_proxy.sh`.

<details>
<summary><b>CI and releases</b></summary>

[`build.yml`](.github/workflows/build.yml) builds and checks the APKs on every push to `main` and every PR.
Pushing a `v*` tag also publishes a GitHub Release with the APKs attached.

Repository secrets (Settings > Secrets and variables > Actions):

| Secret | Value |
|---|---|
| `APP_ID`, `APP_HASH` | from my.telegram.org |
| `KEYSTORE_BASE64` | `base64 -i release.keystore` (required for releases) |
| `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD` | keystore credentials |

</details>

<details>
<summary><b>Syncing with upstream Telegram</b></summary>

```bash
git remote add upstream https://github.com/DrKLO/Telegram.git   # once
git fetch upstream && git merge upstream/master
```

</details>

## Credits

- [Telegram for Android](https://github.com/DrKLO/Telegram), the base of all this
- [tg-ws-proxy](https://github.com/Flowseal/tg-ws-proxy) by Flowseal and its Rust port [tg-ws-proxy-rs](https://github.com/valnesfjord/tg-ws-proxy-rs) by valnesfjord, the TG WS Proxy core
- Banner set in [Cal Sans](https://github.com/calcom/sans) (OFL 1.1), the in-app wordmark in [Roboto](https://github.com/googlefonts/roboto) (Apache 2.0)

## License

GPL v2, see [LICENSE](LICENSE). The source of every published build is in this repository.

<sub>dumGram is not affiliated with Telegram. Made by dum.</sub>
