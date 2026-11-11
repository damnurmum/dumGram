package org.telegram.utils.settings;

// dumgram's own toggles. lives in this package because BooleanSetting.of is package-private
public final class DumSettings {

    public static final BooleanSetting showPeerId =
        BooleanSetting.of("dum_show_peer_id", true);

    public static final BooleanSetting messageSeconds =
        BooleanSetting.of("dum_message_seconds", false);

    public static final BooleanSetting appFont =
        BooleanSetting.of("dum_app_font", true);

    public static final BooleanSetting hidePhone =
        BooleanSetting.of("dum_hide_phone", false);

    // bottom tabs. chats is not here on purpose, it can't be hidden
    public static final BooleanSetting hideContactsTab =
        BooleanSetting.of("dum_hide_contacts_tab", false);

    public static final BooleanSetting hideSettingsTab =
        BooleanSetting.of("dum_hide_settings_tab", false);

    public static final BooleanSetting hideProfileTab =
        BooleanSetting.of("dum_hide_profile_tab", false);

    private DumSettings() {
    }
}
