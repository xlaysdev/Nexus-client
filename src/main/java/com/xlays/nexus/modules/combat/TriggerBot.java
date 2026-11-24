package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;

public final class TriggerBot extends Module {
    public TriggerBot(){super("TriggerBot",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null||!(mc.hitResult instanceof EntityHitResult hit)||
            !(hit.getEntity() instanceof LivingEntity target)||!target.isAlive()||mc.player.getAttackStrengthScale(0)<1)return;
        mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);
    }
}