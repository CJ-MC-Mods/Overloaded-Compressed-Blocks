package com.cjm721.overloaded.cb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CompressedConfig {

  public static final CompressedConfig INSTANCE = new CompressedConfig();

  private static final CompressedEntry[] defaults = new CompressedEntry[]{
      new CompressedEntry("minecraft:cobblestone", "compressed_cobblestone", "minecraft:textures/block/cobblestone" +
          ".png", 16, 9.0f, true),
      new CompressedEntry("minecraft:sand", "compressed_sand", "minecraft:textures/block/sand.png", 16, 9.0f, true),
      new CompressedEntry("minecraft:stone", "compressed_stone", "minecraft:textures/block/stone.png", 16, 9.0f, true),
      new CompressedEntry("minecraft:obsidian", "compressed_obsidian", "minecraft:textures/block/obsidian.png", 16, 9.0f, true),
      new CompressedEntry("minecraft:netherrack", "compressed_netherrack", "minecraft:textures/block/netherrack.png", 16, 9.0f, true),
      new CompressedEntry("minecraft:dirt", "compressed_dirt", "minecraft:textures/block/dirt.png", 16, 9.0f, true),
      new CompressedEntry("minecraft:gravel", "compressed_gravel", "minecraft:textures/block/gravel.png", 16, 9.0f, true)
  };

  public static CompressedEntry[] getCompressedEntries() {
    try {
      File file = FMLPaths.CONFIGDIR.get().resolve("overloaded_compressed_blocks").toFile();
      file.mkdirs();

      file = file.toPath().resolve("compressed.json").toFile();

      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      if (!file.exists()) {
        String json = gson.toJson(defaults, defaults.getClass());
        FileWriter writer = null;
        writer = new FileWriter(file);
        writer.write(json);
        writer.flush();
        writer.close();
        return defaults;
      }

      return gson.fromJson(new FileReader(file), CompressedEntry[].class);
    } catch (IOException e) {
      throw new RuntimeException("Impossible Exception, file existed moments ago", e);
    }
  }
}
