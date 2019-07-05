package com.cjm721.overloaded.cb.config;

public class CompressedEntry {
    public String baseRegistryName;
    public String compressedPathRegistryName;
    public String texturePath;
    public int depth;
    public float hardnessMultiplier;
    public boolean recipeEnabled;
    public boolean copyBaseBlockState;

    // Required as used by GSON
    public CompressedEntry() {
    }

    public CompressedEntry(String baseRegistryName, String compressedPathRegistryName, String texturePath, int depth, float hardnessMultiplier, boolean recipeEnabled, boolean copyBaseBlockState) {
        this.baseRegistryName = baseRegistryName;
        this.compressedPathRegistryName = compressedPathRegistryName;
        this.texturePath = texturePath;
        this.depth = depth;
        this.hardnessMultiplier = hardnessMultiplier;
        this.recipeEnabled = recipeEnabled;
        this.copyBaseBlockState = copyBaseBlockState;
    }
}
