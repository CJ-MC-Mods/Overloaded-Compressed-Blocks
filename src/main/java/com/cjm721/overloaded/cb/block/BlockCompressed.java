package com.cjm721.overloaded.cb.block;

import com.cjm721.overloaded.cb.config.ClientConfig;
import com.cjm721.overloaded.cb.config.CompressedEntry;
import com.cjm721.overloaded.cb.resources.CompressedBlockAssets;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateContainer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.List;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

public class BlockCompressed extends Block {

  private static Block currentConstructionBaseBlock;

  private static final DecimalFormat numberFormat;

  static {
    numberFormat = new DecimalFormat("#,###");
    numberFormat.setRoundingMode(RoundingMode.HALF_DOWN);
    numberFormat.setMaximumFractionDigits(0);
  }

  private Block baseBlock;
  private final int compressionLevel;
  private final CompressedEntry entry;

  private BlockCompressed compressed;
  private Block unCompressed;

  BlockCompressed(@Nonnull String registryName, Block baseBlock, CompressedEntry entry, int compressionLevel) {
    super(Properties.from(recordBaseBlock(baseBlock))
        .hardnessAndResistance((float) Math.min(
            baseBlock.getDefaultState().getBlockHardness(null, null) *
                Math.pow(entry.hardnessMultiplier, compressionLevel), Float.MAX_VALUE)));

    setRegistryName(MODID, registryName + "_" + compressionLevel);
    this.baseBlock = baseBlock;

    this.entry = entry;
    this.compressionLevel = compressionLevel;
  }

  private static Block recordBaseBlock(Block baseBlock) {
    currentConstructionBaseBlock = baseBlock;
    return baseBlock;
  }

  @Override
  public void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
    if (baseBlock == null) {
      currentConstructionBaseBlock.fillStateContainer(builder);
    } else {
      baseBlock.fillStateContainer(builder);
    }
  }

  @OnlyIn(Dist.CLIENT)
  @Override
  public void addInformation(ItemStack stack, @Nullable IBlockReader worldIn, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn) {
    if (ClientConfig.INSTANCE.showHardness.get())
      tooltip.add(new StringTextComponent(String.format("Hardness: %s", numberFormat.format(((BlockItem) stack.getItem())
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

  public Block getBaseBlock() {
    return baseBlock;
  }

  public int getCompressionLevel() {
    return compressionLevel;
  }

  public void setCompressed(BlockCompressed compressed) {
    this.compressed = compressed;
  }

  void setUnCompressed(Block unCompressed) {
    this.unCompressed = unCompressed;
  }
}
