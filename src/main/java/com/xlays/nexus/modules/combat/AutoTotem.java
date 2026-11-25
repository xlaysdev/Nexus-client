package com.xlays.nexus.modules.combat;
import com.xlays.nexus.modules.Module;import net.minecraft.client.Minecraft;import net.minecraft.world.item.Items;
public final class AutoTotem extends Module {
 private double health=10;
 public AutoTotem(){super("AutoTotem",Category.COMBAT,false);numberSetting("health","Health threshold",()->health,v->health=v,1,20,1);}
 @Override public void onTick(){Minecraft mc=Minecraft.getInstance();if(mc.player==null||mc.gameMode==null||mc.screen!=null||mc.player.getHealth()>health||com.xlays.nexus.NexusClient.enabled("OffhandManager")||mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))return;int slot=CombatInventory.find(mc,Items.TOTEM_OF_UNDYING);if(slot>=0)CombatInventory.swapOffhand(mc,slot);}
}