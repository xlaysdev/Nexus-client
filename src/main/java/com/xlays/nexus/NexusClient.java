package com.xlays.nexus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

@Mod(value = NexusClient.MOD_ID, dist = Dist.CLIENT)
public final class NexusClient {
    public static final String MOD_ID = "nexus";
    public static final Map<String, Feature> FEATURES = new LinkedHashMap<>();
    private static final long STARTED_AT = System.currentTimeMillis();
    private static final KeyMapping OPEN_GUI = new KeyMapping(
        "key.nexus.clickgui", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC
    );

    public NexusClient(IEventBus modBus) {
        registerFeatures();
        loadConfig();
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRenderHud);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_GUI); }

    private static void registerFeatures() {
        add("Watermark", "HUD", true);
        add("FPS", "HUD", true);
        add("Coordinates", "HUD", true);
        add("Direction", "HUD", true);
        add("Speedometer", "Movement", false);
        add("Sprint Status", "Movement", true);
        add("Armor Status", "Player", true);
        add("Player Status", "Player", false);
        add("Session Timer", "Misc", false);
        add("Custom Crosshair", "Render", false);
        add("Clean HUD", "Render", false);
        add("Module List", "HUD", false);
        add("Compact Layout", "Settings", false);
        add("Accent Glow", "Settings", true);
    }

    private static void add(String name, String category, boolean enabled) {
        FEATURES.put(name, new Feature(name, category, enabled));
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_GUI.consumeClick()) {
            if (mc.screen instanceof ClickGuiScreen) mc.setScreen(null);
            else if (mc.screen == null) mc.setScreen(new ClickGuiScreen());
        }
    }

    private void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null) return;
        GuiGraphics g = event.getGuiGraphics();
        int x = 8, y = 8, line = 11;
        if (enabled("Watermark")) {
            panel(g, x - 4, y - 4, x + 104, y + 14, 0xCC101820);
            g.drawString(mc.font, "NEXUS", x, y, 0xFF45E6C4, false);
            g.drawString(mc.font, "V1", x + 47, y, 0xFFB8C8D0, false);
            y += 20;
        }
        if (enabled("FPS")) {
            text(g, "FPS  " + mc.getFps(), x, y); y += line;
        }
        if (enabled("Coordinates")) {
            text(g, String.format("XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), x, y);
            y += line;
        }
        if (enabled("Direction")) {
            text(g, "FACING  " + mc.player.getDirection().getName().toUpperCase(), x, y); y += line;
        }
        if (enabled("Speedometer")) {
            double speed = mc.player.getDeltaMovement().horizontalDistance() * 20.0;
            text(g, String.format("SPEED  %.2f m/s", speed), x, y); y += line;
        }
        if (enabled("Sprint Status")) {
            text(g, "SPRINT  " + (mc.player.isSprinting() ? "ON" : "OFF"), x, y); y += line;
        }
        if (enabled("Armor Status")) {
            int pieces = 0;
            for (var stack : mc.player.getInventory().armor) if (!stack.isEmpty()) pieces++;
            text(g, "ARMOR  " + pieces + "/4 equipped", x, y); y += line;
        }
        if (enabled("Player Status")) {
            text(g, "HEALTH  " + Math.round(mc.player.getHealth()) + "   FOOD  " + mc.player.getFoodData().getFoodLevel(), x, y);
            y += line;
        }
        if (enabled("Session Timer")) {
            long seconds = (System.currentTimeMillis() - STARTED_AT) / 1000;
            text(g, String.format("SESSION  %02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60), x, y);
        }
        if (enabled("Custom Crosshair")) {
            int cx = mc.getWindow().getGuiScaledWidth() / 2;
            int cy = mc.getWindow().getGuiScaledHeight() / 2;
            g.fill(cx - 5, cy, cx + 6, cy + 1, 0xFF45E6C4);
            g.fill(cx, cy - 5, cx + 1, cy + 6, 0xFF45E6C4);
        }
        if (enabled("Module List")) {
            int rx = mc.getWindow().getGuiScaledWidth() - 8;
            int ry = 8;
            for (Feature f : FEATURES.values()) if (f.enabled) {
                String label = f.name.toUpperCase();
                int w = mc.font.width(label);
                g.fill(rx - w - 8, ry - 2, rx + 2, ry + 10, 0xAA101820);
                g.drawString(mc.font, label, rx - w - 3, ry, 0xFF45E6C4, false);
                ry += 13;
            }
        }
    }

    private static void text(GuiGraphics g, String value, int x, int y) {
        g.drawString(Minecraft.getInstance().font, value, x, y, 0xFFE7F1F4, true);
    }

    private static void panel(GuiGraphics g, int x1, int y1, int x2, int y2, int color) {
        g.fill(x1, y1, x2, y2, color);
        g.fill(x1, y1, x2, y1 + 1, 0xFF45E6C4);
    }

    public static boolean enabled(String name) {
        Feature f = FEATURES.get(name);
        return f != null && f.enabled;
    }

    public static void toggle(Feature f) { f.enabled = !f.enabled; saveConfig(); }
    public static void saveConfig() {
        Properties p = new Properties();
        FEATURES.values().forEach(f -> p.setProperty(f.name, Boolean.toString(f.enabled)));
        Path path = FMLPaths.CONFIGDIR.get().resolve("nexus.properties");
        try {
            Files.createDirectories(path.getParent());
            try (OutputStream out = Files.newOutputStream(path)) { p.store(out, "NEXUS V1 settings"); }
        } catch (IOException e) { System.err.println("[NEXUS] Could not save settings: " + e.getMessage()); }
    }
    private static void loadConfig() {
        Path path = FMLPaths.CONFIGDIR.get().resolve("nexus.properties");
        if (!Files.exists(path)) return;
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            FEATURES.values().forEach(f -> f.enabled = Boolean.parseBoolean(p.getProperty(f.name, Boolean.toString(f.enabled))));
        } catch (IOException e) { System.err.println("[NEXUS] Could not load settings: " + e.getMessage()); }
    }

    public static final class Feature {
        public final String name;
        public final String category;
        public boolean enabled;
        private Feature(String name, String category, boolean enabled) {
            this.name = name; this.category = category; this.enabled = enabled;
        }
    }
}
