package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import org.lwjgl.input.Keyboard;

@Mod.EventBusSubscriber
public class KeyHandler {

    public static final net.minecraft.client.settings.KeyBinding OPEN_GUI_KEY = new net.minecraft.client.settings.KeyBinding(
            "Открыть панель управления лабораторией",
            KeyConflictContext.IN_GAME,
            Keyboard.KEY_MINUS, // Клавиша "-"
            "Lab Control"
    );

    public static void register() {
        ClientRegistry.registerKeyBinding(OPEN_GUI_KEY);
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Minecraft.getMinecraft().currentScreen == null) {
            if (OPEN_GUI_KEY.isPressed()) {
                Minecraft.getMinecraft().displayGuiScreen(new GuiLabControl());
            }
        }
    }
}
