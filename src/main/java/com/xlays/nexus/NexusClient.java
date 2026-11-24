package com.xlays.nexus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

@Mod(value = NexusClient.MOD_ID, dist = Dist.CLIENT)
public final class NexusClient {
    public static final String MOD_ID = "nexus";
    public static final Map<String, Feature> FEATURES = new LinkedHashMap<>();
    public static final int[] ACCENTS = {0xFF45E6C4, 0xFF58A6FF, 0xFFB58CFF, 0xFFFFB86B, 0xFFFF6B8A};
    public static int accentIndex = 0;
    public static int hudX = 12;
    public static int hudY = 12;
    public static int hudScale = 100;
    public static boolean hudBackground = true;

    private static final long STARTED_AT = System.currentTimeMillis();
    private static final Deque<Long> LEFT_CLICKS = new ArrayDeque<>();
    private static boolean leftWasDown;
    private static final KeyMapping OPEN_GUI = new KeyMapping(
        "key.nexus.clickgui", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC
    );

    public NexusClient(IEventBus modBus) {
        registerFeatures();
        NexusConfig.load();
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(this::onRenderHud);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_GUI); }

    private static void registerFeatures() {
        add("CPS Display", "Combat", false);
        add("Target Info", "Combat", false);
        add("Watermark", "HUD", true);
        add("FPS", "HUD", true);
        add("Coordinates", "HUD", true);
        add("Direction", "HUD", true);
        add("Module List", "HUD", false);
        add("Speedometer", "Movement", false);
        add("Sprint Status", "Movement", true);
        add("Movement State", "Movement", false);
        add("Armor Status", "Player", true);
        add("Armor Durability", "Player", false);
        add("Player Status", "Player", false);
        add("Session Timer", "Misc", false);
        add("Biome", "Misc", false);
        add("Custom Crosshair", "Render", false);
    }

    private static void add(String name, String category, boolean enabled) {
        FEATURES.put(name, new Feature(name, category, enabled));
    }

    private void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        while (OPEN_GUI.consumeClick()) {
            if (mc.screen instanceof ClickGuiScreen || mc.screen instanceof HudEditorScreen) mc.setScreen(null);
            else if (mc.screen == null) mc.setScreen(new ClickGuiScreen());
        }
        if (mc.player != null && mc.screen == null) {
            boolean down = GLFW.glfwGetMouseButton(mc.getWindow().handle(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
            if (down && !leftWasDown) LEFT_CLICKS.addLast(System.currentTimeMillis());
            leftWasDown = down;
        } else {
            leftWasDown = false;
        }
        long cutoff = System.currentTimeMillis() - 1000;
        while (!LEFT_CLICKS.isEmpty() && LEFT_CLICKS.peekFirst() < cutoff) LEFT_CLICKS.removeFirst();
    }

    private void onRenderHud(RenderGuiEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui || mc.screen != null) return;
        GuiGraphics g = event.getGuiGraphics();
        int x = hudX;
        int y = hudY;
        int line = 12;
        float scale = hudScale / 100.0f;
        g.pose().pushMatrix();
        g.pose().translate((float) x, (float) y);
        g.pose().scale(scale, scale);
        x = 0;
        y = 0;

        if (hudBackground) {
            g.fill(-5, -5, 190, 4, 0xCC101820);
            g.fill(-5, -5, 190, -3, accent());
        }
        if (enabled("Watermark")) {
            text(g, "NEXUS", x, y, accent());
            text(g, "V1", x + 48, y, 0xFFB8C8D0);
            y += 19;
        }
        if (enabled("FPS")) { text(g, "FPS  " + mc.getFps(), x, y); y += line; }
        if (enabled("Coordinates")) {
            text(g, String.format("XYZ  %.1f  %.1f  %.1f", mc.player.getX(), mc.player.getY(), mc.player.getZ()), x, y);
            y += line;
        }
        if (enabled("Direction")) {
            text(g, "FACING  " + mc.player.getDirection().getName().toUpperCase(), x, y);
            y += line;
        }
        if (enabled("Speedometer")) {
            double speed = mc.player.getDeltaMovement().horizontalDistance() * 20.0;
            text(g, String.format("SPEED  %.2f m/s", speed), x, y);
            y += line;
        }
        if (enabled("Sprint Status")) {
            text(g, "SPRINT  " + (mc.player.isSprinting() ? "ON" : "OFF"), x, y);
            y += line;
        }
        if (enabled("Movement State")) {
            String state = mc.player.isFallFlying() ? "GLIDING" : mc.player.onGround() ? "GROUNDED" : "AIRBORNE";
            text(g, "STATE  " + state, x, y);
            y += line;
        }
        if (enabled("CPS Display")) { text(g, "CPS  " + LEFT_CLICKS.size(), x, y); y += line; }
        if (enabled("Target Info") && mc.hitResult instanceof EntityHitResult hit) {
            Entity target = hit.getEntity();
            text(g, "TARGET  " + target.getName().getString(), x, y);
            y += line;
            text(g, String.format("DISTANCE  %.1f m", mc.player.distanceTo(target)), x, y);
            y += line;
        }
        if (enabled("Armor Status") || enabled("Armor Durability")) {
            int pieces = 0;
            int remaining = 0;
            int totalDurability = 0;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                var stack = mc.player.getItemBySlot(slot);
                if (!stack.isEmpty()) {
                    pieces++;
                    if (stack.isDamageableItem()) {
                        totalDurability += stack.getMaxDamage();
                        remaining += Math.max(0, stack.getMaxDamage() - stack.getDamageValue());
                    }
                }
            }
            if (enabled("Armor Status")) { text(g, "ARMOR  " + pieces + "/4 equipped", x, y); y += line; }
            if (enabled("Armor Durability")) {
                text(g, "DURABILITY  " + (totalDurability == 0 ? "N/A" : (remaining * 100 / Math.max(1, totalDurability)) + "% remaining"), x, y);
                y += line;
            }
        }
        if (enabled("Player Status")) {
            text(g, "HEALTH  " + Math.round(mc.player.getHealth()) + "   FOOD  " + mc.player.getFoodData().getFoodLevel(), x, y);
            y += line;
        }
        if (enabled("Biome")) {
            text(g, "BIOME  " + mc.level.getBiome(mc.player.blockPosition()).unwrapKey()
                .map(key -> key.identifier().getPath().toUpperCase()).orElse("UNKNOWN"), x, y);
            y += line;
        }
        if (enabled("Session Timer")) {
            long seconds = (System.currentTimeMillis() - STARTED_AT) / 1000;
            text(g, String.format("SESSION  %02d:%02d:%02d", seconds / 3600, (seconds / 60) % 60, seconds % 60), x, y);
        }
        g.pose().popMatrix();
        if (enabled("Custom Crosshair")) {
            int cx = mc.getWindow().getGuiScaledWidth() / 2;
            int cy = mc.getWindow().getGuiScaledHeight() / 2;
            g.fill(cx - 5, cy, cx + 6, cy + 1, accent());
            g.fill(cx, cy - 5, cx + 1, cy + 6, accent());
        }
        if (enabled("Module List")) {
            int rx = mc.getWindow().getGuiScaledWidth() - 8;
            int ry = 8;
            for (Feature f : FEATURES.values()) if (f.enabled) {
                String label = f.name.toUpperCase();
                int w = mc.font.width(label);
                g.fill(rx - w - 8, ry - 2, rx + 2, ry + 10, 0xAA101820);
                g.drawString(mc.font, label, rx - w - 3, ry, accent(), false);
                ry += 13;
            }
        }
    }

    private static void text(GuiGraphics g, String value, int x, int y) {
        text(g, value, x, y, 0xFFE7F1F4);
    }

    private static void text(GuiGraphics g, String value, int x, int y, int color) {
        g.drawString(Minecraft.getInstance().font, value, x, y, color, true);
    }

    public static int accent() { return ACCENTS[Math.max(0, Math.min(ACCENTS.length - 1, accentIndex))]; }
    public static boolean enabled(String name) {
        Feature f = FEATURES.get(name);
        return f != null && f.enabled;
    }
    public static void toggle(Feature f) { f.enabled = !f.enabled; saveConfig(); }
    public static void saveConfig() { NexusConfig.save(); }

    public static final class Feature {
        public final String name;
        public final String category;
        public boolean enabled;
        private Feature(String name, String category, boolean enabled) {
            this.name = name; this.category = category; this.enabled = enabled;
        }
    }
}
