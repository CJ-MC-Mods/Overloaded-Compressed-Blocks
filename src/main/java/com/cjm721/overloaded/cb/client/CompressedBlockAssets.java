package com.cjm721.overloaded.cb.client;

import com.cjm721.overloaded.cb.config.CompressedConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class CompressedBlockAssets {

  private static final List<CompressedResourceLocation> toCreateTextures = new ArrayList<>();

  public static void addToTextureQueue(CompressedResourceLocation location) {
    toCreateTextures.add(location);

    generateBaseResouces(location);
  }

  private static String getBlockState(@Nonnull ResourceLocation location) {
    return String.format("{\n" +
        "  \"variants\": {\n" +
        "    \"\": {\n" +
        "      \"model\": \"%s\"\n" +
        "    }\n" +
        "  }\n" +
        "}\n", getBlockModelPath(location));

    //    return String.format(
//        "{ " +
//            "\"forge_marker\": 1, " +
//            "\"defaults\": { " +
//            "\"model\": \"cube_all\", " +
//            "\"textures\": { " +
//            "\"all\": \"%1$s\" " +
//            "} " +
//            "}," +
//            "\"variants\": { " +
//            "\"normal\": [{ }], " +
//            "\"inventory\": [{ }] " +
//            "}" +
//            "}", getBlocksPath(location));
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

  private static ResourceLocation getBlockModelPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "block/" + base.getPath());
  }

  private static ResourceLocation getItemModelPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "item/" + base.getPath());
  }

  private static ResourceLocation getTexturePath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "block/" + base.getPath());
  }

  private static ResourceLocation getJsonPath(@Nonnull ResourceLocation base) {
    return new ResourceLocation(base.getNamespace(), "blockstates/" + base.getPath() + ".json");
  }

  @SubscribeEvent
  public static void texturePre(TextureStitchEvent.Pre event) {
    System.out.println("TextureStitchEvent.Pre HIT : " + event.getMap().getBasePath());
    if (!event.getMap().getBasePath().equals("textures")) {
      return;
    }

    for (CompressedResourceLocation locations : toCreateTextures) {
      if (!generateTexture(locations)) {
        continue;
      }
      ITextureObject texture = Minecraft.getInstance().getTextureManager().getTexture(new ResourceLocation("minecraft",
          "cobblestone"));
      event.addSprite(getTexturePath(locations.compressed));
    }
  }

  @SubscribeEvent
  public static void texturePost(TextureStitchEvent.Post event) {
    System.out.println("TextureStitchEvent.Post HIT");
  }

  private static ResourceLocation modifyPath(ResourceLocation location, String prefix, String suffix) {
    return new ResourceLocation(location.getNamespace(), prefix + location.getPath() + suffix);
  }

  private static void generateBaseResouces(@Nonnull CompressedResourceLocation locations) {
    BlockResourcePack.INSTANCE.addResouce(getJsonPath(locations.compressed), getBlockState(locations.compressed));
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

    if (compressedImage.getWidth() > CompressedConfig.INSTANCE.maxTextureWidth) {
      compressedImage = ImageUtil.scaleDownToWidth(compressedImage, CompressedConfig.INSTANCE.maxTextureWidth);
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
