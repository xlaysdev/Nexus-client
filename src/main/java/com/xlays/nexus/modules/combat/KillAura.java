package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.Comparator;

public final class KillAura extends Module {
    private long lastAttack;
    public static float cps=8.0F, range=3.5F;
    public KillAura(){super("KillAura",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.gameMode==null||mc.screen!=null)return;
        long now=System.currentTimeMillis();
        if(now-lastAttack<(long)(1000.0F/Math.max(1.0F,Math.min(20.0F,cps)))||mc.player.getAttackStrengthScale(0)<1)return;
        LivingEntity target=mc.level.getEntitiesOfClass(LivingEntity.class,mc.player.getBoundingBox().inflate(range),
            e->e!=mc.player&&e.isAlive()&&!(e instanceof Player p&&p.isSpectator())&&mc.player.hasLineOfSight(e))
            .stream().filter(e->mc.player.distanceTo(e)<=range).min(Comparator.comparingDouble(mc.player::distanceTo)).orElse(null);
        if(target!=null){mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);lastAttack=now;}
    }
}