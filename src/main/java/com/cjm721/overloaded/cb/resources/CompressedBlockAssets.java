package com.cjm721.overloaded.cb.resources;

import com.cjm721.overloaded.cb.CompressedBlocks;
import com.cjm721.overloaded.cb.config.ClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.cjm721.overloaded.cb.CompressedBlocks.LOGGER;
import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompressedBlockAssets {

  private static final List<CompressedResourceLocation> toCreateTextures = new ArrayList<>();

  public static void addToClientResourcesQueue(CompressedResourceLocation location) {
    toCreateTextures.add(location);

    generateBaseResouces(location);
  }

  public static void addToRecipes(ResourceLocation unCompressed, ResourceLocation compressed) {
    BlockResourcePack.INSTANCE.addResouce(getRecipesPath(unCompressed, "compress"), getCompressionRecipe
        (unCompressed, compressed));
    BlockResourcePack.INSTANCE.addResouce(getRecipesPath(compressed, "de_compress"), getDeCompressionRecipe
        (compressed, unCompressed));
  }

  public static void addToDropLootTable(ResourceLocation block) {
    BlockResourcePack.INSTANCE.addResouce(getBlockLootPath(block), getBlockLootDrop(block));
  }

  private static String getBlockState(@Nonnull ResourceLocation location) {
    return String.format("{\n" +
        "  \"variants\": {\n" +
        "    \"\": {\n" +
        "      \"model\": \"%s\"\n" +
        "    }\n" +
        "  }\n" +
        "}\n", getBlockModelPath(location));
  }

  private static String getBlockModel(@Nonnull ResourceLocation location) {
    return String.format("{\n" +
        "  \"parent\": \"block/cube_all\",\n" +
        "  \"textures\": {\n" +
        "    \"all\": \"%s\"\n" +
        "  }\n" +
        "}", getTexturePath(location));
  }

  private static String getItemModel(@Nonnull ResourceLocation location) {
    return String.format("{\n" +
        "  \"parent\": \"%s\"\n" +
        "}", getBlockModelPath(location));
  }

  private static String getCompressionRecipe(ResourceLocation input, ResourceLocation result) {
    return String.format("{\n" +
        "  \"type\": \"minecraft:crafting_shaped\",\n" +
        "  \"pattern\": [\n" +
        "    \"NNN\",\n" +
        "    \"NNN\",\n" +
        "    \"NNN\"\n" +
        "  ],\n" +
        "  \"key\": {\n" +
        "    \"N\": {\n" +
        "      \"item\": \"%s\"\n" +
        "    }\n" +
        "  },\n" +
        "  \"result\": {\n" +
        "    \"item\": \"%s\"\n" +
        "  }\n" +
        "}", input, result);
  }

  private static String getDeCompressionRecipe(ResourceLocation input, ResourceLocation result) {
    return String.format("{\n" +
        "  \"type\": \"minecraft:crafting_shapeless\",\n" +
        "  \"ingredients\": [\n" +
        "    {\n" +
        "      \"item\": \"%s\"\n" +
        "    }\n" +
        "  ],\n" +
        "  \"result\": {\n" +
        "    \"item\": \"%s\",\n" +
        "    \"count\": 9\n" +
        "  }\n" +
        "}", input, result);
  }

  private static String getBlockLootDrop(ResourceLocation block) {
    return String.format("{\n" +
        "  \"type\": \"minecraft:block\",\n" +
        "  \"pools\": [\n" +
        "    {\n" +
        "      \"name\": \"%s\",\n" +
        "      \"rolls\": 1,\n" +
        "      \"entries\": [\n" +
        "        {\n" +
        "          \"type\": \"minecraft:item\",\n" +
        "          \"functions\": [\n" +
        "            {\n" +
        "              \"function\": \"minecraft:copy_name\",\n" +
        "              \"source\": \"block_entity\"\n" +
        "            }\n" +
        "          ],\n" +
        "          \"name\": \"%s\"\n" +
        "        }\n" +
        "      ],\n" +
        "      \"conditions\": [\n" +
        "        {\n" +
        "          \"condition\": \"minecraft:survives_explosion\"\n" +
        "        }\n" +
        "      ]\n" +
        "    }\n" +
        "  ]\n" +
        "}", block.getPath(), block);
  }

  private static ResourceLocation getBlockModelPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "block/" + base.getPath());
  }

  private static ResourceLocation getItemModelPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "item/" + base.getPath());
  }

  private static ResourceLocation getTexturePath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "block/" + base.getPath());
  }

  private static ResourceLocation getBlockStatesPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "blockstates/" + base.getPath() + ".json");
  }

  private static ResourceLocation getRecipesPath(@Nonnull ResourceLocation base, String type) {
    return new ResourceLocation(MODID, "recipes/" + type + "_" + base.getPath() + ".json");
  }

  private static ResourceLocation getBlockLootPath(@Nonnull ResourceLocation block) {
    return new ResourceLocation(block.getNamespace(), "loot_tables/blocks/" + block.getPath() + ".json");
  }

  @SubscribeEvent
  public static void texturePre(TextureStitchEvent.Pre event) {
    if (!event.getMap().getBasePath().equals("textures")) {
      return;
    }

    for (CompressedResourceLocation locations : toCreateTextures) {
      ResourceLocation rl = getTexturePath(locations.compressed);
      BlockResourcePack.INSTANCE.addImage(modifyPath(rl, "textures/", ".png"), locations);
      event.addSprite(getTexturePath(locations.compressed));
    }

    if(!ClientConfig.INSTANCE.generateTexturesLazy.get()) {
      BlockResourcePack.INSTANCE.forceGenerateTextures();
    }
  }

  @SubscribeEvent
  public static void texturePost(TextureStitchEvent.Post event) {
  }

  private static ResourceLocation modifyPath(ResourceLocation location, String prefix, String suffix) {
    return new ResourceLocation(location.getNamespace(), prefix + location.getPath() + suffix);
  }

  private static void generateBaseResouces(@Nonnull CompressedResourceLocation locations) {
    BlockResourcePack.INSTANCE.addResouce(getBlockStatesPath(locations.compressed), getBlockState(locations.compressed));
    BlockResourcePack.INSTANCE.addResouce(modifyPath(getBlockModelPath(locations.compressed), "models/", ".json"),
        getBlockModel
            (locations
                .compressed));
    BlockResourcePack.INSTANCE.addResouce(modifyPath(getItemModelPath(locations.compressed), "models/", ".json"), getItemModel
        (locations
            .compressed));
  }

  public static class CompressedResourceLocation {
    final ResourceLocation baseBlock;
    final String baseTexture;
    final ResourceLocation compressed;
    final int compressionAmount;

    public CompressedResourceLocation(ResourceLocation baseBlock, String baseTexture, @Nonnull ResourceLocation
        compressed, int
                                          compressionAmount) {
      this.baseBlock = baseBlock;
      this.baseTexture = baseTexture;
      this.compressed = compressed;
      this.compressionAmount = compressionAmount;
    }
  }

}
