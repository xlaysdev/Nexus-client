package com.xlays.nexus;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class HudEditorScreen extends Screen {
    private boolean dragging;
    private int grabX;
    private int grabY;

    public HudEditorScreen() { super(Component.literal("NEXUS HUD Editor")); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x99040A10);
        g.drawCenteredString(font, "HUD EDITOR", width / 2, 18, 0xFF45E6C4);
        g.drawCenteredString(font, "Drag the preview to move it. Right-click the preview to reset.", width / 2, 32, 0xFFE7F1F4);
        int x = NexusClient.hudX;
        int y = NexusClient.hudY;
        int scale = NexusClient.hudScale;
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale / 100.0f, scale / 100.0f, 1.0f);
        if (NexusClient.hudBackground) g.fill(-5, -5, 150, 58, 0xCC101820);
        g.fill(-5, -5, 150, -3, NexusClient.accent());
        g.drawString(font, "NEXUS  V1", 0, 0, NexusClient.accent(), false);
        g.drawString(font, "FPS  144", 0, 13, 0xFFE7F1F4, false);
        g.drawString(font, "XYZ  120.5  64.0  -32.5", 0, 26, 0xFFE7F1F4, false);
        g.drawString(font, "FACING  NORTH", 0, 39, 0xFFE7F1F4, false);
        g.pose().popPose();
        g.drawCenteredString(font, "Scale: " + scale + "%   •   Use ClickGUI Settings to adjust", width / 2, height - 28, 0xFFB8C8D0);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int scale = NexusClient.hudScale;
        int boxW = 155 * scale / 100;
        int boxH = 64 * scale / 100;
        if (mouseX >= NexusClient.hudX - 6 && mouseX <= NexusClient.hudX + boxW
            && mouseY >= NexusClient.hudY - 6 && mouseY <= NexusClient.hudY + boxH) {
            if (button == 1) {
                NexusClient.hudX = 12;
                NexusClient.hudY = 12;
                NexusClient.saveConfig();
                return true;
            }
            if (button == 0) {
                dragging = true;
                grabX = (int) mouseX - NexusClient.hudX;
                grabY = (int) mouseY - NexusClient.hudY;
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (dragging && button == 0) {
            NexusClient.hudX = Math.max(0, Math.min(width - 80, (int) mouseX - grabX));
            NexusClient.hudY = Math.max(0, Math.min(height - 40, (int) mouseY - grabY));
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            NexusClient.saveConfig();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
