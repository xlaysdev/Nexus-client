package com.xlays.nexus.modules.render;

import com.xlays.nexus.NexusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class OreESP extends com.xlays.nexus.modules.Module {
    private static final int BLOCKS_PER_TICK = 4096;

    public enum OreGroup {
        COAL("Coal", Blocks.COAL_ORE, Blocks.DEEPSLATE_COAL_ORE),
        IRON("Iron", Blocks.IRON_ORE, Blocks.DEEPSLATE_IRON_ORE),
        COPPER("Copper", Blocks.COPPER_ORE, Blocks.DEEPSLATE_COPPER_ORE),
        GOLD("Gold", Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE),
        REDSTONE("Redstone", Blocks.REDSTONE_ORE, Blocks.DEEPSLATE_REDSTONE_ORE),
        LAPIS("Lapis", Blocks.LAPIS_ORE, Blocks.DEEPSLATE_LAPIS_ORE),
        DIAMOND("Diamond", Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE),
        EMERALD("Emerald", Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE),
        ANCIENT_DEBRIS("Ancient Debris", Blocks.ANCIENT_DEBRIS);

        public final String label;
        public final List<Block> blocks;

        OreGroup(String label, Block... blocks) {
            this.label = label;
            this.blocks = List.of(blocks);
        }
    }

    private static int radius = 12;
    private static int cursor;
    private static BlockPos scanCenter;
    private static final List<BlockPos> FOUND = new ArrayList<>();
    private static final List<BlockPos> PENDING = new ArrayList<>();
    private static final EnumMap<OreGroup, Boolean> FILTERS = new EnumMap<>(OreGroup.class);
    private static final Set<Block> ACTIVE_BLOCKS = new HashSet<>();

    static {
        for (OreGroup group : OreGroup.values()) FILTERS.put(group, true);
        rebuildTargets();
    }

    public OreESP() {
        super("OreESP", Category.RENDER, false);
    }

    @Override public void onTick() { tick(); }
    @Override public void onRender() { render(); }

    public static int getRadius() { return radius; }

    public static void setRadius(int value) {
        radius = Math.max(4, Math.min(32, value));
        restartScan();
    }

    public static boolean isEnabled(OreGroup group) {
        return FILTERS.getOrDefault(group, true);
    }

    public static void setEnabled(OreGroup group, boolean enabled) {
        FILTERS.put(group, enabled);
        rebuildTargets();
        FOUND.clear();
        restartScan();
    }

    private static void rebuildTargets() {
        ACTIVE_BLOCKS.clear();
        for (OreGroup group : OreGroup.values()) {
            if (isEnabled(group)) ACTIVE_BLOCKS.addAll(group.blocks);
        }
    }

    private static void restartScan() {
        cursor = 0;
        scanCenter = null;
        PENDING.clear();
    }

    public static void tick() {
        if (!NexusClient.enabled("OreESP")) {
            clear();
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || ACTIVE_BLOCKS.isEmpty()) {
            clear();
            return;
        }

        BlockPos playerPos = mc.player.blockPosition();
        if (scanCenter == null || scanCenter.distSqr(playerPos) > 16) {
            scanCenter = playerPos.immutable();
            cursor = 0;
            PENDING.clear();
        }

        int diameter = radius * 2 + 1;
        int total = diameter * diameter * diameter;
        int processed = 0;

        while (cursor < total && processed++ < BLOCKS_PER_TICK) {
            int x = cursor % diameter - radius;
            int y = (cursor / diameter) % diameter - radius;
            int z = cursor / (diameter * diameter) - radius;
            cursor++;

            if (x * x + y * y + z * z > radius * radius) continue;
            BlockPos pos = scanCenter.offset(x, y, z);
            if (!mc.level.hasChunkAt(pos)) continue;

            if (ACTIVE_BLOCKS.contains(mc.level.getBlockState(pos).getBlock())) {
                PENDING.add(pos.immutable());
            }
        }

        if (cursor >= total) {
            FOUND.clear();
            FOUND.addAll(PENDING);
            restartScan();
        }
    }

    public static void render() {
        if (!NexusClient.enabled("OreESP") || FOUND.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        for (BlockPos pos : FOUND) {
            if (!mc.level.hasChunkAt(pos)) continue;
            Block block = mc.level.getBlockState(pos).getBlock();
            if (ACTIVE_BLOCKS.contains(block)) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(colorFor(block), 2.0F))
                        .setAlwaysOnTop();
            }
        }
    }

    private static int colorFor(Block block) {
        if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE) return 0xFF55FFFF;
        if (block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE) return 0xFF55FF55;
        if (block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE) return 0xFFFFD700;
        if (block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE) return 0xFFFFAA66;
        if (block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE) return 0xFFFF8844;
        if (block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE) return 0xFFFF3333;
        if (block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE) return 0xFF5577FF;
        if (block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE) return 0xFF888888;
        if (block == Blocks.ANCIENT_DEBRIS) return 0xFFCC66DD;
        return 0xFFFFFFFF;
    }

    public static void clear() {
        FOUND.clear();
        restartScan();
    }
}
