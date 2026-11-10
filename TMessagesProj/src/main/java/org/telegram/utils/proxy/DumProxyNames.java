package org.telegram.utils.proxy;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import org.telegram.messenger.ApplicationLoader;

// dumgram: user-given proxy names. kept in their own prefs file keyed by the proxy itself,
// so telegram's serialized proxy list (and its schema versions) stays untouched
public final class DumProxyNames {

    private DumProxyNames() {
    }

    private static SharedPreferences prefs() {
        return ApplicationLoader.applicationContext.getSharedPreferences("dum_proxy_names", Context.MODE_PRIVATE);
    }

    // everything that makes a proxy "that proxy". for TG WS the random secret alone is enough,
    // but the same shape for all types keeps it simple
    private static String key(ProxySettings settings) {
        return ProxySettings.typeToInt(settings.getType()) + "|" + settings.getAddress() + "|" + settings.getPort()
                + "|" + settings.getUser() + "|" + settings.getSecret();
    }

    public static String get(ProxySettings settings) {
        return settings == null ? "" : prefs().getString(key(settings), "");
    }

    // editing a proxy changes its key, so the old entry is dropped and the name moves over
    public static void set(ProxySettings oldSettings, ProxySettings newSettings, String name) {
        final SharedPreferences.Editor editor = prefs().edit();
        if (oldSettings != null) {
            editor.remove(key(oldSettings));
        }
        if (newSettings != null && !TextUtils.isEmpty(name)) {
            editor.putString(key(newSettings), name.trim());
        }
        editor.apply();
    }

    public static void remove(ProxySettings settings) {
        if (settings != null) {
            prefs().edit().remove(key(settings)).apply();
        }
    }
}
