package com.cjm721.overloaded.cb;

import com.cjm721.overloaded.cb.block.BlockCompressed;
import com.cjm721.overloaded.cb.block.CompressedBlockHandler;
import com.cjm721.overloaded.cb.block.CompressedBlockItem;
import com.cjm721.overloaded.cb.client.BlockResourcePack;
import com.cjm721.overloaded.cb.recipe.CompressionRecipe;
import com.cjm721.overloaded.cb.recipe.DeCompressionRecipe;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
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
    MinecraftForge.EVENT_BUS.addListener(this::serverStartingEvent);
  }

  public void serverAboutToStartEvent(FMLServerAboutToStartEvent event) {
//    event.getServer().getResourcePacks().addPackFinder();
//        .getResourcePack()
//        .ResourcePackInfo
//        .createResourcePack" +
//        "("overloaded_cb", false,()
//        -> BlockResourcePack.INSTANCE, , ResourcePackInfo.Priority.BOTTOM));

  }

  public void serverStartingEvent(FMLServerStartingEvent event) {
//    event.getServer().getResourceManager().addResourcePack(BlockResourcePack.INSTANCE);
//    event.getServer().getRecipeManager().
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
      DistExecutor.runWhenOn(Dist.CLIENT, () -> BlockResourcePack.INSTANCE::inject);
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

    @SubscribeEvent
    public static void registerRecipes(final RegistryEvent.Register<IRecipeSerializer<?>> event) {
      event.getRegistry().register(new SpecialRecipeSerializer<>(CompressionRecipe::new).setRegistryName(new
          ResourceLocation(MODID, "compressor")));
      event.getRegistry().register(new SpecialRecipeSerializer<>(DeCompressionRecipe::new).setRegistryName(new
          ResourceLocation(MODID, "de_compressor")));
    }
  }
}
