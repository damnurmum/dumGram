package io.github.valnesfjord.tgwsproxyrs;

import org.telegram.messenger.FileLog;
import org.telegram.utils.proxy.TgWsProxy;

// Bridge for the prebuilt tg-ws-proxy-rs JNI shim (libtg_ws_proxy_jni.so).
// The package and class name are baked into the .so's JNI symbols, so they
// have to stay exactly this. Callbacks come from Rust worker threads.
public final class NativeProxy {

    static {
        System.loadLibrary("tg_ws_proxy_jni");
    }

    private NativeProxy() {
    }

    // same command line the desktop binary takes. returns an error text or null
    public static native String nativeStart(String args);

    public static native void nativeStop();

    public static native boolean nativeIsRunning();

    public static void onNativeLog(String line) {
        FileLog.d("tgws: " + line);
    }

    public static void onNativeListening(String link) {
        TgWsProxy.onListening();
    }

    // per-connection errors land here too, a dead core also reports onNativeStopped
    public static void onNativeError(String message) {
        FileLog.e("tgws error: " + message);
    }

    public static void onNativeStopped() {
        TgWsProxy.onStopped();
    }
}
