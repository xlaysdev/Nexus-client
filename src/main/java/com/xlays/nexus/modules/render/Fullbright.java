package com.xlays.nexus.modules.render;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;

/**
 * Client-only Fullbright implemented with Minecraft's Night Vision effect.
 * The effect is refreshed locally and never sent to the server.
 */
public final class Fullbright extends Module {
    private static final int EFFECT_DURATION = 220;
    private static final int REFRESH_THRESHOLD = 40;

    private MobEffectInstance previousNightVision;

    public Fullbright() {
        super("Fullbright", Category.RENDER, false);
    }

    @Override
    protected void onEnable() {
        Player player = Minecraft.getInstance().player;
        previousNightVision = player == null
            ? null
            : player.getEffect(MobEffects.NIGHT_VISION) == null
                ? null
                : new MobEffectInstance(player.getEffect(MobEffects.NIGHT_VISION));
        applyNightVision(player);
    }

    @Override
    public void onTick() {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;

        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        if (current == null || current.getDuration() <= REFRESH_THRESHOLD) {
            applyNightVision(player);
        }
    }

    private static void applyNightVision(Player player) {
        if (player == null) return;

        player.addEffect(new MobEffectInstance(
            MobEffects.NIGHT_VISION,
            EFFECT_DURATION,
            0,
            true,
            false,
            false
        ));
    }

    @Override
    protected void onDisable() {
        Player player = Minecraft.getInstance().player;
        if (player != null) {
            player.removeEffect(MobEffects.NIGHT_VISION);
            if (previousNightVision != null) {
                player.addEffect(new MobEffectInstance(previousNightVision));
            }
        }
        previousNightVision = null;
    }
}
