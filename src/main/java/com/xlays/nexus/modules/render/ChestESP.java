package com.xlays.nexus.modules.render;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Blocks;

import java.util.ArrayList;
import java.util.List;

public final class ChestESP extends Module {
    private static final int BLOCKS_PER_TICK = 4096;
    private int radius = 12;
    private int cursor;
    private BlockPos scanCenter;
    private final List<BlockPos> found = new ArrayList<>();
    private final List<BlockPos> pending = new ArrayList<>();

    public ChestESP() {
        super("ChestESP", Category.RENDER, false);
        numberSetting("radius", "Scan radius", () -> radius,
                v -> setRadius(v.intValue()), 4, 48, 1);
    }

    public int radius() { return radius; }

    public void setRadius(int value) {
        radius = Math.max(4, Math.min(48, value));
        restartScan();
    }

    private void restartScan() {
        cursor = 0;
        scanCenter = null;
        pending.clear();
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            found.clear();
            pending.clear();
            scanCenter = null;
            cursor = 0;
            return;
        }

        if (scanCenter == null || scanCenter.distSqr(mc.player.blockPosition()) > 16) {
            scanCenter = mc.player.blockPosition().immutable();
            cursor = 0;
            pending.clear();
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

            var block = mc.level.getBlockState(pos).getBlock();
            if (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST
                    || block == Blocks.ENDER_CHEST || block == Blocks.BARREL) {
                pending.add(pos.immutable());
                if (pending.size() >= 256) {
                    cursor = total;
                    break;
                }
            }
        }

        if (cursor >= total) {
            found.clear();
            found.addAll(pending);
            restartScan();
        }
    }

    @Override
    public void onRender() {
        if (found.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        for (BlockPos pos : found) {
            if (mc.level.hasChunkAt(pos)) {
                Gizmos.cuboid(pos, GizmoStyle.stroke(0xFFFFC857, 2.0F))
                        .setAlwaysOnTop();
            }
        }
    }

    @Override
    protected void onDisable() {
        found.clear();
        pending.clear();
        restartScan();
    }
}
