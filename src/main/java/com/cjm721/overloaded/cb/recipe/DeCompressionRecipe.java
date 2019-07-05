package com.cjm721.overloaded.cb.recipe;

import com.cjm721.overloaded.cb.block.CompressedBlockItem;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import static com.cjm721.overloaded.cb.recipe.RecipeHolder.DE_COMPRESSOR;

public class DeCompressionRecipe extends SpecialRecipe {
  public DeCompressionRecipe(ResourceLocation idIn) {
    super(idIn);
  }

  @Override
  public boolean matches(CraftingInventory inv, World worldIn) {
    boolean foundItem = false;
    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (stack.isEmpty()) {
        continue;
      }
      if (stack.getItem() instanceof CompressedBlockItem) {
        if(!((CompressedBlockItem) stack.getItem()).getCompressedBlock().getRecipeEnabled()) {
          return false;
        }
        if (foundItem) {
          // More then one item in inventory
          return false;
        }
        foundItem = true;
      } else {
        // Not a Compressed Block
        return false;
      }
    }
    return foundItem;
  }

  @Override
  public ItemStack getCraftingResult(CraftingInventory inv) {
    if(!matches(inv, null)) {
      return ItemStack.EMPTY;
    }

    for (int i = 0; i < inv.getSizeInventory(); i++) {
      ItemStack stack = inv.getStackInSlot(i);
      if (stack.isEmpty()) {
        continue;
      }
        return new ItemStack(((CompressedBlockItem) stack.getItem()).getCompressedBlock().getUnCompressedVersion(), 9);
    }
    throw new RuntimeException("Impossible Condition");
  }

  @Override
  public IRecipeSerializer<?> getSerializer() {
    return DE_COMPRESSOR;
  }
}
