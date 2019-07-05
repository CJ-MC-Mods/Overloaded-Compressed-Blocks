package com.cjm721.overloaded.cb.recipe;

import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraftforge.registries.ObjectHolder;

import static com.cjm721.overloaded.cb.CompressedBlocks.MODID;

@ObjectHolder(MODID)
public class RecipeHolder {
  public static final IRecipeSerializer<CompressionRecipe> COMPRESSOR = null;
  public static final IRecipeSerializer<DeCompressionRecipe> DE_COMPRESSOR = null;
}
