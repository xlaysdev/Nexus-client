package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.Comparator;
public final class AimAssist extends Module {
 private double range=6.0; private float speed=5.0F; private boolean onlyWhenAttacking;
 public AimAssist(){super("AimAssist",Category.COMBAT,false);}
 public double range(){return range;} public void range(double v){range=Math.max(2,Math.min(10,v));}
 public float speed(){return speed;} public void speed(float v){speed=Math.max(.5F,Math.min(10,v));}
 @Override public void onTick(){
  Minecraft mc=Minecraft.getInstance();var p=mc.player;
  if(p==null||mc.level==null||mc.screen!=null||(onlyWhenAttacking&&!mc.options.keyAttack.isDown()))return;
  LivingEntity t=mc.level.getEntitiesOfClass(LivingEntity.class,p.getBoundingBox().inflate(range),e->e!=p&&e.isAlive()&&p.hasLineOfSight(e)).stream().min(Comparator.comparingDouble(e->score(p,e))).orElse(null);
  if(t==null)return;
  Vec3 d=t.getBoundingBox().getCenter().subtract(p.getEyePosition());double h=Math.sqrt(d.x*d.x+d.z*d.z);
  float yaw=(float)Math.toDegrees(-Math.atan2(d.x,d.z)),pitch=(float)Math.toDegrees(-Math.atan2(d.y,h));
  float yd=yaw-p.getYRot();while(yd>180)yd-=360;while(yd< -180)yd+=360;
  p.setYRot(p.getYRot()+Math.max(-speed*.8F,Math.min(speed*.8F,yd)));
  p.setXRot(p.getXRot()+Math.max(-speed*.48F,Math.min(speed*.48F,pitch-p.getXRot())));
 }
 private double score(net.minecraft.world.entity.player.Player p,LivingEntity e){return p.distanceTo(e)*(2-p.getViewVector(1).dot(e.position().subtract(p.getEyePosition()).normalize()));}
}
