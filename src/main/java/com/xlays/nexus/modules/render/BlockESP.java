package com.xlays.nexus.modules.render;
import com.xlays.nexus.NexusClient;
import com.xlays.nexus.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
public final class BlockESP extends Module {
 public BlockESP(){super("BlockESP",Category.RENDER,false);}
 public static void onWorldRender(RenderLevelStageEvent.AfterEntities event){
  if(!(event instanceof RenderLevelStageEvent.AfterEntities)||!NexusClient.enabled("BlockESP"))return;
  Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui)return;
  PoseStack pose=event.getPoseStack();if(pose==null)return;
  Vec3 camera=mc.gameRenderer.getMainCamera().position();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
  MultiBufferSource.BufferSource buffers=mc.renderBuffers().bufferSource();var type=RenderUtil.lineType();
  if(type==null){pose.popPose();return;}VertexConsumer out=buffers.getBuffer(type);
  BlockPos center=mc.player.blockPosition();int radius=12;
  for(BlockPos pos:BlockPos.betweenClosed(center.offset(-radius,-radius,-radius),center.offset(radius,radius,radius))){
   var block=mc.level.getBlockState(pos).getBlock();
   boolean wanted=(VisualSettings.scanDiamond&&(block==Blocks.DIAMOND_ORE||block==Blocks.DEEPSLATE_DIAMOND_ORE))
    ||(VisualSettings.scanDebris&&block==Blocks.ANCIENT_DEBRIS)
    ||(VisualSettings.scanChests&&(block==Blocks.CHEST||block==Blocks.TRAPPED_CHEST||block==Blocks.ENDER_CHEST));
   if(wanted){AABB box=new AABB(pos.getX(),pos.getY(),pos.getZ(),pos.getX()+1,pos.getY()+1,pos.getZ()+1).inflate(.015);
    ShapeRenderer.renderShape(pose,out,Shapes.create(box),0,0,0,VisualSettings.blockColor,2);}
  }
  buffers.endBatch(type);pose.popPose();
 }
}