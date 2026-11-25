package com.xlays.nexus.modules.render;

import com.xlays.nexus.NexusClient;
import com.xlays.nexus.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

public final class BlockESP extends Module {
    private static final int SCAN_SIZE = 12;
    private static final int SCAN_MIN = -(SCAN_SIZE / 2);
    private static final int SCAN_MAX = SCAN_MIN + SCAN_SIZE - 1;
    private static final int MAX_MARKERS = 160;
    private static final List<BlockPos> FOUND = new ArrayList<>();
    private static int scanTicks;

    public BlockESP() { super("BlockESP", Category.RENDER, false); }

    @Override
    public void onTick() {
        if (Minecraft.getInstance().player == null || Minecraft.getInstance().level == null) {
            FOUND.clear();
            scanTicks = 0;
            return;
        }
        if (++scanTicks < 10) return;
        scanTicks = 0;

        Minecraft mc = Minecraft.getInstance();
        BlockPos center = mc.player.blockPosition();
        List<BlockPos> next = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(SCAN_MIN, SCAN_MIN, SCAN_MIN),
                center.offset(SCAN_MAX, SCAN_MAX, SCAN_MAX))) {
            if (!mc.level.hasChunkAt(pos)) continue;
            var block = mc.level.getBlockState(pos).getBlock();
            if (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE
                    || block == Blocks.ANCIENT_DEBRIS || block == Blocks.CHEST
                    || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST) {
                next.add(pos.immutable());
                if (next.size() >= MAX_MARKERS) break;
            }
        }
        FOUND.clear();
        FOUND.addAll(next);
    }

    public static void onWorldRender(RenderLevelStageEvent.AfterEntities event) {
        if (!NexusClient.enabled("BlockESP") || FOUND.isEmpty()) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;

        PoseStack pose = event.getPoseStack();
        if (pose == null) return;
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);

        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        var type = RenderUtil.lineType();
        if (type == null) {
            pose.popPose();
            return;
        }

        VertexConsumer out = buffers.getBuffer(type);
        for (BlockPos pos : FOUND) {
            AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                    pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(.015);
            ShapeRenderer.renderShape(pose, out, Shapes.create(box),
                    0, 0, 0, VisualSettings.blockColor, 2);
        }
        buffers.endBatch(type);
        pose.popPose();
    }
}