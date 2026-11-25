package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

/**
 * Single-player crystal automation. It selects hostile mobs, places crystals on
 * nearby valid obsidian/bedrock, then attacks spawned crystals automatically.
 * It intentionally does not rotate the player's camera or require crosshair aim.
 */
public final class CrystalAura extends Module {
    public static boolean targetHostiles = true;
    public static boolean targetPlayers = false;
    public static double targetRange = 12.0;
    public static double placeRange = 4.5;
    public static int actionDelayMs = 150;
    public static double explosionRange = 6.0;

    private long lastAction;

    public CrystalAura() {
        super("CrystalAura", Category.COMBAT, false);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;

        long now = System.currentTimeMillis();
        if (now - lastAction < actionDelayMs) return;

        LivingEntity target = mc.level.getEntitiesOfClass(
                LivingEntity.class,
                mc.player.getBoundingBox().inflate(targetRange),
                entity -> isValidTarget(entity, mc.player)
        ).stream()
                .filter(entity -> mc.player.distanceTo(entity) <= targetRange)
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);

        if (target == null) return;

        // Detonate a crystal already close enough to damage the selected target.
        EndCrystal crystal = mc.level.getEntitiesOfClass(
                EndCrystal.class,
                mc.player.getBoundingBox().inflate(placeRange + 2.0),
                Entity::isAlive
        ).stream()
                .filter(existing -> existing.distanceTo(target) <= explosionRange)
                .min(Comparator.comparingDouble(mc.player::distanceTo))
                .orElse(null);

        if (crystal != null) {
            mc.gameMode.attack(mc.player, crystal);
            mc.player.swing(InteractionHand.MAIN_HAND);
            lastAction = now;
            return;
        }

        InteractionHand hand = mc.player.getMainHandItem().is(Items.END_CRYSTAL)
                ? InteractionHand.MAIN_HAND
                : mc.player.getOffhandItem().is(Items.END_CRYSTAL)
                    ? InteractionHand.OFF_HAND : null;
        if (hand == null) return;

        BlockPos support = findPlacement(mc, target);
        if (support == null) return;

        BlockHitResult hit = new BlockHitResult(
                Vec3.atCenterOf(support).add(0.0, 0.5, 0.0),
                Direction.UP,
                support,
                false
        );
        mc.gameMode.useItemOn(mc.player, hand, hit);
        mc.player.swing(hand);
        lastAction = now;
    }

    private static boolean isValidTarget(LivingEntity entity, Player player) {
        if (entity == player || !entity.isAlive() || entity.isInvulnerable()) return false;
        if (entity instanceof Player other && (other.isSpectator() || !targetPlayers)) return false;
        return (targetHostiles && entity instanceof Enemy)
                || (targetPlayers && entity instanceof Player);
    }

    private static BlockPos findPlacement(Minecraft mc, LivingEntity target) {
        BlockPos origin = target.blockPosition();
        BlockPos best = null;
        double bestScore = Double.MAX_VALUE;
        int scan = (int) Math.ceil(placeRange) + 1;

        for (int dx = -scan; dx <= scan; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                for (int dz = -scan; dz <= scan; dz++) {
                    BlockPos base = origin.offset(dx, dy, dz);
                    var state = mc.level.getBlockState(base);
                    if (!(state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))) continue;

                    BlockPos spawn = base.above();
                    if (!mc.level.getBlockState(spawn).isAir()
                            || !mc.level.getBlockState(spawn.above()).isAir()) continue;

                    Vec3 crystalPos = Vec3.atCenterOf(spawn);
                    double playerDistance = mc.player.position().distanceTo(crystalPos);
                    double targetDistance = target.position().distanceTo(crystalPos);
                    if (playerDistance > placeRange || targetDistance > explosionRange) continue;

                    // Crystal's two-block spawn volume must not overlap a living entity.
                    AABB volume = new AABB(spawn.getX(), spawn.getY(), spawn.getZ(),
                            spawn.getX() + 1.0, spawn.getY() + 2.0, spawn.getZ() + 1.0);
                    boolean occupied = !mc.level.getEntitiesOfClass(
                            Entity.class, volume,
                            entity -> entity.isAlive() && !(entity instanceof EndCrystal)
                    ).isEmpty();
                    if (occupied) continue;

                    // Prefer a crystal position close to the target and reachable from the player.
                    double score = targetDistance * targetDistance
                            + playerDistance * playerDistance * 0.35;
                    if (score < bestScore) {
                        bestScore = score;
                        best = base;
                    }
                }
            }
        }
        return best;
    }

    @Override
    protected void onDisable() {
        lastAction = 0L;
    }
}
