# ProjectEX Prime

Unofficial port of **ProjectEX** to Minecraft **1.20.1** (Forge).

ProjectEX is an extension mod for [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte),
adding EMC links, collectors, relays, power flowers, matter blocks, the Arcane Tablet
and the Final Star.

## Status

Work in progress. The port advances one layer at a time, and every commit compiles.

Ported so far:

- Build system: ModDevGradle legacy, Forge 47.4.10, Java 17
- Registries: `DeferredRegister` for blocks, items, block entities and the creative tab
- Config: `ForgeConfigSpec`, including the per-tier EMC values and the stone table whitelist,
  whose ore dictionary entries became item tags. 1.12's `blacklist_power_flower_from_watch` is
  gone: ProjectE reads what the Watch of Flowing Time skips from the
  `projecte:blacklist/time_watch` tag now, and power flowers are added to it
- Collectors, relays and power flowers, all 16 tiers, generating and moving EMC through
  ProjectE's `IEmcStorage` capability
- EMC links (energy, personal, refined, compressed refined): EMC and items both go to the
  owner's transmutation balance, exposed as an item handler so hoppers, pipes and storage
  networks drive them with no interface involved. They keep producing while their owner is
  offline, by spending from the balance in that player's save file
- The link screen, where you pick what each output slot produces
- Stone Table, with its own transmutation panel so that the whitelist a pack can put on it
  actually restricts what it handles
- On screen EMC counter, with the rate it is moving at, placed by config
- Alchemy Table, turning one item into another for EMC. The chains that were a hardcoded list in
  1.12 are a recipe type now, so a pack can add, remove and reprice them
- Matter items, compressed collectors, final star shard, clay matter
- EMC storage items: Magnum and Colossal Stars (12 tiers) and the Final Star, through
  ProjectE's `IItemEmcHolder`, including the Final Star's pedestal item copying and the EMC
  a star keeps when it is crafted into the next tier up
- Arcane Transmutation Tablet: a portable crafting grid that buys its ingredients back out of
  your EMC after every craft, beside a transmutation panel that searches, learns, unlearns,
  sells and buys. The panel is client side display only; every decision is the server's
- Knowledge Sharing Book: sign it and hand it over, and the reader learns everything you know.
  The author does not have to be online
- JEI: an Alchemy Table category, the ring answering to R and U, the transfer arrow filling
  the tablet's grid from your inventory first and your EMC for the rest, and the two JEI Sync
  search modes driving JEI's own filter
- A keybind, tab by default, that puts the cursor in a transmutation screen's search bar
- Assets and data generation: blockstates, models, translations, loot tables and
  recipes for everything above
- In game guide, written for [GuideME](https://github.com/AppliedEnergistics/GuideME) and
  covering collectors, relays, power flowers, the EMC links, the tier ladder and the tablet

Everything the 1.12.2 mod did is ported. Its sources are preserved in the import commit
`742a85c` and can be pulled back out with `git show 742a85c:<path>`.

## Requirements

- Minecraft 1.20.1
- Forge 47+
- [ProjectE](https://www.curseforge.com/minecraft/mc-mods/projecte) 1.20.1 (PE1.0.1 or newer)
- JEI 15 (optional)

[GuideME](https://github.com/AppliedEnergistics/GuideME) 20.1.15 is required as well, but it
ships inside this mod's jar, so there is nothing extra to download. It is redistributed
unmodified and is licensed under the LGPL-3.0, the same license as this mod. If a newer copy is
present in the instance, Forge loads that one instead of the bundled jar.

## Attribution and license

This is a **modified work**. It is derived from
[FTBTeam/FTB-ProjectEX](https://github.com/FTBTeam/FTB-ProjectEX), branch `1.12`,
commit `884d3ccdc35c2682f23247373e7e47fbace81006`, originally written by
LatvianModder and the FTB Team.

Modifications made in this repository, starting 2026-07-25, port the mod from
Minecraft 1.12.2 to 1.20.1: the build system was replaced, the mod was migrated to
the modern registry, block state, menu, networking and data generation APIs, and the
integration with ProjectE and JEI was rewritten against their current APIs.

Licensed under the **GNU Lesser General Public License v3.0**, the same license as the
original project. See [LICENSE.txt](LICENSE.txt).

This project is not affiliated with the FTB Team or with ProjectE, and it is not
endorsed by them. It does not use the "FTB" name or branding.
