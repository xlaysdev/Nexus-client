package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class NoHurtCam extends Module {
 private double previous=Double.NaN;
 public NoHurtCam(){super("NoHurtCam",Category.RENDER,false);}
 @Override public void onTick(){var option=Minecraft.getInstance().options.damageTiltStrength();if(Double.isNaN(previous))previous=option.get();option.set(0.0);}
 @Override protected void onDisable(){if(!Double.isNaN(previous)){Minecraft.getInstance().options.damageTiltStrength().set(previous);previous=Double.NaN;}}
}