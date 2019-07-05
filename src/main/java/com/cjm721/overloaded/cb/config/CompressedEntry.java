package com.cjm721.overloaded.cb.config;

public class CompressedEntry {
    public String baseRegistryName;
    public String texturePath;
    public int depth;
    public float hardnessMultiplier;
    public boolean recipeEnabled;

    // Required as used by GSON
    public CompressedEntry() {
        recipeEnabled = true;
        depth = 16;
        hardnessMultiplier = 9;
    }

    public CompressedEntry(String baseRegistryName, String texturePath, int depth, float hardnessMultiplier,
            boolean recipeEnabled) {
        this.baseRegistryName = baseRegistryName;
        this.texturePath = texturePath;
        this.depth = depth;
        this.hardnessMultiplier = hardnessMultiplier;
        this.recipeEnabled = recipeEnabled;
    }
}
