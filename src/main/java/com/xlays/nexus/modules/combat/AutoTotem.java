package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public final class AutoTotem extends Module {
    public AutoTotem(){super("AutoTotem",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null||mc.player.getOffhandItem().is(Items.TOTEM_OF_UNDYING))return;
        int slot=CombatInventory.find(mc,Items.TOTEM_OF_UNDYING);
        if(slot>=0)CombatInventory.swapOffhand(mc,slot);
    }
}