package com.xlays.nexus.modules.render;

import com.xlays.nexus.NexusClient;
import com.xlays.nexus.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

public final class ESP extends Module {
    private static final float BOX_LINE_WIDTH = 2.0F;
    private static final float TRACER_LINE_WIDTH = 1.5F;

    // Twelve edges of a box, indexed into the eight min/max corners.
    private static final int[][] BOX_EDGES = {
        {0, 1}, {0, 2}, {0, 4}, {1, 3}, {1, 5}, {2, 3},
        {2, 6}, {3, 7}, {4, 5}, {4, 6}, {5, 7}, {6, 7}
    };

    public ESP() {
        super("ESP", Category.RENDER, false);
        booleanSetting("3d", "3D boxes", () -> VisualSettings.esp3d,
            v -> VisualSettings.esp3d = v);
        booleanSetting("tracers", "Show tracers", () -> VisualSettings.showTracers,
            v -> VisualSettings.showTracers = v);
        booleanSetting("nametags", "Show nametags", () -> VisualSettings.showNametags,
            v -> VisualSettings.showNametags = v);
    }

    public static void onWorldRender(RenderLevelStageEvent.AfterEntities event) {
        if (!NexusClient.enabled("ESP")) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) {
            return;
        }

        PoseStack pose = event.getPoseStack();
        if (pose == null) {
            return;
        }

        Vec3 camera = mc.gameRenderer.getMainCamera().position();
        pose.pushPose();
        try {
            // Vertices below are supplied in world coordinates; this makes them
            // camera-relative exactly once through the pose transform.
            pose.translate(-camera.x, -camera.y, -camera.z);

            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            RenderType lineType = RenderUtil.lineType();
            if (lineType == null) {
                return;
            }

            VertexConsumer out = buffers.getBuffer(lineType);
            Matrix4f matrix = pose.last().pose();

            for (Player target : mc.level.players()) {
                if (target == mc.player || !target.isAlive()) {
                    continue;
                }

                var selfTeam = mc.player.getTeam();
                var targetTeam = target.getTeam();
                int color = selfTeam != null && selfTeam.equals(targetTeam)
                    ? VisualSettings.friendColor
                    : VisualSettings.enemyColor;

                if (VisualSettings.esp3d) {
                    drawBox(out, matrix, target.getBoundingBox().inflate(0.035), color);
                }

                if (VisualSettings.showTracers) {
                    drawLine(out, matrix,
                        camera.x, camera.y, camera.z,
                        target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                        color, TRACER_LINE_WIDTH);
                }
            }

            buffers.endBatch(lineType);
        } finally {
            pose.popPose();
        }
    }

    private static void drawBox(VertexConsumer out, Matrix4f matrix, AABB box, int color) {
        double[][] corners = {
            {box.minX, box.minY, box.minZ},
            {box.maxX, box.minY, box.minZ},
            {box.minX, box.maxY, box.minZ},
            {box.maxX, box.maxY, box.minZ},
            {box.minX, box.minY, box.maxZ},
            {box.maxX, box.minY, box.maxZ},
            {box.minX, box.maxY, box.maxZ},
            {box.maxX, box.maxY, box.maxZ}
        };

        for (int[] edge : BOX_EDGES) {
            double[] from = corners[edge[0]];
            double[] to = corners[edge[1]];
            drawLine(out, matrix,
                from[0], from[1], from[2],
                to[0], to[1], to[2],
                color, BOX_LINE_WIDTH);
        }
    }

    private static void drawLine(
        VertexConsumer out,
        Matrix4f matrix,
        double x1, double y1, double z1,
        double x2, double y2, double z2,
        int color,
        float width
    ) {
        // RenderTypes.lines() uses POSITION_COLOR_NORMAL_LINE_WIDTH in 1.21.11.
        // Every vertex must therefore provide position, color, normal and width.
        out.addVertex(matrix, (float) x1, (float) y1, (float) z1)
            .setColor(color)
            .setNormal(0.0F, 1.0F, 0.0F)
            .setLineWidth(width);
        out.addVertex(matrix, (float) x2, (float) y2, (float) z2)
            .setColor(color)
            .setNormal(0.0F, 1.0F, 0.0F)
            .setLineWidth(width);
    }
}
