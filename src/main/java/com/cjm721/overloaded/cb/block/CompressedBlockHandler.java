package com.cjm721.overloaded.cb.block;

import com.cjm721.overloaded.cb.config.CompressedConfig;
import com.cjm721.overloaded.cb.config.CompressedEntry;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class CompressedBlockHandler {

  public static Map<Block, BlockCompressed> firstStageCompression = new HashMap<>();

  public static List<BlockCompressed> initFromConfig() {
    List<BlockCompressed> compressedBlocks = new LinkedList<>();

    for (CompressedEntry entry : CompressedConfig.getCompressedEntries()) {
      Block baseBlock = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(entry.baseRegistryName));
      Block unCompressed = baseBlock;

      for (int i = 0; i < entry.depth; i++) {
        BlockCompressed compressedBlock = new BlockCompressed(entry.compressedPathRegistryName,
            baseBlock, entry, i + 1);

        if(i == 0 && compressedBlock.getRecipeEnabled()) {
          firstStageCompression.put(baseBlock, compressedBlock);
        }

        compressedBlock.setUnCompressed(unCompressed);
        if(unCompressed instanceof BlockCompressed) {
          ((BlockCompressed) unCompressed).setCompressed(compressedBlock);
        }
        compressedBlocks.add(compressedBlock);

        unCompressed = compressedBlock;
      }
    }
    return compressedBlocks;
  }
}
