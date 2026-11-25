package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class NoFall extends Module {
 private double minFall=2.0;
 public NoFall(){super("NoFall",Category.MOVEMENT,false);numberSetting("minFall","Minimum fall distance",()->minFall,v->minFall=v,0,10,.5);}
 @Override public void onTick(){var p=Minecraft.getInstance().player;if(p!=null&&p.getDeltaMovement().y<0&&p.fallDistance>=minFall)p.fallDistance=0;}
}