package com.cjm721.overloaded.cb;

import com.cjm721.overloaded.cb.block.BlockCompressed;
import com.cjm721.overloaded.cb.block.CompressedBlockHandler;
import com.cjm721.overloaded.cb.block.CompressedBlockItem;
import com.cjm721.overloaded.cb.config.ClientConfig;
import com.cjm721.overloaded.cb.config.CompressedConfig;
import com.cjm721.overloaded.cb.config.CompressedEntry;
import com.cjm721.overloaded.cb.resources.BlockResourcePack;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.resources.IPackFinder;
import net.minecraft.resources.ResourcePackInfo;
import net.minecraft.resources.ResourcePackList;
import net.minecraft.util.NonNullList;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.forgespi.language.IModInfo;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.maven.artifact.versioning.VersionRange;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

@Mod(MODID)
public class CompressedBlocks {

  public static final Logger LOGGER = LogManager.getLogger();

  public static final String MODID = "overloaded_cb";

  public CompressedBlocks() {
    ClientConfig.init();
    MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStartEvent);

    Set<String> modids = new HashSet<>();
    for (CompressedEntry entry : CompressedConfig.getCompressedEntries()) {
      int index = entry.baseRegistryName.indexOf(':');

      if (index != -1) {
        modids.add(entry.baseRegistryName.substring(0, index).replaceAll("[^a-z0-9/._-]", ""));
      }
    }

    IModInfo owner = ModLoadingContext.get().getActiveContainer().getModInfo();
    List<IModInfo.ModVersion> dependencies = (List<IModInfo.ModVersion>) owner.getDependencies();

    modids.removeAll(dependencies.stream().map(d -> d.getModId()).collect(Collectors.toList()));
    for (String modid : modids) {
      dependencies.add(new ModVersion(owner, modid));
    }
  }

  public void serverAboutToStartEvent(FMLServerAboutToStartEvent event) {
    ResourcePackList<ResourcePackInfo> list = event.getServer().resourcePacks;
    list.addPackFinder(new IPackFinder() {
      @Override
      public <T extends ResourcePackInfo> void addPackInfosToMap(Map<String, T> nameToPackMap, ResourcePackInfo.IFactory<T> packInfoFactory) {
        T pack = ResourcePackInfo.createResourcePack("overloaded_cb_injected", false, () -> BlockResourcePack.INSTANCE, packInfoFactory, ResourcePackInfo.Priority.BOTTOM);
        nameToPackMap.put("overloaded_cb_injected", pack);
      }
    });
  }

  public static final ItemGroup ITEM_GROUP = new ItemGroup("Overloaded_Compressed_Blocks") {
    @Override
    public ItemStack createIcon() {
      return new ItemStack(Items.COBBLESTONE);
    }

    @Override
    public void fill(NonNullList<ItemStack> items) {
      super.fill(items);

      items.sort(Comparator.<ItemStack, String>comparing(o -> ((CompressedBlockItem) o.getItem()).getCompressedBlock().getBaseBlock().getRegistryName().toString())
          .thenComparing(o -> ((CompressedBlockItem) o.getItem()).getCompressedBlock().getCompressionLevel()));
    }
  };

  @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class EventHandler {
    public static List<BlockCompressed> blocks;

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
      DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
        try {
          BlockResourcePack.INSTANCE.inject(Minecraft.getInstance().getResourceManager());
        } catch (InvocationTargetException | IllegalAccessException e) {
          throw new IllegalStateException(e);
        }
      });
      blocks = CompressedBlockHandler.initFromConfig();
      IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

      LOGGER.info(String.format("Registering %d compressed blocks", blocks.size()));
      for (BlockCompressed block : blocks) {
        registry.register(block);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () ->
            block.registerModel()
        );
      }
    }

    @SubscribeEvent
    public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
      IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
      for (BlockCompressed block : blocks) {
        registry.register(new CompressedBlockItem(block));
      }
    }
  }

  /**
   * Pulled from Forge, Needed to prevent having to ASM / Reflection into forge
   */
  private static class ModVersion implements IModInfo.ModVersion {
    private IModInfo owner;
    private final String modId;
    private final VersionRange versionRange;
    private final boolean mandatory;
    private final IModInfo.Ordering ordering;
    private final IModInfo.DependencySide side;

    public ModVersion(IModInfo owner, String modId) {
      this.owner = owner;
      this.modId = modId;
      this.mandatory = false;
      this.versionRange = IModInfo.UNBOUNDED;
      this.ordering = IModInfo.Ordering.AFTER;
      this.side = IModInfo.DependencySide.BOTH;
    }

    @Override
    public String getModId() {
      return this.modId;
    }

    @Override
    public VersionRange getVersionRange() {
      return this.versionRange;
    }

    @Override
    public boolean isMandatory() {
      return this.mandatory;
    }

    @Override
    public IModInfo.Ordering getOrdering() {
      return this.ordering;
    }

    @Override
    public IModInfo.DependencySide getSide() {
      return this.side;
    }

    @Override
    public void setOwner(IModInfo owner) {
      this.owner = owner;
    }

    @Override
    public IModInfo getOwner() {
      return this.owner;
    }
  }
}
