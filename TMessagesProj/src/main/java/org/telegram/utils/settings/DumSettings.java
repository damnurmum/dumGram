package org.telegram.utils.settings;

// dumgram's own toggles. lives in this package because BooleanSetting.of is package-private
public final class DumSettings {

    public static final BooleanSetting showPeerId =
        BooleanSetting.of("dum_show_peer_id", true);

    public static final BooleanSetting messageSeconds =
        BooleanSetting.of("dum_message_seconds", false);

    private DumSettings() {
    }
}
