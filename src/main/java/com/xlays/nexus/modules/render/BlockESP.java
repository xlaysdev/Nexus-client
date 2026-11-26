package com.xlays.nexus.modules.render;

import com.xlays.nexus.NexusClient;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class BlockESP extends Module {
    private static final int SCAN_SIZE = 12;
    private static final int SCAN_MIN = -(SCAN_SIZE / 2);
    private static final int SCAN_MAX = SCAN_MIN + SCAN_SIZE - 1;
    private static final int MAX_MARKERS = 160;
    private static final List<BlockPos> FOUND = new ArrayList<>();
    private static int scanTicks;

    public BlockESP() {
        super("BlockESP", Category.RENDER, false);
        booleanSetting("diamond", "Diamond ore", () -> VisualSettings.scanDiamond, v -> {
            VisualSettings.scanDiamond = v;
            refreshFilters();
        });
        booleanSetting("debris", "Ancient debris", () -> VisualSettings.scanDebris, v -> {
            VisualSettings.scanDebris = v;
            refreshFilters();
        });
        booleanSetting("chests", "Chests", () -> VisualSettings.scanChests, v -> {
            VisualSettings.scanChests = v;
            refreshFilters();
        });
    }

    public static void refreshFilters() {
        FOUND.clear();
        scanTicks = 9;
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            FOUND.clear();
            scanTicks = 0;
            return;
        }
        if (++scanTicks < 10) return;
        scanTicks = 0;

        BlockPos center = mc.player.blockPosition();
        List<BlockPos> next = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(SCAN_MIN, SCAN_MIN, SCAN_MIN),
                center.offset(SCAN_MAX, SCAN_MAX, SCAN_MAX))) {
            if (!mc.level.hasChunkAt(pos)) continue;
            var block = mc.level.getBlockState(pos).getBlock();
            boolean diamond = VisualSettings.scanDiamond
                    && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE);
            boolean debris = VisualSettings.scanDebris && block == Blocks.ANCIENT_DEBRIS;
            boolean chest = VisualSettings.scanChests
                    && (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST);
            if (diamond || debris || chest) {
                next.add(pos.immutable());
                if (next.size() >= MAX_MARKERS) break;
            }
        }
        FOUND.clear();
        FOUND.addAll(next);
    }

    public static void onWorldRender(net.neoforged.neoforge.client.event.RenderLevelStageEvent.AfterEntities event) {
        if (!NexusClient.enabled("BlockESP") || FOUND.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        // Use the engine's Gizmo renderer rather than submitting incomplete vertices
        // to the custom lines render type. This avoids the ESP vertex-format crash.
        for (BlockPos pos : FOUND) {
            if (!mc.level.hasChunkAt(pos)) continue;
            Gizmos.cuboid(pos, GizmoStyle.stroke(VisualSettings.blockColor, 2.0F))
                    .setAlwaysOnTop();
        }
    }

    @Override
    protected void onDisable() {
        FOUND.clear();
        scanTicks = 0;
    }
}
