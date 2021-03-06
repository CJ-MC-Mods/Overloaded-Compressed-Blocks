package com.cjm721.overloaded.cb.block;

import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

import static com.cjm721.overloaded.cb.CompressedBlocks.ITEM_GROUP;
import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

public class CompressedBlockItem extends BlockItem {

  BlockCompressed compressedBlock;

  public CompressedBlockItem(BlockCompressed blockIn) {
    super(blockIn, new Item.Properties().group(ITEM_GROUP));
    compressedBlock = blockIn;

    setRegistryName(blockIn.getRegistryName());
  }

  @Override
  public ITextComponent getDisplayName(ItemStack stack) {
    return new StringTextComponent(compressedBlock.getCompressionLevel() + "x ")
        .appendSibling(new TranslationTextComponent(MODID + ".text.compressed"))
        .appendText(" ")
        .appendSibling(compressedBlock.getBaseBlock().asItem().getDisplayName(stack));
  }

  public BlockCompressed getCompressedBlock() {
    return compressedBlock;
  }
}
