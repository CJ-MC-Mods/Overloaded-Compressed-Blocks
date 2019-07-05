package com.cjm721.overloaded.cb.resources;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.*;
import net.minecraft.resources.data.IMetadataSectionSerializer;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Predicate;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;
import static java.util.stream.Collectors.toList;

public class BlockResourcePack implements IResourcePack {

  public static final BlockResourcePack INSTANCE = new BlockResourcePack();

  private BlockResourcePack() {
    this.domains.add(MODID);
  }

  private final Map<ResourceLocation, BufferedImage> images = Maps.newHashMap();
  private final Map<ResourceLocation, String> resource = Maps.newHashMap();

  private final Set<String> domains = Sets.newHashSet();

  public void addImage(@Nonnull ResourceLocation res, @Nonnull BufferedImage image) {
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
    System.out.println("getAllResoruceLocations");
    return resource.entrySet().stream().filter(e -> e.getKey().getPath().startsWith(pathIn)).map(e -> new
        ResourceLocation
        (e.getKey().getNamespace(), e.getKey().getPath()))
        .collect(toList());
  }

  private InputStream getImageInputStream(@Nonnull ResourceLocation location) throws IOException {
    BufferedImage image = images.get(location);
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
}
