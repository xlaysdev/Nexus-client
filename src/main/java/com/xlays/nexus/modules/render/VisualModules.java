package com.xlays.nexus.modules.render;

import com.xlays.nexus.NexusClient;

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
    private static double oldGamma = Double.NaN;
    private static double oldDamageTilt = Double.NaN;
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
        if (NexusClient.enabled("NoHurtCam")) {
            if (Double.isNaN(oldDamageTilt)) oldDamageTilt = mc.options.damageTiltStrength().get();
            mc.options.damageTiltStrength().set(0.0D);
        } else if (!Double.isNaN(oldDamageTilt)) {
            mc.options.damageTiltStrength().set(oldDamageTilt);
            oldDamageTilt = Double.NaN;
        }
    }

    public static void onWorldRender(RenderLevelStageEvent event) {
        if (!(event instanceof RenderLevelStageEvent.AfterEntities)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.options.hideGui) return;
        OreESP.render();
        boolean esp = NexusClient.enabled("ESP");
        boolean blockEsp = NexusClient.enabled("BlockESP");
        if (!esp && !blockEsp) return;

        PoseStack pose = event.getPoseStack();
        if (pose == null) return;
        Vec3 camera = mc.gameRenderer.getMainCamera().position();
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
                if (esp3d) ShapeRenderer.renderShape(pose, consumer, Shapes.create(box), 0, 0, 0, color, 2.0F);
                if (showTracers) {
                    var matrix = pose.last().pose();
                    consumer.addVertex(matrix, 0.0F, 0.0F, 0.0F).setColor(color);
                    consumer.addVertex(matrix, (float) target.getX(), (float) (target.getY() + target.getBbHeight()), (float) target.getZ()).setColor(color);
                }
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

    public static void renderOverlay(net.minecraft.client.gui.GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (!NexusClient.enabled("ESP") || mc.player == null || mc.level == null) return;
        int screenW = mc.getWindow().getGuiScaledWidth();
        int screenH = mc.getWindow().getGuiScaledHeight();
        for (Player target : mc.level.players()) {
            if (target == mc.player || !target.isAlive()) continue;
            int color = isFriend(target) ? friendColor : enemyColor;
            Vec3 topPoint = target.position().add(0.0D, target.getBbHeight() + 0.25D, 0.0D);
            Vec3 bottomPoint = target.position().add(0.0D, 0.05D, 0.0D);
            Vec3 top = mc.gameRenderer.projectPointToScreen(topPoint);
            Vec3 bottom = mc.gameRenderer.projectPointToScreen(bottomPoint);
            if (top.z < 0.0D || bottom.z < 0.0D) continue;
            if (!esp3d) {
                int boxH = Math.max(4, (int) Math.abs(bottom.y - top.y));
                int boxW = Math.max(3, boxH / 3);
                int cx = (int) ((top.x + bottom.x) * 0.5D);
                int x1 = cx - boxW / 2, x2 = cx + boxW / 2;
                int y1 = (int) Math.min(top.y, bottom.y), y2 = y1 + boxH;
                graphics.fill(x1, y1, x2, y1 + 1, color);
                graphics.fill(x1, y2 - 1, x2, y2, color);
                graphics.fill(x1, y1, x1 + 1, y2, color);
                graphics.fill(x2 - 1, y1, x2, y2, color);
            }
            if (showTracers && !esp3d) {
                int x1 = screenW / 2, y1 = screenH - 2;
                int x2 = (int) bottom.x, y2 = (int) bottom.y;
                int dx = x2 - x1, dy = y2 - y1;
                int steps = Math.max(Math.abs(dx), Math.abs(dy));
                if (steps > 0 && steps < 2000) {
                    for (int i = 0; i <= steps; i++) {
                        int px = x1 + dx * i / steps;
                        int py = y1 + dy * i / steps;
                        graphics.fill(px, py, px + 1, py + 1, color);
                    }
                }
            }
            if (showNametags) {
                String label = target.getName().getString() + "  " + Math.round(target.getHealth()) + "HP  "
                    + String.format("%.1fm", mc.player.distanceTo(target));
                graphics.drawCenteredString(mc.font, label, (int) top.x, (int) top.y - 10, color);
            }
        }
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
