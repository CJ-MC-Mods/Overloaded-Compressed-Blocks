**Version: 1.0.13 (Release)**

* Fix crash due to forge changes. Changes are same as in 1.16 mod loading.

-------------------------------------------------------------------
**Version: 1.0.12 (Release)**

* Remove Config hack from 1.14 as seems to cause issues for Inventory Tweaks and no longer needed

-------------------------------------------------------------------
**Version: 1.0.11 (Release)**

* Removed Force Generate config option and always do it now.
* Better caching for when texture-reloads happen not all images need to be generated
* Reduced memory load and now able to handle much larger base textures
* Hardness Tooltip reports the correct hardness for high density blocks
* Config option for level of texture generation parallelization 

-------------------------------------------------------------------
**Version: 1.0.10 (Release)**

* Updated to 1.15.2

-------------------------------------------------------------------
**Version: 1.0.9 (Release)**

* Use Obfuscated name instead of mapped as something is not transferring correcting when run outside dev environments.

-------------------------------------------------------------------
**Version: 1.0.8 (Release)**

* Updated to Minecraft 1.14.4

-------------------------------------------------------------------
**Version: 1.0.7 (Release)**

* Sort Creative / JEI menu for items by Mod-Base Name-Compression Amount
* New Setting for force loading early (early loading is multi-threaded)

-------------------------------------------------------------------
**Version: 1.0.6 (Release)**

* Fix breaking data-packs. (Fix #4)

-------------------------------------------------------------------
**Version: 1.0.5 (Release)**

*After release update - This version is broken*
* Make the dynamic texture picker a bit smarter.

-------------------------------------------------------------------
**Version: 1.0.4 (Release)**

* Generate Textures as late as possible.
* Dynamically add dependencies so make sure all other mods registry events happen first.

-------------------------------------------------------------------
**Version: 1.0.3 (Release)**

* Regex support for config added for registry name.
* Added default values for all other options so regex can work.
* If texturePath is provided it disables the auto-lookup (auto-look is not very smart).

-------------------------------------------------------------------
**Version: 1.0.2 (Release)**

* Remove physical data pack as no longer needed.
* Remove some extra print statements.
* Drops from blocks are now in loot_table so can be overridden.
* Force config to load early so can use it in blocks / textures consistently.

-------------------------------------------------------------------
**Version: 1.0.1 (Release)**

* Recipes now sync from server. (Fixes #1)

-------------------------------------------------------------------
**Version: 1.0.0 (Beta)**

* Initial port and split from Overloaded v1.12.
* Mostly feature complete. Still some small sync issues (See GitHub).
* Should have no corruption or crash issues. If found please report.
* Effectively Release quality version, just want more testing.