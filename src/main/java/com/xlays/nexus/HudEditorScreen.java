package com.xlays.nexus;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class HudEditorScreen extends Screen {
    private boolean dragging;
    private int grabX;
    private int grabY;

    public HudEditorScreen() { super(Component.literal("NEXUS HUD Editor")); }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        g.fill(0, 0, width, height, 0x99040A10);
        g.drawCenteredString(font, "HUD EDITOR", width / 2, 18, NexusClient.accent());
        g.drawCenteredString(font, "Drag the preview to move it. Right-click the preview to reset.", width / 2, 32, 0xFFE7F1F4);
        int x = NexusClient.hudX;
        int y = NexusClient.hudY;
        float scale = NexusClient.hudScale / 100.0f;
        g.pose().pushMatrix();
        g.pose().translate((float) x, (float) y);
        g.pose().scale(scale, scale);
        if (NexusClient.hudBackground) g.fill(-5, -5, 190, 58, 0xCC101820);
        g.fill(-5, -5, 190, -3, NexusClient.accent());
        g.drawString(font, "NEXUS  V1", 0, 0, NexusClient.accent(), false);
        g.drawString(font, "FPS  144", 0, 13, 0xFFE7F1F4, false);
        g.drawString(font, "XYZ  120.5  64.0  -32.5", 0, 26, 0xFFE7F1F4, false);
        g.drawString(font, "FACING  NORTH", 0, 39, 0xFFE7F1F4, false);
        g.pose().popMatrix();
        g.drawCenteredString(font, "Scale: " + NexusClient.hudScale + "%", width / 2, height - 28, 0xFFB8C8D0);
        super.render(g, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int scale = NexusClient.hudScale;
        int boxW = 195 * scale / 100;
        int boxH = 64 * scale / 100;
        if (mouseX >= NexusClient.hudX - 6 && mouseX <= NexusClient.hudX + boxW
            && mouseY >= NexusClient.hudY - 6 && mouseY <= NexusClient.hudY + boxH) {
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
                NexusClient.hudX = 12;
                NexusClient.hudY = 12;
                NexusClient.saveConfig();
                return true;
            }
            if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
                dragging = true;
                grabX = (int) mouseX - NexusClient.hudX;
                grabY = (int) mouseY - NexusClient.hudY;
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent click, double offsetX, double offsetY) {
        if (dragging && click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            NexusClient.hudX = Math.max(0, Math.min(width - 80, (int) click.x() - grabX));
            NexusClient.hudY = Math.max(0, Math.min(height - 40, (int) click.y() - grabY));
            return true;
        }
        return super.mouseDragged(click, offsetX, offsetY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent click) {
        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT && dragging) {
            dragging = false;
            NexusClient.saveConfig();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
