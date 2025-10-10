package com.flash.tlauncher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.*;
import java.util.*;
import com.google.gson.*;

import static com.flash.tlauncher.map.variables.getMapVariableValue;

public class GuiImpactClick extends GuiScreen {

    private static final File POS_FILE = new File(Minecraft.getMinecraft().mcDataDir, "config/tlauncher.json");

    private static final int CAT_WIDTH = 110;
    private static final int CAT_HEADER = 14;
    private static final int MOD_HEIGHT = 12;
    private static final int BG_COLOR = 0x90000000;

    private final List<Category> categories = new ArrayList<>();
    private Category dragged = null;
    private int dragX, dragY;
    private long lastFrame = 0L;

    // HUD state: enabled names (preserve insertion order), and anim map for smooth show/hide
    private static boolean showHUD = false;
    private static final LinkedHashSet<String> enabledModuleNames = new LinkedHashSet<>();
    // animMap: name -> anim value 0..1 (0 hidden, 1 visible)
    private static final Map<String, Float> animMap = new HashMap<>();

    // Register HUD renderer once
    static {
        MinecraftForge.EVENT_BUS.register(new HudRenderer());
    }

    public GuiImpactClick() {
        // Categories and modules
        Category laboratory = new Category("Laboratory");

        Category ob = new Category("Operation Blizard");
        ob.add(new Module("ESP", false, "toggle")); // example toggle

        Category def = new Category("Default");
        def.add(new Module("GUI", showHUD, "toggle"));

        categories.addAll(Arrays.asList(laboratory, ob, def));
        categories.forEach(c -> c.collapsed = true);

        int x = 10;
        for (Category c : categories) {
            c.x = x;
            c.y = 8;
            x += CAT_WIDTH + 5;
        }
    }

    @Override
    public void initGui() { loadPositions(); super.initGui(); }

    @Override
    public void onGuiClosed() { savePositions(); super.onGuiClosed(); }

    private void savePositions() {
        JsonObject root = new JsonObject();
        for (Category cat : categories) {
            JsonObject jo = new JsonObject();
            jo.addProperty("x", cat.x);
            jo.addProperty("y", cat.y);
            jo.addProperty("collapsed", cat.collapsed);

            JsonObject mods = new JsonObject();
            for (Module m : cat.modules) mods.addProperty(m.name, m.enabled);
            jo.add("modules", mods);

            root.add(cat.name, jo);
        }
        root.addProperty("showHUD", showHUD);

        // Also save enabled module names explicitly for safety
        JsonArray enabledArr = new JsonArray();
        synchronized (enabledModuleNames) {
            for (String name : enabledModuleNames) enabledArr.add(new JsonPrimitive(name));
        }
        root.add("enabledModules", enabledArr);

        try (Writer w = new OutputStreamWriter(new FileOutputStream(POS_FILE), "UTF-8")) {
            new Gson().toJson(root, w);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void loadPositions() {
        if (!POS_FILE.exists()) return;
        try (Reader r = new InputStreamReader(new FileInputStream(POS_FILE), "UTF-8")) {
            JsonObject root = new Gson().fromJson(r, JsonObject.class);

            if (root != null && root.has("showHUD")) showHUD = root.get("showHUD").getAsBoolean();

            // load per-category module states (legacy)
            for (Category cat : categories) {
                JsonObject jo = (root == null) ? null : root.getAsJsonObject(cat.name);
                if (jo == null) continue;

                if (jo.has("x")) cat.x = jo.get("x").getAsInt();
                if (jo.has("y")) cat.y = jo.get("y").getAsInt();
                if (jo.has("collapsed")) cat.collapsed = jo.get("collapsed").getAsBoolean();
                cat.animFactor = cat.collapsed ? 0f : 1f;

                JsonObject mods = jo.getAsJsonObject("modules");
                if (mods != null) {
                    for (Module m : cat.modules) {
                        JsonElement el = mods.get(m.name);
                        if (el != null) {
                            m.enabled = el.getAsBoolean();
                            if (m.enabled) {
                                synchronized (enabledModuleNames) {
                                    enabledModuleNames.add(m.name);
                                }
                            }
                        }
                    }
                }
            }

            // load unified enabledModules if present (preferred)
            if (root != null && root.has("enabledModules")) {
                JsonArray arr = root.getAsJsonArray("enabledModules");
                synchronized (enabledModuleNames) {
                    enabledModuleNames.clear();
                    for (JsonElement je : arr) {
                        if (je.isJsonPrimitive()) enabledModuleNames.add(je.getAsString());
                    }
                }
            }

            // initialize animMap entries for currently enabled modules
            synchronized (enabledModuleNames) {
                for (String n : enabledModuleNames) animMap.putIfAbsent(n, 1f); // visible at start
            }

        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void drawScreen(int mx, int my, float pt) {
        if (dragged != null) { dragged.x = mx - dragX; dragged.y = my - dragY; }

        drawDefaultBackground();
        long now = System.currentTimeMillis();
        long dt = lastFrame == 0 ? 0 : now - lastFrame;
        lastFrame = now;

        float globalHue = (now % 3000L) / 3000.0f;
        int rgb = java.awt.Color.HSBtoRGB(globalHue, 0.4f, 0.9f);

        for (Category cat : categories) {
            cat.updateAnimation(dt);
            int drawnHeight = CAT_HEADER + (int)(cat.animFactor * cat.modules.size() * MOD_HEIGHT);
            drawBorderedRect(cat.x - 1, cat.y - 1,
                    cat.x + CAT_WIDTH + 1, cat.y + drawnHeight + 1, rgb, BG_COLOR);
            drawCenteredString(fontRenderer, cat.name, cat.x + CAT_WIDTH / 2, cat.y + 2, 0xFFFFFF);

            if (cat.animFactor > 0.01f) {
                int totalModulePx = (int)(cat.animFactor * cat.modules.size() * MOD_HEIGHT);
                int myOff = cat.y + CAT_HEADER;
                for (int i = 0; i < cat.modules.size(); i++) {
                    int moduleTop = myOff + i * MOD_HEIGHT;
                    int moduleBottom = moduleTop + MOD_HEIGHT;
                    if (moduleBottom - cat.y - CAT_HEADER <= totalModulePx) {
                        Module m = cat.modules.get(i);
                        int color;
                        if ("toggle".equals(m.type)) {
                            color = m.enabled ? 0x00FF00 : 0xFF4444;
                        } else {
                            color = 0xCCCCCC;
                        }
                        drawString(fontRenderer, m.name, cat.x + 2, moduleTop + 2, color);
                    }
                }
            }
        }

        super.drawScreen(mx, my, pt);
    }

    @Override
    protected void mouseClicked(int mx, int my, int button) {
        for (Category cat : categories) {
            int h = CAT_HEADER + (int)(cat.animFactor * cat.modules.size() * MOD_HEIGHT);
            if (!inBounds(mx, my, cat.x, cat.y, CAT_WIDTH, h)) continue;

            if (my < cat.y + CAT_HEADER) {
                if (button == 1) cat.collapsed = !cat.collapsed;
                else { dragged = cat; dragX = mx - cat.x; dragY = my - cat.y; }
                return;
            }

            if (cat.collapsed) return;
            int idx = (my - cat.y - CAT_HEADER) / MOD_HEIGHT;
            if (idx >= 0 && idx < cat.modules.size()) {
                Module m = cat.modules.get(idx);
                if (button == 0) m.toggle();
            }
        }
    }

    @Override
    protected void mouseReleased(int mx, int my, int state) { dragged = null; }

    private void drawBorderedRect(int x, int y, int x2, int y2, int border, int inside) {
        drawRect(x, y, x2, y2, border);
        drawRect(x + 1, y + 1, x2 - 1, y2 - 1, inside);
    }

    private boolean inBounds(int mx, int my, int x, int y, int w, int h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    @Override public boolean doesGuiPauseGame() { return false; }

    /* ----------------------------- CLASSES ----------------------------- */
    public static class Category {
        final String name;
        final List<Module> modules = new ArrayList<>();
        int x, y;
        boolean collapsed = true;
        float animFactor = 0f;

        Category(String n) { name = n; }
        void add(Module m) { modules.add(m); }

        void updateAnimation(long dt) {
            float target = collapsed ? 0f : 1f;
            // make delta frame-time aware: dt in ms
            float delta = 0.0012f * dt; // tuned
            if (Math.abs(animFactor - target) < delta) animFactor = target;
            else animFactor += (target > animFactor ? delta : -delta);
        }
    }

    public static class Module {
        final String name;
        boolean enabled;
        final String type;

        Module(String n, boolean d) { this(n, d, "toggle"); }
        Module(String n, boolean d, String t) { name = n; enabled = d; type = t; }

        void toggle() {
            if ("toggle".equals(type)) {
                enabled = !enabled;
                synchronized (enabledModuleNames) {
                    if (enabled) {
                        // add to enabled set and ensure anim starts (if missing)
                        if (!enabledModuleNames.contains(name)) {
                            enabledModuleNames.add(name);
                            animMap.putIfAbsent(name, 0f); // start hidden -> animate in
                        }
                    } else {
                        // disable: keep animMap entry, HUD renderer will animate out and remove
                        enabledModuleNames.remove(name);
                    }
                }

                if ("GUI".equalsIgnoreCase(name)) showHUD = enabled;
            } else if ("default".equals(type)) run();
        }

        void run() {
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.player == null) return;
            switch (name) {
//                case "Time":
//                    mc.player.sendChatMessage("time = " + getMapVariableValue("lab", "time"));
//                    break;
                default: break;
            }
        }
    }

    /* ----------------------------- HUD RENDERER (smooth animations) ----------------------------- */
    public static class HudRenderer {
        private final Minecraft mc = Minecraft.getMinecraft();
        private long prevTime = System.currentTimeMillis();
        private float globalHudAnim = 0f;

        @SubscribeEvent
        public void onRenderHUD(RenderGameOverlayEvent.Text event) {
            if (mc.fontRenderer == null || mc.ingameGUI == null) return;

            long now = System.currentTimeMillis();
            float dt = Math.max(1f, now - prevTime); // ms
            prevTime = now;

            // global HUD appear/disappear smoother (time-based)
            float gTarget = showHUD ? 1f : 0f;
            float gSpeed = 0.01f * dt; // larger dt -> faster convergence
            if (Math.abs(globalHudAnim - gTarget) < 0.001f) globalHudAnim = gTarget;
            else globalHudAnim += (gTarget - globalHudAnim) * Math.min(1f, gSpeed);

            if (globalHudAnim <= 0.001f && !hasAnyAnimating()) return; // fully hidden and nothing animating

            ScaledResolution sr = new ScaledResolution(mc);
            int sw = sr.getScaledWidth();
            int y = 6;
            float perSpeed = 0.015f * dt; // per-module smoothing factor (frame-time aware)

            // Build ordered list: keep the order of enabledModuleNames, but also include currently animating names
            List<String> renderOrder = new ArrayList<>();
            synchronized (enabledModuleNames) {
                renderOrder.addAll(enabledModuleNames);
            }
            // also include animMap keys (these may be currently fading out)
            synchronized (animMap) {
                for (String k : animMap.keySet()) if (!renderOrder.contains(k)) renderOrder.add(k);
            }

            // iterate and animate each
            Iterator<String> it = renderOrder.iterator();
            while (it.hasNext()) {
                String name = it.next();
                boolean targetVisible;
                synchronized (enabledModuleNames) {
                    targetVisible = enabledModuleNames.contains(name);
                }
                float cur = animMap.getOrDefault(name, targetVisible ? 0f : 0f);
                float target = (globalHudAnim > 0.001f && targetVisible) ? 1f : 0f; // only visible when global HUD present and enabled
                // smooth approach
                float approach = (float)(1.0 - Math.pow(0.001, perSpeed)); // convert to easing factor >0
                // simpler time-based lerp:
                cur += (target - cur) * Math.min(1f, perSpeed);
                // clamp
                if (cur < 1e-4f) cur = 0f;
                if (cur > 1f) cur = 1f;
                animMap.put(name, cur);

                // if fully hidden and not targetVisible -> cleanup
                if (cur <= 0.0001f && !targetVisible) {
                    animMap.remove(name);
                    continue;
                }

                // combined alpha with globalHudAnim: use smoothstep for nicer curve
                float alpha = smoothStep(0f, 1f, cur * globalHudAnim);
                if (alpha <= 0.01f) continue;

                // easing for slide (use easeOutCubic)
                float ease = easeOutCubic(alpha);
                int fullW = mc.fontRenderer.getStringWidth(name);
                int finalX = sw - fullW - 6;     // final aligned-right X
                int startX = sw + 6;             // start outside screen
                int x = finalX + Math.round((startX - finalX) * (1f - ease));

                // color with alpha (white text)
                int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255f)));
                int color = (alphaByte << 24) | 0xFFFFFF;

                // draw with shadow for readability, no background
                mc.fontRenderer.drawStringWithShadow(name, x, y, color);

                y += 12;
            }
        }

        private boolean hasAnyAnimating() {
            synchronized (animMap) { return !animMap.isEmpty(); }
        }

        private static float smoothStep(float a, float b, float t) {
            // clamp t
            if (t <= 0f) return 0f;
            if (t >= 1f) return 1f;
            // Hermite interpolation
            t = (t - a) / (b - a);
            return t * t * (3f - 2f * t);
        }

        private static float easeOutCubic(float t) {
            t = Math.max(0f, Math.min(1f, t));
            return 1f - (float)Math.pow(1f - t, 3);
        }
    }

    // Helpers
    public static boolean isShowHUD() { return showHUD; }
    public static Set<String> getEnabledModuleNames() {
        synchronized (enabledModuleNames) { return new LinkedHashSet<>(enabledModuleNames); }
    }
}
