package com.cjm721.overloaded.cb.block;

import com.cjm721.overloaded.cb.config.ClientConfig;
import com.cjm721.overloaded.cb.config.CompressedEntry;
import com.cjm721.overloaded.cb.resources.CompressedBlockAssets;
import com.google.common.collect.Lists;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.storage.loot.LootContext;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

public class BlockCompressed extends Block {

  private Block baseBlock;
  private final int compressionLevel;
  private final CompressedEntry entry;

  private BlockCompressed compressed;
  private Block unCompressed;

  BlockCompressed(@Nonnull String registryName, Block baseBlock, CompressedEntry entry, int compressionLevel) {
    super(Properties.from(baseBlock).hardnessAndResistance((float) Math.min(baseBlock.getDefaultState().getBlockHardness(null,
        null) * Math.pow(entry.hardnessMultiplier, compressionLevel), Float.MAX_VALUE)));

    setRegistryName(MODID, registryName + "_" + compressionLevel);
    this.baseBlock = baseBlock;

    this.entry = entry;
    this.compressionLevel = compressionLevel;
  }

  @Nullable
  @Override
  public net.minecraftforge.common.ToolType getHarvestTool(BlockState state) {
    return baseBlock.getHarvestTool(baseBlock.getDefaultState());
  }

  @Override
  public int getHarvestLevel(BlockState state) {
    return baseBlock.getHarvestLevel(baseBlock.getDefaultState());
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    if (ClientConfig.INSTANCE.showHardness.get())
      tooltip.add(new StringTextComponent(String.format("Hardness: %,d", Math.round((double) ((BlockItem) stack.getItem())
          .getBlock()
          .getDefaultState()
          .getBlockHardness(null, null)))));

    super.addInformation(stack, worldIn, tooltip, flagIn);
  }

  @OnlyIn(Dist.CLIENT)
  public void registerModel() {
    CompressedBlockAssets.addToClientResourcesQueue(
        new CompressedBlockAssets.CompressedResourceLocation(this.baseBlock.getRegistryName(), entry.texturePath,
            getRegistryName(),
            compressionLevel));
  }

  Block getBaseBlock() {
    return baseBlock;
  }

  int getCompressionLevel() {
    return compressionLevel;
  }

  public void setCompressed(BlockCompressed compressed) {
    this.compressed = compressed;
  }

  void setUnCompressed(Block unCompressed) {
    this.unCompressed = unCompressed;
  }
}
