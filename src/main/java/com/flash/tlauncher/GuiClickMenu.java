package com.flash.tlauncher;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GuiClickMenu extends GuiScreen {
    private static final List<Tab> tabs = new ArrayList<>();
    private static int selectedTab = 0;

    public static int buttonColor = 0xFF303030;
    public static String currentLanguage = "en";

    private static final int tabWidth = 60;
    private static final int tabHeight = 15;
    private static final int titleBarHeight = 20;

    private final int windowWidth = 260;
    private final int windowHeight = 180;

    private int windowX;
    private int windowY;

    private boolean dragging = false;
    private int dragOffsetX;
    private int dragOffsetY;

    private static final File CONFIG_FILE = new File(Minecraft.getMinecraft().mcDataDir, "clickmenu_position.json");

    private static final Map<String, Map<String, String>> translations = new HashMap<>();
    static {
        Map<String, String> en = new HashMap<>();
        en.put("Lab", "Laboratory");
        en.put("light", "Light");
        en.put("Settings", "Settings");
        en.put("Language", "Language: ");

        Map<String, String> ru = new HashMap<>();
        ru.put("Lab", "Лаборатория");
        ru.put("light", "Свет");
        ru.put("Settings", "Настройки");
        ru.put("Language", "Язык: ");

        translations.put("en", en);
        translations.put("ru", ru);

        Tab lab = new Tab("Lab");
        lab.modules.add(new Module("light", true));
        lab.modules.add(new Module("Elevator A", false));

        Tab ob = new Tab("Ob");
        Tab settings = new Tab("Settings");

        tabs.add(lab);
        tabs.add(ob);
        tabs.add(settings);
    }

    private static String tr(String key) {
        return translations.getOrDefault(currentLanguage, translations.get("en")).getOrDefault(key, key);
    }

    @Override
    public void initGui() {
        super.initGui();
        if (!loadPosition()) {
            windowX = (width - windowWidth) / 2;
            windowY = (height - windowHeight) / 2;
        }
    }

    @Override
    public void onGuiClosed() {
        savePosition();
        super.onGuiClosed();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (dragging) {
            windowX = mouseX - dragOffsetX;
            windowY = mouseY - dragOffsetY;
        }

        drawRect(windowX, windowY, windowX + windowWidth, windowY + windowHeight, 0xCC101010);
        drawRect(windowX, windowY, windowX + windowWidth, windowY + 1, 0xFFFFFFFF);
        drawRect(windowX, windowY + windowHeight - 1, windowX + windowWidth, windowY + windowHeight, 0xFFFFFFFF);
        drawRect(windowX, windowY, windowX + 1, windowY + windowHeight, 0xFFFFFFFF);
        drawRect(windowX + windowWidth - 1, windowY, windowX + windowWidth, windowY + windowHeight, 0xFFFFFFFF);

        drawRect(windowX, windowY, windowX + windowWidth, windowY + titleBarHeight, 0xAA303030);
        drawCenteredString(fontRenderer, tr(tabs.get(selectedTab).name), windowX + windowWidth / 2, windowY + 6, 0xFFFFFF);

        int tx = windowX + 5;
        int ty = windowY + titleBarHeight + 5;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            int color = (i == selectedTab) ? 0xFF505050 : 0xFF303030;
            drawRect(tx, ty, tx + tabWidth, ty + tabHeight, color);
            drawCenteredString(fontRenderer, tr(tab.name), tx + tabWidth / 2, ty + 4, 0xFFFFFF);
            ty += tabHeight + 2;
        }

        if (selectedTab < tabs.size()) {
            Tab current = tabs.get(selectedTab);
            if ("Settings".equals(current.name)) {
                int margin = 20;
                int bx = windowX + margin;
                int bw = windowWidth - 2 * margin;
                int bh = 20;
                int by = windowY + titleBarHeight + 40;

                drawRect(bx, by, bx + bw, by + bh, buttonColor);
                drawCenteredString(fontRenderer, tr("Language") + currentLanguage.toUpperCase(), bx + bw / 2, by + 6, 0xFFFFFF);
            } else {
                ty = windowY + titleBarHeight + 5;
                int mx = windowX + 80;
                for (Module mod : current.modules) {
                    drawRect(mx, ty, mx + 120, ty + 20, buttonColor);
                    int textColor = mod.toggleable ? (mod.enabled ? 0x00FF00 : 0xFF5555) : 0xFFFFFF;
                    drawString(fontRenderer, tr(mod.name), mx + 5, ty + 6, textColor);
                    ty += 22;
                }
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseY >= windowY && mouseY <= windowY + titleBarHeight && mouseX >= windowX && mouseX <= windowX + windowWidth) {
            dragging = true;
            dragOffsetX = mouseX - windowX;
            dragOffsetY = mouseY - windowY;
            return;
        }

        int tx = windowX + 5;
        int ty = windowY + titleBarHeight + 5;
        for (int i = 0; i < tabs.size(); i++) {
            if (mouseX >= tx && mouseX <= tx + tabWidth && mouseY >= ty && mouseY <= ty + tabHeight) {
                selectedTab = i;
                return;
            }
            ty += tabHeight + 2;
        }

        Tab current = tabs.get(selectedTab);
        if ("Settings".equals(current.name)) {
            int margin = 20;
            int bx = windowX + margin;
            int bw = windowWidth - 2 * margin;
            int bh = 20;
            int by = windowY + titleBarHeight + 40;

            if (mouseX >= bx && mouseX <= bx + bw && mouseY >= by && mouseY <= by + bh) {
                currentLanguage = currentLanguage.equals("en") ? "ru" : "en";
                return;
            }
        }

        if (!"Settings".equals(current.name)) {
            ty = windowY + titleBarHeight + 5;
            int mx = windowX + 80;
            for (Module mod : current.modules) {
                if (mouseX >= mx && mouseX <= mx + 120 && mouseY >= ty && mouseY <= ty + 20) {
                    mod.onClick();
                    return;
                }
                ty += 22;
            }
        }

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        dragging = false;
        super.mouseReleased(mouseX, mouseY, state);
    }

    private void savePosition() {
        JsonObject obj = new JsonObject();
        obj.addProperty("x", windowX);
        obj.addProperty("y", windowY);
        try (Writer w = new OutputStreamWriter(new FileOutputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            new Gson().toJson(obj, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean loadPosition() {
        if (!CONFIG_FILE.exists()) return false;
        try (Reader r = new InputStreamReader(new FileInputStream(CONFIG_FILE), StandardCharsets.UTF_8)) {
            JsonObject obj = new Gson().fromJson(r, JsonObject.class);
            if (obj.has("x") && obj.has("y")) {
                windowX = obj.get("x").getAsInt();
                windowY = obj.get("y").getAsInt();
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
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

        private void toggle() {
            enabled = !enabled;
            if ("light".equals(name))
                Minecraft.getMinecraft().player.sendChatMessage(enabled ? "1" : "2");
        }

        private void activate() {
            Minecraft.getMinecraft().player.sendChatMessage("Activated " + name);
        }
    }
}