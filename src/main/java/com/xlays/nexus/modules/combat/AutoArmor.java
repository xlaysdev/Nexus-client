package com.xlays.nexus.modules.combat;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;

public final class AutoArmor extends Module {
    private long lastAction;
    public AutoArmor(){super("AutoArmor",Category.COMBAT,false);}
    @Override public void onTick(){
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null||mc.gameMode==null||mc.screen!=null||System.currentTimeMillis()-lastAction<180)return;
        var inv=mc.player.getInventory();
        for(int i=0;i<36;i++){
            ItemStack candidate=inv.getItem(i); Equippable eq=candidate.get(DataComponents.EQUIPPABLE);
            if(eq==null)continue;
            var slot=eq.slot();
            int menuSlot=switch(slot){case HEAD->5;case CHEST->6;case LEGS->7;case FEET->8;default->-1;};
            if(menuSlot<0||score(candidate)<=score(mc.player.getItemBySlot(slot)))continue;
            int source=i<9?36+i:i, menu=mc.player.inventoryMenu.containerId;
            mc.gameMode.handleInventoryMouseClick(menu,source,0,ClickType.PICKUP,mc.player);
            mc.gameMode.handleInventoryMouseClick(menu,menuSlot,0,ClickType.PICKUP,mc.player);
            mc.gameMode.handleInventoryMouseClick(menu,source,0,ClickType.PICKUP,mc.player);
            lastAction=System.currentTimeMillis();return;
        }
    }
    private static double score(ItemStack s){
        if(s.isEmpty()||s.get(DataComponents.EQUIPPABLE)==null)return -1;
        String id=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).getPath();
        double m=id.startsWith("netherite_")?6:id.startsWith("diamond_")?5:id.startsWith("iron_")?4:
            id.startsWith("chainmail_")?3:id.startsWith("golden_")?2.5:id.startsWith("turtle_")?3.5:id.startsWith("leather_")?1:0;
        double d=s.isDamageableItem()?(double)(s.getMaxDamage()-s.getDamageValue())/Math.max(1,s.getMaxDamage()):1;
        return m*100+d*20;
    }
}