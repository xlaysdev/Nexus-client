package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
import net.minecraft.world.entity.LivingEntity;
public final class EntityHighlight extends Module {
 public EntityHighlight(){super("EntityHighlight",Category.RENDER,false);}
 @Override public void onRender(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;for(var e:mc.level.entitiesForRendering())if(e instanceof LivingEntity l&&l!=mc.player&&l.isAlive())Gizmos.cuboid(l.getBoundingBox(),GizmoStyle.stroke(0xFFFFFF55,1.5F)).setAlwaysOnTop();}
}
