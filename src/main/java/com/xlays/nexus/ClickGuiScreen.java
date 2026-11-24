package com.xlays.nexus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final List<String> CATEGORIES = List.of("HUD", "Render", "Movement", "Player", "Misc", "Settings", "Profiles");
    private final String selected;
    private int left, top, panelWidth, panelHeight, sidebarWidth;

    public ClickGuiScreen() { this("HUD"); }
    public ClickGuiScreen(String selected) {
        super(Component.literal("NEXUS V1"));
        this.selected = selected;
    }

    @Override
    protected void init() {
        panelWidth = Math.min(720, width - 20);
        panelHeight = Math.min(430, height - 20);
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        sidebarWidth = Math.max(92, Math.min(142, panelWidth / 4));
        int navY = top + 62;
        int navW = sidebarWidth - 18;
        for (String category : CATEGORIES) {
            final String target = category;
            addRenderableWidget(Button.builder(Component.literal((selected.equals(category) ? "◆  " : "   ") + category),
                b -> Minecraft.getInstance().setScreen(new ClickGuiScreen(target)))
                .bounds(left + 9, navY, navW, 24).build());
            navY += 29;
        }

        int contentX = left + sidebarWidth + 18;
        int contentW = panelWidth - sidebarWidth - 34;
        if (selected.equals("Profiles")) {
            addRenderableWidget(Button.builder(Component.literal("SAVE CURRENT PROFILE"),
                b -> NexusClient.saveConfig()).bounds(contentX, top + 94, contentW, 28).build());
            addRenderableWidget(Button.builder(Component.literal("PROFILE: DEFAULT"),
                b -> Minecraft.getInstance().setScreen(new ClickGuiScreen("HUD"))).bounds(contentX, top + 130, contentW, 28).build());
        } else {
            int rowY = top + 91;
            int shown = 0;
            for (NexusClient.Feature feature : NexusClient.FEATURES.values()) {
                if (!feature.category.equals(selected)) continue;
                if (rowY > top + panelHeight - 58) break;
                String state = feature.enabled ? "ON" : "OFF";
                String label = feature.name + "     [" + state + "]";
                addRenderableWidget(Button.builder(Component.literal(label),
                    b -> {
                        NexusClient.toggle(feature);
                        Minecraft.getInstance().setScreen(new ClickGuiScreen(selected));
                    }).bounds(contentX, rowY, contentW, 27).build());
                rowY += 34;
                shown++;
            }
            if (shown == 0) {
                // Settings and future categories can remain empty without breaking the layout.
            }
        }

        addRenderableWidget(Button.builder(Component.literal("SAVE"),
            b -> NexusClient.saveConfig()).bounds(left + 10, top + panelHeight - 39, sidebarWidth - 20, 25).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"),
            b -> Minecraft.getInstance().setScreen(null)).bounds(left + panelWidth - 91, top + panelHeight - 39, 80, 25).build());
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x99050A10);
        g.fill(left, top, left + panelWidth, top + panelHeight, 0xF0101822);
        g.fill(left, top, left + panelWidth, top + 3, 0xFF45E6C4);
        g.fill(left, top + 48, left + panelWidth, top + 49, 0xFF263A46);
        g.fill(left + sidebarWidth, top + 49, left + sidebarWidth + 1, top + panelHeight - 48, 0xFF263A46);
        g.drawString(font, "NEXUS", left + 15, top + 15, 0xFF45E6C4, false);
        g.drawString(font, "V1  /  CLIENT CONTROL CENTER", left + 75, top + 16, 0xFFB8C8D0, false);
        g.drawString(font, selected.toUpperCase(), left + sidebarWidth + 18, top + 65, 0xFFFFFFFF, false);
        g.drawString(font, "CLIENT-SIDE UTILITIES", left + 15, top + 51, 0xFF78909C, false);
        g.drawString(font, "Right Shift  •  Toggle interface", left + 15, top + panelHeight - 19, 0xFF78909C, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
