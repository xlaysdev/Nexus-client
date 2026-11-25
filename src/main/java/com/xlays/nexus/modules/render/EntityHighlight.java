package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;import net.minecraft.client.Minecraft;import net.minecraft.gizmos.GizmoStyle;import net.minecraft.gizmos.Gizmos;import net.minecraft.world.entity.LivingEntity;
public final class EntityHighlight extends Module {
 private boolean players=true,mobs=true;private float width=1.5F;
 public EntityHighlight(){super("EntityHighlight",Category.RENDER,false);booleanSetting("players","Highlight players",()->players,v->players=v);booleanSetting("mobs","Highlight mobs",()->mobs,v->mobs=v);numberSetting("width","Outline width",()->width,v->width=v.floatValue(),.5,4,.5);}
 @Override public void onRender(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;for(var e:mc.level.entitiesForRendering())if(e instanceof LivingEntity l&&l!=mc.player&&l.isAlive()&&((l instanceof net.minecraft.world.entity.player.Player&&players)||(!(l instanceof net.minecraft.world.entity.player.Player)&&mobs)))Gizmos.cuboid(l.getBoundingBox(),GizmoStyle.stroke(0xFFFFFF55,width)).setAlwaysOnTop();}
}