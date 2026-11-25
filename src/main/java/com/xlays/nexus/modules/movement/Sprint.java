package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class Sprint extends Module {
 private boolean omni;
 public Sprint(){super("Sprint",Category.MOVEMENT,false);booleanSetting("omni","Omnidirectional",()->omni,v->omni=v);}
 @Override public void onTick(){var p=Minecraft.getInstance().player;if(p!=null&&(p.zza>0||(omni&&p.xxa!=0)))p.setSprinting(true);}
}