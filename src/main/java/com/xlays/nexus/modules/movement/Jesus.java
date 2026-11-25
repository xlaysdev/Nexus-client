package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
public final class Jesus extends Module {
 private double lift=.08;
 public Jesus(){super("Jesus",Category.MOVEMENT,false);numberSetting("lift","Water lift",()->lift,v->lift=v,.02,.2,.01);}
 @Override public void onTick(){var p=Minecraft.getInstance().player;if(p==null||!p.isInWater()||p.isUnderWater())return;Vec3 v=p.getDeltaMovement();if(v.y<0)p.setDeltaMovement(v.x,Math.max(v.y,lift),v.z);}
}