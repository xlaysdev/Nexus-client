package com.xlays.nexus.modules.render;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.gizmos.GizmoStyle;
import net.minecraft.gizmos.Gizmos;
public final class PlayerESP extends Module {
 public PlayerESP(){super("PlayerESP",Category.RENDER,false);}
 @Override public void onRender(){var mc=Minecraft.getInstance();if(mc.level==null||mc.player==null)return;for(var p:mc.level.players())if(p!=mc.player&&p.isAlive())Gizmos.cuboid(p.getBoundingBox(),GizmoStyle.stroke(0xFFFF5555,2F)).setAlwaysOnTop();}
}
