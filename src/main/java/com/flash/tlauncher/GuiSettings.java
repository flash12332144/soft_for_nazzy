package com.flash.tlauncher;

import net.minecraft.client.gui.GuiScreen;
import java.io.IOException;

public class GuiSettings extends GuiScreen {

    private int langButtonX, langButtonY;
    private int backButtonX, backButtonY;
    private final int buttonWidth = 120;
    private final int buttonHeight = 15;

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        String title = GuiClickMenu.currentLanguage.equals("ru") ? "Настройки" : "Settings";
        drawCenteredString(fontRenderer, title, width / 2, 10, 0xFFFFFF);

        langButtonX = width / 2 - buttonWidth / 2;
        langButtonY = 40;

        backButtonX = langButtonX;
        backButtonY = langButtonY + buttonHeight + 10;

        // Кнопка смены языка
        drawRect(langButtonX, langButtonY, langButtonX + buttonWidth, langButtonY + buttonHeight, GuiClickMenu.buttonColor);
        drawCenteredString(fontRenderer, getLangButtonText(), langButtonX + buttonWidth / 2, langButtonY + 4, 0xFFFFFF);

        // Кнопка Назад
        drawRect(backButtonX, backButtonY, backButtonX + buttonWidth, backButtonY + buttonHeight, GuiClickMenu.buttonColor);
        drawCenteredString(fontRenderer, GuiClickMenu.currentLanguage.equals("ru") ? "Назад" : "Back", backButtonX + buttonWidth / 2, backButtonY + 4, 0xFFFFFF);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        // Смена языка
        if (isInside(mouseX, mouseY, langButtonX, langButtonY, buttonWidth, buttonHeight)) {
            GuiClickMenu.currentLanguage = GuiClickMenu.currentLanguage.equals("en") ? "ru" : "en";
        }

        // Назад
        if (isInside(mouseX, mouseY, backButtonX, backButtonY, buttonWidth, buttonHeight)) {
            mc.displayGuiScreen(new GuiClickMenu());
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 1) { // ESC
            mc.displayGuiScreen(new GuiClickMenu());
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private boolean isInside(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private String getLangButtonText() {
        return (GuiClickMenu.currentLanguage.equals("ru") ? "Язык: " : "Language: ") +
                GuiClickMenu.currentLanguage.toUpperCase();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
