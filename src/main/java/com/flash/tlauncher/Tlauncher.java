package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;

@Mod(modid = "tlauncher", name = "Mainer", version = "Pre-Alpha", clientSideOnly = true)
public class Tlauncher {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(KeyHandler.class); // Обработка событий клавиш
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        if (event.getSide().isClient()) {
            KeyHandler.register();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatEvent event) {
        String msg = event.getMessage();
        EntityPlayerSP player = Minecraft.getMinecraft().player;
        World world = player.world;

        if (msg.equalsIgnoreCase("/pass")) {
            event.setCanceled(true);

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);

                Field codeGuardField = clazz.getDeclaredField("code_guard");
                Field codeAdminField = clazz.getDeclaredField("code_admin");

                codeGuardField.setAccessible(true);
                codeAdminField.setAccessible(true);

                String codeGuard = (String) codeGuardField.get(instance);
                String codeAdmin = (String) codeAdminField.get(instance);

                player.sendMessage(new TextComponentString("Код охраны: " + codeGuard));
                player.sendMessage(new TextComponentString("Код админа: " + codeAdmin));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось прочитать коды"));
            }
        }
        else if (msg.equalsIgnoreCase("/info")) {
            event.setCanceled(true);

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);
                String[] ids = {"elevator_floor", "elevator_CanMove", "elevator_exit_CanMove", "elevator_exit_first", "isNowPlaying",
                        "time", "reactor", "power", "train", "lift", "csg_count", "sci_count", "monster_count", "players_ready", "csg_trigger", "msr_trigger", "sci_trigger"};
                for(String id: ids){
                    Field info = clazz.getDeclaredField(id);
                    info.setAccessible(true);
                    Object data = info.get(instance);
                    player.sendMessage(new TextComponentString("Значение поля " + id + " равняется: "+ data));
                }
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось прочитать поля"));
            }
        }
        else if (msg.toLowerCase().startsWith("/set ")) {
            event.setCanceled(true);
            String[] parts = msg.split(" ", 3);
            String[] vars = {"elevator_CanMove", "elevator_exit_CanMove", "elevator_exit_first", "isNowPlaying", "reactor",
                    "power", "train", "lift", "csg_trigger", "msr_trigger", "sci_trigger", "elevator_floor", "time", "csg_count",
                    "sci_count", "monster_count", "players_ready", "code", "code2", "code_guard", "code_admin"};
            String[] bool = {"elevator_CanMove", "elevator_exit_CanMove", "elevator_exit_first", "isNowPlaying", "reactor",
            "power", "train", "lift", "csg_trigger", "msr_trigger", "sci_trigger"};
            String[] doub = {"elevator_floor", "time", "csg_count", "sci_count", "monster_count", "players_ready", "code",
            "code2"};
            String[] str = {"code_guard", "code_admin"};
            if(parts.length > 1) {
                if(Arrays.asList(vars).contains(parts[1])){
                try {
                    Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                    Field targetField = clazz.getDeclaredField(parts[1]);
                    Method getMethod = clazz.getDeclaredMethod("get", World.class);
                    Object instance = getMethod.invoke(null, world);
                    if (Arrays.asList(bool).contains(parts[1])) {
                        if (isBooleanString(parts[2])) {

                            Boolean value = Boolean.parseBoolean(parts[2]);
                            targetField.setAccessible(true);
                            targetField.set(instance, value);

                            player.sendMessage(new TextComponentString("Значение "+ parts[1] +" было установлено на: " + value));
                        } else {
                            player.sendMessage(new TextComponentString("Введённое значение не является 'true' или 'false'"));
                            return;
                        }
                    }
                    else if (Arrays.asList(doub).contains(parts[1])){
                        if(isDouble(parts[2])){
                            Double value = Double.parseDouble(parts[2]);
                            targetField.setAccessible(true);
                            targetField.set(instance, value);

                            player.sendMessage(new TextComponentString("Значение "+ parts[1] +" было установлено на: " + value));
                        } else{
                            player.sendMessage(new TextComponentString("Введённое значение не является числом"));
                            return;
                        }
                    }
                    else if (Arrays.asList(str).contains(parts[1])){
                        String value = parts[2];
                        targetField.setAccessible(true);
                        targetField.set(instance, value);

                        player.sendMessage(new TextComponentString("Значение "+ parts[1] +" было установлено на: " + value));
                    }
                    Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                    syncMethod.invoke(instance, world);
                }catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage(new TextComponentString("Error Exception"));
                    }
                }
                else {
                    player.sendMessage(new TextComponentString("Введённый ключ "+ parts[1] +" не является переменной, которую можно изменить"));
                }
            }
            else{
                    player.sendMessage(new TextComponentString("Вы должны ввести 2 аргумента"));
            }
        }
    }
    public static boolean isBooleanString(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
    public static boolean isDouble(String str) {
        if (str == null) return false;
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
