package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class AutoCritical extends Module {
 private boolean swordOnly=true,wasAttacking;
 public AutoCritical(){super("AutoCritical",Category.COMBAT,false);}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();var p=mc.player;
  if(p==null||mc.options==null||mc.screen!=null){wasAttacking=false;return;}
  boolean attack=mc.options.keyAttack.isDown();
  if(attack&&!wasAttacking&&p.onGround()&&!p.isInWater()&&!p.onClimbable()){
   String item=p.getMainHandItem().getItem().getClass().getSimpleName();
   if(!swordOnly||item.contains("Sword")||item.contains("Axe"))p.jumpFromGround();
  }
  wasAttacking=attack;
 }
}