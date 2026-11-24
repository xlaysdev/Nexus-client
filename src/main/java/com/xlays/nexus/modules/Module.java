package com.xlays.nexus.modules;

import java.util.Locale;

public abstract class Module {
    public enum Category { COMBAT, HUD, MOVEMENT, PLAYER, RENDER, MISC, SETTINGS, PROFILES }

    public final String name;
    public final Category category;
    public boolean enabled;

    protected Module(String name, Category category, boolean enabled) {
        this.name = name;
        this.category = category;
        this.enabled = enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) return;
        this.enabled = enabled;
        if (enabled) onEnable(); else onDisable();
    }

    public final void toggle() { setEnabled(!enabled); }
    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick() {}
    public void onRender() {}

    public final String categoryName() {
        return category.name().toLowerCase(Locale.ROOT);
    }
}
