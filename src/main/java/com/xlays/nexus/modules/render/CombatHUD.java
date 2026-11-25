package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import com.xlays.nexus.NexusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@EventBusSubscriber(modid=NexusClient.MOD_ID,value=Dist.CLIENT)
public final class CombatHUD extends Module {
 public CombatHUD(){super("CombatHUD",Category.COMBAT,false);}
 @SubscribeEvent public static void renderHud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();if(!NexusClient.enabled("CombatHUD")||mc.player==null||mc.options.hideGui||mc.screen!=null)return;
  GuiGraphics g=e.getGuiGraphics();int x=8,y=28;
  g.fill(x-3,y-3,x+154,y+43,0xA910141D);
  g.drawString(mc.font,"HP  "+Math.round(mc.player.getHealth())+" / "+Math.round(mc.player.getMaxHealth()),x,y,0xFF75E0B0,false);
  g.drawString(mc.font,"ARMOR  "+mc.player.getArmorValue(),x,y+13,0xFFE5EAF4,false);
  if(mc.hitResult instanceof EntityHitResult hit&&hit.getEntity() instanceof LivingEntity l&&l.isAlive())
   g.drawString(mc.font,"TARGET  "+Math.round(l.getHealth())+" HP",x,y+26,0xFFFFA0B0,false);
 }
}