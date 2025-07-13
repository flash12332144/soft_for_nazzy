package com.flash.tlauncher.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;

public class ClientHandler {
    @SubscribeEvent
    public void onChat(ClientChatEvent event){
        String msg = event.getMessage();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        World world = player.world;

        if(msg.toLowerCase().startsWith("/list ")){
            try {
                event.setCanceled(true);
                String[] arr = msg.split(" ");
                Map<String, Object> map = variables.getClassVariables();
                String str = "";
                for(Map.Entry<String, Object> entry : map.entrySet()){
                    str += "Value of ["+entry.getKey()+"] is: "+ entry.getValue()+", and it's type is: "+ entry.getValue().getClass().toString()+"\n";
                }
                player.sendChatMessage(str);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
