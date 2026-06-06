package com.ultravisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod("ultravisuals")
public class UltraVisualsMod {
    
    private Minecraft mc;
    private boolean menuOpen = false;
    private float killEffectTimer = 0f;
    private Queue<StarParticle> starParticles = new ConcurrentLinkedQueue<>();
    private Map<String, Integer> cpsCounter = new HashMap<>();
    private long lastCpsReset = System.currentTimeMillis();
    
    private boolean keyW=false, keyA=false, keyS=false, keyD=false;
    
    public static Config CONFIG = new Config();
    
    public static class Config {
        public boolean chineseHatEnabled = true;
        public boolean tracersEnabled = true;
        public boolean cpsEnabled = true;
        public boolean keystrokesEnabled = true;
        public boolean flyingStarsEnabled = true;
        public boolean fullbrightEnabled = false;
        public boolean chatFilterEnabled = true;
        public int menuKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
    }
    
    private Set<String> blockedWords = new HashSet<>(Arrays.asList("бля","сука","хуй","пизда","ебать","fuck","shit"));
    
    public UltraVisualsMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLClientSetupEvent event) {
        mc = Minecraft.getInstance();
        System.out.println("§6[UltraVisuals] §aЗавантажено!");
    }
    
    @SubscribeEvent
    public void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() != RenderGameOverlayEvent.ElementType.ALL) return;
        
        if (CONFIG.cpsEnabled) {
            int cps = getCPS();
            mc.font.draw(event.getMatrixStack(), "CPS: " + cps, 5, 5, 0xFFFFFF);
        }
        
        if (killEffectTimer > 0) {
            killEffectTimer -= 0.05f;
            String text = "§6§l✦ ELIMINATION + ✦";
            int x = mc.getWindow().getGuiScaledWidth() / 2 - mc.font.width(text) / 2;
            mc.font.draw(event.getMatrixStack(), text, x, mc.getWindow().getGuiScaledHeight() / 2 - 50, 0xFFFF00);
        }
        
        if (CONFIG.keystrokesEnabled) {
            int w = mc.getWindow().getGuiScaledWidth();
            int h = mc.getWindow().getGuiScaledHeight();
            drawKeyButton(event.getMatrixStack(), w-61, h-61, "W", keyW);
            drawKeyButton(event.getMatrixStack(), w-101, h-41, "A", keyA);
            drawKeyButton(event.getMatrixStack(), w-61, h-41, "S", keyS);
            drawKeyButton(event.getMatrixStack(), w-21, h-41, "D", keyD);
        }
        
        if (CONFIG.flyingStarsEnabled) renderFlyingStars(event.getMatrixStack());
        if (menuOpen) renderMenu(event.getMatrixStack());
    }
    
    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (mc.world == null || mc.player == null) return;
        for (PlayerEntity player : mc.world.players()) {
            if (player == mc.player) continue;
            if (CONFIG.chineseHatEnabled) renderChineseHat(player, event.getPartialTicks());
        }
    }
    
    private void renderChineseHat(PlayerEntity player, float partialTicks) {
        double x = player.getX() + (player.getX() - player.xOld) * partialTicks;
        double y = player.getY() + player.getBbHeight() + 0.5f;
        double z = player.getZ() + (player.getZ() - player.zOld) * partialTicks;
        
        GL11.glPushMatrix();
        GL11.glTranslated(x, y, z);
        GL11.glLineWidth(2f);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glColor4f(1, 0, 1, 1);
        
        GL11.glBegin(GL11.GL_LINES);
        for (int i = 0; i < 3; i++) {
            double angle = i * Math.PI * 2 / 3;
            double x1 = Math.cos(angle) * 0.8;
            double z1 = Math.sin(angle) * 0.8;
            GL11.glVertex3d(x1, 0, z1);
            GL11.glVertex3d(0, 1.2, 0);
        }
        GL11.glEnd();
        
        GL11.glBegin(GL11.GL_LINE_LOOP);
        for (int i = 0; i < 32; i++) {
            double angle = i * Math.PI * 2 / 32;
            GL11.glVertex3d(Math.cos(angle) * 0.8, 0, Math.sin(angle) * 0.8);
        }
        GL11.glEnd();
        
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glPopMatrix();
    }
    
    @SubscribeEvent
    public void onClientChat(net.minecraftforge.client.event.ClientChatEvent event) {
        if (!CONFIG.chatFilterEnabled) return;
        for (String word : blockedWords) {
            if (event.getMessage().toLowerCase().contains(word)) {
                event.setCanceled(true);
                if (mc.player != null)
                    mc.player.sendMessage(new StringTextComponent("§c[UltraVisuals] §7Повідомлення заблоковано!"), mc.player.getUUID());
                return;
            }
        }
    }
    
    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (CONFIG.fullbrightEnabled) mc.options.gamma = 100.0;
        else if (mc.options.gamma > 1.0) mc.options.gamma = 1.0;
    }
    
    @SubscribeEvent
    public void onKeyInput(net.minecraftforge.client.event.InputEvent.KeyInputEvent event) {
        int key = event.getKey();
        boolean pressed = event.getAction() == GLFW.GLFW_PRESS;
        if (key == GLFW.GLFW_KEY_W) keyW = pressed;
        if (key == GLFW.GLFW_KEY_A) keyA = pressed;
        if (key == GLFW.GLFW_KEY_S) keyS = pressed;
        if (key == GLFW.GLFW_KEY_D) keyD = pressed;
        if (key == CONFIG.menuKey && pressed) menuOpen = !menuOpen;
    }
    
    @SubscribeEvent
    public void onMouseInput(net.minecraftforge.client.event.InputEvent.MouseInputEvent event) {
        if (event.getButton() == GLFW.GLFW_MOUSE_BUTTON_LEFT && event.getAction() == GLFW.GLFW_PRESS) {
            cpsCounter.merge("left", 1, Integer::sum);
        }
    }
    
    private int getCPS() {
        long now = System.currentTimeMillis();
        if (now - lastCpsReset > 1000) {
            cpsCounter.clear();
            lastCpsReset = now;
        }
        return cpsCounter.getOrDefault("left", 0);
    }
    
    private void drawKeyButton(net.minecraft.client.gui.GuiGraphics matrixStack, int x, int y, String text, boolean pressed) {
        int color = pressed ? 0xFF555555 : 0xFF888888;
        matrixStack.fill(x, y, x+38, y+18, color);
        mc.font.draw(matrixStack, text, x+19-mc.font.width(text)/2, y+5, pressed ? 0xFFFFFF : 0xAAAAAA);
    }
    
    private void renderFlyingStars(net.minecraft.client.gui.GuiGraphics matrixStack) {
        if (starParticles.size() < 30 && new Random().nextFloat() < 0.1f && mc.player != null) {
            StarParticle s = new StarParticle();
            s.x = mc.player.getX() + (Math.random()-0.5)*40;
            s.y = mc.player.getY() + 25 + Math.random()*30;
            s.z = mc.player.getZ() + (Math.random()-0.5)*40;
            s.life = 200;
            starParticles.add(s);
        }
        starParticles.removeIf(p -> { p.life--; return p.life <= 0; });
    }
    
    private void renderMenu(net.minecraft.client.gui.GuiGraphics matrixStack) {
        int w = mc.getWindow().getGuiScaledWidth();
        int h = mc.getWindow().getGuiScaledHeight();
        matrixStack.fill(w/2-150, h/2-100, w/2+150, h/2+100, 0xCC000000);
        mc.font.draw(matrixStack, "§6§lULTRA VISUALS", w/2 - 60, h/2 - 80, 0xFFAA00);
        mc.font.draw(matrixStack, "§aШляпа §7" + (CONFIG.chineseHatEnabled ? "ON" : "OFF"), w/2-130, h/2-50, 0xFFFFFF);
        mc.font.draw(matrixStack, "§eTracers §7" + (CONFIG.tracersEnabled ? "ON" : "OFF"), w/2-130, h/2-30, 0xFFFFFF);
        mc.font.draw(matrixStack, "§bCPS §7" + (CONFIG.cpsEnabled ? "ON" : "OFF"), w/2-130, h/2-10, 0xFFFFFF);
        mc.font.draw(matrixStack, "§7RIGHT_SHIFT → закрити", w/2-80, h/2+70, 0x888888);
    }
    
    private static class StarParticle {
        double x, y, z;
        int life;
    }
  }
