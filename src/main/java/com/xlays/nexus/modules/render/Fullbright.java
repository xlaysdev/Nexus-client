package com.xlays.nexus.modules.render;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Fullbright copied from C00lgui's Night Vision tick implementation,
 * adapted to Nexus's Module lifecycle.
 */
public final class Fullbright extends Module {

    public Fullbright() {
        super("Fullbright", Category.RENDER, false);
    }

    @Override
    public void onTick() {
        if (!enabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null) {
            return;
        }

        // C00lgui behavior: refresh a short Night Vision effect every tick.
        player.addEffect(
            new MobEffectInstance(
                MobEffects.NIGHT_VISION,
                40,
                0,
                false,
                false,
                false
            )
        );
    }

    @Override
    protected void onDisable() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player != null) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
