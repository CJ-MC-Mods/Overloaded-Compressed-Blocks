package com.cjm721.overloaded.cb.block;

import com.cjm721.overloaded.cb.config.CompressedConfig;
import com.cjm721.overloaded.cb.config.CompressedEntry;
import com.cjm721.overloaded.cb.resources.CompressedBlockAssets;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CompressedBlockHandler {
  public static List<BlockCompressed> initFromConfig() {
    List<BlockCompressed> compressedBlocks = new LinkedList<>();


    for (CompressedEntry entry : CompressedConfig.getCompressedEntries()) {
      List<Map.Entry<ResourceLocation, Block>> matchedEntries = ForgeRegistries.BLOCKS.getEntries().stream().filter(e -> e.getKey().toString().matches(entry
          .baseRegistryName))
          .collect(Collectors.toList());

      for (Map.Entry<ResourceLocation, Block> matchedEntry : matchedEntries) {
        Block unCompressed = matchedEntry.getValue();

        for (int i = 0; i < entry.depth; i++) {
          BlockCompressed compressedBlock = new BlockCompressed("compressed_" + matchedEntry.getKey().getPath(),
              matchedEntry.getValue(), entry, i + 1);

          if (entry.recipeEnabled) {
            CompressedBlockAssets.addToRecipes(unCompressed.getRegistryName(), compressedBlock.getRegistryName());
          }
          CompressedBlockAssets.addToDropLootTable(compressedBlock.getRegistryName());

          compressedBlock.setUnCompressed(unCompressed);
          if (unCompressed instanceof BlockCompressed) {
            ((BlockCompressed) unCompressed).setCompressed(compressedBlock);
          }
          compressedBlocks.add(compressedBlock);

          unCompressed = compressedBlock;
        }
      }
    }
    return compressedBlocks;
  }
}
