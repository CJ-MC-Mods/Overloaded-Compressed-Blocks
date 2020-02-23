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

  public final ForgeConfigSpec.BooleanValue showHardness;
  public final ForgeConfigSpec.IntValue maxTextureWidth;
  public final ForgeConfigSpec.IntValue threadsToKeepFree;

  ClientConfig(ForgeConfigSpec.Builder builder) {
    maxTextureWidth = builder.comment("The max width of a single tiled texture.").defineInRange("maxTextureWidth", 256,
        1,
        Integer.MAX_VALUE);

    showHardness = builder.comment("Should hardness be shown on the tooltip").define("showHardness", true);

    threadsToKeepFree = builder.comment("How many system threads to keep free when generating textures. Smaller values mean faster generation but can cause 100% CPU utilization. Will always use at least 1 thread").defineInRange("threadsToKeepFree", 2,0, Integer.MAX_VALUE);

  }
}
