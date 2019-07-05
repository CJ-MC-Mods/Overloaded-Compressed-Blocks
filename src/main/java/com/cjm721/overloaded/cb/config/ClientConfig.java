package com.cjm721.overloaded.cb.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.commons.lang3.tuple.Pair;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientConfig {

  public static ClientConfig INSTANCE;
  public static ForgeConfigSpec SPEC;

  public static void init() {
    Pair<ClientConfig, ForgeConfigSpec> pair = new ForgeConfigSpec.Builder().configure(ClientConfig::new);
    INSTANCE = pair.getLeft();
    SPEC = pair.getRight();

    ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, SPEC);
    ConfigTracker.INSTANCE.loadConfigs(ModConfig.Type.CLIENT, FMLPaths.CONFIGDIR.get());
  }

  public ForgeConfigSpec.BooleanValue showHardness;
  public ForgeConfigSpec.IntValue maxTextureWidth;

  ClientConfig(ForgeConfigSpec.Builder builder) {
    maxTextureWidth = builder.comment("The max width of a single tiled texture.").defineInRange("maxTextureWidth", 256,
        1,
        Integer.MAX_VALUE);
    showHardness = builder.comment("Should hardness be shown on the tooltip").define("showHardness", true);
  }
}
