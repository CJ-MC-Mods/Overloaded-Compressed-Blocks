package com.cjm721.overloaded.cb.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class CompressedConfig {
  private static final CompressedEntry[] defaults = new CompressedEntry[]{
      new CompressedEntry("minecraft:cobblestone", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:sand", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:stone", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:obsidian", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:netherrack", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:dirt", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:gravel", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:.*_log", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:.*_wool", null, 16, 9.0f, true),
      new CompressedEntry("minecraft:.*_concrete$", null, 16, 9.0f, true),
  };

  private static CompressedEntry[] entries;

  public static CompressedEntry[] getCompressedEntries() {
    if (entries != null) {
      return entries;
    }

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
        return entries = defaults;
      }

      return entries = gson.fromJson(new FileReader(file), CompressedEntry[].class);
    } catch (IOException e) {
      throw new RuntimeException("Impossible Exception, file existed moments ago", e);
    }
  }
}
