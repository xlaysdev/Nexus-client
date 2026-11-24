package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.Items;

public final class OffhandManager extends Module {
    public OffhandManager(){super("OffhandManager",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null)return;
        boolean low=mc.player.getHealth()<=10.0F||(mc.player.getAbsorptionAmount()<=0&&mc.player.getHealth()<=14.0F);
        var wanted=low?Items.TOTEM_OF_UNDYING:Items.GOLDEN_APPLE;
        if(mc.player.getOffhandItem().is(wanted))return;
        int slot=CombatInventory.find(mc,wanted);
        if(slot>=0)CombatInventory.swapOffhand(mc,slot);
    }
}