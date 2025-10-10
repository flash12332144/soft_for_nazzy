package com.flash.tlauncher.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.util.text.TextComponentString;

import java.util.Map;

public class ClientHandler {
    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String msg = event.getMessage();
        EntityPlayerSP player = Minecraft.getMinecraft().player;

        if (msg.toLowerCase().startsWith("/list ")) {
            event.setCanceled(true);

            try {
                String[] arr = msg.split(" ", 2);
                if (arr.length < 2 || arr[1].trim().isEmpty()) {
                    player.sendMessage(new TextComponentString("§cИспользование: /list <modname>"));
                    return;
                }

                String modName = arr[1].trim().toLowerCase();
                if (!variables.hasMod(modName)) {
                    player.sendMessage(new TextComponentString("§cМод '" + modName + "' не найден в списке."));
                    return;
                }

                Map<String, Object> map = variables.getClassVariables(modName);
                if (map.isEmpty()) {
                    player.sendMessage(new TextComponentString("§eНет доступных переменных для мода '" + modName + "'."));
                    return;
                }

                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    String line = String.format(
                            "§a[%s]§r = %s §7(type: %s)",
                            entry.getKey(),
                            String.valueOf(entry.getValue()),
                            entry.getValue() != null ? entry.getValue().getClass().getSimpleName() : "null"
                    );
                    player.sendMessage(new TextComponentString(line));
                }

            } catch (Throwable t) {
                player.sendMessage(new TextComponentString("§cОшибка при обработке команды: " + t.getClass().getSimpleName()));
                System.err.println("[TLauncherMap] Command error: ");
                t.printStackTrace();
            }
        }
    }
}
