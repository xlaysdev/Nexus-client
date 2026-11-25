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
import com.xlays.nexus.modules.combat.Reach;
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
import com.xlays.nexus.modules.render.VisualSettings;
import com.xlays.nexus.modules.render.ESP;
import com.xlays.nexus.modules.render.BlockESP;
import com.xlays.nexus.modules.render.OreESP;
import com.xlays.nexus.modules.render.Fullbright;
import com.xlays.nexus.modules.render.NoHurtCam;
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
    private static final KeyMapping OPEN_GUI = new KeyMapping(
        "key.nexus.clickgui", InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_RIGHT_SHIFT, KeyMapping.Category.MISC
    );

    public NexusClient(IEventBus modBus) {
        registerFeatures();
        NexusConfig.load();
        modBus.addListener(this::registerKeys);
        NeoForge.EVENT_BUS.addListener(this::onClientTick);
        NeoForge.EVENT_BUS.addListener(Velocity::onKnockback);
        NeoForge.EVENT_BUS.addListener(ESP::onWorldRender);
        NeoForge.EVENT_BUS.addListener(BlockESP::onWorldRender);
    }

    private void registerKeys(RegisterKeyMappingsEvent event) { event.register(OPEN_GUI); }

    private static void registerFeatures() {
        MODULES.register(new AutoClicker());
        MODULES.register(new TriggerBot());
        MODULES.register(new KillAura());
        MODULES.register(new AutoTotem());
        MODULES.register(new CrystalAura());
        MODULES.register(new AutoArmor());
        MODULES.register(new OffhandManager());
        MODULES.register(new Reach());
        MODULES.register(new Speed());
        MODULES.register(new Fly());
        MODULES.register(new NoFall());
        MODULES.register(new Sprint());
        MODULES.register(new Step());
        MODULES.register(new Velocity());
        MODULES.register(new Safewalk());
        MODULES.register(new InventoryMove());
        MODULES.register(new FastPlace());
        MODULES.register(new ESP());
        MODULES.register(new BlockESP());
        MODULES.register(new OreESP());
        MODULES.register(new Fullbright());
        MODULES.register(new NoHurtCam());
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

        long cutoff = System.currentTimeMillis() - 1000;
        while (!LEFT_CLICKS.isEmpty() && LEFT_CLICKS.peekFirst() < cutoff) LEFT_CLICKS.removeFirst();
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
