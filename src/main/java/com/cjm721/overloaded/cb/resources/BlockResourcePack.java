package com.cjm721.overloaded.cb.resources;

import com.cjm721.overloaded.cb.CompressedBlocks;
import com.cjm721.overloaded.cb.config.ClientConfig;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.client.Minecraft;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static com.cjm721.overloaded.cb.CompressedBlocks.LOGGER;
import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import static java.util.stream.Collectors.toList;

public class BlockResourcePack implements IResourcePack {

  public static final BlockResourcePack INSTANCE = new BlockResourcePack();

  private BlockResourcePack() {
    this.domains.add(MODID);
  }

  private final Map<ResourceLocation, CompressedBlockAssets.CompressedResourceLocation> images = Maps.newHashMap();
  private final Map<ResourceLocation, String> resource = Maps.newHashMap();

  private final Set<String> domains = Sets.newHashSet();

  public void addImage(@Nonnull ResourceLocation res, @Nonnull CompressedBlockAssets.CompressedResourceLocation image) {
    images.put(res, image);
  }

  public void addResouce(ResourceLocation res, String state) {
    resource.put(res, state);
  }

  public final void inject(IResourceManager manager) {
    manager.addResourcePack(this);
    if (manager instanceof IReloadableResourceManager) {
      ((IReloadableResourceManager) manager).addReloadListener(new IFutureReloadListener() {
        @Override
        public CompletableFuture<Void> reload(IStage iStage, IResourceManager iResourceManager, IProfiler iProfiler, IProfiler iProfiler1, Executor executor, Executor executor1) {
          iResourceManager.addResourcePack(BlockResourcePack.this);
          return iStage.markCompleteAwaitingOthers(null);
        }
      });
    }
  }

  @Override
  @Nonnull
  public InputStream getRootResourceStream(@Nonnull String fileName) throws IOException {
    return null;
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
  public Collection<ResourceLocation> getAllResourceLocations(@Nonnull ResourcePackType type, @Nonnull String pathIn,
                                                              int maxDepth, @Nonnull Predicate<String> filter) {
    return resource.entrySet().stream().filter(e -> e.getKey().getPath().startsWith(pathIn)).map(e -> new
        ResourceLocation
        (e.getKey().getNamespace(), e.getKey().getPath()))
        .collect(toList());
  }

  private InputStream getImageInputStream(@Nonnull ResourceLocation location) throws IOException {
    BufferedImage image = generateTexture(images.get(location));
    if (image != null) {
      ByteArrayOutputStream os = new ByteArrayOutputStream();
      ImageIO.write(image, "png", os);
      return new ByteArrayInputStream(os.toByteArray());
    }
    System.err.println("Not Found: " + location.toString());
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
    return null;
  }

  @Override
  @Nonnull
  public String getName() {
    return "Overloaded Compressed Textures";
  }

  @Override
  public void close() throws IOException {
  }

  private static final ResourceLocation MISSING_TEXTURE = new ResourceLocation(MODID, "textures/missing.png");

  private static ResourceLocation findTexture(@Nonnull CompressedBlockAssets.CompressedResourceLocation locations) {
    IResourceManager manager = Minecraft.getInstance().getResourceManager();
    String fileName = locations.baseBlock.getPath() + ".png";

    if(locations.baseTexture != null) {
      ResourceLocation forceTexture = new ResourceLocation(locations.baseTexture);
      if(manager.hasResource(forceTexture)) {
        return forceTexture;
      } else {
        CompressedBlocks.LOGGER.warn("Unable to find texture from config: " + locations.baseTexture);
        return MISSING_TEXTURE;
      }
    }

    ResourceLocation vanillaPattern =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/block/" + fileName);
    if(manager.hasResource(vanillaPattern)) {
      return vanillaPattern;
    }

    ResourceLocation oldPattern =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/blocks/" + fileName);
    if(manager.hasResource(oldPattern)) {
      return oldPattern;
    }

    ResourceLocation baseDirectory =
        new ResourceLocation(locations.baseBlock.getNamespace(), "textures/" + fileName);
    if(manager.hasResource(baseDirectory)) {
      return baseDirectory;
    }

    Collection<ResourceLocation> lastChance = manager.getAllResourceLocations("textures/", l -> l.endsWith(fileName));

    if(!lastChance.isEmpty()) {
      return lastChance.iterator().next();
    }

    return MISSING_TEXTURE;
  }

  private static BufferedImage generateTexture(@Nonnull CompressedBlockAssets.CompressedResourceLocation locations) {
    BufferedImage image;

    ResourceLocation toLoad = findTexture(locations);
    try {
      image = ImageIO.read(ImageUtil.getTextureInputStream(toLoad));
    } catch (IOException e) {
      LOGGER.warn("Unable to load texture: " + toLoad, e);
      return new BufferedImage(1,1, TYPE_INT_RGB);
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
    return compressedImage;
  }
}
