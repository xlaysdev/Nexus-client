package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
public final class Speed extends Module {
 public Speed(){super("Speed",Category.MOVEMENT,false);}
 @Override public void onTick(){
  Minecraft mc=Minecraft.getInstance(); Player p=mc.player;
  if(p==null||mc.level==null){setTimer(mc,1);return;}
  if(p.isSprinting()&&!p.isFallFlying()){Vec3 v=p.getDeltaMovement();p.setDeltaMovement(v.x*MovementSettings.speedMultiplier,v.y,v.z*MovementSettings.speedMultiplier);}
  if("Bhop".equals(MovementSettings.speedMode)&&p.onGround()&&(p.zza!=0||p.xxa!=0))p.jumpFromGround();
  setTimer(mc,"Timer".equals(MovementSettings.speedMode)?MovementSettings.timerMultiplier:1.0F);
 }
 private static void setTimer(Minecraft mc,float multiplier){
  try{Field f=Minecraft.class.getDeclaredField("timer");f.setAccessible(true);Object t=f.get(mc);
   for(Field x:t.getClass().getDeclaredFields())if(x.getType()==float.class&&x.getName().toLowerCase().contains("tick")){x.setAccessible(true);x.setFloat(t,20*multiplier);return;}
   for(Method x:t.getClass().getDeclaredMethods())if(x.getParameterCount()==1&&x.getParameterTypes()[0]==float.class&&x.getName().toLowerCase().contains("tick")){x.setAccessible(true);x.invoke(t,20*multiplier);return;}
  }catch(ReflectiveOperationException|RuntimeException ignored){}
 }
}