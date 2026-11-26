package com.xlays.nexus.modules.player;

import com.xlays.nexus.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;

public final class Scaffold extends Module {
    private int delay = 2;
    private int ticks;
    private boolean placeUnder = true;
    private Field selectedField;

    public Scaffold() {
        super("Scaffold", Category.PLAYER, false);
        numberSetting("delay", "Place delay", () -> delay,
                v -> delay = Math.max(1, Math.min(10, v.intValue())), 1, 10, 1);
        booleanSetting("under", "Place below player", () -> placeUnder, v -> placeUnder = v);
    }

    @Override
    public void onTick() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || mc.gameMode == null
                || mc.screen != null || !mc.options.keyUse.isDown()) {
            ticks = 0;
            return;
        }

        if (++ticks < delay) return;
        ticks = 0;
        if (!placeUnder) return;

        BlockPos target = player.blockPosition().below();
        if (!mc.level.isEmptyBlock(target)) return;
        BlockPos support = target.below();
        if (mc.level.isEmptyBlock(support) || !mc.level.getBlockState(support).isSolid()) return;

        int oldSlot = getSelected(player);
        int blockSlot = -1;
        for (int slot = 0; slot < 9; slot++) {
            if (player.getInventory().getItem(slot).getItem() instanceof BlockItem) {
                blockSlot = slot;
                break;
            }
        }
        if (blockSlot < 0) return;

        try {
            setSelected(player, blockSlot);
            mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND,
                    new BlockHitResult(Vec3.atCenterOf(support), Direction.UP, support, false));
        } finally {
            setSelected(player, oldSlot);
        }
    }

    private int getSelected(LocalPlayer player) {
        try {
            return selectedField().getInt(player.getInventory());
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return player.getInventory().getSelectedSlot();
        }
    }

    private void setSelected(LocalPlayer player, int slot) {
        try {
            selectedField().setInt(player.getInventory(), slot);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            player.getInventory().setSelectedSlot(slot);
        }
    }

    private Field selectedField() throws NoSuchFieldException {
        if (selectedField == null) {
            for (String name : new String[]{"selected", "f_35977_"}) {
                try {
                    selectedField = playerInventoryField(name);
                    break;
                } catch (NoSuchFieldException ignored) {
                    // Try the next mapped field name.
                }
            }
            if (selectedField == null) throw new NoSuchFieldException("Selected hotbar slot field");
            selectedField.setAccessible(true);
        }
        return selectedField;
    }

    private static Field playerInventoryField(String name) throws NoSuchFieldException {
        return net.minecraft.world.entity.player.Inventory.class.getDeclaredField(name);
    }

    @Override
    protected void onDisable() {
        ticks = 0;
    }
}
