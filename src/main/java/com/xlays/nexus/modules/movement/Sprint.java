package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class Sprint extends Module {
 public Sprint(){super("Sprint",Category.MOVEMENT,false);}
 @Override public void onTick(){var p=Minecraft.getInstance().player;if(p!=null&&p.zza>0)p.setSprinting(true);}
}