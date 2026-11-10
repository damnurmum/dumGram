package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.BuildVars;
import org.telegram.messenger.R;
import org.telegram.messenger.browser.Browser;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.utils.settings.DumSettings;

import java.util.ArrayList;

// settings > dumgram. new fork toggles go here
public class DumSettingsActivity extends UniversalFragment {
    private static final int ID_SHOW_PEER_ID = 1;
    private static final int ID_MESSAGE_SECONDS = 2;
    private static final int ID_VERSION = 3;
    private static final int ID_SOURCE = 4;

    private static final String REPO_URL = "https://github.com/damnurmum/dumgram";

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
        return getString(R.string.DumSettings);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(getString(R.string.DumProfileHeader)));
        items.add(UItem.asCheck(ID_SHOW_PEER_ID, getString(R.string.DumShowPeerId)).setChecked(DumSettings.showPeerId.get()));
        items.add(UItem.asShadow(getString(R.string.DumShowPeerIdInfo)));

        items.add(UItem.asHeader(getString(R.string.DumChatsHeader)));
        items.add(UItem.asCheck(ID_MESSAGE_SECONDS, getString(R.string.DumMessageSeconds)).setChecked(DumSettings.messageSeconds.get()));
        items.add(UItem.asShadow(getString(R.string.DumMessageSecondsInfo)));

        items.add(UItem.asHeader(getString(R.string.DumAboutHeader)));
        items.add(UItem.asButton(ID_VERSION, getString(R.string.DumVersion), BuildVars.BUILD_VERSION_STRING));
        items.add(UItem.asButton(ID_SOURCE, getString(R.string.DumSourceCode)));
        items.add(UItem.asShadow(null));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_SHOW_PEER_ID:
                DumSettings.showPeerId.toggle();
                break;
            case ID_MESSAGE_SECONDS:
                DumSettings.messageSeconds.toggle();
                break;
            case ID_SOURCE:
                Browser.openUrl(getParentActivity(), REPO_URL);
                return;
            default:
                return;
        }
        listView.adapter.update(true);
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
