package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import com.xlays.nexus.NexusClient;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import java.awt.Color;
import java.util.Comparator;
@EventBusSubscriber(modid=NexusClient.MOD_ID,value=Dist.CLIENT)
public final class RainbowHUD extends Module {
 public RainbowHUD(){super("RainbowHUD",Category.RENDER,false);}
 @SubscribeEvent public static void renderHud(RenderGuiEvent.Post e){
  var mc=Minecraft.getInstance();if(!NexusClient.enabled("RainbowHUD")||mc.player==null||mc.options.hideGui||mc.screen!=null)return;
  var modules=NexusClient.MODULES.all().stream().filter(m->m.enabled&&!m.name.equals("RainbowHUD")).map(m->m.name).sorted(Comparator.comparingInt(String::length).reversed()).toList();
  int x=mc.getWindow().getGuiScaledWidth()-8,y=8;
  for(int i=0;i<modules.size();i++){String name=modules.get(i);int color=Color.HSBtoRGB((System.currentTimeMillis()/500F+i*.12F)%1F,.85F,1F);e.getGuiGraphics().drawString(mc.font,name,x-mc.font.width(name),y+i*(mc.font.lineHeight+2),color,false);}
 }
}