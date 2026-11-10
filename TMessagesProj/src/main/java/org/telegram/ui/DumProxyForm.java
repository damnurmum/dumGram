package org.telegram.ui;

import android.content.Context;
import android.text.InputType;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeBind;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.utils.proxy.DumProxyNames;
import org.telegram.utils.proxy.ProxySettings;
import org.telegram.utils.proxy.TgWsProxy;

// dumgram's additions to the proxy editor: a name for any proxy and the TG WS core options.
// lives outside ProxySettingsActivity so that telegram file only gets a few hook-up lines
public class DumProxyForm {

    public final FrameLayout nameRow;
    public final LinearLayout tgWsBlock;

    private final EditTextBoldCursor nameField;
    private final EditTextBoldCursor cfField;
    private final EditTextBoldCursor workerField;
    private final EditTextBoldCursor dcIpField;
    private final TextCheckCell communityCell;

    public DumProxyForm(Context context) {
        nameField = createField(context, LocaleController.getString(R.string.DumProxyName), InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
        nameRow = wrap(context, nameField, 12);

        tgWsBlock = new LinearLayout(context);
        tgWsBlock.setOrientation(LinearLayout.VERTICAL);

        final HeaderCell header = new HeaderCell(context);
        header.setText(LocaleController.getString(R.string.DumProxyTgWsRouting));
        tgWsBlock.addView(header);

        communityCell = new TextCheckCell(context);
        communityCell.setTextAndCheck(LocaleController.getString(R.string.DumProxyTgWsCommunity), true, false);
        communityCell.setOnClickListener(v -> communityCell.setChecked(!communityCell.isChecked()));
        tgWsBlock.addView(communityCell, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 50));

        cfField = createField(context, LocaleController.getString(R.string.DumProxyTgWsCfDomain), InputType.TYPE_TEXT_VARIATION_URI);
        tgWsBlock.addView(wrap(context, cfField, 0), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 64));
        workerField = createField(context, LocaleController.getString(R.string.DumProxyTgWsWorker), InputType.TYPE_TEXT_VARIATION_URI);
        tgWsBlock.addView(wrap(context, workerField, 0), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 64));
        dcIpField = createField(context, LocaleController.getString(R.string.DumProxyTgWsDcIp), InputType.TYPE_TEXT_VARIATION_URI);
        tgWsBlock.addView(wrap(context, dcIpField, 0), LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, 64));
    }

    public void load(ProxySettings settings) {
        nameField.setText(DumProxyNames.get(settings));
        final TgWsProxy.Options options = TgWsProxy.Options.parse(
                settings.getType() == ProxySettings.Type.TG_WS ? settings.getUser() : "");
        communityCell.setChecked(options.communityDomains);
        cfField.setText(options.cfDomains);
        workerField.setText(options.workerDomains);
        dcIpField.setText(options.dcIps);
    }

    public String readName() {
        return nameField.getText().toString().trim();
    }

    public TgWsProxy.Options readOptions() {
        final TgWsProxy.Options options = new TgWsProxy.Options();
        options.communityDomains = communityCell.isChecked();
        options.cfDomains = cfField.getText().toString();
        options.workerDomains = workerField.getText().toString();
        options.dcIps = dcIpField.getText().toString();
        return options;
    }

    // same look as the stock fields in ProxySettingsActivity
    private static EditTextBoldCursor createField(Context context, String hint, int inputVariation) {
        final EditTextBoldCursor field = new EditTextBoldCursor(context);
        field.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        ThemeBind.editHintColor(field, Theme.key_windowBackgroundWhiteHintText);
        ThemeBind.textColor(field, Theme.key_windowBackgroundWhiteBlackText);
        field.setBackground(null);
        ThemeBind.cursorColor(field, Theme.key_windowBackgroundWhiteBlackText);
        field.setCursorSize(AndroidUtilities.dp(20));
        field.setCursorWidth(1.5f);
        field.setSingleLine(true);
        field.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        ThemeBind.editHeaderHintColor(field, Theme.key_windowBackgroundWhiteBlueHeader);
        field.setTransformHintToHeader(true);
        ThemeBind.lineColors(field, Theme.key_windowBackgroundWhiteInputField, Theme.key_windowBackgroundWhiteInputFieldActivated, Theme.key_text_RedRegular);
        field.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | inputVariation);
        field.setImeOptions(EditorInfo.IME_ACTION_NEXT | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
        field.setHintText(hint);
        field.setPadding(0, 0, 0, 0);
        return field;
    }

    private static FrameLayout wrap(Context context, EditTextBoldCursor field, int topMargin) {
        final FrameLayout container = new FrameLayout(context);
        container.addView(field, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.LEFT | Gravity.TOP, 17, topMargin, 17, 0));
        return container;
    }
}
