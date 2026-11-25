package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.BlockItem;
import java.lang.reflect.Field;
public final class FastPlace extends Module {
 private Field delayField;
 public FastPlace(){super("FastPlace",Category.PLAYER,false);}
 @Override public void onTick(){
  Minecraft mc=Minecraft.getInstance();var p=mc.player;
  if(!enabled||p==null||mc.options==null||mc.screen!=null||!mc.options.keyUse.isDown()||!(p.getMainHandItem().getItem() instanceof BlockItem))return;
  try{
   if(delayField==null){for(String n:new String[]{"rightClickDelay","f_91074_"})try{delayField=Minecraft.class.getDeclaredField(n);break;}catch(NoSuchFieldException ignored){}}
   if(delayField==null)return;delayField.setAccessible(true);if(delayField.getInt(mc)>1)delayField.setInt(mc,1);
  }catch(ReflectiveOperationException|RuntimeException ex){delayField=null;}
 }
}