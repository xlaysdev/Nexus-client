package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
public final class TriggerBot extends Module {
 private boolean players=true,hostiles=true;
 public TriggerBot(){super("TriggerBot",Category.COMBAT,false);booleanSetting("players","Target players",()->players,v->players=v);booleanSetting("hostiles","Target hostile mobs",()->hostiles,v->hostiles=v);}
 @Override public void onTick(){Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.gameMode==null||mc.screen!=null||!(mc.hitResult instanceof EntityHitResult hit)||!(hit.getEntity() instanceof LivingEntity target)||!target.isAlive()||mc.player.getAttackStrengthScale(0)<1)return;
  boolean player=target instanceof net.minecraft.world.entity.player.Player;if((player&&!players)||(!player&&!hostiles))return;mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);
 }
}