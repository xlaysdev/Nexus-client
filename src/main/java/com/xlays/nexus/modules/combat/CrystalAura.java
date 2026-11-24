package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;

public final class CrystalAura extends Module {
    private long lastAction;
    public CrystalAura(){super("CrystalAura",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.level==null||mc.gameMode==null||mc.screen!=null||System.currentTimeMillis()-lastAction<250)return;
        Player target=mc.level.getEntitiesOfClass(Player.class,mc.player.getBoundingBox().inflate(4.5),
            p->p!=mc.player&&p.isAlive()&&!p.isSpectator()&&mc.player.hasLineOfSight(p))
            .stream().filter(p->mc.player.distanceTo(p)<=4.5F).min(Comparator.comparingDouble(mc.player::distanceTo)).orElse(null);
        if(target==null)return;
        EndCrystal crystal=mc.level.getEntitiesOfClass(EndCrystal.class,target.getBoundingBox().inflate(4.5),Entity::isAlive)
            .stream().min(Comparator.comparingDouble(target::distanceTo)).orElse(null);
        if(crystal!=null&&mc.player.getAttackStrengthScale(0)>=1){
            rotate(mc,crystal);mc.gameMode.attack(mc.player,crystal);mc.player.swing(InteractionHand.MAIN_HAND);lastAction=System.currentTimeMillis();return;
        }
        BlockPos support=target.blockPosition().below();
        var state=mc.level.getBlockState(support);
        if(!(state.is(Blocks.OBSIDIAN)||state.is(Blocks.BEDROCK))||
            !mc.level.getBlockState(support.above()).canBeReplaced()||!mc.level.getBlockState(support.above(2)).isAir())return;
        InteractionHand hand=mc.player.getMainHandItem().is(Items.END_CRYSTAL)?InteractionHand.MAIN_HAND:
            mc.player.getOffhandItem().is(Items.END_CRYSTAL)?InteractionHand.OFF_HAND:null;
        if(hand==null)return;
        rotate(mc,target);
        mc.gameMode.useItemOn(mc.player,hand,new BlockHitResult(Vec3.atCenterOf(support).add(0,.5,0),Direction.UP,support,false));
        lastAction=System.currentTimeMillis();
    }
    private static void rotate(Minecraft mc,Entity target){
        Vec3 eyes=mc.player.getEyePosition(), aim=target.getBoundingBox().getCenter();
        double dx=aim.x-eyes.x,dy=aim.y-eyes.y,dz=aim.z-eyes.z,h=Math.sqrt(dx*dx+dz*dz);
        float yaw=(float)(Mth.atan2(dz,dx)*Mth.RAD_TO_DEG)-90;
        float pitch=(float)-(Mth.atan2(dy,h)*Mth.RAD_TO_DEG);
        mc.player.setYRot(mc.player.getYRot()+Mth.clamp(Mth.wrapDegrees(yaw-mc.player.getYRot()),-18,18));
        mc.player.setXRot(Mth.clamp(mc.player.getXRot()+Mth.clamp(pitch-mc.player.getXRot(),-14,14),-90,90));
    }
}