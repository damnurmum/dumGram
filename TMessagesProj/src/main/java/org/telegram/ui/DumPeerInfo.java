package org.telegram.ui;

import static org.telegram.messenger.LocaleController.getString;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.messenger.UserConfig;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLRPC;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.Cells.TextDetailCell;
import org.telegram.ui.Components.BulletinFactory;

// the "id + data center" row in profiles. kept out of ProfileActivity so that file barely changes
public final class DumPeerInfo {

    private DumPeerInfo() {
    }

    public static void bind(TextDetailCell cell, int account, long userId, long chatId) {
        cell.setTextAndValue(String.valueOf(peerId(userId, chatId)), caption(account, userId, chatId), false);
    }

    public static void copy(BaseFragment fragment, long userId, long chatId) {
        AndroidUtilities.addToClipboard(String.valueOf(peerId(userId, chatId)));
        BulletinFactory.of(fragment).createCopyBulletin(getString(R.string.DumIdCopied)).show();
    }

    private static long peerId(long userId, long chatId) {
        return userId != 0 ? userId : chatId;
    }

    private static String caption(int account, long userId, long chatId) {
        final int dc = dcId(account, userId, chatId);
        if (dc <= 0) {
            return getString(R.string.DumPeerId);
        }
        return LocaleController.formatString(R.string.DumPeerIdDc, dc, city(dc)).trim();
    }

    // there's no api for someone's dc, but their avatar is stored on it, so the photo's dc_id is a good guess.
    // no avatar means we just don't know
    private static int dcId(int account, long userId, long chatId) {
        final MessagesController controller = MessagesController.getInstance(account);
        if (userId != 0) {
            if (userId == UserConfig.getInstance(account).getClientUserId()) {
                return ConnectionsManager.getInstance(account).getCurrentDatacenterId();
            }
            final TLRPC.User user = controller.getUser(userId);
            return user != null && user.photo != null ? user.photo.dc_id : 0;
        }
        final TLRPC.Chat chat = controller.getChat(chatId);
        return chat != null && chat.photo != null ? chat.photo.dc_id : 0;
    }

    private static String city(int dc) {
        switch (dc) {
            case 1:
            case 3:
                return getString(R.string.DumDcMiami);
            case 2:
            case 4:
                return getString(R.string.DumDcAmsterdam);
            case 5:
                return getString(R.string.DumDcSingapore);
            default:
                return "";
        }
    }
}
