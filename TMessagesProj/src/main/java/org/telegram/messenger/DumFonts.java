package org.telegram.messenger;

import android.graphics.Paint;
import android.graphics.Typeface;

import org.telegram.ui.ActionBar.Theme;
import org.telegram.utils.settings.DumSettings;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;

// dumGram's app font: Onest (OFL, fonts/onest_OFL.txt), a geometric face close to Cal Sans that also has cyrillic.
// bold text goes through AndroidUtilities.bold(), that one is easy. regular text is the system default typeface,
// which telegram never sets explicitly (thousands of paints and views), so the default itself gets swapped.
// android has no public api for that, hence reflection; every step is optional and a blocked one
// just leaves that part on the system font. the native default (Typeface.setDefault) is off limits since
// android 9, so bare paints are covered separately: fixThemePaints() and SimpleTextView/AnimatedTextView
public final class DumFonts {

    private static final String REGULAR = "fonts/onest_regular.ttf";
    private static final String SEMIBOLD = "fonts/onest_semibold.ttf";

    private DumFonts() {
    }

    public static boolean enabled() {
        return DumSettings.appFont.get();
    }

    public static Typeface semibold() {
        return AndroidUtilities.getTypeface(SEMIBOLD);
    }

    // plenty of places load telegram's roboto files by path instead of going through bold(), hand them onest too.
    // mono, condensed, numbers and the article serif keep their own look
    public static Typeface instead(String assetPath) {
        if (assetPath == null || !assetPath.startsWith("fonts/r") || !enabled()) return null;
        switch (assetPath) {
            case AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM:
            case AndroidUtilities.TYPEFACE_ROBOTO_EXTRA_BOLD:
                return semibold();
            case AndroidUtilities.TYPEFACE_ROBOTO_MEDIUM_ITALIC:
                return Typeface.create(semibold(), Typeface.ITALIC);
            case "fonts/ritalic.ttf":
                return Typeface.create(AndroidUtilities.getTypeface(REGULAR), Typeface.ITALIC);
            default:
                return null;
        }
    }

    public static void apply() {
        if (!enabled()) return;
        final Typeface regular = AndroidUtilities.getTypeface(REGULAR);
        final Typeface semibold = semibold();
        if (regular == null || semibold == null) return;

        // typeface fields people read directly
        setStatic("DEFAULT", regular);
        setStatic("SANS_SERIF", regular);
        setStatic("DEFAULT_BOLD", semibold);

        // what Typeface.create(null, style) hands out, that's how TextViews without a font get theirs
        try {
            final Field field = Typeface.class.getDeclaredField("sDefaults");
            field.setAccessible(true);
            field.set(null, new Typeface[] {
                regular,
                semibold,
                Typeface.create(regular, Typeface.ITALIC),
                Typeface.create(semibold, Typeface.BOLD_ITALIC),
            });
        } catch (Throwable e) {
            FileLog.e("dumfonts sDefaults: " + e);
        }

        // "sans-serif" by name, used by fontFamily lookups
        try {
            final Field field = Typeface.class.getDeclaredField("sSystemFontMap");
            field.setAccessible(true);
            @SuppressWarnings("unchecked")
            final Map<String, Typeface> map = (Map<String, Typeface>) field.get(null);
            if (map != null) {
                map.put("sans-serif", regular);
            }
        } catch (Throwable e) {
            FileLog.e("dumfonts sSystemFontMap: " + e);
        }
    }

    // Theme's shared paints (message text, chat list rows...) are made without a typeface, so they draw with
    // the native default that android won't let us swap. give the bare ones our regular face instead.
    // called from Theme's apply*Theme, which runs after the paints are created and on every theme change
    public static void fixThemePaints() {
        if (!enabled()) return;
        final Typeface regular = AndroidUtilities.getTypeface(REGULAR);
        if (regular == null) return;
        for (Field field : Theme.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers())) continue;
            final Class<?> type = field.getType();
            try {
                if (Paint.class.isAssignableFrom(type)) {
                    field.setAccessible(true);
                    setIfBare((Paint) field.get(null), regular);
                } else if (type.isArray() && Paint.class.isAssignableFrom(type.getComponentType())) {
                    field.setAccessible(true);
                    final Paint[] paints = (Paint[]) field.get(null);
                    if (paints != null) {
                        for (Paint paint : paints) setIfBare(paint, regular);
                    }
                }
            } catch (Throwable ignore) {
            }
        }
    }

    // only paints nobody gave a font to, bold/mono/italic ones keep theirs
    private static void setIfBare(Paint paint, Typeface typeface) {
        if (paint != null && paint.getTypeface() == null) {
            paint.setTypeface(typeface);
        }
    }

    private static void setStatic(String name, Typeface typeface) {
        try {
            final Field field = Typeface.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, typeface);
        } catch (Throwable e) {
            FileLog.e("dumfonts " + name + ": " + e);
        }
    }
}
