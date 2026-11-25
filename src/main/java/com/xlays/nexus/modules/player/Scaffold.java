package com.xlays.nexus.modules.player;
import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
public final class Scaffold extends Module {
 private int delay=2,ticks; private boolean placeUnder=true;
 public Scaffold(){super("Scaffold",Category.PLAYER,false);}
 @Override public void onTick(){
  var mc=Minecraft.getInstance();var p=mc.player;
  if(p==null||mc.level==null||mc.gameMode==null||mc.screen!=null||!mc.options.keyUse.isDown())return;
  if(++ticks<delay)return;ticks=0;
  BlockPos target=p.blockPosition().below();if(!placeUnder||!mc.level.isEmptyBlock(target))return;
  BlockPos support=target.below();if(mc.level.isEmptyBlock(support)||!mc.level.getBlockState(support).isSolid())return;
  int old=p.getInventory().getSelectedSlot(),slot=-1;
  for(int i=0;i<9;i++)if(p.getInventory().getItem(i).getItem() instanceof BlockItem){slot=i;break;}
  if(slot<0)return;
  p.getInventory().setSelectedSlot(slot);
  mc.gameMode.useItemOn(p,InteractionHand.MAIN_HAND,new BlockHitResult(Vec3.atCenterOf(support),Direction.UP,support,false));
  p.getInventory().setSelectedSlot(old);
 }
}
