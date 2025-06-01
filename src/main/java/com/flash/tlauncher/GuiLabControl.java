package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiLabControl extends GuiScreen {

    private GuiTextField guardField;
    private GuiTextField adminField;
    private GuiButton setGuardBtn;
    private GuiButton setAdminBtn;
    private GuiButton showCodesBtn;
    private GuiButton powerOnBtn;

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.guardField = new GuiTextField(0, this.fontRenderer, centerX - 100, centerY - 40, 200, 20);
        this.adminField = new GuiTextField(1, this.fontRenderer, centerX - 100, centerY, 200, 20);

        this.buttonList.clear();
        this.setGuardBtn = new GuiButton(2, centerX - 100, centerY - 15, 95, 20, "Изм. охрана");
        this.setAdminBtn = new GuiButton(3, centerX + 5, centerY - 15, 95, 20, "Изм. админ");

        this.showCodesBtn = new GuiButton(4, centerX - 100, centerY + 30, 95, 20, "Показать коды");
        this.powerOnBtn = new GuiButton(5, centerX + 5, centerY + 30, 95, 20, "Вкл. систему");

        this.buttonList.add(setGuardBtn);
        this.buttonList.add(setAdminBtn);
        this.buttonList.add(showCodesBtn);
        this.buttonList.add(powerOnBtn);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        drawCenteredString(this.fontRenderer, "Управление лабораторией", this.width / 2, 20, 0xFFFFFF);
        guardField.drawTextBox();
        adminField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        String guardCode = guardField.getText();
        String adminCode = adminField.getText();

        switch (button.id) {
            case 2:
                sendChatMessage("/setnote охрана " + guardCode);
                break;
            case 3:
                sendChatMessage("/setnote админ " + adminCode);
                break;
            case 4:
                sendChatMessage("/pass");
                break;
            case 5:
                sendChatMessage("/poweron");
                break;
        }
    }

    @Override
    public void sendChatMessage(String message) {
        if (Minecraft.getMinecraft().player != null) {
            Minecraft.getMinecraft().player.sendChatMessage(message);
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        guardField.textboxKeyTyped(typedChar, keyCode);
        adminField.textboxKeyTyped(typedChar, keyCode);
        super.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        guardField.mouseClicked(mouseX, mouseY, mouseButton);
        adminField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
