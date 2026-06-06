package com.ultravisuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.*;
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
    
    private static UltraVisualsMod INSTANCE;
    private Minecraft mc;
    private boolean menuOpen = false;
    
    public static class Config {
        public boolean chineseHatEnabled = true;
        public float chineseHatRadius = 0.8f;
        public float chineseHatHeight = 1.2f;
        public float chineseHatYOffset = 0.5f;
        public int chineseHatColor = 0xFF00FF;
        public boolean chineseHatRainbow = true;
        public boolean espEnabled = true;
        public boolean espRainbow = true;
        public int espColor = 0x00FF00;
        public float espWidth = 2.0f;
        public boolean tracersEnabled = true;
        public int tracersColor = 0xFFFFFF;
        public boolean tracersRainbow = false;
        public boolean hitboxesEnabled = true;
        public boolean hitboxesFilled = false;
        public int hitboxesColor = 0xFFFF00;
        public boolean chamsEnabled = false;
        public int chamsColor = 0x88FF0000;
        public boolean viewModelEnabled = true;
        public float viewModelScale = 1.0f;
        public float viewModelX = 0f;
        public float viewModelY = 0f;
        public boolean killEffectEnabled = true;
        public boolean killParticlesEnabled = true;
        public int killEffectType = 0;
        public int hitEffectType = 0;
        public boolean cpsEnabled = true;
        public boolean keystrokesEnabled = true;
        public boolean armorHudEnabled = true;
        public boolean flyingStarsEnabled = true;
        public boolean fullbrightEnabled = false;
        public boolean chatFilterEnabled = true;
        public int menuKey = GLFW.GLFW_KEY_RIGHT_SHIFT;
        public String language = "uk";
    }
    
    public static Config CONFIG = new Config();
    
    private Set<String> blockedWords = new HashSet<>(Arrays.asList(
        "бля", "сука", "хуй", "пизда", "ебать", "нах", "блять",
        "fuck", "shit", "bitch", "asshole", "damn", "cunt", "cock"
    ));
    
    private List<KillEffect> killEffects = new ArrayList<>();
    private Queue<StarParticle> starParticles = new ConcurrentLinkedQueue<>();
    private Map<String, Integer> cpsCounter = new HashMap<>();
    private long lastCpsReset = System.currentTimeMillis();
    private int comboCounter = 0;
    private long lastHitTime = 0;
    private float killEffectTimer = 0f;
    
    private boolean keyW = false, keyA = false, keyS = false, keyD = false;
    private boolean keyLeftClick = false, keyRightClick = false;
    
    public UltraVisualsMod() {
        INSTANCE = this;
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    private void setup(final FMLClientSetupEvent event) {
        mc = Minecraft.getInstance();
        System.out.println("§6[UltraVisuals] §aЗавантажено 100+ візуальних функцій!");
      }
