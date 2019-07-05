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