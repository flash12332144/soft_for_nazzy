package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

@Mod(modid = "tlauncher", name = "Mainer", version = "Pre-Alpha", clientSideOnly = true)
public class Tlauncher {
    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);
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

        else if (msg.toLowerCase().startsWith("/setnote ")) {
            event.setCanceled(true);
            String[] parts = msg.split(" ", 3);

            String type = parts[1].toLowerCase();
            String newPass = parts[2];

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);

                Field targetField;
                if (type.equals("охрана")) {
                    targetField = clazz.getDeclaredField("code_guard");
                } else if (type.equals("админ")) {
                    targetField = clazz.getDeclaredField("code_admin");
                } else {
                    player.sendMessage(new TextComponentString("Тип должен быть 'охрана' или 'админ'"));
                    return;
                }

                targetField.setAccessible(true);
                targetField.set(instance, newPass);

                Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                syncMethod.invoke(instance, world);

                player.sendMessage(new TextComponentString("Код '" + type + "' успешно изменён на: '" + newPass + "'"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось изменить код"));
            }
        }

        else if (msg.equalsIgnoreCase("/poweron")) {
            event.setCanceled(true);

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);

                Field powerField = clazz.getDeclaredField("power");
                powerField.setAccessible(true);
                powerField.setBoolean(instance, true); // Устанавливаем true

                Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                syncMethod.invoke(instance, world);

                player.sendMessage(new TextComponentString("Успешно (power = true)"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось включить систему"));
            }
        }

        else if (msg.equalsIgnoreCase("/poweroff")) {
            event.setCanceled(true);

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);

                Field powerField = clazz.getDeclaredField("power");
                powerField.setAccessible(true);
                powerField.setBoolean(instance, false);

                Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                syncMethod.invoke(instance, world);

                player.sendMessage(new TextComponentString("Успешно (power = true)"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось включить систему"));
            }
        }

        else if (msg.toLowerCase().startsWith("/setpass ")) {
            event.setCanceled(true);
            String[] parts = msg.split(" ", 3);

            String type = parts[1].toLowerCase();
            String newPass = parts[2];
            double value = Double.parseDouble(newPass);

            try {
                Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                Method getMethod = clazz.getDeclaredMethod("get", World.class);
                Object instance = getMethod.invoke(null, world);

                Field targetField;
                if (type.equals("охрана")) {
                    targetField = clazz.getDeclaredField("code");
                } else if (type.equals("админ")) {
                    targetField = clazz.getDeclaredField("code2");
                } else {
                    player.sendMessage(new TextComponentString("Тип должен быть 'охрана' или 'админ'"));
                    return;
                }

                targetField.setAccessible(true);
                targetField.set(instance, value);

                Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                syncMethod.invoke(instance, world);

                player.sendMessage(new TextComponentString("Произведение кода '" + type + "' успешно изменён на: '" + value + "'"));
            } catch (Exception e) {
                e.printStackTrace();
                player.sendMessage(new TextComponentString("Ошибка Не удалось изменить код"));
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
        else if (msg.toLowerCase().startsWith("/game ")) {
            event.setCanceled(true);
            String[] parts = msg.split(" ", 3);
            if(parts.length > 1) {
                String value = parts[1].toLowerCase();

                if(isBooleanString(value)) {
                    try {
                        Class<?> clazz = Class.forName("nazzy.lab.LabVariables$MapVariables");
                        Method getMethod = clazz.getDeclaredMethod("get", World.class);
                        Object instance = getMethod.invoke(null, world);
                        Field targetField = clazz.getDeclaredField("isNowPlaying");
                        Boolean status = Boolean.parseBoolean(value);

                        targetField.setAccessible(true);
                        targetField.set(instance, status);

                        Method syncMethod = clazz.getDeclaredMethod("syncData", World.class);
                        syncMethod.invoke(instance, world);

                        player.sendMessage(new TextComponentString("Значение isNowPlaying было установлено на: "+ value));
                    } catch (Exception e) {
                        e.printStackTrace();
                        player.sendMessage(new TextComponentString("Ошибка Не удалось установить статус игры"));
                    }
                }
                else {
                    player.sendMessage(new TextComponentString("1"));
                    return;
                }
            }
        }
    }
    public static boolean isBooleanString(String str) {
        return "true".equalsIgnoreCase(str) || "false".equalsIgnoreCase(str);
    }
}
