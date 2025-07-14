package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.util.*;

public class GuiClickMenu extends GuiScreen {

    private static final List<Tab> tabs = new ArrayList<>();
    private static int selectedTab = 0;

    public static int buttonColor = 0xFF303030; // <-- Настраиваемый цвет
    public static String currentLanguage = "en"; // <-- Выбранный язык (en / ru)

    private static final int tabWidth = 60, tabHeight = 15;

    // Переводы
    private static final Map<String, Map<String, String>> translations = new HashMap<>();

    static {
        Map<String, String> en = new HashMap<>();
        en.put("Combat", "Combat");
        en.put("Combat2", "Combat 2");
        en.put("Render", "Render");
        en.put("Settings", "Settings");
        en.put("Aim", "Aim");
        en.put("KillAura", "KillAura");
        en.put("AimAssist", "Aim Assist");
        en.put("KillAuraPlus", "KillAura+");
        en.put("ESP", "ESP");
        en.put("Fullbright", "Fullbright");
        en.put("Electrik", "Electrik");

        Map<String, String> ru = new HashMap<>();
        ru.put("Combat", "Бой");
        ru.put("Combat2", "Бой 2");
        ru.put("Render", "Визуал");
        ru.put("Settings", "Настройки");
        ru.put("Aim", "Прицел");
        ru.put("KillAura", "Аура");
        ru.put("AimAssist", "Помощь прицела");
        ru.put("KillAuraPlus", "Улучш. аура");
        ru.put("ESP", "ESP");
        ru.put("Fullbright", "Яркость");
        ru.put("Electrik", "Электрик");

        translations.put("en", en);
        translations.put("ru", ru);

        // Создание вкладок
        Tab combat = new Tab("Combat");
        combat.modules.add(new Module("Aim", true));
        combat.modules.add(new Module("KillAura", true));

        Tab combat2 = new Tab("Combat2");
        combat2.modules.add(new Module("AimAssist", true));
        combat2.modules.add(new Module("KillAuraPlus", true));

        Tab render = new Tab("Render");
        render.modules.add(new Module("ESP", true));
        render.modules.add(new Module("Fullbright", true));
        render.modules.add(new Module("Electrik", false));

        Tab settings = new Tab("Settings");

        tabs.add(combat);
        tabs.add(combat2);
        tabs.add(render);
        tabs.add(settings);
    }

    public static String tr(String key) {
        return translations.getOrDefault(currentLanguage, translations.get("en")).getOrDefault(key, key);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        int x = 10, y = 10;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            drawRect(x, y, x + tabWidth, y + tabHeight, 0xFF202020);
            drawCenteredString(fontRenderer, tr(tab.name), x + tabWidth / 2, y + 4,
                    (i == selectedTab) ? 0xFFFFFF : 0xAAAAAA);
            y += tabHeight + 2;
        }

        if (selectedTab < tabs.size()) {
            Tab current = tabs.get(selectedTab);
            if (!"Settings".equals(current.name)) {
                y = 10;
                x = 80;
                for (Module mod : current.modules) {
                    drawRect(x, y, x + 100, y + 15, buttonColor);
                    int textColor = mod.toggleable
                            ? (mod.enabled ? 0x00FF00 : 0xFF5555)
                            : 0xFFFFFF;
                    drawString(fontRenderer, tr(mod.name), x + 5, y + 4, textColor);
                    y += 18;
                }
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int x = 10, y = 10;
        for (int i = 0; i < tabs.size(); i++) {
            if (mouseX >= x && mouseX <= x + tabWidth &&
                    mouseY >= y && mouseY <= y + tabHeight) {

                Tab tab = tabs.get(i);
                if ("Settings".equals(tab.name)) {
                    mc.displayGuiScreen(new GuiSettings());
                } else {
                    selectedTab = i;
                }
                return;
            }
            y += tabHeight + 2;
        }

        if (selectedTab < tabs.size()) {
            Tab current = tabs.get(selectedTab);
            if ("Settings".equals(current.name)) return;

            y = 10;
            x = 80;
            for (Module mod : current.modules) {
                if (mouseX >= x && mouseX <= x + 100 &&
                        mouseY >= y && mouseY <= y + 15) {
                    mod.onClick();
                    return;
                }
                y += 18;
            }
        }
    }

    public static class Tab {
        public final String name;
        public final List<Module> modules = new ArrayList<>();

        public Tab(String name) {
            this.name = name;
        }
    }

    public static class Module {
        public final String name;
        public boolean enabled = false;
        public final boolean toggleable;

        public Module(String name, boolean toggleable) {
            this.name = name;
            this.toggleable = toggleable;
        }

        public void onClick() {
            if (toggleable) toggle();
            else activate();
        }

        public void toggle() {
            enabled = !enabled;
        }

        public void activate() {
            Minecraft.getMinecraft().player.sendChatMessage("Activated " + name);
        }
    }
}
