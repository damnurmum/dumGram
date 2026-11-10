#!/usr/bin/env bash
# Builds the TG WS Proxy core (github.com/valnesfjord/tg-ws-proxy-rs, MIT) as
# libtg_ws_proxy_jni.so for every ABI and drops it into src/main/jniLibs.
# The .so files are committed, same as the other prebuilt libs here, so CI
# doesn't need a Rust toolchain. Rerun this when bumping TAG.
#
# needs: rustup, git, the NDK from the app's ndkVersion (ANDROID_NDK_HOME or
# ANDROID_HOME/ndk/<version>)

set -euo pipefail

TAG="v2.7.0"
REPO="https://github.com/valnesfjord/tg-ws-proxy-rs.git"
API=23
NDK_VERSION="27.2.12479018"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
OUT_DIR="$SCRIPT_DIR/../../src/main/jniLibs"
WORK_DIR="${WORK_DIR:-$(mktemp -d)}"

NDK="${ANDROID_NDK_HOME:-${ANDROID_HOME:-}/ndk/$NDK_VERSION}"
if [ ! -d "$NDK" ]; then
    echo "NDK not found at $NDK, set ANDROID_NDK_HOME" >&2
    exit 1
fi
case "$(uname -s)" in
    Darwin) HOST_TAG="darwin-x86_64" ;;
    Linux) HOST_TAG="linux-x86_64" ;;
    *) echo "unsupported host" >&2; exit 1 ;;
esac
BIN="$NDK/toolchains/llvm/prebuilt/$HOST_TAG/bin"

SRC="$WORK_DIR/tg-ws-proxy-rs"
if [ ! -d "$SRC" ]; then
    git clone --depth 1 --branch "$TAG" "$REPO" "$SRC"
fi
cd "$SRC"

# abi | rust target | clang prefix (armv7 is the odd one out)
TARGETS=(
    "arm64-v8a aarch64-linux-android aarch64-linux-android"
    "armeabi-v7a armv7-linux-androideabi armv7a-linux-androideabi"
    "x86 i686-linux-android i686-linux-android"
    "x86_64 x86_64-linux-android x86_64-linux-android"
)

# rust-toolchain.toml in the repo pins the compiler, add the targets to that one
for entry in "${TARGETS[@]}"; do
    read -r _ triple _ <<< "$entry"
    rustup target add "$triple" > /dev/null
done

for entry in "${TARGETS[@]}"; do
    read -r abi triple clang <<< "$entry"
    env_triple="$(echo "$triple" | tr '[:lower:]-' '[:upper:]_')"
    echo "==> $abi"
    env \
        "CC_${triple//-/_}=$BIN/${clang}${API}-clang" \
        "AR_${triple//-/_}=$BIN/llvm-ar" \
        "CARGO_TARGET_${env_triple}_LINKER=$BIN/${clang}${API}-clang" \
        "CARGO_TARGET_${env_triple}_AR=$BIN/llvm-ar" \
        cargo build --release --locked -p tg-ws-proxy-jni --target "$triple"
    mkdir -p "$OUT_DIR/$abi"
    cp "target/$triple/release/libtg_ws_proxy_jni.so" "$OUT_DIR/$abi/"
done

ls -la "$OUT_DIR"/*/libtg_ws_proxy_jni.so
