package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
public final class Velocity extends Module {
 public Velocity(){super("Velocity",Category.MOVEMENT,false);numberSetting("scale","Knockback scale",()->MovementSettings.velocityScale,v->MovementSettings.velocityScale=v.floatValue(),0,1,.1);}
 public static void onKnockback(LivingKnockBackEvent e){
  if(e.getEntity() instanceof Player){float s=Math.max(0,Math.min(1,MovementSettings.velocityScale));if(s==0)e.setCanceled(true);else e.setStrength(e.getStrength()*s);}
 }
}