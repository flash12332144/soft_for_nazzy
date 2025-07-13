package com.flash.tlauncher;

import net.minecraft.client.gui.GuiScreen;

import java.awt.*;

public class GuiSettings extends GuiScreen {
    private static final int OPTION_X = 20;
    private static final int OPTION_Y_START = 20;
    private static final int OPTION_SPACING = 30;
    private static final int OPTION_WIDTH = 200;
    private static final int OPTION_HEIGHT = 20;

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (inBox(mouseX, mouseY, OPTION_Y_START)) {
            GuiClickMenu.tabColorIndex = (GuiClickMenu.tabColorIndex + 1) % GuiClickMenu.PRESET_COLORS.length;
            GuiClickMenu.tabColor = GuiClickMenu.PRESET_COLORS[GuiClickMenu.tabColorIndex];
        } else if (inBox(mouseX, mouseY, OPTION_Y_START + OPTION_SPACING)) {
            GuiClickMenu.moduleColorIndex = (GuiClickMenu.moduleColorIndex + 1) % GuiClickMenu.PRESET_COLORS.length;
            GuiClickMenu.moduleColor = GuiClickMenu.PRESET_COLORS[GuiClickMenu.moduleColorIndex];
        } else if (inBox(mouseX, mouseY, OPTION_Y_START + 2 * OPTION_SPACING)) {
            GuiClickMenu.textColorIndex = (GuiClickMenu.textColorIndex + 1) % GuiClickMenu.PRESET_COLORS.length;
            GuiClickMenu.textColor = GuiClickMenu.PRESET_COLORS[GuiClickMenu.textColorIndex];
        }
    }

    private boolean inBox(int mouseX, int mouseY, int y) {
        return mouseX >= OPTION_X && mouseX <= OPTION_X + OPTION_WIDTH &&
                mouseY >= y && mouseY <= y + OPTION_HEIGHT;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int y = OPTION_Y_START;

        drawString(fontRenderer, "Tab Color", OPTION_X, y, Color.WHITE.getRGB());
        drawRect(OPTION_X + 100, y, OPTION_X + 120, y + OPTION_HEIGHT, GuiClickMenu.tabColor);
        y += OPTION_SPACING;

        drawString(fontRenderer, "Module Color", OPTION_X, y, Color.WHITE.getRGB());
        drawRect(OPTION_X + 100, y, OPTION_X + 120, y + OPTION_HEIGHT, GuiClickMenu.moduleColor);
        y += OPTION_SPACING;

        drawString(fontRenderer, "Text Color", OPTION_X, y, Color.WHITE.getRGB());
        drawRect(OPTION_X + 100, y, OPTION_X + 120, y + OPTION_HEIGHT, GuiClickMenu.textColor);
        y += OPTION_SPACING;

        drawString(fontRenderer, "Click to cycle through neon presets", OPTION_X, y, Color.GRAY.getRGB());
    }
}
