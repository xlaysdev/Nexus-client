package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class NoFall extends Module {
 public NoFall(){super("NoFall",Category.MOVEMENT,false);}
 @Override public void onTick(){var p=Minecraft.getInstance().player;if(p!=null&&p.getDeltaMovement().y<0)p.fallDistance=0;}
}