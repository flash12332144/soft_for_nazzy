package com.flash.tlauncher.map;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.world.storage.MapStorage;
import net.minecraft.world.storage.WorldSavedData;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class variables {

    public static NBTTagCompound getMapVariables(String key){
        try {
            Class<?> clazz = Class.forName("nazzy.ob.ObVariables$MapVariables");
            World world = Minecraft.getMinecraft().world;
            MapStorage ms = world.getMapStorage();

            WorldSavedData idk = ms.getOrLoadData((Class<? extends WorldSavedData>) clazz, key);

            if (idk != null) {
                return idk.serializeNBT();
            }
            throw new NullPointerException("world data for this key is null");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> getClassVariables(){
        try {
            Class<?> clazz = Class.forName("nazzy.ob.ObVariables$MapVariables");
            Method getMethod = clazz.getDeclaredMethod("get", World.class);
            Object instance = getMethod.invoke(null, Minecraft.getMinecraft().world);
            Field[] fields = clazz.getDeclaredFields();
            Map<String, Object> arr = new HashMap<>();
            for(Field field : fields){
                field.setAccessible(true);
                Object data = field.get(instance);
                arr.put(field.getName(), data);
            }
            return arr;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
