package com.cjm721.overloaded.cb.resources;

import com.cjm721.overloaded.cb.CompressedBlocks;
import com.cjm721.overloaded.cb.config.ClientConfig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.data.LanguageMetadataSection;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import static com.cjm721.overloaded.cb.CompressedBlocks.LOGGER;
import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.stream.Collectors.*;

public class BlockResourcePack implements IResourcePack {

  public static final BlockResourcePack INSTANCE = new BlockResourcePack();
  private static final ResourceLocation MISSING_TEXTURE = new ResourceLocation(MODID, "textures/missing.png");

  private BlockResourcePack() {
    this.domains.add(MODID);
  }

  private final Map<ResourceLocation, CompressedBlockAssets.CompressedResourceLocation> images = Maps.newConcurrentMap();
  private final Map<ResourceLocation, String> resource = Maps.newConcurrentMap();

  private Map<ResourceLocation, TextureEntry> imagesCache = Maps.newConcurrentMap();
  private Map<ResourceLocation, TextureEntry> oldImageCache = Maps.newConcurrentMap();

  private final Set<String> domains = Sets.newHashSet();

  public void addImage(@Nonnull ResourceLocation res, @Nonnull CompressedBlockAssets.CompressedResourceLocation image) {
    images.put(res, image);
  }

  void forceGenerateTextures() {
    LOGGER.info("Force Texture Generation Started");
    Instant start = Instant.now();
    try {
      int poolSize = Math.min(Runtime.getRuntime().availableProcessors(), Math.max(1, Runtime.getRuntime().availableProcessors() - ClientConfig.INSTANCE.threadsToKeepFree.get()));
      imagesCache = new ForkJoinPool(poolSize).submit(() ->
          images.entrySet().parallelStream().map(entry -> generateTexture(entry.getKey(), entry.getValue()))
              .collect(toConcurrentMap(entry -> entry.location, entry -> entry))).get();
    } catch (Exception e) {
      LOGGER.error("Failed to generate textures", e);
      throw new RuntimeException(e);
    }
    LOGGER.info("Force Texture Generation Ended. Number of Textures: " + imagesCache.size() + " Time taken in seconds: " + ChronoUnit.SECONDS.between(start, Instant.now()));
  }

  public void addResouce(ResourceLocation res, String state) {
    resource.put(res, state);
  }

  public final void inject(IResourceManager manager) throws InvocationTargetException, IllegalAccessException {
    ObfuscationReflectionHelper.findMethod(manager.getClass(), "func_199021_a", IResourcePack.class).invoke(manager, this);
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager) manager).addReloadListener(new IFutureReloadListener() {
        @Override
        @Nonnull
        public CompletableFuture<Void> reload(IStage iStage, IResourceManager iResourceManager, IProfiler iProfiler, IProfiler iProfiler1, Executor executor, Executor executor1) {
          BlockResourcePack.INSTANCE.reload();
          try {
            ObfuscationReflectionHelper.findMethod(manager.getClass(), "func_199021_a", IResourcePack.class).invoke(manager, BlockResourcePack.this);
          } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
          }
          return iStage.markCompleteAwaitingOthers(null);
        }
      });
    }
  }

  @Override
  @Nonnull
  public InputStream getRootResourceStream(@Nonnull String fileName) throws IOException {
    return new ByteArrayInputStream(new byte[0]);
  }

  @Override
  @Nonnull
  public InputStream getResourceStream(@Nonnull ResourcePackType type, @Nonnull ResourceLocation location) throws IOException {
    if (location.getPath().endsWith(".png")) {
      return getImageInputStream(location);
    } else {
      String state = resource.get(location);

      if (state != null) {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(state.getBytes());
        return new ByteArrayInputStream(os.toByteArray());
      }
    }
    System.err.println("Not Found: " + location.toString());
    throw new FileNotFoundException(location.toString());
  }

  @Override
  @Nonnull
  public Collection<ResourceLocation> getAllResourceLocations(@Nonnull ResourcePackType type, @Nonnull String nameSpace, @Nonnull String path,
                                                              int maxDepth, @Nonnull Predicate<String> filter) {
    return resource.entrySet().stream().filter(e -> e.getKey().getPath().startsWith(path)).map(e -> new
        ResourceLocation
        (e.getKey().getNamespace(), e.getKey().getPath()))
        .collect(toList());
  }

  private InputStream getImageInputStream(@Nonnull ResourceLocation location) throws IOException {
    TextureEntry image;
    if (imagesCache.containsKey(location)) {
      image = imagesCache.get(location);

      try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
        ImageIO.write(image.image, "png", os);
        return new ByteArrayInputStream(os.toByteArray());
      }
    }

    LOGGER.warn("Compressed Entry Not Found: " + location.toString());
    throw new FileNotFoundException(location.toString());
  }

  @Override
  public boolean resourceExists(@Nonnull ResourcePackType type, @Nonnull ResourceLocation location) {
    return images.containsKey(location) || resource.containsKey(location);
  }

  @Override
  @Nonnull
  public Set<String> getResourceNamespaces(@Nonnull ResourcePackType type) {
    return domains;
  }

  @Nullable
  @Override
  public <T> T getMetadata(@Nonnull IMetadataSectionSerializer<T> deserializer) throws IOException {
    JsonObject packData = new JsonObject();
    if ("language".equals(deserializer.getSectionName())) {
      return deserializer.deserialize(packData);
    }
    packData.addProperty("pack_format", 6);
    packData.addProperty("description", "Overloaded Compressed Assets");
    return deserializer.deserialize(packData);
  }

  @Override
  @Nonnull
  public String getName() {
    return "Overloaded Compressed Textures";
  }

  @Override
  public void close() {
  }

  public void reload() {
    oldImageCache = imagesCache;
    imagesCache = Maps.newConcurrentMap();
  }

  private static ResourceLocation findTexture(@Nonnull CompressedBlockAssets.CompressedResourceLocation locations) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    String fileName = locations.baseBlock.getPath() + ".png";

    if (locations.baseTexture != null) {
      ResourceLocation forceTexture = new ResourceLocation(locations.baseTexture);
      if (manager.hasResource(forceTexture)) {
        return forceTexture;
      } else {
        CompressedBlocks.LOGGER.warn("Unable to find texture from config: " + locations.baseTexture);
        return MISSING_TEXTURE;
      }
    }

    ResourceLocation vanillaPattern =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/block/" + fileName);
    if (manager.hasResource(vanillaPattern)) {
      return vanillaPattern;
    }

    ResourceLocation oldPattern =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/blocks/" + fileName);
    if (manager.hasResource(oldPattern)) {
      return oldPattern;
    }

    ResourceLocation baseDirectory =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/" + fileName);
    if (manager.hasResource(baseDirectory)) {
      return baseDirectory;
    }

    Collection<ResourceLocation> lastChance = manager.getAllResourceLocations("textures/", l -> l.endsWith(fileName));

    if (!lastChance.isEmpty()) {
      return lastChance.iterator().next();
    }

    return MISSING_TEXTURE;
  }

  TextureEntry generateTexture(ResourceLocation key, @Nonnull CompressedBlockAssets.CompressedResourceLocation locations) {
    try {
      LOGGER.debug("Generating Texture for: " + locations.compressed);

      BufferedImage image;
      ResourceLocation toLoad = findTexture(locations);
      try {
        image = ImageIO.read(ImageUtil.getTextureInputStream(toLoad));
      } catch (IOException e) {
        LOGGER.warn("Unable to load texture: " + toLoad, e);
        return new TextureEntry(null, key, new BufferedImage(1, 1, TYPE_INT_RGB));
      }

      int[] pixels = getPixelsForImage(image);

      if (oldImageCache.containsKey(key)) {
        TextureEntry entry = oldImageCache.get(key);
        int[] oldPixels = entry.baseImage == null ? new int[0] : getPixelsForImage(entry.baseImage);

        if (Arrays.equals(pixels, oldPixels)) {
          LOGGER.debug("Using Cached Texture for: " + locations.compressed);
          return entry;
        }
      }

      int scale = locations.compressionAmount + 1;
      int squareSize = Math.min(image.getWidth(), image.getHeight());

      BufferedImage compressedBaseImage = image;
      if (squareSize > ClientConfig.INSTANCE.maxTextureWidth.get() / scale) {
        compressedBaseImage = ImageUtil.scaleDownToWidth(compressedBaseImage, Math.max(ClientConfig.INSTANCE.maxTextureWidth.get() / scale, 1));
        squareSize = Math.min(compressedBaseImage.getWidth(), compressedBaseImage.getHeight());
        pixels = getPixelsForImage(compressedBaseImage);
      }

      BufferedImage compressedImage;
      if(compressedBaseImage.getWidth() == 1) {
        compressedImage = compressedBaseImage;
      } {
        int newSize = squareSize * scale;
        WritableRaster raster = compressedBaseImage.getColorModel().createCompatibleWritableRaster(newSize, newSize);
        for (int x = 0; x < scale; x++) {
          for (int y = 0; y < scale; y++) {
            raster.setPixels(x * squareSize, y * squareSize, squareSize, squareSize, pixels);
          }
        }

        compressedImage = new BufferedImage(compressedBaseImage.getColorModel(), raster, false, null);
      }
      return new TextureEntry(image, key, compressedImage);

    } catch (Exception e) {
      LOGGER.error(String.format("Unable to generate %s texture", key), e);
      throw new RuntimeException(e);
    }
  }

  private static int[] getPixelsForImage(BufferedImage image) {
    int squareSize = Math.min(image.getWidth(), image.getHeight());
    return image.getData().getPixels(0, 0, squareSize, squareSize, (int[]) null);
  }

}
