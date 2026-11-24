package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import java.lang.reflect.Field;
public final class FastPlace extends Module {
 public FastPlace(){super("FastPlace",Category.MOVEMENT,false);}
 @Override public void onTick(){try{Minecraft mc=Minecraft.getInstance();for(Field f: Minecraft.class.getDeclaredFields())if(f.getType()==int.class&&f.getName().toLowerCase().contains("rightclick")){f.setAccessible(true);f.setInt(mc,0);return;}}catch(ReflectiveOperationException|RuntimeException ignored){}}
}