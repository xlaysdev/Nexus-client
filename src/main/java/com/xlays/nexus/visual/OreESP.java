package com.xlays.nexus.visual;

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

/** Cached, spherical ore scanner adapted for NEXUS V1. */
public final class OreESP {
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
    private static int tickCounter;
    private static final List<BlockPos> FOUND = new ArrayList<>();
    private static final EnumMap<OreGroup, Boolean> FILTERS = new EnumMap<>(OreGroup.class);
    private static final Set<Block> ACTIVE_BLOCKS = new HashSet<>();

    static {
        for (OreGroup group : OreGroup.values()) FILTERS.put(group, true);
        rebuildTargets();
    }

    private OreESP() {}

    public static int getRadius() { return radius; }
    public static void setRadius(int value) {
        radius = Math.max(4, Math.min(32, value));
        tickCounter = 9;
    }

    public static boolean isEnabled(OreGroup group) {
        return FILTERS.getOrDefault(group, true);
    }

    public static void setEnabled(OreGroup group, boolean enabled) {
        FILTERS.put(group, enabled);
        rebuildTargets();
        FOUND.clear();
        tickCounter = 9;
    }

    private static void rebuildTargets() {
        ACTIVE_BLOCKS.clear();
        for (OreGroup group : OreGroup.values()) {
            if (isEnabled(group)) ACTIVE_BLOCKS.addAll(group.blocks);
        }
    }

    public static void tick() {
        if (!NexusClient.enabled("OreESP")) {
            FOUND.clear();
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || ACTIVE_BLOCKS.isEmpty()) {
            FOUND.clear();
            return;
        }
        if (++tickCounter < 10) return;
        tickCounter = 0;

        BlockPos center = mc.player.blockPosition();
        int r2 = radius * radius;
        List<BlockPos> found = new ArrayList<>();
        for (int x = -radius; x <= radius; x++) {
            for (int y = -radius; y <= radius; y++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + y * y + z * z > r2) continue;
                    BlockPos pos = center.offset(x, y, z);
                    if (!mc.level.hasChunkAt(pos)) continue;
                    if (ACTIVE_BLOCKS.contains(mc.level.getBlockState(pos).getBlock())) {
                        found.add(pos.immutable());
                    }
                }
            }
        }
        FOUND.clear();
        FOUND.addAll(found);
    }

    public static void render() {
        if (!NexusClient.enabled("OreESP") || FOUND.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;

        for (BlockPos pos : FOUND) {
            Block block = mc.level.getBlockState(pos).getBlock();
            if (ACTIVE_BLOCKS.contains(block)) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(colorFor(block), 2.0F)).setAlwaysOnTop();
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
        tickCounter = 0;
    }
}
