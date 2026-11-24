package com.xlays.nexus.movement;

import com.xlays.nexus.NexusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class MovementModules {
    public static float speedMultiplier = 1.3F;
    public static float verticalSpeed = 0.42F;
    public static float velocityScale = 0.0F;
    public static float timerMultiplier = 1.15F;
    public static String speedMode = "Normal";
    private static boolean wasFlying;
    private static boolean previousMayfly;

    private MovementModules() {}

    public static void tick(Minecraft mc) {
        Player player = mc.player;
        if (player == null || mc.level == null) {
            resetTimer(mc);
            return;
        }

        if (NexusClient.enabled("Speed")) {
            if (player.isSprinting() && player.onGround() || player.isSprinting() && !player.isFallFlying()) {
                Vec3 velocity = player.getDeltaMovement();
                player.setDeltaMovement(velocity.x * speedMultiplier, velocity.y, velocity.z * speedMultiplier);
            }
            if ("Bhop".equals(speedMode) && player.onGround() && hasMovementInput(player)) {
                player.jumpFromGround();
            }
            if ("Timer".equals(speedMode)) setTimer(mc, timerMultiplier);
            else resetTimer(mc);
        } else {
            resetTimer(mc);
        }

        if (NexusClient.enabled("Fly")) {
            if (!wasFlying) {
                previousMayfly = player.getAbilities().mayfly;
                wasFlying = true;
            }
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            Vec3 motion = player.getDeltaMovement();
            double vertical = 0.0D;
            if (mc.options.keyJump.isDown()) vertical += verticalSpeed;
            if (mc.options.keyShift.isDown()) vertical -= verticalSpeed;
            player.setDeltaMovement(motion.x, vertical, motion.z);
            player.fallDistance = 0.0F;
        } else if (wasFlying) {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = previousMayfly;
            if (mc.getConnection() != null) {
                mc.getConnection().send(new ServerboundPlayerAbilitiesPacket(player.getAbilities()));
            }
            wasFlying = false;
        }

        if (NexusClient.enabled("NoFall") && player.getDeltaMovement().y < 0.0D) {
            player.fallDistance = 0.0F;
        }

        if (NexusClient.enabled("Sprint") && hasForwardInput(player)) {
            player.setSprinting(true);
        }

        if (NexusClient.enabled("Step") && player.onGround() && player.horizontalCollision
            && hasMovementInput(player)) {
            var raisedBox = player.getBoundingBox().move(0.0D, 1.0D, 0.0D);
            if (mc.level.noCollision(player, raisedBox)) {
                player.setPos(player.getX(), player.getY() + 1.0D, player.getZ());
            }
        }

        if (NexusClient.enabled("Safewalk") && player.onGround()) {
            Vec3 motion = player.getDeltaMovement();
            double x = motion.x;
            double z = motion.z;
            double step = 0.05D;
            while (x != 0.0D && !hasSupport(mc, player, x, 0.0D)) x = reduceTowardZero(x, step);
            while (z != 0.0D && !hasSupport(mc, player, 0.0D, z)) z = reduceTowardZero(z, step);
            while (x != 0.0D && z != 0.0D && !hasSupport(mc, player, x, z)) {
                x = reduceTowardZero(x, step);
                z = reduceTowardZero(z, step);
            }
            player.setDeltaMovement(x, motion.y, z);
        }

        if (NexusClient.enabled("InventoryMove") && mc.screen != null) {
            player.zza = (mc.options.keyUp.isDown() ? 1.0F : 0.0F)
                - (mc.options.keyDown.isDown() ? 1.0F : 0.0F);
            player.xxa = (mc.options.keyLeft.isDown() ? 1.0F : 0.0F)
                - (mc.options.keyRight.isDown() ? 1.0F : 0.0F);
            if (mc.options.keyJump.isDown() && player.onGround()) player.jumpFromGround();
            if (mc.options.keySprint.isDown() && player.zza > 0.0F) player.setSprinting(true);
        }

        if (NexusClient.enabled("FastPlace")) setRightClickDelay(mc, 0);
    }

    public static void onKnockback(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player && NexusClient.enabled("Velocity")) {
            float scale = Math.max(0.0F, Math.min(1.0F, velocityScale));
            if (scale == 0.0F) event.setCanceled(true);
            else event.setStrength(event.getStrength() * scale);
        }
    }

    private static boolean hasMovementInput(Player player) {
        return player.zza != 0.0F || player.xxa != 0.0F;
    }

    private static boolean hasForwardInput(Player player) {
        return player.zza > 0.0F;
    }

    private static double reduceTowardZero(double value, double amount) {
        if (Math.abs(value) <= amount) return 0.0D;
        return value - Math.copySign(amount, value);
    }

    private static boolean hasSupport(Minecraft mc, Player player, double dx, double dz) {
        var box = player.getBoundingBox().move(dx, -0.06D, dz);
        return mc.level.getBlockCollisions(player, box).iterator().hasNext();
    }

    private static void setTimer(Minecraft mc, float multiplier) {
        try {
            Field timerField = Minecraft.class.getDeclaredField("timer");
            timerField.setAccessible(true);
            Object timer = timerField.get(mc);
            for (Field field : timer.getClass().getDeclaredFields()) {
                if (field.getType() == float.class && field.getName().toLowerCase().contains("tick")) {
                    field.setAccessible(true);
                    field.setFloat(timer, 20.0F * multiplier);
                    return;
                }
            }
            for (Method method : timer.getClass().getDeclaredMethods()) {
                if (method.getParameterCount() == 1 && method.getParameterTypes()[0] == float.class
                    && method.getName().toLowerCase().contains("tick")) {
                    method.setAccessible(true);
                    method.invoke(timer, 20.0F * multiplier);
                    return;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // Timer internals differ across Minecraft versions; movement modules remain usable without Timer mode.
        }
    }

    private static void resetTimer(Minecraft mc) {
        setTimer(mc, 1.0F);
    }

    private static void setRightClickDelay(Minecraft mc, int ticks) {
        try {
            for (Field field : Minecraft.class.getDeclaredFields()) {
                if (field.getType() == int.class && field.getName().toLowerCase().contains("rightclick")) {
                    field.setAccessible(true);
                    field.setInt(mc, ticks);
                    return;
                }
            }
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            // The right-click delay field is version-dependent.
        }
    }
}
