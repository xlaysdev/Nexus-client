package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.level.block.Blocks;
import java.util.ArrayList;
import java.util.List;
public final class ChestESP extends Module {
 private int radius=12,tick; private final List<BlockPos> found=new ArrayList<>();
 public ChestESP(){super("ChestESP",Category.RENDER,false);numberSetting("radius","Scan radius",()->radius,v->radius(v.intValue()),4,48,1);}
 public int radius(){return radius;} public void radius(int v){radius=Math.max(4,Math.min(48,v));tick=9;found.clear();}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();if(mc.player==null||mc.level==null){found.clear();return;}
  if(++tick<10)return;tick=0;BlockPos c=mc.player.blockPosition();int r2=radius*radius;List<BlockPos> next=new ArrayList<>();
  scan: for(int x=-radius;x<=radius;x++)for(int y=-radius;y<=radius;y++)for(int z=-radius;z<=radius;z++){
   if(x*x+y*y+z*z>r2)continue;BlockPos p=c.offset(x,y,z);if(!mc.level.hasChunkAt(p))continue;
   var b=mc.level.getBlockState(p).getBlock();
   if(b==Blocks.CHEST||b==Blocks.TRAPPED_CHEST||b==Blocks.ENDER_CHEST||b==Blocks.BARREL){next.add(p.immutable());if(next.size()>=256)break scan;}
  }
  found.clear();found.addAll(next);
 }
 @Override public void onRender(){if(found.isEmpty())return;var mc=Minecraft.getInstance();if(mc.level==null)return;for(BlockPos p:found)Gizmos.cuboid(p,GizmoStyle.stroke(0xFFFFC857,2F)).setAlwaysOnTop();}
 @Override protected void onDisable(){found.clear();tick=0;}
}