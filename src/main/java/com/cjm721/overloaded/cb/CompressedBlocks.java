package com.cjm721.overloaded.cb;

import com.cjm721.overloaded.cb.block.BlockCompressed;
import com.cjm721.overloaded.cb.block.CompressedBlockHandler;
import com.cjm721.overloaded.cb.block.CompressedBlockItem;
import com.cjm721.overloaded.cb.config.ClientConfig;
import com.cjm721.overloaded.cb.resources.BlockResourcePack;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.registries.IForgeRegistry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

@Mod(MODID)
public class CompressedBlocks {

  private static final Logger LOGGER = LogManager.getLogger();

  public static final String MODID = "overloaded_cb";

  public CompressedBlocks() {
    ClientConfig.init();
    MinecraftForge.EVENT_BUS.addListener(this::serverAboutToStartEvent);
  }

  public void serverAboutToStartEvent(FMLServerAboutToStartEvent event) {
    BlockResourcePack.INSTANCE.inject(event.getServer().getResourceManager());
  }

  public static final ItemGroup ITEM_GROUP = new ItemGroup("Overloaded_Compressed_Blocks") {
    @Override
    public ItemStack createIcon() {
      return new ItemStack(Items.COBBLESTONE);
    }
  };

  @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
  public static class EventHandler {
    public static List<BlockCompressed> blocks;

    @SubscribeEvent
    public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {
      DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> BlockResourcePack.INSTANCE.inject(Minecraft.getInstance()
          .getResourceManager()));
      blocks = CompressedBlockHandler.initFromConfig();
      IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();

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
}
