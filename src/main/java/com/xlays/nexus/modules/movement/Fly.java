package com.xlays.nexus.modules.movement;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
public final class Fly extends Module {
 private boolean captured,previousMayfly;
 public Fly(){super("Fly",Category.MOVEMENT,false);numberSetting("verticalSpeed","Vertical speed",()->MovementSettings.verticalSpeed,v->MovementSettings.verticalSpeed=v.floatValue(),.1,1.5,.05);}
 @Override public void onTick(){
  Minecraft mc=Minecraft.getInstance();Player p=mc.player;
  if(p==null){captured=false;return;}
  if(!captured){previousMayfly=p.getAbilities().mayfly;captured=true;}
  p.getAbilities().mayfly=true;p.getAbilities().flying=true;
  Vec3 v=p.getDeltaMovement();double y=(mc.options.keyJump.isDown()?MovementSettings.verticalSpeed:0)-(mc.options.keyShift.isDown()?MovementSettings.verticalSpeed:0);
  p.setDeltaMovement(v.x,y,v.z);p.fallDistance=0;
 }
 @Override protected void onDisable(){
  Minecraft mc=Minecraft.getInstance();if(mc.player==null||!captured)return;
  mc.player.getAbilities().flying=false;mc.player.getAbilities().mayfly=previousMayfly;
  if(mc.getConnection()!=null)mc.getConnection().send(new ServerboundPlayerAbilitiesPacket(mc.player.getAbilities()));
  captured=false;
 }
}