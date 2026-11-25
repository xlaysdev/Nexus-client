package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class AutoCritical extends Module {
 private boolean swordOnly=true;
 public AutoCritical(){super("AutoCritical",Category.COMBAT,false);}
 @Override public void onTick(){
  Minecraft mc=Minecraft.getInstance();var p=mc.player;
  if(p==null||mc.screen!=null||!mc.options.keyAttack.isDown())return;
  if(swordOnly&&!p.getMainHandItem().getItem().getClass().getSimpleName().matches(".*(Sword|Axe).*"))return;
  // Never falsify grounded state or movement; only report whether a natural critical is available.
  if(!p.onGround()&&p.getDeltaMovement().y<0&&!p.isInWater()&&!p.onClimbable()){}
 }
}
