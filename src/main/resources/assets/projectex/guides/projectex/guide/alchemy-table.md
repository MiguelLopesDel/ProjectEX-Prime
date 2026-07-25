---
navigation:
  parent: index.md
  title: Alchemy Table
  icon: alchemy_table
  position: 54
---

# Alchemy Table

<RecipeFor id="projectex:alchemy_table" />

Turns one item into another for EMC. Put something in the left slot, feed the table EMC from a
<ItemLink id="projectex:basic_collector" /> or a <ItemLink id="projectex:basic_relay" /> next to it,
and the arrow fills until the item comes out the right side.

It is not a transmutation table. It has no knowledge, no balance of its own and no ring of items:
it walks fixed chains, one step at a time, and the only choice you make is where along a chain you
stop.

## The chains

Each arrow is one step, and each step costs EMC and takes ten seconds:

- Redstone → Gunpowder → Glowstone Dust → Blaze Powder → Blaze Rod
- Lapis Lazuli → Prismarine Shard → Prismarine Crystals
- Low → Medium → High Covalence Dust
- Raw Beef → Rotten Flesh → Leather → Spider Eye → Bone
- Wheat Seeds → Melon Slice → Apple → Carrot → Beetroot → Potato → Pumpkin
- Cookie → Bread → Cake
- Alchemical Coal → Block of Redstone → Lava Bucket → Obsidian
- Oak Leaves → Grass → Fern → Vines → Lily Pad
- Charcoal → Coal
- Eye of Ender → Chorus Fruit, String → Feather, Stick → Dead Bush

The steps only run one way. To go back down a chain, sell the item and buy the other one.

## What a step costs

Three times the EMC of what goes in plus what comes out, and never less than 64. So the redstone
step costs 768 and the blaze rod step a good deal more. Nothing is written into the recipe, which
means a pack that changes an item's EMC value changes what the chain costs to walk without having
to touch anything here.

The table asks for no more EMC than eight steps' worth, so a collector wired to a table with an
empty input slot is not quietly filling a buffer. It also cannot be drained: EMC that goes in is
spent on transmutation or sits there.

## For pack makers

Unlike 1.12, the chains are recipes rather than a hardcoded list. They live in
`data/projectex/recipes/alchemy/` and look like this:

```json
{
  "type": "projectex:alchemy_table",
  "ingredient": { "item": "minecraft:redstone" },
  "result": { "item": "minecraft:gunpowder" }
}
```

`ingredient` is an ordinary ingredient, so a tag works as well as an item. Add `"emc"` to charge a
fixed amount instead of the derived one, and `"duration"` to change the ten seconds.
