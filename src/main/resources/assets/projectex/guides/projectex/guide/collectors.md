---
navigation:
  parent: index.md
  title: Collectors
  icon: basic_collector
  position: 10
---

# Collectors

A collector makes EMC out of nothing. Once a second it adds its tier's output to its own buffer,
then pushes as much of that buffer as it can into whatever sits against its six faces.

Unlike ProjectE's collectors, it does not need light and it does not need fuel in a slot. It runs
the moment it is placed, anywhere, including underground.

<RecipeFor id="projectex:basic_collector" />

## What it pushes into

Anything that accepts EMC: a <ItemLink id="projectex:basic_relay" />, an
<ItemLink id="projectex:energy_link" />, a ProjectE relay, a Condenser, an
Energy Condenser MK2, a Klein Star in a ProjectE relay.

Feeding a relay does something extra, which is the whole reason relays exist — see
[Relays](relays.md).

If nothing next to it accepts EMC, the collector keeps what it made. Nothing is lost while it
waits, so a collector that fills up and stops is simply a collector with nowhere to send its
output.

## Tiers

There are sixteen, and each one produces four times the one before it:

| Tier | Output |
| --- | --- |
| Basic [MK1] | 4/s |
| Dark Matter [MK2] | 12/s |
| Red Matter [MK3] | 40/s |
| Magenta [MK4] | 160/s |
| Pink [MK5] | 640/s |
| Purple [MK6] | 2,560/s |
| Violet [MK7] | 10,240/s |
| Blue [MK8] | 40,960/s |
| Cyan [MK9] | 163,840/s |
| Green [MK10] | 655,360/s |
| Lime [MK11] | 2,621,440/s |
| Yellow [MK12] | 10,485,760/s |
| Orange [MK13] | 41,943,040/s |
| White [MK14] | 167,772,160/s |
| Fading [MK15] | 671,088,640/s |
| The Final Collector | 1,000,000,000,000/s |

Every one of these numbers can be changed in the mod's config, so a pack you are playing may not
match this table. The tooltip on the block always shows the value actually in use.

## Compressed collectors

A <ItemLink id="projectex:basic_compressed_collector" /> is nine collectors of its tier packed
into a single item. It is **not** a block and it does not generate anything on its own: it exists
so that a [power flower](power-flowers.md), which is worth eighteen collectors, can be crafted
without filling a crafting grid twice over.
