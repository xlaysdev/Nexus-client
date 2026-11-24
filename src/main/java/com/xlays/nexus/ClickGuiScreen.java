package com.xlays.nexus;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.xlays.nexus.modules.movement.MovementSettings;
import com.xlays.nexus.modules.render.VisualSettings;
import com.xlays.nexus.modules.render.OreESP;
import com.xlays.nexus.modules.Module;

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
        panelWidth = Math.max(1, Math.min(760, width - 16));
        panelHeight = Math.max(1, Math.min(620, height - 16));
        left = (width - panelWidth) / 2;
        top = (height - panelHeight) / 2;
        sidebarWidth = Math.min(Math.max(1, panelWidth - 8), Math.max(50, Math.min(145, panelWidth / 4)));
        int navY = top + 61;
        int navW = Math.max(1, sidebarWidth - 18);
        for (String category : CATEGORIES) {
            final String target = category;
            addRenderableWidget(Button.builder(Component.literal((selected.equals(category) ? "◆ " : "  ") + category),
                b -> Minecraft.getInstance().setScreen(new ClickGuiScreen(target)))
                .bounds(left + 9, navY, navW, 24).build());
            navY += 27;
        }

        int contentX = left + sidebarWidth + 18;
        int contentW = Math.max(1, panelWidth - sidebarWidth - 34);
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
        } else if (selected.equals("Movement")) {
            int half = Math.max(1, contentW / 2 - 3);
            addRenderableWidget(Button.builder(Component.literal("SPEED  -  " + String.format("%.2fx", MovementSettings.speedMultiplier)),
                b -> { MovementSettings.speedMultiplier = Math.max(1.0F, MovementSettings.speedMultiplier - 0.1F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX, top + 91, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("SPEED  +"),
                b -> { MovementSettings.speedMultiplier = Math.min(3.0F, MovementSettings.speedMultiplier + 0.1F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX + half + 6, top + 91, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("SPEED MODE  -  " + MovementSettings.speedMode),
                b -> { MovementSettings.speedMode = switch (MovementSettings.speedMode) { case "Normal" -> "Bhop"; case "Bhop" -> "Timer"; default -> "Normal"; }; Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX, top + 121, contentW, 25).build());
            addRenderableWidget(Button.builder(Component.literal("VERTICAL SPEED  -  " + String.format("%.2f", MovementSettings.verticalSpeed)),
                b -> { MovementSettings.verticalSpeed = Math.max(0.1F, MovementSettings.verticalSpeed - 0.05F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX, top + 151, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("VERTICAL SPEED  +"),
                b -> { MovementSettings.verticalSpeed = Math.min(1.5F, MovementSettings.verticalSpeed + 0.05F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX + half + 6, top + 151, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("KNOCKBACK  -  " + String.format("%.1fx", MovementSettings.velocityScale)),
                b -> { MovementSettings.velocityScale = Math.max(0.0F, MovementSettings.velocityScale - 0.1F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX, top + 181, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("KNOCKBACK  +"),
                b -> { MovementSettings.velocityScale = Math.min(1.0F, MovementSettings.velocityScale + 0.1F); Minecraft.getInstance().setScreen(new ClickGuiScreen("Movement")); })
                .bounds(contentX + half + 6, top + 181, half, 25).build());
        } else if (selected.equals("Render")) {
            int half = contentW / 2 - 3;
            addRenderableWidget(Button.builder(Component.literal("ESP BOX  •  " + (VisualSettings.esp3d ? "3D" : "2D")),
                b -> { VisualSettings.esp3d = !VisualSettings.esp3d; Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 91, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("ENEMY COLOR  •  " + String.format("#%06X", VisualSettings.enemyColor & 0xFFFFFF)),
                b -> { VisualSettings.enemyColor = cycleColor(VisualSettings.enemyColor); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 91, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("FRIEND COLOR  •  " + String.format("#%06X", VisualSettings.friendColor & 0xFFFFFF)),
                b -> { VisualSettings.friendColor = cycleColor(VisualSettings.friendColor); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 121, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("BLOCK COLOR  •  " + String.format("#%06X", VisualSettings.blockColor & 0xFFFFFF)),
                b -> { VisualSettings.blockColor = cycleColor(VisualSettings.blockColor); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 121, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("NAMETAGS  •  " + (VisualSettings.showNametags ? "ON" : "OFF")),
                b -> { VisualSettings.showNametags = !VisualSettings.showNametags; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 151, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("TRACERS  •  " + (VisualSettings.showTracers ? "ON" : "OFF")),
                b -> { VisualSettings.showTracers = !VisualSettings.showTracers; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 151, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("CROSSHAIR SIZE  −  " + VisualSettings.crosshairSize),
                b -> { VisualSettings.crosshairSize = Math.max(2, VisualSettings.crosshairSize - 1); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 181, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("CROSSHAIR SIZE  +"),
                b -> { VisualSettings.crosshairSize = Math.min(14, VisualSettings.crosshairSize + 1); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 181, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("CROSSHAIR COLOR  •  " + String.format("#%06X", VisualSettings.crosshairColor & 0xFFFFFF)),
                b -> { VisualSettings.crosshairColor = cycleColor(VisualSettings.crosshairColor); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 211, contentW, 25).build());
            addRenderableWidget(Button.builder(Component.literal("DIAMOND ORE  •  " + (VisualSettings.scanDiamond ? "ON" : "OFF")),
                b -> { VisualSettings.scanDiamond = !VisualSettings.scanDiamond; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 241, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("ANCIENT DEBRIS  •  " + (VisualSettings.scanDebris ? "ON" : "OFF")),
                b -> { VisualSettings.scanDebris = !VisualSettings.scanDebris; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 241, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("CHESTS  •  " + (VisualSettings.scanChests ? "ON" : "OFF")),
                b -> { VisualSettings.scanChests = !VisualSettings.scanChests; NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 271, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("ORE RADIUS  −  " + OreESP.getRadius()),
                b -> { OreESP.setRadius(OreESP.getRadius() - 2); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX, top + 301, half, 25).build());
            addRenderableWidget(Button.builder(Component.literal("ORE RADIUS  +"),
                b -> { OreESP.setRadius(OreESP.getRadius() + 2); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                .bounds(contentX + half + 6, top + 301, half, 25).build());
            int oreIndex = 0;
            for (OreESP.OreGroup group : OreESP.OreGroup.values()) {
                int col = oreIndex % 3;
                int row = oreIndex / 3;
                int cellW = Math.max(1, (contentW - 12) / 3);
                addRenderableWidget(Button.builder(Component.literal(group.label + "  " + (OreESP.isEnabled(group) ? "ON" : "OFF")),
                    b -> { OreESP.setEnabled(group, !OreESP.isEnabled(group)); NexusClient.saveConfig(); Minecraft.getInstance().setScreen(new ClickGuiScreen("Render")); })
                    .bounds(contentX + col * (cellW + 6), top + 331 + row * 28, cellW, 24).build());
                oreIndex++;
            }
            int rowY = top + 421;
            for (Module feature : NexusClient.MODULES.all()) {
                if (!feature.categoryName().equalsIgnoreCase(selected)) continue;
                if (rowY > top + panelHeight - 58) break;
                addRenderableWidget(Button.builder(Component.literal(feature.name + "     [" + (feature.enabled ? "ON" : "OFF") + "]"), b -> {
                    NexusClient.toggle(feature);
                    Minecraft.getInstance().setScreen(new ClickGuiScreen(selected));
                }).bounds(contentX, rowY, contentW, 22).build());
                rowY += 25;
            }
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
            int rowY = selected.equals("Movement") ? top + 215 : top + 91;
            int shown = 0;
            for (Module feature : NexusClient.MODULES.all()) {
                if (!feature.categoryName().equalsIgnoreCase(selected)) continue;
                if (rowY > top + panelHeight - 58) break;
                String state = feature.enabled ? "ON" : "OFF";
                String label = feature.name + "     [" + state + "]";
                int buttonHeight = selected.equals("Movement") ? 20 : 27;
                int rowGap = selected.equals("Movement") ? 22 : 33;
                addRenderableWidget(Button.builder(Component.literal(label), b -> {
                    NexusClient.toggle(feature);
                    Minecraft.getInstance().setScreen(new ClickGuiScreen(selected));
                }).bounds(contentX, rowY, contentW, buttonHeight).build());
                rowY += rowGap;
                shown++;
            }
            if (shown == 0) notice = "No modules in this category yet.";
        }

        addRenderableWidget(Button.builder(Component.literal("SAVE"),
            b -> { NexusClient.saveConfig(); notice = "Configuration saved."; })
            .bounds(left + 4, top + panelHeight - 34, Math.max(1, sidebarWidth - 8), 22).build());
        addRenderableWidget(Button.builder(Component.literal("CLOSE"),
            b -> Minecraft.getInstance().setScreen(null)).bounds(Math.max(left + 4, left + panelWidth - 82), top + panelHeight - 34, Math.max(1, Math.min(78, panelWidth - 8)), 22).build());
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

    private static int cycleColor(int color) {
        int[] colors = {0xFFFF4545, 0xFFFFB347, 0xFF45E68A, 0xFF45B8FF, 0xFFB58CFF, 0xFFFF6BCE, 0xFFFFFFFF};
        for (int i = 0; i < colors.length; i++) if (colors[i] == color) return colors[(i + 1) % colors.length];
        return colors[0];
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
        g.drawString(font, "NEXUS", left + 10, top + 15, NexusClient.accent(), false);
        if (panelWidth >= 300) g.drawString(font, "V1  /  CLIENT CONTROL CENTER", left + 75, top + 16, 0xFFB8C8D0, false);
        g.drawString(font, selected.toUpperCase(), left + sidebarWidth + 18, top + 65, 0xFFFFFFFF, false);
        if (panelWidth >= 300) g.drawString(font, "CLIENT-SIDE UTILITIES", left + 15, top + 51, 0xFF78909C, false);
        if (selected.equals("Profiles")) {
            int profileY = top + 193;
            for (String profile : NexusConfig.profiles()) {
                if (profileY > top + panelHeight - 55) break;
                g.drawString(font, "- " + profile, left + sidebarWidth + 18, profileY, 0xFFB8C8D0, false);
                profileY += 15;
            }
        }
        if (panelHeight >= 190) g.drawString(font, notice, left + 10, top + panelHeight - 17, 0xFF78909C, false);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
