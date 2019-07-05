package com.cjm721.overloaded.cb.recipe;

import com.cjm721.overloaded.cb.block.BlockCompressed;
import com.cjm721.overloaded.cb.block.CompressedBlockHandler;
import com.cjm721.overloaded.cb.block.CompressedBlockItem;
import net.minecraft.block.Block;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class CompressionRecipe extends SpecialRecipe {

  public CompressionRecipe(ResourceLocation idIn) {
    super(idIn);
  }

  @Override
  public boolean matches(CraftingInventory inv, World worldIn) {
    Item item = inv.getStackInSlot(0).getItem();

    if (!(item instanceof BlockItem)) {
      return false;
    }

    Block block = ((BlockItem) item).getBlock();

    if (block instanceof BlockCompressed) {
      if (!((CompressedBlockItem) item).getCompressedBlock().getRecipeEnabled()) {
        return false;
      }
    } else if (!CompressedBlockHandler.firstStageCompression.containsKey(((BlockItem)item).getBlock())) {
      return false;
    }

    for (int i = 1; i < 9; i++) {
      if (inv.getStackInSlot(i).getItem() != item) {
        return false;
      }
    }
    return true;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory inv) {
    if (!matches(inv, null)) {
      return ItemStack.EMPTY;
    }

    Block block = ((BlockItem) inv.getStackInSlot(0).getItem()).getBlock();

    if(block instanceof BlockCompressed) {
      BlockCompressed blockCompressed = ((BlockCompressed) block).getCompressedVersion();
      return new ItemStack(blockCompressed);
    } else {
      return new ItemStack(CompressedBlockHandler.firstStageCompression.get(block));
    }
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return RecipeHolder.COMPRESSOR;
  }

  @Override
  public boolean canFit(int width, int height) {
    return width == 3 && height == 3;
  }
}
