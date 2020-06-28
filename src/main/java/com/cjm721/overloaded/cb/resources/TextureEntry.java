package com.cjm721.overloaded.cb.resources;

import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.Serializable;

class TextureEntry implements Serializable {

  final BufferedImage baseImage;
  final BufferedImage image;
  final ResourceLocation location;

  TextureEntry(BufferedImage baseImage, ResourceLocation location, BufferedImage image) {
    this.baseImage = baseImage;
    this.location = location;
    this.image = image;
  }
}
