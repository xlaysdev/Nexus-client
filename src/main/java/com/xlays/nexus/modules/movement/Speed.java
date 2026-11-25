package com.xlays.nexus.modules.movement;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/** Port of C00lgui's blocks-per-second Speed implementation. */
public final class Speed extends Module {
    private static final double MIN_BPS = 5.0;
    private static final double MAX_BPS = 15.0;
    private static double blocksPerSecond = 5.0;

    public Speed() {
        super("Speed", Category.MOVEMENT, false);
        numberSetting("bps","Blocks per second",Speed::getBlocksPerSecond,Speed::setBlocksPerSecond,5,15,.5);
    }

    public static double getBlocksPerSecond() {
        return blocksPerSecond;
    }

    public static void setBlocksPerSecond(double value) {
        if (!Double.isFinite(value)) return;
        blocksPerSecond = Math.max(MIN_BPS, Math.min(MAX_BPS, value));
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.input == null || player.isFallFlying()) return;

        float forward = player.input.getMoveVector().y;
        float strafe = player.input.getMoveVector().x;
        if (forward == 0.0F && strafe == 0.0F) return;

        // Same movement model as C00lgui: set horizontal velocity in blocks/tick.
        double speedPerTick = blocksPerSecond / 20.0;
        double radians = Math.toRadians(player.getYRot());
        double sin = Math.sin(radians);
        double cos = Math.cos(radians);

        double motionX = (-sin * forward + cos * strafe) * speedPerTick;
        double motionZ = (cos * forward + sin * strafe) * speedPerTick;
        var velocity = player.getDeltaMovement();

        player.setDeltaMovement(motionX, velocity.y, motionZ);
        player.setSprinting(true);
    }
}
