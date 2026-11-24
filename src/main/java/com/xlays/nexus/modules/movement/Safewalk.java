package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
public final class Safewalk extends Module {
 public Safewalk(){super("Safewalk",Category.MOVEMENT,false);}
 @Override public void onTick(){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.level==null||!p.onGround())return;
  Vec3 v=p.getDeltaMovement();double x=v.x,z=v.z,s=.05;
  while(x!=0&&!support(mc,p,x,0))x=reduce(x,s);
  while(z!=0&&!support(mc,p,0,z))z=reduce(z,s);
  while(x!=0&&z!=0&&!support(mc,p,x,z)){x=reduce(x,s);z=reduce(z,s);}
  p.setDeltaMovement(x,v.y,z);
 }
 private static double reduce(double v,double a){return Math.abs(v)<=a?0:v-Math.copySign(a,v);}
 private static boolean support(Minecraft mc,net.minecraft.world.entity.player.Player p,double x,double z){return mc.level.getBlockCollisions(p,p.getBoundingBox().move(x,-.06,z)).iterator().hasNext();}
}