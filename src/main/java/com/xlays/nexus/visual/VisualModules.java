package com.xlays.nexus.visual;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.lang.reflect.Method;
import java.util.Set;

public final class VisualModules {
    public static int enemyColor = 0xFFFF4545;
    public static int friendColor = 0xFF45E68A;
    public static int blockColor = 0xFF55B8FF;
    public static int crosshairColor = 0xFF45E6C4;
    public static int crosshairSize = 5;
    public static boolean esp3d = true;
    public static boolean showNametags = true;
    public static boolean showTracers = true;
    public static boolean scanDiamond = true;
    public static boolean scanDebris = true;
    public static boolean scanChests = true;
    public static boolean scanCrystals = true;
    private static double oldGamma = Double.NaN;
    private static final Set<net.minecraft.world.level.block.Block> SEARCH_BLOCKS = Set.of(
        Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS,
        Blocks.CHEST, Blocks.TRAPPED_CHEST, Blocks.ENDER_CHEST
    );
    private static Method linesMethod;

    private VisualModules() {}

    public static void tick(Minecraft mc) {
        if (mc.options == null) return;
        if (NexusClient.enabled("Fullbright")) {
            if (Double.isNaN(oldGamma)) oldGamma = mc.options.gamma().get();
            mc.options.gamma().set(1000.0D);
        } else if (!Double.isNaN(oldGamma)) {
            mc.options.gamma().set(oldGamma);
            oldGamma = Double.NaN;
        }
        // NoHurtCam is implemented through the vanilla accessibility option.
        if (NexusClient.enabled("NoHurtCam")) mc.options.damageTiltStrength().set(0.0D);
    }

    public static void onWorldRender(RenderLevelStageEvent event) {
        if (!(event instanceof RenderLevelStageEvent.AfterEntities)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        boolean esp = NexusClient.enabled("ESP");
        boolean blockEsp = NexusClient.enabled("BlockESP");
        if (!esp && !blockEsp) return;

        PoseStack pose = event.getPoseStack();
        if (pose == null) return;
        Vec3 camera = event.getCamera().position();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        RenderType lineType = getLineType();
        if (lineType == null) {
            pose.popPose();
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(lineType);

        if (esp) {
            for (Player target : mc.level.players()) {
                if (target == mc.player || !target.isAlive()) continue;
                int color = isFriend(target) ? friendColor : enemyColor;
                AABB box = target.getBoundingBox().inflate(0.035D);
                ShapeRenderer.renderShape(pose, consumer, Shapes.create(box), 0, 0, 0, color, 2.0F);
            }
        }

        if (blockEsp) {
            BlockPos center = mc.player.blockPosition();
            int radius = 12;
            for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
                var block = mc.level.getBlockState(pos).getBlock();
                boolean wanted = (scanDiamond && (block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE))
                    || (scanDebris && block == Blocks.ANCIENT_DEBRIS)
                    || (scanChests && (block == Blocks.CHEST || block == Blocks.TRAPPED_CHEST || block == Blocks.ENDER_CHEST));
                if (wanted) {
                    AABB box = new AABB(pos.getX(), pos.getY(), pos.getZ(),
                        pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1).inflate(0.015D);
                    ShapeRenderer.renderShape(pose, consumer, Shapes.create(box), 0, 0, 0, blockColor, 2.0F);
                }
            }
        }
        buffers.endBatch(lineType);
        pose.popPose();
    }

    private static RenderType getLineType() {
        try {
            if (linesMethod == null) {
                linesMethod = RenderTypes.class.getDeclaredMethod("lines");
                linesMethod.setAccessible(true);
            }
            return (RenderType) linesMethod.invoke(null);
        } catch (ReflectiveOperationException e) {
            return null;
        }
    }

    private static boolean isFriend(Player player) {
        var selfTeam = Minecraft.getInstance().player.getTeam();
        var targetTeam = player.getTeam();
        return selfTeam != null && selfTeam.equals(targetTeam);
    }

    public static boolean isSearchBlock(net.minecraft.world.level.block.Block block) {
        return SEARCH_BLOCKS.contains(block);
    }
}
