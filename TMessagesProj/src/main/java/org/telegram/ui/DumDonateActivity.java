package org.telegram.ui;

import static org.telegram.messenger.AndroidUtilities.dp;
import static org.telegram.messenger.LocaleController.getString;

import android.content.Context;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeBind;
import org.telegram.ui.Components.BulletinFactory;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.QRCodeBottomSheet;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;

import java.util.ArrayList;

// settings > dumGram > thank the author
public class DumDonateActivity extends UniversalFragment {

    // one TON wallet takes both TON and USDT (a jetton on TON). checksum verified
    private static final String ADDRESS = "UQBNK1hutrzHnRYUAi78IFhKJOb3hDT84VsSWqN-MkV-c6fJ";

    private static final int ID_USDT = 1;
    private static final int ID_TON = 2;
    private static final int ID_COPY = 3;

    private View header;
    private View address;

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        header = createHeader(context);
        address = createAddress(context);
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
        return getString(R.string.DumDonate);
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asCustom(header));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(getString(R.string.DumDonateWhere)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_USDT, 0xFF26A17B, 0xFF1A8566,
            R.drawable.dum_usdt, "USDT", getString(R.string.DumDonateNetwork)));
        items.add(SettingsActivity.SettingCell.Factory.of(ID_TON, 0xFF2AABEE, 0xFF0088CC,
            R.drawable.settings_gram_24, getString(R.string.DumDonateTon), getString(R.string.DumDonateNetwork)));
        items.add(UItem.asShadow(getString(R.string.DumDonateHint)));

        items.add(UItem.asHeader(getString(R.string.DumDonateAddress)));
        items.add(UItem.asCustom(address));
        items.add(UItem.asButton(ID_COPY, R.drawable.msg_copy, getString(R.string.DumDonateCopy)).accent());
        items.add(UItem.asShadow(getString(R.string.DumDonateSameAddress)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_USDT:
                // a bare address: wallets would read a ton:// transfer link as TON, not USDT
                showQr(getString(R.string.DumDonateQrUsdt), ADDRESS);
                break;
            case ID_TON:
                showQr(getString(R.string.DumDonateTon), "ton://transfer/" + ADDRESS);
                break;
            case ID_COPY:
                copyAddress();
                break;
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }

    private void showQr(String title, String link) {
        showDialog(new QRCodeBottomSheet(getContext(), title, link, getString(R.string.DumDonateQrHelp), true, resourceProvider));
    }

    private void copyAddress() {
        AndroidUtilities.addToClipboard(ADDRESS);
        BulletinFactory.of(this).createCopyBulletin(getString(R.string.DumAddressCopied)).show();
    }

    private View createHeader(Context context) {
        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setPadding(dp(24), dp(8), dp(24), dp(20));

        // the splash drawable is the logo with air around it, so it sits nicely without extra margins
        final ImageView logo = new ImageView(context);
        logo.setImageResource(R.drawable.dum_splash);
        layout.addView(logo, LayoutHelper.createLinear(132, 132, Gravity.CENTER_HORIZONTAL));

        final TextView title = new TextView(context);
        title.setText(getString(R.string.DumDonate));
        title.setTypeface(AndroidUtilities.bold());
        title.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22);
        title.setGravity(Gravity.CENTER);
        ThemeBind.textColor(title, Theme.key_windowBackgroundWhiteBlackText);
        layout.addView(title, LayoutHelper.createLinear(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, -4, 0, 0));

        final TextView text = new TextView(context);
        text.setText(getString(R.string.DumDonateText));
        text.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        text.setGravity(Gravity.CENTER);
        text.setLineSpacing(dp(2), 1f);
        ThemeBind.textColor(text, Theme.key_windowBackgroundWhiteGrayText);
        layout.addView(text, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL, 0, 8, 0, 0));
        return layout;
    }

    // the whole address in mono so every character is easy to check by eye, tap copies it
    private View createAddress(Context context) {
        final TextView view = new TextView(context);
        view.setText(ADDRESS);
        view.setTypeface(AndroidUtilities.getTypeface(AndroidUtilities.TYPEFACE_ROBOTO_MONO));
        view.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        view.setLineSpacing(dp(3), 1f);
        view.setPadding(dp(21), dp(12), dp(21), dp(12));
        ThemeBind.textColor(view, Theme.key_windowBackgroundWhiteBlackText);
        view.setBackground(Theme.getSelectorDrawable(false));
        view.setOnClickListener(v -> copyAddress());
        return view;
    }
}
