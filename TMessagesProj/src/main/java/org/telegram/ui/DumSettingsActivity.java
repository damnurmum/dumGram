package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.Components.IconBackgroundColors;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.web.WebBrowserSettings;
import org.telegram.utils.settings.BooleanSetting;
import org.telegram.utils.settings.DumSettings;

import java.util.ArrayList;
import java.util.Locale;

// settings > dumGram. the main page lists the categories and searches across all of them,
// each category opens the same screen with its own options
public class DumSettingsActivity extends UniversalFragment {

    private static final int CAT_PROFILE = 1;
    private static final int CAT_CHATS = 2;
    private static final int CAT_INTERFACE = 3;
    private static final int CAT_NETWORK = 4;
    private static final int[] CATEGORIES = {CAT_PROFILE, CAT_CHATS, CAT_INTERFACE, CAT_NETWORK};

    private static final int ID_SHOW_PEER_ID = 1;
    private static final int ID_HIDE_PHONE = 2;
    private static final int ID_MESSAGE_SECONDS = 3;
    private static final int ID_TAB_CONTACTS = 4;
    private static final int ID_TAB_SETTINGS = 5;
    private static final int ID_TAB_PROFILE = 6;
    private static final int ID_PROXIES = 7;
    private static final int ID_BROWSER = 8;
    private static final int ID_APP_FONT = 9;

    private static final int ID_CATEGORY = 100; // + category
    private static final int ID_DONATE = 200;
    private static final int ID_VERSION = 201;
    private static final int ID_SOURCE = 202;
    private static final int ID_RESTART = 203;

    private static final String REPO_URL = "https://github.com/damnurmum/dumGram";

    // every option once, so the category pages and the search can't drift apart
    private static final class Option {
        final int id, category, title, info;
        final BooleanSetting setting; // null means it opens something instead of toggling
        final boolean inverted; // the tab settings are stored as "hide", shown as "show"

        Option(int id, int category, int title, int info, BooleanSetting setting, boolean inverted) {
            this.id = id;
            this.category = category;
            this.title = title;
            this.info = info;
            this.setting = setting;
            this.inverted = inverted;
        }

        boolean checked() {
            return setting.get() != inverted;
        }
    }

    private static final Option[] OPTIONS = {
        new Option(ID_SHOW_PEER_ID, CAT_PROFILE, R.string.DumShowPeerId, R.string.DumShowPeerIdInfo, DumSettings.showPeerId, false),
        new Option(ID_HIDE_PHONE, CAT_PROFILE, R.string.DumHidePhone, R.string.DumHidePhoneInfo, DumSettings.hidePhone, false),
        new Option(ID_MESSAGE_SECONDS, CAT_CHATS, R.string.DumMessageSeconds, R.string.DumMessageSecondsInfo, DumSettings.messageSeconds, false),
        new Option(ID_APP_FONT, CAT_INTERFACE, R.string.DumAppFont, R.string.DumAppFontInfo, DumSettings.appFont, false),
        new Option(ID_TAB_CONTACTS, CAT_INTERFACE, R.string.MainTabsContacts, 0, DumSettings.hideContactsTab, true),
        new Option(ID_TAB_SETTINGS, CAT_INTERFACE, R.string.DumTabSettingsOrCalls, 0, DumSettings.hideSettingsTab, true),
        new Option(ID_TAB_PROFILE, CAT_INTERFACE, R.string.MainTabsProfile, R.string.DumTabsInfo, DumSettings.hideProfileTab, true),
        new Option(ID_PROXIES, CAT_NETWORK, R.string.DumProxies, R.string.DumProxiesInfo, null, false),
        new Option(ID_BROWSER, CAT_NETWORK, R.string.DumBrowser, R.string.DumBrowserInfo, null, false),
    };

    // tabs and the font are read once at startup, so after touching them we offer a restart.
    // static: the flag has to survive going back to the main page and into the category again
    private static boolean restartNeeded;

    private final int category; // 0 = the main page
    private String query = "";

    public DumSettingsActivity() {
        this(0);
    }

    private DumSettingsActivity(int category) {
        this.category = category;
    }

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        fragmentView = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        if (category == 0) {
            final ActionBarMenuItem searchItem = simpleActionBar.createMenu().addItem(0, R.drawable.outline_header_search, resourceProvider)
                .setIsSearchField(true)
                .setActionBarMenuItemSearchListener(new ActionBarMenuItem.ActionBarMenuItemSearchListener() {
                    @Override
                    public void onSearchCollapse() {
                        query = "";
                        listView.adapter.update(true);
                    }

                    @Override
                    public void onTextChanged(EditText editText) {
                        query = editText.getText().toString().trim();
                        listView.adapter.update(true);
                    }
                });
            searchItem.setSearchFieldHint(getString(R.string.Search));
        }
        return fragmentView;
    }

    @Override
    protected RecyclerListView getListViewForSimpleGlass() {
        return listView;
    }

    @Override
    public boolean isSupportEdgeToEdge() {
        return true;
    }

    @Override
    protected CharSequence getTitle() {
        return category == 0 ? getString(R.string.DumSettings) : getString(categoryTitle(category));
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        if (category != 0) {
            fillCategory(items);
        } else if (!query.isEmpty()) {
            fillSearch(items);
        } else {
            fillMain(items);
        }
    }

    private void fillMain(ArrayList<UItem> items) {
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CATEGORY + CAT_PROFILE, IconBackgroundColors.BLUE.top, IconBackgroundColors.BLUE.bottom,
            R.drawable.settings_account, getString(R.string.DumCatProfile), getString(R.string.DumCatProfileInfo)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CATEGORY + CAT_CHATS, IconBackgroundColors.ORANGE.top, IconBackgroundColors.ORANGE.bottom,
            R.drawable.settings_chat, getString(R.string.DumChatsHeader), getString(R.string.DumCatChatsInfo)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CATEGORY + CAT_INTERFACE, IconBackgroundColors.PURPLE.top, IconBackgroundColors.PURPLE.bottom,
            R.drawable.settings_features, getString(R.string.DumCatInterface), getString(R.string.DumCatInterfaceInfo)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_CATEGORY + CAT_NETWORK, IconBackgroundColors.CYAN.top, IconBackgroundColors.CYAN.bottom,
            R.drawable.settings_data, getString(R.string.DumCatNetwork), getString(R.string.DumCatNetworkInfo)));
        items.add(UItem.asShadow(null));

        // the logo's reds, so it reads as dumGram's own row
        items.add(SettingsActivity.SettingCell.Factory.of(ID_DONATE, 0xFFE83030, 0xFFA62C2C,
            R.drawable.dum_heart, getString(R.string.DumDonate), getString(R.string.DumDonateInfo)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.DumAboutHeader)));
        items.add(UItem.asButton(ID_VERSION, getString(R.string.DumVersion), BuildVars.BUILD_VERSION_STRING));
        items.add(UItem.asButton(ID_SOURCE, getString(R.string.DumSourceCode)));
        items.add(UItem.asShadow(null));
    }

    private void fillCategory(ArrayList<UItem> items) {
        for (Option option : OPTIONS) {
            if (option.category != category) continue;
            items.add(optionItem(option));
            if (option.info != 0) {
                items.add(UItem.asShadow(getString(option.info)));
            }
        }
        if (category == CAT_INTERFACE && restartNeeded) {
            items.add(UItem.asButton(ID_RESTART, getString(R.string.DumRestart)).accent());
            items.add(UItem.asShadow(null));
        }
    }

    // matches on the option name and its description, grouped under the category it lives in
    private void fillSearch(ArrayList<UItem> items) {
        final String q = query.toLowerCase(Locale.ROOT);
        boolean found = false;
        for (int cat : CATEGORIES) {
            boolean header = false;
            for (Option option : OPTIONS) {
                if (option.category != cat || !matches(option, q)) continue;
                if (!header) {
                    items.add(UItem.asHeader(getString(categoryTitle(cat))));
                    header = true;
                }
                items.add(optionItem(option));
            }
            if (header) {
                items.add(UItem.asShadow(null));
                found = true;
            }
        }
        if (!found) {
            items.add(UItem.asShadow(getString(R.string.DumSearchEmpty)));
        }
    }

    private static boolean matches(Option option, String q) {
        return getString(option.title).toLowerCase(Locale.ROOT).contains(q)
            || option.info != 0 && getString(option.info).toLowerCase(Locale.ROOT).contains(q);
    }

    private static UItem optionItem(Option option) {
        if (option.setting != null) {
            return UItem.asCheck(option.id, getString(option.title)).setChecked(option.checked());
        }
        return UItem.asButton(option.id, getString(option.title));
    }

    private static int categoryTitle(int category) {
        switch (category) {
            case CAT_PROFILE: return R.string.DumCatProfile;
            case CAT_CHATS: return R.string.DumChatsHeader;
            case CAT_INTERFACE: return R.string.DumCatInterface;
            default: return R.string.DumCatNetwork;
        }
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id > ID_CATEGORY && item.id < ID_DONATE) {
            presentFragment(new DumSettingsActivity(item.id - ID_CATEGORY));
            return;
        }
        switch (item.id) {
            case ID_DONATE:
                presentFragment(new DumDonateActivity());
                return;
            case ID_SOURCE:
                Browser.openUrl(getParentActivity(), REPO_URL);
                return;
            case ID_RESTART:
                restartApp();
                return;
            case ID_PROXIES:
                presentFragment(new ProxyListActivity());
                return;
            case ID_BROWSER:
                presentFragment(new WebBrowserSettings(null));
                return;
        }
        for (Option option : OPTIONS) {
            if (option.id == item.id && option.setting != null) {
                option.setting.toggle();
                if (option.category == CAT_INTERFACE) {
                    restartNeeded = true;
                }
                listView.adapter.update(true);
                return;
            }
        }
    }

    // same trick DebugSettingsActivity uses for the tablet mode switch
    private void restartApp() {
        final Activity activity = getParentActivity();
        if (activity == null) return;
        final Intent intent = activity.getPackageManager().getLaunchIntentForPackage(activity.getPackageName());
        activity.finishAffinity();
        activity.startActivity(intent);
        System.exit(0);
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
