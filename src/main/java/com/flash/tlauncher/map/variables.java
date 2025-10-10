package com.flash.tlauncher.map;

import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class variables {

    private static final Map<String, String> modsList = new HashMap<>();

    static {
        modsList.put("blizzard", "nazzy.ob.ObVariables$MapVariables");
        modsList.put("lab", "nazzy.lab.LabVariables$MapVariables");
        // добавь свои, если нужно
    }

    /** Проверка наличия мода */
    public static boolean hasMod(String modName) {
        return modsList.containsKey(modName.toLowerCase());
    }

    /** Список зарегистрированных модов */
    public static Set<String> getAvailableMods() {
        return modsList.keySet();
    }

    /** Получение всех переменных у мода */
    public static Map<String, Object> getClassVariables(String modName) {
        Map<String, Object> arr = new LinkedHashMap<>();

        try {
            String className = modsList.get(modName.toLowerCase());
            if (className == null) return arr;

            Class<?> clazz = Class.forName(className);
            Method getMethod = clazz.getDeclaredMethod("get", World.class);
            Object instance = getMethod.invoke(null, Minecraft.getMinecraft().world);

            for (Field field : clazz.getDeclaredFields()) {
                field.setAccessible(true);
                Object value = field.get(instance);
                arr.put(field.getName(), value);
            }

        } catch (Exception e) {
            System.err.println("[variables] Ошибка при получении переменных " + modName + ": " + e.getMessage());
            e.printStackTrace();
        }

        return arr;
    }

    public static Object getMapVariableValue(String modName, String key) {
        try {
            // Проверяем, что мод зарегистрирован
            String className = modsList.get(modName.toLowerCase());
            if (className == null) {
                System.err.println("[variables] Мод " + modName + " не найден в списке modsList!");
                return null;
            }

            // Получаем класс и экземпляр MapVariables
            Class<?> clazz = Class.forName(className);
            Method getMethod = clazz.getDeclaredMethod("get", World.class);
            Object instance = getMethod.invoke(null, Minecraft.getMinecraft().world);

            // Пытаемся найти поле с таким именем
            Field field = clazz.getDeclaredField(key);
            field.setAccessible(true);

            Object value = field.get(instance);
            return value;

        } catch (NoSuchFieldException e) {
            System.err.println("[variables] Переменная '" + key + "' не найдена в " + modName + "!");
        } catch (Exception e) {
            System.err.println("[variables] Ошибка при получении " + key + " из " + modName + ": " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

}
