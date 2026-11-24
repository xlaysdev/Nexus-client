package com.xlays.nexus.modules.render;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import java.lang.reflect.Method;
final class RenderUtil {
 private static Method lines;
 private RenderUtil(){}
 static RenderType lineType(){try{if(lines==null){lines=RenderTypes.class.getDeclaredMethod("lines");lines.setAccessible(true);}return (RenderType)lines.invoke(null);}catch(ReflectiveOperationException e){return null;}}
}