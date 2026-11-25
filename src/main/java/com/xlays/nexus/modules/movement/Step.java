package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class Step extends Module {
 private double height=1.0;
 public Step(){super("Step",Category.MOVEMENT,false);numberSetting("height","Step height",()->height,v->height=v,.5,1.5,.5);}
 @Override public void onTick(){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||!p.onGround()||!p.horizontalCollision||(p.zza==0&&p.xxa==0))return;if(mc.level.noCollision(p,p.getBoundingBox().move(0,height,0)))p.setPos(p.getX(),p.getY()+height,p.getZ());}
}