package com.cjm721.overloaded.cb.resources;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

@OnlyIn(Dist.CLIENT)
public class ImageUtil {
  public static BufferedImage scaleDownToWidth(@Nonnull BufferedImage original, int width) {
    double scale = original.getWidth() / (double) width;

    if (scale <= 1) {
      return original;
    }

    AffineTransform at = new AffineTransform();
    at.scale(1 / scale, 1 / scale);

    AffineTransformOp scaleOp = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);

    return scaleOp.filter(original, null);
  }

  public static InputStream getTextureInputStream(ResourceLocation location) throws IOException {
    return Minecraft.getInstance().getResourceManager().getResource(location).getInputStream();
  }
}
