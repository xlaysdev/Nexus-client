package com.xlays.nexus;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.common.NeoForge;
import org.lwjgl.glfw.GLFW;\nimport com.xlays.nexus.combat.CombatModules;

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
    private static long lastAutoClickAt;
    private static long lastAuraAttackAt;
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
        add("Target Health", "Combat", false);
        add("Attack Cooldown", "Combat", false);
        add("Totem Counter", "Combat", false);
        add("Potion Effects", "Combat", false);
        add("AutoClicker", "Combat", false);
        add("TriggerBot", "Combat", false);
        add("KillAura", "Combat", false);
        add("Target Armor", "Combat", false);
        add("Target Held Item", "Combat", false);
        add("Target Effects", "Combat", false);
        add("Target Hurt Time", "Combat", false);
        add("Range Indicator", "Combat", false);
        add("Low Health Warning", "Combat", false);
        add("Critical Hit Indicator", "Combat", false);
        add("Nearby Players", "Combat", false);
        add("Enemy Health Bar", "Combat", false);\n        add("AutoTotem", "Combat", false);\n        add("CrystalAura", "Combat", false);\n        add("AutoArmor", "Combat", false);\n        add("OffhandManager", "Combat", false);\n        add("Reach", "Combat", false);\n        add("Rotations", "Combat", true);
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
        if (mc.player != null && mc.level != null && mc.gameMode != null && mc.screen == null) {
            long now = System.currentTimeMillis();
            LivingEntity crosshairTarget = mc.hitResult instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity living && living.isAlive() ? living : null;

            // Manual-hold click repeater: only repeats while the player physically holds left mouse.
            if (enabled("AutoClicker") && leftWasDown && crosshairTarget != null
                && now - lastAutoClickAt >= 125L && mc.player.getAttackStrengthScale(0.0F) >= 1.0F) {
                performAttack(mc, crosshairTarget);
                lastAutoClickAt = now;
            }

            // TriggerBot attacks only the living entity already under the crosshair.
            if (enabled("TriggerBot") && crosshairTarget != null
                && mc.player.getAttackStrengthScale(0.0F) >= 1.0F) {
                performAttack(mc, crosshairTarget);
            }

            CombatModules.tick(mc);\n        }\n\n        long cutoff = System.currentTimeMillis() - 1000;
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
        if (mc.hitResult instanceof EntityHitResult hit) {
            Entity target = hit.getEntity();
            if (enabled("Target Info")) {
                text(g, "TARGET  " + target.getName().getString(), x, y);
                y += line;
                text(g, String.format("DISTANCE  %.1f m", mc.player.distanceTo(target)), x, y);
                y += line;
            }
            if (enabled("Target Health") && target instanceof LivingEntity living) {
                float health = Math.max(0.0F, living.getHealth());
                float maxHealth = Math.max(1.0F, living.getMaxHealth());
                text(g, String.format("TARGET HP  %.1f / %.1f", health, maxHealth), x, y);
                y += line;
                int barWidth = 120;
                g.fill(x, y, x + barWidth, y + 5, 0xFF263A46);
                g.fill(x, y, x + Math.round(barWidth * Math.min(1.0F, health / maxHealth)), y + 5, accent());
                y += 9;
            }
            if (target instanceof LivingEntity living) {
                if (enabled("Target Armor")) {
                    int armor = living.getArmorValue();
                    text(g, "TARGET ARMOR  " + armor, x, y);
                    y += line;
                }
                if (enabled("Target Held Item") && living instanceof Player playerTarget) {
                    String held = playerTarget.getMainHandItem().isEmpty() ? "EMPTY"
                        : playerTarget.getMainHandItem().getHoverName().getString();
                    text(g, "TARGET ITEM  " + held, x, y);
                    y += line;
                }
                if (enabled("Target Effects")) {
                    int shownEffects = 0;
                    for (var effect : living.getActiveEffects()) {
                        if (shownEffects++ >= 3) break;
                        text(g, "TARGET " + effect.getEffect().value().getDisplayName().getString()
                            + " " + Math.max(0, effect.getDuration() / 20) + "s", x, y);
                        y += line;
                    }
                    if (shownEffects == 0) {
                        text(g, "TARGET EFFECTS  NONE", x, y);
                        y += line;
                    }
                }
                if (enabled("Target Hurt Time")) {
                    text(g, "HURT TIME  " + living.hurtTime, x, y);
                    y += line;
                }
                if (enabled("Enemy Health Bar")) {
                    int width = 140;
                    float ratio = Math.max(0.0F, Math.min(1.0F, living.getHealth() / Math.max(1.0F, living.getMaxHealth())));
                    g.fill(x, y, x + width, y + 6, 0xFF263A46);
                    g.fill(x, y, x + Math.round(width * ratio), y + 6, ratio > 0.5F ? 0xFF45E6C4 : 0xFFFF6B8A);
                    y += 10;
                }
            }
            if (enabled("Range Indicator")) {
                float distance = mc.player.distanceTo(target);
                text(g, String.format("ATTACK RANGE  %.2f m  %s", distance,
                    distance <= 3.0F ? "IN RANGE" : "OUT OF RANGE"), x, y,
                    distance <= 3.0F ? 0xFF45E6C4 : 0xFFFF6B8A);
                y += line;
            }
        }
        if (enabled("Low Health Warning") && mc.player.getHealth() <= 6.0F) {
            text(g, "LOW HEALTH  " + String.format("%.1f HP", mc.player.getHealth()), x, y, 0xFFFF6B8A);
            y += line;
        }
        if (enabled("Critical Hit Indicator")) {
            boolean canCrit = !mc.player.onGround() && mc.player.getDeltaMovement().y < 0.0D
                && !mc.player.isFallFlying() && !mc.player.isInWater();
            text(g, "CRITICAL  " + (canCrit ? "READY" : "NOT READY"), x, y,
                canCrit ? 0xFF45E6C4 : 0xFFFFB86B);
            y += line;
        }
        if (enabled("Nearby Players")) {
            long nearby = mc.level.players().stream()
                .filter(other -> other != mc.player && other.isAlive() && mc.player.distanceTo(other) <= 16.0F)
                .count();
            text(g, "PLAYERS NEARBY  " + nearby, x, y);
            y += line;
        }
        if (enabled("Attack Cooldown")) {
            int cooldown = Math.round(mc.player.getAttackStrengthScale(0.0F) * 100.0F);
            text(g, "ATTACK READY  " + cooldown + "%", x, y);
            y += line;
            g.fill(x, y, x + 100, y + 4, 0xFF263A46);
            g.fill(x, y, x + cooldown, y + 4, accent());
            y += 8;
        }
        if (enabled("Totem Counter")) {
            text(g, "TOTEMS  " + mc.player.getInventory().countItem(Items.TOTEM_OF_UNDYING), x, y);
            y += line;
        }
        if (enabled("Potion Effects")) {
            var effects = mc.player.getActiveEffects();
            text(g, "EFFECTS  " + effects.size(), x, y);
            y += line;
            int shown = 0;
            for (var effect : effects) {
                if (shown++ >= 3) break;
                String name = effect.getEffect().value().getDisplayName().getString();
                int seconds = Math.max(0, effect.getDuration() / 20);
                text(g, name + "  " + seconds + "s", x, y);
                y += line;
            }
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

    private static void performAttack(Minecraft mc, LivingEntity target) {
        if (mc.player == null || mc.gameMode == null || target == null || !target.isAlive()) return;
        mc.gameMode.attack(mc.player, target);
        mc.player.swing(InteractionHand.MAIN_HAND);
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
