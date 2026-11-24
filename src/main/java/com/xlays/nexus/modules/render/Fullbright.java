package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class Fullbright extends Module {
 private double previous=Double.NaN;
 public Fullbright(){super("Fullbright",Category.RENDER,false);}
 @Override public void onTick(){var option=Minecraft.getInstance().options.gamma();if(Double.isNaN(previous))previous=option.get();option.set(1000.0);}
 @Override protected void onDisable(){if(!Double.isNaN(previous)){Minecraft.getInstance().options.gamma().set(previous);previous=Double.NaN;}}
}