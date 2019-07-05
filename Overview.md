# Overview
Allows creation of compressed blocks for any other block (this includes other mod's blocks) using a relatively simple
JSON config. In that config you only need to mention resource name for the base, resource name for the compressed
version, texture to use, and how many compression levels you want.

It will generate recipes for the 3x3 compression and un-compression for each block automatically (which can be disabled in the config entry).

This was part of [Overloaded](https://www.curseforge.com/minecraft/mc-mods/overloaded) for 1.12 and has since been split out.  

![InGame Example](https://raw.githubusercontent.com/CJ-MC-Mods/Online-Resources/master/IBHSTD/blocks/Compressed_Blocks.png "Compressed Blocks")

## Eample Config Entry
```
[
  {
    "baseRegistryName": "minecraft:cobblestone",
    "compressedPathRegistryName": "compressed_cobblestone",
    "texturePath": "minecraft:textures/blocks/cobblestone.png",
    "depth": 16,
    "hardnessMultiplier": 9.0,
    "recipeEnabled": true
  }
]
```

### Warnings

#### Client - Server Syncing
The config needs to be kept in sync between client and server. If they are out of sync issues will occur.

#### Texture Sheet Size
Since this generates dynamic textures for many blocks there is the possibility of filling out the entire texture space.
The amount of texture space you have depends on your graphics card. If you get a TextureStich exception while loading
you have run out of space. To alleviate this there is a config entry for max texture size.