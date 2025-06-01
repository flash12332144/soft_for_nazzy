package com.flash.tlauncher;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

@Mod(modid = "tlauncher", name = "Mainer", version = "Alpha", clientSideOnly = true)
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
        else if (msg.toLowerCase().startsWith("/search ")) {
            event.setCanceled(true);
            BlockPos center = player.getPosition();
            String[] parts = msg.split(" ", 3);
            if(parts[1].equalsIgnoreCase("clear")){
                HighlightHandler.highlightedBlocks.clear();
            }
            if(parts.length > 2) {
                try {
                    Block block = Block.REGISTRY.getObject(new ResourceLocation(parts[1]));// Или любой другой блок
                    int searchRadius = Integer.parseInt(parts[2]);

                    List<BlockPos> found = findBlockAroundPlayer(player, block, searchRadius);

                    if (found != null) {
                        for (BlockPos pos : found) {
                            HighlightHandler.highlightedBlocks.add(pos);
                            //player.sendMessage(new TextComponentString("Блок "+ block +"был найден на: "+ pos));
                        }
                    } else {
                        player.sendMessage(new TextComponentString("Блок не найден в радиусе " + searchRadius));
                    }
                }catch(Exception e){
                    player.sendMessage(new TextComponentString("Ошибка: " + e));
                }
            }
            else{
                player.sendMessage(new TextComponentString("Вы должны ввести 2 аргумента"));
                return;
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
                        player.sendMessage(new TextComponentString("Error Exceptions"));
                    }
                }
                else {
                    player.sendMessage(new TextComponentString("Введённый ключ "+ parts[1] +" не является переменной, которую можно изменить"));
                    return;
                }
            }
            else{
                player.sendMessage(new TextComponentString("Вы должны ввести 2 аргумента"));
                return;
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.world == null || mc.player == null) return;
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        Minecraft mc = Minecraft.getMinecraft();

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.glLineWidth(2.0F);

        double camX = mc.getRenderManager().viewerPosX;
        double camY = mc.getRenderManager().viewerPosY;
        double camZ = mc.getRenderManager().viewerPosZ;

        for (BlockPos pos : HighlightHandler.highlightedBlocks) {
            double x = pos.getX() - camX;
            double y = pos.getY() - camY;
            double z = pos.getZ() - camZ;

            AxisAlignedBB box = new AxisAlignedBB(0, 0, 0, 1, 1, 1).offset(x, y, z);
            RenderGlobal.drawSelectionBoundingBox(box, 0.0F, 1.0F, 0.0F, 0.4F); // зелёный
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
    public static List<BlockPos> findBlockAroundPlayer(EntityPlayer player, Block targetBlock, int radius) {
        BlockPos playerPos = player.getPosition();
        World world = player.world;

        int px = playerPos.getX();
        int py = playerPos.getY();
        int pz = playerPos.getZ();

        List<BlockPos> positions = new ArrayList<>();
        for (int y = -radius; y <= radius; y++) {
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    BlockPos pos = new BlockPos(px + x, py + y, pz + z);
                    IBlockState state = world.getBlockState(pos);
                    if (state.getBlock() == targetBlock) {
                        positions.add(pos);
                    }
                }
            }
        }
        return positions;

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
    public static class HighlightHandler {
        public static final Set<BlockPos> highlightedBlocks = new HashSet<>();
    }
}