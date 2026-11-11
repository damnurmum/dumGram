package org.telegram.ui.Components.blur3.drawable.color;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;

import org.telegram.messenger.LiteMode;
import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeBind;

public class BlurredBackgroundColorProviderThemed implements BlurredBackgroundColorProvider {

    private final Theme.ResourcesProvider resourcesProvider;
    private final int backgroundColorId;
    private float alpha;

    public BlurredBackgroundColorProviderThemed(Theme.ResourcesProvider resourcesProvider, int backgroundColorId) {
        this(resourcesProvider, backgroundColorId, LiteMode.isEnabled(LiteMode.FLAG_LIQUID_GLASS) ? 0.85f : 0.76f);
    }

    public BlurredBackgroundColorProviderThemed(Theme.ResourcesProvider resourcesProvider, int backgroundColorId, float alpha) {
        this.resourcesProvider = resourcesProvider;
        this.backgroundColorId = backgroundColorId;
        this.alpha = alpha;

        updateColors();
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
        updateColors();
    }

    private int backgroundColor, shadowColor, strokeColorTop, strokeColorBottom;

    public boolean isDark() {
        final int color = Theme.getColor(backgroundColorId, resourcesProvider);
        return AndroidUtilities.computePerceivedBrightness(color) < .721f;
    }

    private int themeGeneration = ThemeBind.generation();

    // not a view: nothing dispatches updateColors() on theme switches for every owner, so the getters refresh lazily
    private void checkThemeGeneration() {
        final int generation = ThemeBind.generation();
        if (themeGeneration != generation) {
            updateColors();
        }
    }

    public void updateColors() {
        themeGeneration = ThemeBind.generation();
        final int color = Theme.getColor(backgroundColorId, resourcesProvider);
        backgroundColor = Theme.multAlpha(color, alpha);

        if (isDark()) {
            strokeColorTop = 0x28FFFFFF;
            strokeColorBottom = 0x14FFFFFF;
            shadowColor = 0;
        } else {
            strokeColorTop = 0xFFFFFFFF;
            strokeColorBottom = 0xFFFFFFFF;
            shadowColor = 0x20000000; //0x19000000;
        }
    }

    @Override
    public int getShadowColor() {
        checkThemeGeneration();
        return shadowColor;
    }

    @Override
    public int getBackgroundColor() {
        checkThemeGeneration();
        return backgroundColor;
    }

    @Override
    public int getStrokeColorTop() {
        checkThemeGeneration();
        return strokeColorTop;
    }

    @Override
    public int getStrokeColorBottom() {
        checkThemeGeneration();
        return strokeColorBottom;
    }
}

