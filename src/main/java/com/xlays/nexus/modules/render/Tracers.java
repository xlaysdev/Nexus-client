package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;import net.minecraft.client.Minecraft;import net.minecraft.gizmos.GizmoStyle;import net.minecraft.gizmos.Gizmos;import net.minecraft.world.phys.AABB;import net.minecraft.world.phys.Vec3;
public final class Tracers extends Module {
 private float width=1.5F;private int color=0xFF55D6A0;
 public Tracers(){super("Tracers",Category.RENDER,false);numberSetting("width","Line width",()->width,v->width=v.floatValue(),.5,4,.5);}
 @Override public void onRender(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;Vec3 start=mc.player.getEyePosition();for(var target:mc.level.players()){if(target==mc.player||!target.isAlive())continue;Vec3 end=target.getBoundingBox().getCenter();Gizmos.cuboid(new AABB(start,end).inflate(.025),GizmoStyle.stroke(color,width)).setAlwaysOnTop();}}
}