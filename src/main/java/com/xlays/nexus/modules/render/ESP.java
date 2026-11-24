package com.xlays.nexus.modules.render;
import com.xlays.nexus.NexusClient;
import com.xlays.nexus.modules.Module;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.ShapeRenderer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
public final class ESP extends Module {
 public ESP(){super("ESP",Category.RENDER,false);}
 public static void onWorldRender(RenderLevelStageEvent event){
  if(!(event instanceof RenderLevelStageEvent.AfterEntities)||!NexusClient.enabled("ESP"))return;
  Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.level==null||mc.options.hideGui)return;
  PoseStack pose=event.getPoseStack();if(pose==null)return;
  Vec3 camera=mc.gameRenderer.getMainCamera().position();pose.pushPose();pose.translate(-camera.x,-camera.y,-camera.z);
  MultiBufferSource.BufferSource buffers=mc.renderBuffers().bufferSource();
  var type=RenderUtil.lineType();if(type==null){pose.popPose();return;}VertexConsumer out=buffers.getBuffer(type);
  for(Player target:mc.level.players()){
   if(target==mc.player||!target.isAlive())continue;
   var selfTeam=mc.player.getTeam();var team=target.getTeam();
   int color=selfTeam!=null&&selfTeam.equals(team)?VisualModules.friendColor:VisualModules.enemyColor;
   if(VisualModules.esp3d)ShapeRenderer.renderShape(pose,out,Shapes.create(target.getBoundingBox().inflate(.035)),0,0,0,color,2);
   if(VisualModules.showTracers){var matrix=pose.last().pose();out.addVertex(matrix,0,0,0).setColor(color);
    out.addVertex(matrix,(float)target.getX(),(float)(target.getY()+target.getBbHeight()),(float)target.getZ()).setColor(color);}
  }
  buffers.endBatch(type);pose.popPose();
 }
}