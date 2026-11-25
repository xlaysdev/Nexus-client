package com.xlays.nexus.modules.player;
import com.xlays.nexus.modules.Module;import net.minecraft.client.player.LocalPlayer;import net.neoforged.neoforge.event.entity.player.PlayerEvent;
public final class FastBreak extends Module {
 private float multiplier=2.0F;private boolean instant;
 public FastBreak(){super("FastBreak",Category.PLAYER,false);numberSetting("multiplier","Break multiplier",()->multiplier,v->multiplier(v.floatValue()),1,10,.5);booleanSetting("instant","Instant break",()->instant,v->instant(v));}
 public void onBreakSpeed(PlayerEvent.BreakSpeed e){if(enabled&&e.getEntity() instanceof LocalPlayer){float base=e.getOriginalSpeed();e.setNewSpeed(instant?Math.max(base,1000F):base*multiplier);}}
 public float multiplier(){return multiplier;}public void multiplier(float v){multiplier=Math.max(1,Math.min(10,v));}public boolean instant(){return instant;}public void instant(boolean v){instant=v;}
}