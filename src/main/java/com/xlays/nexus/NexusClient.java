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
import org.lwjgl.glfw.GLFW;
import com.xlays.nexus.modules.combat.KillAura;
import com.xlays.nexus.modules.combat.AutoTotem;
import com.xlays.nexus.modules.combat.OffhandManager;
import com.xlays.nexus.modules.combat.AutoArmor;
import com.xlays.nexus.modules.combat.CrystalAura;
import com.xlays.nexus.modules.combat.AutoClicker;
import com.xlays.nexus.modules.combat.TriggerBot;
import com.xlays.nexus.modules.movement.MovementSettings;
import com.xlays.nexus.modules.movement.Velocity;
import com.xlays.nexus.modules.movement.Speed;
import com.xlays.nexus.modules.movement.Fly;
import com.xlays.nexus.modules.movement.NoFall;
import com.xlays.nexus.modules.movement.Sprint;
import com.xlays.nexus.modules.movement.Step;
import com.xlays.nexus.modules.movement.Safewalk;
import com.xlays.nexus.modules.movement.InventoryMove;
import com.xlays.nexus.modules.movement.FastPlace;
import com.xlays.nexus.modules.render.VisualModules;
import com.xlays.nexus.modules.render.OreESP;
import com.xlays.nexus.modules.Module;
import com.xlays.nexus.modules.ModuleManager;

import java.util.ArrayDeque;
import java.util.Deque;

@Mod(value = NexusClient.MOD_ID, dist = Dist.CLIENT)
public final class NexusClient {
    public static final String MOD_ID = "nexus";
    public static final ModuleManager MODULES = new ModuleManager();
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
        NeoForge.EVENT_BUS.addListener(Velocity::onKnockback);
        NeoForge.EVENT_BUS.addListener(VisualModules::onWorldRender);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_GUI); }

    private static void registerFeatures() {
        add("CPS Display", "Combat", false);
        add("Target Info", "Combat", false);
        add("Target Health", "Combat", false);
        add("Attack Cooldown", "Combat", false);
        add("Totem Counter", "Combat", false);
        add("Potion Effects", "Combat", false);
        MODULES.register(new AutoClicker());
        MODULES.register(new TriggerBot());
        MODULES.register(new KillAura());
        add("Target Armor", "Combat", false);
        add("Target Held Item", "Combat", false);
        add("Target Effects", "Combat", false);
        add("Target Hurt Time", "Combat", false);
        add("Range Indicator", "Combat", false);
        add("Low Health Warning", "Combat", false);
        add("Critical Hit Indicator", "Combat", false);
        add("Nearby Players", "Combat", false);
        add("Enemy Health Bar", "Combat", false);
        MODULES.register(new AutoTotem());
        MODULES.register(new CrystalAura());
        MODULES.register(new AutoArmor());
        MODULES.register(new OffhandManager());
        add("Reach", "Combat", false);
        add("Watermark", "HUD", true);
        add("ArmorHUD", "HUD", false);
        add("Keystrokes", "HUD", false);
        add("FPS", "HUD", true);
        add("Coordinates", "HUD", true);
        add("Direction", "HUD", true);
        add("Module List", "HUD", false);
        add("Speedometer", "Movement", false);
        add("Sprint Status", "Movement", true);
        add("Movement State", "Movement", false);
        MODULES.register(new Speed());
        MODULES.register(new Fly());
        MODULES.register(new NoFall());
        add("Sprint", "Movement", false);
        MODULES.register(new Step());
        MODULES.register(new Velocity());
        MODULES.register(new Safewalk());
        MODULES.register(new InventoryMove());
        MODULES.register(new FastPlace());
        add("Armor Status", "Player", true);
        add("Armor Durability", "Player", false);
        add("Player Status", "Player", false);
        add("Session Timer", "Misc", false);
        add("Biome", "Misc", false);
        add("Custom Crosshair", "Render", false);
        add("ESP", "Render", false);
        add("BlockESP", "Render", false);
        add("OreESP", "Render", false);
        add("Fullbright", "Render", false);
        add("NoHurtCam", "Render", false);
    }

    private static void add(String name, String category, boolean enabled) {
        MODULES.register(new Module(name, Module.Category.valueOf(category.toUpperCase(java.util.Locale.ROOT)), enabled) {});
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
        MODULES.tick();
        VisualModules.tick(mc);
        OreESP.tick();

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
            int ping = mc.getConnection() == null || mc.getConnection().getPlayerInfo(mc.player.getUUID()) == null
                ? 0 : mc.getConnection().getPlayerInfo(mc.player.getUUID()).getLatency();
            text(g, "FPS " + mc.getFps() + "  •  " + ping + "ms", x + 76, y, 0xFFE7F1F4);
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
        if (enabled("ArmorHUD")) {
            int ax = 8;
            int ay = mc.getWindow().getGuiScaledHeight() - 28;
            for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD}) {
                var stack = mc.player.getItemBySlot(slot);
                int durability = stack.isEmpty() || !stack.isDamageableItem() ? 0
                    : Math.max(0, 100 - stack.getDamageValue() * 100 / Math.max(1, stack.getMaxDamage()));
                g.fill(ax - 3, ay - 3, ax + 49, ay + 19, 0xAA101820);
                g.drawString(mc.font, slot.getName().substring(0, 1).toUpperCase(), ax, ay, accent(), false);
                g.drawString(mc.font, stack.isEmpty() ? "EMPTY" : durability + "%", ax + 12, ay, 0xFFE7F1F4, false);
                g.fill(ax + 12, ay + 10, ax + 46, ay + 12, 0xFF263A46);
                if (!stack.isEmpty()) g.fill(ax + 12, ay + 10, ax + 12 + (34 * durability / 100), ay + 12, durability > 50 ? 0xFF45E6C4 : 0xFFFF6B8A);
                ax += 55;
            }
        }
        if (enabled("Keystrokes")) {
            int kx = 10;
            int ky = mc.getWindow().getGuiScaledHeight() - 86;
            String[] labels = {"W", "A", "S", "D"};
            boolean[] down = {mc.options.keyUp.isDown(), mc.options.keyLeft.isDown(), mc.options.keyDown.isDown(), mc.options.keyRight.isDown()};
            int[][] positions = {{22,0},{0,22},{22,22},{44,22}};
            for (int i = 0; i < labels.length; i++) {
                int px = kx + positions[i][0], py = ky + positions[i][1];
                g.fill(px, py, px + 20, py + 18, down[i] ? 0xDD45E6C4 : 0xAA101820);
                g.drawCenteredString(mc.font, labels[i], px + 10, py + 5, down[i] ? 0xFF071018 : 0xFFE7F1F4);
            }
            boolean leftDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            boolean rightDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(mc.getWindow().handle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
            g.fill(kx, ky + 43, kx + 31, ky + 62, leftDown ? 0xDD45E6C4 : 0xAA101820);
            g.fill(kx + 35, ky + 43, kx + 66, ky + 62, rightDown ? 0xDD45E6C4 : 0xAA101820);
            g.drawCenteredString(mc.font, "LMB", kx + 15, ky + 49, leftDown ? 0xFF071018 : 0xFFE7F1F4);
            g.drawCenteredString(mc.font, "RMB", kx + 50, ky + 49, rightDown ? 0xFF071018 : 0xFFE7F1F4);
        }
        if (enabled("Custom Crosshair")) {
            int cx = mc.getWindow().getGuiScaledWidth() / 2;
            int cy = mc.getWindow().getGuiScaledHeight() / 2;
            int size = Math.max(2, Math.min(14, VisualModules.crosshairSize));
            int color = VisualModules.crosshairColor;
            g.fill(cx - size, cy, cx + size + 1, cy + 1, color);
            g.fill(cx, cy - size, cx + 1, cy + size + 1, color);
        }
        if (enabled("Module List")) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int ry = 8;
            int index = 0;
            for (Module f : MODULES.all()) if (f.enabled) {
                String label = f.name.toUpperCase();
                int w = mc.font.width(label);
                int rx = screenWidth - 8;
                int rainbow = java.awt.Color.HSBtoRGB((index++ * 0.075f + (System.currentTimeMillis() % 6000L) / 6000.0f) % 1.0f, 0.82f, 1.0f) | 0xFF000000;
                g.fill(rx - w - 8, ry - 2, rx + 2, ry + 10, 0xAA101820);
                g.fill(rx - w - 8, ry - 2, rx - w - 6, ry + 10, rainbow);
                g.drawString(mc.font, label, rx - w - 3, ry, rainbow, false);
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
        return MODULES.isEnabled(name);
    }
    public static void toggle(Module module) { module.toggle(); saveConfig(); }
    public static void saveConfig() { NexusConfig.save(); }

}
