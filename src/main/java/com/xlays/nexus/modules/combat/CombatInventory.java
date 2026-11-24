package com.xlays.nexus.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.Item;

final class CombatInventory {
    private CombatInventory() {}
    static int find(Minecraft mc, Item item) {
        for (int i=0;i<36;i++) if (mc.player.getInventory().getItem(i).is(item)) return i<9?36+i:i;
        return -1;
    }
    static void swapOffhand(Minecraft mc, int slot) {
        int menu=mc.player.inventoryMenu.containerId;
        mc.gameMode.handleInventoryMouseClick(menu,slot,0,ClickType.PICKUP,mc.player);
        mc.gameMode.handleInventoryMouseClick(menu,45,0,ClickType.PICKUP,mc.player);
        mc.gameMode.handleInventoryMouseClick(menu,slot,0,ClickType.PICKUP,mc.player);
    }
}