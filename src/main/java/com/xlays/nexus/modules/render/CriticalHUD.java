package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import com.xlays.nexus.NexusClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
@EventBusSubscriber(modid=NexusClient.MOD_ID,value=Dist.CLIENT)
public final class CriticalHUD extends Module {
 public CriticalHUD(){super("CriticalHUD",Category.RENDER,false);}
 @SubscribeEvent public static void renderHud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();var p=mc.player;
  if(!NexusClient.enabled("CriticalHUD")||p==null||mc.options.hideGui||mc.screen!=null)return;
  if(!p.onGround()&&p.getDeltaMovement().y<-.001&&!p.isInWater()){
   String s="CRITICAL READY";int x=mc.getWindow().getGuiScaledWidth()/2-mc.font.width(s)/2;
   e.getGuiGraphics().drawString(mc.font,s,x,mc.getWindow().getGuiScaledHeight()/2+24,0xFF55D6A0,true);
  }
 }
}