package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class AirJump extends Module {
 private boolean wasPressed;
 public AirJump(){super("AirJump",Category.MOVEMENT,false);}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.options==null||mc.screen!=null){wasPressed=false;return;}
  boolean down=mc.options.keyJump.isDown();
  if(down&&!wasPressed&&!p.onGround()&&p.getAbilities().mayfly==false){p.jumpFromGround();}
  wasPressed=down;
 }
}
