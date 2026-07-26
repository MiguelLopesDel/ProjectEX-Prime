---
navigation:
  parent: index.md
  title: Power Flowers
  icon: basic_power_flower
  position: 30
item_ids:
  - projectex:basic_power_flower
  - projectex:dark_power_flower
  - projectex:red_power_flower
  - projectex:magenta_power_flower
  - projectex:pink_power_flower
  - projectex:purple_power_flower
  - projectex:violet_power_flower
  - projectex:blue_power_flower
  - projectex:cyan_power_flower
  - projectex:green_power_flower
  - projectex:lime_power_flower
  - projectex:yellow_power_flower
  - projectex:orange_power_flower
  - projectex:white_power_flower
  - projectex:fading_power_flower
  - projectex:final_power_flower
  - projectex:basic_compressed_collector
  - projectex:dark_compressed_collector
  - projectex:red_compressed_collector
  - projectex:magenta_compressed_collector
  - projectex:pink_compressed_collector
  - projectex:purple_compressed_collector
  - projectex:violet_compressed_collector
  - projectex:blue_compressed_collector
  - projectex:cyan_compressed_collector
  - projectex:green_compressed_collector
  - projectex:lime_compressed_collector
  - projectex:yellow_compressed_collector
  - projectex:orange_compressed_collector
  - projectex:white_compressed_collector
  - projectex:fading_compressed_collector
  - projectex:final_compressed_collector
---

# Power Flowers

A power flower is a whole EMC farm folded into one block. Eighteen collectors and thirty relays of
its tier, arranged the way you would have arranged them yourself, and it pays the result **straight
into your transmutation balance** once a second.

<RecipeFor id="projectex:basic_power_flower" />

## It has an owner

Whoever places it owns it. The EMC goes to that player's Transmutation Table, not into the world,
and no pipe or machine can take it out on the way. There is nothing to wire up and nothing to
collect.

Because the EMC never enters the world, a power flower is not an EMC storage: pointing a
<ItemLink id="projectex:basic_relay" /> at one does nothing, and neither does pointing it at a
Condenser.

## While you are logged out

It keeps running. What it makes is written to your player data, so a flower placed before you log
off has been paying you the entire time you were away. This is the main reason to build one
instead of a wall of collectors feeding a link.

## Output

The rate is `collector output × 18 + relay bonus × 30` for its tier. For a Basic power flower that
is `4 × 18 + 1 × 30`, or 102 EMC per second, against the 4/s of a single Basic collector.

The block's tooltip shows the figure for its tier, including any change a pack has made in the
config.

## Why "Bonsai Pot"

The 1.12 mod called them Power Flower Bonsai Pots and the name stuck. It is the same block.
