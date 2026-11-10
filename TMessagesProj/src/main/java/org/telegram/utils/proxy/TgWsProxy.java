package org.telegram.utils.proxy;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.BuildVars;
import org.telegram.messenger.FileLog;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.security.SecureRandom;

import io.github.valnesfjord.tgwsproxyrs.NativeProxy;

// dumgram: "TG WS Proxy" proxy type. Runs the tg-ws-proxy-rs core inside the app:
// it listens as an MTProto proxy on 127.0.0.1 and carries the traffic to Telegram
// over WebSocket. tgnet just sees a regular MTProto proxy at 127.0.0.1:port,
// the same trick the WEB proxy type uses with WebProxyTransport.
public final class TgWsProxy {

    public static final String HOST = "127.0.0.1";

    private static final Object lock = new Object();
    private static volatile String runningArgs;
    // what apply() asked for last, so a delayed retry doesn't revive a proxy the user already left
    private static volatile String wantedArgs;

    private TgWsProxy() {
    }

    // a fresh proxy entry: free local port and a random secret, both kept with the entry
    public static ProxySettings create() {
        final byte[] key = new byte[16];
        new SecureRandom().nextBytes(key);
        final StringBuilder secret = new StringBuilder("dd");
        for (byte b : key) {
            secret.append(String.format("%02x", b & 0xff));
        }
        return ProxySettings.builder()
                .setType(ProxySettings.Type.TG_WS)
                .setAddress(HOST)
                .setPort(freePort())
                .setSecret(secret.toString())
                .build();
    }

    // called whenever tgnet gets new proxy settings: start the core for ours, stop it otherwise
    public static void apply(ProxySettings settings) {
        if (settings != null && settings.getType() == ProxySettings.Type.TG_WS && settings.isValid()) {
            start(settings);
        } else {
            stop();
        }
    }

    public static void start(ProxySettings settings) {
        start(settings, 0);
    }

    // for pings of proxies that aren't active: never kick out a core that's carrying real traffic.
    // true if it had to start the core now, so the caller knows the port isn't listening yet
    public static boolean startIfIdle(ProxySettings settings) {
        if (runningArgs != null) {
            return false;
        }
        start(settings, 0);
        return true;
    }

    private static void start(ProxySettings settings, int attempt) {
        // the core wants the bare 32 hex key, the client side keeps the "dd" prefix
        final String secret = settings.getSecret();
        final String key = secret.startsWith("dd") || secret.startsWith("ee") ? secret.substring(2) : secret;
        // --host matters: left out, the core binds 0.0.0.0 and the proxy is open to the whole wifi
        String args = "--host " + HOST + " --port " + settings.getPort() + " --secret " + key
                + Options.parse(settings.getUser()).toArgs();
        if (!BuildVars.LOGS_ENABLED) {
            args += " --quiet";
        }
        synchronized (lock) {
            wantedArgs = args;
            if (args.equals(runningArgs)) {
                return;
            }
            try {
                if (runningArgs != null) {
                    NativeProxy.nativeStop();
                }
                final String error = NativeProxy.nativeStart(args);
                if (error != null) {
                    // usually "still stopping" right after a stop, the old runtime needs a moment to wind down
                    FileLog.e("tgws start failed: " + error);
                    runningArgs = null;
                    final String wanted = args;
                    if (attempt < 5) {
                        AndroidUtilities.runOnUIThread(() -> {
                            if (wanted.equals(wantedArgs)) {
                                start(settings, attempt + 1);
                            }
                        }, 400);
                    }
                    return;
                }
                runningArgs = args;
            } catch (Throwable e) {
                // no .so for this abi or a broken build, nothing we can do but log
                FileLog.e(e);
                runningArgs = null;
            }
        }
    }

    public static void stop() {
        synchronized (lock) {
            wantedArgs = null;
            if (runningArgs == null) {
                return;
            }
            runningArgs = null;
            try {
                NativeProxy.nativeStop();
            } catch (Throwable e) {
                FileLog.e(e);
            }
        }
    }

    public static void onListening() {
        if (BuildVars.LOGS_ENABLED) {
            FileLog.d("tgws listening");
        }
    }

    // the core died on its own (bad port, crash in a worker). forget it so the next apply restarts it
    // comes from a rust thread, so no lock here: start() may be holding ours while waiting on rust's
    public static void onStopped() {
        try {
            // a late callback from the previous run must not wipe the one we just started
            if (NativeProxy.nativeIsRunning()) {
                return;
            }
        } catch (Throwable ignore) {
        }
        runningArgs = null;
    }

    // a port nothing on the phone listens on right now, used for new TG WS entries
    public static int freePort() {
        try (ServerSocket socket = new ServerSocket(0, 1, InetAddress.getByName(HOST))) {
            return socket.getLocalPort();
        } catch (Exception e) {
            FileLog.e(e);
            return 1443;
        }
    }

    // per-proxy core options, stored in ProxySettings.user as a tiny query string,
    // e.g. "community=1&cf=a.com,b.com&worker=w.workers.dev&dcip=2:149.154.167.220"
    public static final class Options {
        // flowseal's shared cloudflare domains. on by default: on networks that block telegram's
        // own ws endpoints this is what makes the proxy work at all
        public boolean communityDomains = true;
        public String cfDomains = "";
        public String workerDomains = "";
        public String dcIps = "";

        public static Options parse(String raw) {
            final Options options = new Options();
            if (raw == null || raw.isEmpty()) {
                return options;
            }
            for (String pair : raw.split("&")) {
                final int eq = pair.indexOf('=');
                if (eq < 0) continue;
                final String value = pair.substring(eq + 1);
                switch (pair.substring(0, eq)) {
                    case "community": options.communityDomains = !"0".equals(value); break;
                    case "cf": options.cfDomains = value; break;
                    case "worker": options.workerDomains = value; break;
                    case "dcip": options.dcIps = value; break;
                }
            }
            return options;
        }

        @Override
        public String toString() {
            return "community=" + (communityDomains ? "1" : "0")
                    + "&cf=" + clean(cfDomains)
                    + "&worker=" + clean(workerDomains)
                    + "&dcip=" + clean(dcIps);
        }

        String toArgs() {
            final StringBuilder args = new StringBuilder();
            if (communityDomains) {
                args.append(" --default-domains");
            }
            final String cf = clean(cfDomains);
            if (!cf.isEmpty()) {
                args.append(" --cf-domain ").append(cf);
            }
            final String worker = clean(workerDomains);
            if (!worker.isEmpty()) {
                args.append(" --cf-worker-domain ").append(worker);
            }
            // --dc-ip takes one pair per flag, unlike the domain lists
            for (String pair : clean(dcIps).split(",")) {
                if (!pair.isEmpty()) {
                    args.append(" --dc-ip ").append(pair);
                }
            }
            return args.toString();
        }

        // the core gets one space-split command line, and & = would break our own format,
        // so anything outside a domain/ip/list alphabet goes
        private static String clean(String value) {
            return value == null ? "" : value.replaceAll("[^A-Za-z0-9.,:\\-]", "");
        }
    }
}
