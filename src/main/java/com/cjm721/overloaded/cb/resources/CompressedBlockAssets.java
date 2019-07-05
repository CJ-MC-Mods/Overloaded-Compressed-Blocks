package com.cjm721.overloaded.cb.resources;

import com.cjm721.overloaded.cb.config.ClientConfig;
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
import java.util.List;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

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

  @SubscribeEvent
  public static void texturePre(TextureStitchEvent.Pre event) {
    if (!event.getMap().getBasePath().equals("textures")) {
      return;
    }

    for (CompressedResourceLocation locations : toCreateTextures) {
      if (!generateTexture(locations)) {
        continue;
      }
      event.addSprite(getTexturePath(locations.compressed));
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

  private static boolean generateTexture(@Nonnull CompressedResourceLocation locations) {
    BufferedImage image;
    try {
      image = ImageIO.read(ImageUtil.getTextureInputStream(new ResourceLocation(locations.baseTexture)));
    } catch (IOException e) {
      e.printStackTrace();
      return false;
    }

    int scale = locations.compressionAmount + 1;

    int squareSize = Math.min(image.getWidth(), image.getHeight());

    WritableRaster raster = image.getColorModel().createCompatibleWritableRaster(squareSize * scale, squareSize * scale);

    int[] pixels = image.getData().getPixels(0, 0, squareSize, squareSize, (int[]) null);

    for (int x = 0; x < scale; x++) {
      for (int y = 0; y < scale; y++) {
        raster.setPixels(x * squareSize, y * squareSize, squareSize, squareSize, pixels);
      }
    }

    BufferedImage compressedImage = new BufferedImage(image.getColorModel(), raster, false, null);

    if (compressedImage.getWidth() > ClientConfig.INSTANCE.maxTextureWidth.get()) {
      compressedImage = ImageUtil.scaleDownToWidth(compressedImage, ClientConfig.INSTANCE.maxTextureWidth.get());
    }

    ResourceLocation rl = getTexturePath(locations.compressed);
    BlockResourcePack.INSTANCE.addImage(modifyPath(rl, "textures/", ".png"), compressedImage);

    return true;
  }

  public static class CompressedResourceLocation {
    final String baseTexture;
    final ResourceLocation compressed;
    final int compressionAmount;

    public CompressedResourceLocation(String baseTexture, @Nonnull ResourceLocation compressed, int compressionAmount) {
      this.baseTexture = baseTexture;
      this.compressed = compressed;
      this.compressionAmount = compressionAmount;
    }
  }

}
