---
navigation:
  parent: index.md
  title: Stone Transmutation Table
  icon: stone_table
  position: 50
---

# Stone Transmutation Table

A Transmutation Table that can be placed on a wall or a ceiling as well as on the floor. Right
click it and you get a transmutation panel working on your own knowledge and your own EMC: search,
a ring of what you know, and the sell, learn and unlearn buttons. It works exactly like the panel
described under the [Arcane Tablet](arcane-tablet.md), minus the crafting grid.

<RecipeFor id="projectex:stone_table" />

Being able to hang it at eye level next to a wall of [links](emc-links.md) is most of the point.

## What a pack can restrict

Unlike ProjectE's own table, this one can be given a list of what it handles. Two config options
govern it, and both are off by default, so out of the box the table handles everything you know:

- `enable_stone_table_whitelist` — until this is on, the list below is ignored entirely.
- `stone_table_whitelist` — item ids, or item tags written with a leading `#`. The 1.12 version
  used ore dictionary names; those are tags now, so `oreIngot` reads `#forge:ingots`.

An item left out of the list will not appear in the ring, cannot be sold at this table, and says so
in its tooltip. Items that hold EMC are the one exception: a Klein Star can be filled or emptied at
any table, whatever the list says.
