package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

public class KeyHandler {
    @SubscribeEvent
    public void onKey(InputEvent.KeyInputEvent event) {
        if (Keyboard.getEventKey() == Keyboard.KEY_RSHIFT && Keyboard.getEventKeyState()) {
            Minecraft.getMinecraft().displayGuiScreen(new GuiClickMenu());
        }
    }
}
