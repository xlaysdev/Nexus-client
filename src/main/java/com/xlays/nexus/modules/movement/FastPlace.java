package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;import net.minecraft.client.Minecraft;import net.minecraft.world.item.BlockItem;import java.lang.reflect.Field;
public final class FastPlace extends Module {
 private Field delayField;private int delay=1;
 public FastPlace(){super("FastPlace",Category.PLAYER,false);numberSetting("delay","Maximum use delay",()->delay,v->delay=v.intValue(),0,4,1);}
 @Override public void onTick(){Minecraft mc=Minecraft.getInstance();var p=mc.player;if(!enabled||p==null||mc.options==null||mc.screen!=null||!mc.options.keyUse.isDown()||!(p.getMainHandItem().getItem() instanceof BlockItem))return;try{if(delayField==null){for(String n:new String[]{"rightClickDelay","f_91074_"})try{delayField=Minecraft.class.getDeclaredField(n);break;}catch(NoSuchFieldException ignored){}}if(delayField==null)return;delayField.setAccessible(true);if(delayField.getInt(mc)>delay)delayField.setInt(mc,delay);}catch(ReflectiveOperationException|RuntimeException ex){delayField=null;}}
}