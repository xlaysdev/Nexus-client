package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class Step extends Module {
 public Step(){super("Step",Category.MOVEMENT,false);}
 @Override public void onTick(){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||!p.onGround()||!p.horizontalCollision||(p.zza==0&&p.xxa==0))return;
  if(mc.level.noCollision(p,p.getBoundingBox().move(0,1,0)))p.setPos(p.getX(),p.getY()+1,p.getZ());
 }
}