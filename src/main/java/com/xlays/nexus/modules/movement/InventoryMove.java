package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
public final class InventoryMove extends Module {
 public InventoryMove(){super("InventoryMove",Category.MOVEMENT,false);}
 @Override public void onTick(){var mc=Minecraft.getInstance();var p=mc.player;if(p==null||mc.screen==null)return;
  p.zza=(mc.options.keyUp.isDown()?1:0)-(mc.options.keyDown.isDown()?1:0);
  p.xxa=(mc.options.keyLeft.isDown()?1:0)-(mc.options.keyRight.isDown()?1:0);
  if(mc.options.keyJump.isDown()&&p.onGround())p.jumpFromGround();
  if(mc.options.keySprint.isDown()&&p.zza>0)p.setSprinting(true);
 }
}