package com.xlays.nexus.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Comparator;

public final class CombatModules {
    public static float killAuraCps = 8.0F;
    public static float reachDistance = 3.5F;
    public static boolean killAuraRotations = true;
    private static long lastAuraAttack;
    private static long lastCrystalAction;
    private static long lastInventoryAction;
    private static float desiredYaw;
    private static float desiredPitch;

    private CombatModules() {}

    public static void tick(Minecraft mc) {
        if (mc.player == null || mc.level == null || mc.gameMode == null || mc.screen != null) return;
        long now = System.currentTimeMillis();

        if (enabled("AutoTotem") && !enabled("OffhandManager")) autoTotem(mc);
        if (enabled("OffhandManager")) offhandManager(mc);
        if (enabled("AutoArmor")) autoArmor(mc, now);

        if (enabled("KillAura") && now - lastAuraAttack >= (long) (1000.0F / Mth.clamp(killAuraCps, 1.0F, 20.0F))
            && mc.player.getAttackStrengthScale(0.0F) >= 1.0F) {
            LivingEntity target = findTarget(mc, enabled("Reach") ? reachDistance : 3.0F);
            if (target != null) {
                if (killAuraRotations || enabled("Rotations")) rotateToward(mc, target);
                mc.gameMode.attack(mc.player, target);
                mc.player.swing(InteractionHand.MAIN_HAND);
                lastAuraAttack = now;
            }
        }

        if (enabled("CrystalAura") && now - lastCrystalAction >= 250L) {
            crystalAura(mc);
        }
    }

    private static void autoTotem(Minecraft mc) {
        if (mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING)) return;
        int slot = findInventoryItem(mc, Items.TOTEM_OF_UNDYING);
        if (slot >= 0) swapInventoryWithOffhand(mc, slot);
    }

    private static void offhandManager(Minecraft mc) {
        boolean lowHealth = mc.player.getHealth() <= 10.0F || mc.player.getAbsorptionAmount() <= 0.0F
            && mc.player.getHealth() <= 14.0F;
        var wanted = lowHealth ? Items.TOTEM_OF_UNDYING : Items.GOLDEN_APPLE;
        if (mc.player.getOffhandItem().is(wanted)) return;
        int slot = findInventoryItem(mc, wanted);
        if (slot >= 0) swapInventoryWithOffhand(mc, slot);
    }

    private static void autoArmor(Minecraft mc, long now) {
        if (now - lastInventoryAction < 180L) return;
        var inventory = mc.player.getInventory();
        for (int invIndex = 0; invIndex < 36; invIndex++) {
            ItemStack candidate = inventory.getItem(invIndex);
            if (!(candidate.getItem() instanceof ArmorItem candidateArmor)) continue;
            var equipmentSlot = candidateArmor.getEquipmentSlot();
            int armorMenuSlot = switch (equipmentSlot) {
                case HEAD -> 5;
                case CHEST -> 6;
                case LEGS -> 7;
                case FEET -> 8;
                default -> -1;
            };
            if (armorMenuSlot < 0) continue;
            ItemStack equipped = mc.player.getItemBySlot(equipmentSlot);
            if (armorScore(candidateArmor, candidate) <= armorScore(
                equipped.getItem() instanceof ArmorItem currentArmor ? currentArmor : null, equipped)) continue;

            int sourceMenuSlot = inventoryMenuSlot(invIndex);
            int menu = mc.player.inventoryMenu.containerId;
            mc.gameMode.handleInventoryMouseClick(menu, sourceMenuSlot, 0, ClickType.PICKUP, mc.player);
            mc.gameMode.handleInventoryMouseClick(menu, armorMenuSlot, 0, ClickType.PICKUP, mc.player);
            mc.gameMode.handleInventoryMouseClick(menu, sourceMenuSlot, 0, ClickType.PICKUP, mc.player);
            lastInventoryAction = now;
            return;
        }
    }

    private static double armorScore(ArmorItem armor, ItemStack stack) {
        if (armor == null || stack.isEmpty()) return -1.0D;
        double defense = armor.getDefense();
        double toughness = armor.getToughness();
        double durability = stack.isDamageableItem()
            ? (double) (stack.getMaxDamage() - stack.getDamageValue()) / Math.max(1, stack.getMaxDamage()) : 1.0D;
        return defense * 100.0D + toughness * 20.0D + durability * 5.0D;
    }

    private static void crystalAura(Minecraft mc) {
        Player target = mc.level.getEntitiesOfClass(Player.class, mc.player.getBoundingBox().inflate(4.5D),
                p -> p != mc.player && p.isAlive() && !p.isSpectator() && mc.player.hasLineOfSight(p))
            .stream().filter(p -> mc.player.distanceTo(p) <= 4.5F)
            .min(Comparator.comparingDouble(mc.player::distanceTo)).orElse(null);
        if (target == null) return;

        EndCrystal crystal = mc.level.getEntitiesOfClass(EndCrystal.class,
                target.getBoundingBox().inflate(4.5D), Entity::isAlive)
            .stream().min(Comparator.comparingDouble(target::distanceTo)).orElse(null);

        if (crystal != null && mc.player.getAttackStrengthScale(0.0F) >= 1.0F) {
            rotateToward(mc, crystal);
            mc.gameMode.attack(mc.player, crystal);
            mc.player.swing(InteractionHand.MAIN_HAND);
            lastCrystalAction = System.currentTimeMillis();
            return;
        }

        BlockPos support = target.blockPosition().below();
        var state = mc.level.getBlockState(support);
        if (!(state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))) return;
        if (!mc.level.getBlockState(support.above()).canBeReplaced()
            || !mc.level.getBlockState(support.above(2)).isAir()) return;

        InteractionHand hand = mc.player.getMainHandItem().is(Items.END_CRYSTAL) ? InteractionHand.MAIN_HAND
            : mc.player.getOffhandItem().is(Items.END_CRYSTAL) ? InteractionHand.OFF_HAND : null;
        if (hand == null) return;

        rotateToward(mc, target);
        Vec3 hitLocation = Vec3.atCenterOf(support).add(0.0D, 0.5D, 0.0D);
        BlockHitResult hit = new BlockHitResult(hitLocation, Direction.UP, support, false);
        mc.gameMode.useItemOn(mc.player, hand, hit);
        lastCrystalAction = System.currentTimeMillis();
    }

    private static LivingEntity findTarget(Minecraft mc, float range) {
        return mc.level.getEntitiesOfClass(LivingEntity.class, mc.player.getBoundingBox().inflate(range),
                entity -> entity != mc.player && entity.isAlive() && !(entity instanceof Player p && p.isSpectator())
                    && mc.player.hasLineOfSight(entity))
            .stream().filter(entity -> mc.player.distanceTo(entity) <= range)
            .min(Comparator.comparingDouble(mc.player::distanceTo)).orElse(null);
    }

    private static void rotateToward(Minecraft mc, Entity target) {
        Vec3 eyes = mc.player.getEyePosition();
        Vec3 aim = target.getBoundingBox().getCenter();
        double dx = aim.x - eyes.x;
        double dy = aim.y - eyes.y;
        double dz = aim.z - eyes.z;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        desiredYaw = (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0F;
        desiredPitch = (float) -(Mth.atan2(dy, horizontal) * Mth.RAD_TO_DEG);
        float yaw = mc.player.getYRot() + Mth.clamp(Mth.wrapDegrees(desiredYaw - mc.player.getYRot()), -18.0F, 18.0F);
        float pitch = mc.player.getXRot() + Mth.clamp(desiredPitch - mc.player.getXRot(), -14.0F, 14.0F);
        mc.player.setYRot(yaw);
        mc.player.setXRot(Mth.clamp(pitch, -90.0F, 90.0F));
    }

    private static int findInventoryItem(Minecraft mc, net.minecraft.world.item.Item item) {
        for (int i = 0; i < 36; i++) {
            if (mc.player.getInventory().getItem(i).is(item)) return inventoryMenuSlot(i);
        }
        return -1;
    }

    private static int inventoryMenuSlot(int inventoryIndex) {
        return inventoryIndex < 9 ? 36 + inventoryIndex : inventoryIndex;
    }

    private static void swapInventoryWithOffhand(Minecraft mc, int menuSlot) {
        int menu = mc.player.inventoryMenu.containerId;
        mc.gameMode.handleInventoryMouseClick(menu, menuSlot, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(menu, 45, 0, ClickType.PICKUP, mc.player);
        mc.gameMode.handleInventoryMouseClick(menu, menuSlot, 0, ClickType.PICKUP, mc.player);
    }

    private static boolean enabled(String name) {
        return com.xlays.nexus.NexusClient.enabled(name);
    }
}
