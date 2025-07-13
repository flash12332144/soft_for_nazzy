package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiClickMenu extends GuiScreen {
    public static final int[] PRESET_COLORS = new int[] {
            new Color(0, 255, 255).getRGB(), // циан
            new Color(255, 0, 255).getRGB(), // пурпурный
            new Color(255, 255, 0).getRGB(), // желтый
            new Color(0, 255, 0).getRGB(),   // зеленый
            new Color(255, 165, 0).getRGB()  // оранжевый
    };

    public static int tabColorIndex = 0;
    public static int moduleColorIndex = 1;
    public static int textColorIndex = 2;

    public static int tabColor = PRESET_COLORS[tabColorIndex];
    public static int moduleColor = PRESET_COLORS[moduleColorIndex];
    public static int textColor = PRESET_COLORS[textColorIndex];

    private static final List<Tab> tabs = new ArrayList<>();
    private static int selectedTab = 0;
    private static final int tabWidth = 70, tabHeight = 20;
    private static long openTime;

    static {
        Tab combat = new Tab("Combat");
        combat.modules.add(new Module("Aim", true));
        combat.modules.add(new Module("KillAura", true));

        Tab combat1 = new Tab("Combat2");
        combat1.modules.add(new Module("AimAssist", true));
        combat1.modules.add(new Module("KillAuraPlus", true));

        Tab render = new Tab("Render");
        render.modules.add(new Module("ESP", true));
        render.modules.add(new Module("Fullbright", true));
        render.modules.add(new Module("Electrik", false)); // разовый

        Tab settings = new Tab("Settings"); // вкладка настроек

        tabs.add(combat);
        tabs.add(combat1);
        tabs.add(render);
        tabs.add(settings);
    }

    @Override
    public void initGui() {
        openTime = System.currentTimeMillis();
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        int x = 20, y = 20;
        for (int i = 0; i < tabs.size(); i++) {
            if (mouseX >= x && mouseX <= x + tabWidth &&
                    mouseY >= y && mouseY <= y + tabHeight) {
                Tab tab = tabs.get(i);
                if ("Settings".equals(tab.name)) {
                    mc.displayGuiScreen(new GuiSettings());
                    return;
                } else {
                    selectedTab = i;
                    return;
                }
            }
            y += tabHeight + 2;
        }

        if (selectedTab < tabs.size()) {
            Tab current = tabs.get(selectedTab);
            if ("Settings".equals(current.name)) return;

            y = 20;
            x = 100;
            for (Module mod : current.modules) {
                if (mouseX >= x && mouseX <= x + 100 &&
                        mouseY >= y && mouseY <= y + 20) {
                    mod.onClick();
                    return;
                }
                y += 22;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        ScaledResolution sr = new ScaledResolution(mc);
        float alphaProgress = Math.min(1f, (System.currentTimeMillis() - openTime) / 300f);
        int backgroundColor = new Color(0, 0, 0, (int)(140 * alphaProgress)).getRGB();
        drawRect(10, 10, sr.getScaledWidth() - 10, sr.getScaledHeight() - 10, backgroundColor);

        int x = 20, y = 20;
        for (int i = 0; i < tabs.size(); i++) {
            Tab tab = tabs.get(i);
            boolean hovered = mouseX >= x && mouseX <= x + tabWidth &&
                    mouseY >= y && mouseY <= y + tabHeight;
            drawRect(x, y, x + tabWidth, y + tabHeight, tabColor);
            int tabTextColor = (hovered || i == selectedTab) ? 0xFFFFFF : textColor;
            drawCenteredString(fontRenderer, tab.name, x + tabWidth / 2, y + 6, tabTextColor);
            y += tabHeight + 2;
        }

        if (selectedTab < tabs.size()) {
            Tab current = tabs.get(selectedTab);
            if (!"Settings".equals(current.name)) {
                y = 20;
                x = 100;
                for (Module mod : current.modules) {
                    boolean hovered = mouseX >= x && mouseX <= x + 100 &&
                            mouseY >= y && mouseY <= y + 20;
                    int bgColor = hovered ? new Color(255, 255, 255, 60).getRGB()
                            : new Color(0, 0, 0, 120).getRGB();
                    drawRect(x, y, x + 100, y + 20, bgColor);
                    int textCol = mod.toggleable
                            ? (mod.enabled ? Color.GREEN.getRGB() : Color.RED.getRGB())
                            : Color.CYAN.getRGB();
                    drawString(fontRenderer, mod.name, x + 5, y + 6, textCol);
                    y += 22;
                }
            }
        }
    }

    public static class Tab {
        public final String name;
        public final List<Module> modules = new ArrayList<>();
        public Tab(String name) { this.name = name; }
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