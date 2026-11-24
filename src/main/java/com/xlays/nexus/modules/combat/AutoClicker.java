package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.EntityHitResult;
import org.lwjgl.glfw.GLFW;

public final class AutoClicker extends Module {
    private long lastClick;
    public AutoClicker(){super("AutoClicker",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null||mc.hitResult==null)return;
        boolean held=GLFW.glfwGetMouseButton(mc.getWindow().handle(),GLFW.GLFW_MOUSE_BUTTON_LEFT)==GLFW.GLFW_PRESS;
        if(!held||!(mc.hitResult instanceof EntityHitResult hit)||!(hit.getEntity() instanceof LivingEntity target)||!target.isAlive())return;
        long now=System.currentTimeMillis();
        if(now-lastClick<125||mc.player.getAttackStrengthScale(0)<1)return;
        mc.gameMode.attack(mc.player,target);mc.player.swing(InteractionHand.MAIN_HAND);lastClick=now;
    }
}