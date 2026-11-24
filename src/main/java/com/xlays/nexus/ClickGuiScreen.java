package com.xlays.nexus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ClickGuiScreen extends Screen {
    private static final List<String> CATEGORIES = List.of("Combat", "HUD", "Movement", "Player", "Render", "Misc", "Settings", "Profiles");
    private final String selected;
    private int left, top, panelWidth, panelHeight, sidebarWidth;
    private EditBox profileName;
    private String notice = "Right Shift  •  Toggle interface";

    public ClickGuiScreen() { this("HUD"); }
    public ClickGuiScreen(String selected) {
        super(Component.literal("NEXUS V1"));
        this.selected = selected;
    }

    @Override
    protected void init() {
        panelWidth = Math.max(300, Math.min(760, width - 20));
        panelHeight = Math.max(260, Math.min(470, height - 20));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        sidebarWidth = Math.max(92, Math.min(145, panelWidth / 4));
        int navY = top + 61;
        int navW = sidebarWidth - 18;
        for (String category : CATEGORIES) {
            final String target = category;
            addRenderableWidget(Button.builder(Component.literal((selected.equals(category) ? "◆ " : "  ") + category),
                b -> Minecraft.getInstance().setScreen(new ClickGuiScreen(target)))
                .bounds(left + 9, navY, navW, 24).build());
            navY += 27;
        }

        int contentX = left + sidebarWidth + 18;
        int contentW = panelWidth - sidebarWidth - 34;
        if (selected.equals("Settings")) {
            addRenderableWidget(Button.builder(Component.literal("ACCENT COLOR  •  " + accentName()),
                b -> { NexusClient.accentIndex = (NexusClient.accentIndex + 1) % NexusClient.ACCENTS.length; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Settings")); })
                .bounds(contentX, top + 91, contentW, 27).build());
            addRenderableWidget(Button.builder(Component.literal("HUD SCALE  −  " + NexusClient.hudScale + "%"),
                b -> { NexusClient.hudScale = Math.max(75, NexusClient.hudScale - 5); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Settings")); })
                .bounds(contentX, top + 124, contentW / 2 - 3, 27).build());
            addRenderableWidget(Button.builder(Component.literal("HUD SCALE  +"),
                b -> { NexusClient.hudScale = Math.min(150, NexusClient.hudScale + 5); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Settings")); })
                .bounds(contentX + contentW / 2 + 3, top + 124, contentW / 2 - 3, 27).build());
            addRenderableWidget(Button.builder(Component.literal("HUD BACKGROUND  •  " + (NexusClient.hudBackground ? "ON" : "OFF")),
                b -> { NexusClient.hudBackground = !NexusClient.hudBackground; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Settings")); })
                .bounds(contentX, top + 157, contentW, 27).build());
            addRenderableWidget(Button.builder(Component.literal("OPEN HUD EDITOR"),
                b -> Minecraft.getInstance().setScreen(new HudEditorScreen()))
                .bounds(contentX, top + 190, contentW, 27).build());
            addRenderableWidget(Button.builder(Component.literal("RESET HUD POSITION"),
                b -> { NexusClient.hudX = 12; NexusClient.hudY = 12; NexusClient.saveConfig(); notice = "HUD position reset."; })
                .bounds(contentX, top + 223, contentW, 27).build());
            addRenderableWidget(Button.builder(Component.literal("SAVE SETTINGS"),
                b -> { NexusClient.saveConfig(); notice = "Settings saved."; })
                .bounds(contentX, top + 256, contentW, 27).build());
        } else if (selected.equals("Profiles")) {
            profileName = new EditBox(font, contentX, top + 91, contentW, 22, Component.literal("Profile name"));
            profileName.setMaxLength(24);
            profileName.setValue("default");
            addRenderableWidget(profileName);
            int half = contentW / 2 - 3;
            addRenderableWidget(Button.builder(Component.literal("SAVE PROFILE"),
                b -> { boolean ok = NexusConfig.saveProfile(profileName.getValue().trim()); notice = ok ? "Profile saved." : "Use 1-24 letters, numbers, _ or -."; })
                .bounds(contentX, top + 120, half, 26).build());
            addRenderableWidget(Button.builder(Component.literal("LOAD PROFILE"),
                b -> { boolean ok = NexusConfig.loadProfile(profileName.getValue().trim()); notice = ok ? "Profile loaded." : "Profile not found or invalid name."; })
                .bounds(contentX + half + 6, top + 120, half, 26).build());
            addRenderableWidget(Button.builder(Component.literal("DELETE PROFILE"),
                b -> { boolean ok = NexusConfig.deleteProfile(profileName.getValue().trim()); notice = ok ? "Profile deleted." : "Profile not found."; })
                .bounds(contentX, top + 152, contentW, 26).build());
            int rowY = top + 193;
            gappedProfileList(contentX, contentW, rowY);
        } else {
            int rowY = top + 91;
            int shown = 0;
            for (NexusClient.Feature feature : NexusClient.FEATURES.values()) {
                if (!feature.category.equals(selected)) continue;
                if (rowY > top + panelHeight - 58) break;
                String state = feature.enabled ? "ON" : "OFF";
                String label = feature.name + "     [" + state + "]";
                addRenderableWidget(Button.builder(Component.literal(label), b -> {
                    NexusClient.toggle(feature);
                    Minecraft.getInstance().setScreen(new ClickGuiScreen(selected));
                }).bounds(contentX, rowY, contentW, 27).build());
                rowY += 33;
                shown++;
            }
            if (shown == 0) notice = "No modules in this category yet.";
        }

        addRenderableWidget(Button.builder(Component.literal("SAVE"),
            b -> { NexusClient.saveConfig(); notice = "Configuration saved."; })
            .bounds(left + 10, top + panelHeight - 38, sidebarWidth - 20, 24).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"),
            b -> Minecraft.getInstance().setScreen(null)).bounds(left + panelWidth - 88, top + panelHeight - 38, 78, 24).build());
    }

    private void gappedProfileList(int x, int w, int y) {
        List<String> profiles = NexusConfig.profiles();
        int shown = 0;
        for (String profile : profiles) {
            if (shown >= 5 || y + shown * 17 > top + panelHeight - 55) break;
            shown++;
        }
        if (profiles.isEmpty()) notice = "No saved profiles yet. Save one above.";
        else notice = "Saved profiles: " + String.join(", ", profiles.subList(0, Math.min(4, profiles.size())));
    }

    private static String accentName() {
        return switch (NexusClient.accentIndex) {
            case 1 -> "BLUE";
            case 2 -> "VIOLET";
            case 3 -> "AMBER";
            case 4 -> "PINK";
            default -> "CYAN";
        };
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x99050A10);
        g.fill(left, top, left + panelWidth, top + panelHeight, 0xF0101822);
        g.fill(left, top, left + panelWidth, top + 3, NexusClient.accent());
        g.fill(left, top + 48, left + panelWidth, top + 49, 0xFF263A46);
        g.fill(left + sidebarWidth, top + 49, left + sidebarWidth + 1, top + panelHeight - 48, 0xFF263A46);
        g.drawString(font, "NEXUS", left + 15, top + 15, NexusClient.accent(), false);
        g.drawString(font, "V1  /  CLIENT CONTROL CENTER", left + 75, top + 16, 0xFFB8C8D0, false);
        g.drawString(font, selected.toUpperCase(), left + sidebarWidth + 18, top + 65, 0xFFFFFFFF, false);
        g.drawString(font, "CLIENT-SIDE UTILITIES", left + 15, top + 51, 0xFF78909C, false);
        g.drawString(font, notice, left + 15, top + panelHeight - 19, 0xFF78909C, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
