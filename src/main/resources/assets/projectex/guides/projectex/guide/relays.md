---
navigation:
  parent: index.md
  title: Relays
  icon: basic_relay
  position: 20
item_ids:
  - projectex:basic_relay
  - projectex:dark_relay
  - projectex:red_relay
  - projectex:magenta_relay
  - projectex:pink_relay
  - projectex:purple_relay
  - projectex:violet_relay
  - projectex:blue_relay
  - projectex:cyan_relay
  - projectex:green_relay
  - projectex:lime_relay
  - projectex:yellow_relay
  - projectex:orange_relay
  - projectex:white_relay
  - projectex:fading_relay
  - projectex:final_relay
---

# Relays

A relay is a pipe for EMC with a bonus attached. Once a second it hands what it holds to any
adjacent block that accepts EMC, up to its tier's transfer rate.

<RecipeFor id="projectex:basic_relay" />

## The bonus is the point

A relay is not just plumbing. Every time a <ItemLink id="projectex:basic_collector" /> pushes into
it, the relay adds its own **bonus** on top of what arrived. EMC that passes through a relay is
worth more than EMC that went straight to its destination.

So the usual shape is not *collector to link*, it is *collector to relay to link*. Six collectors
around one relay all pay that relay its bonus.

| Tier | Bonus | Max transfer |
| --- | --- | --- |
| Basic [MK1] | 1/s | 64/s |
| Dark Matter [MK2] | 3/s | 192/s |
| Red Matter [MK3] | 10/s | 640/s |
| Magenta [MK4] | 40/s | 2,560/s |
| Pink [MK5] | 150/s | 10,240/s |
| Purple [MK6] | 750/s | 40,960/s |
| Violet [MK7] | 3,750/s | 163,840/s |
| Blue [MK8] | 15,000/s | 655,360/s |
| Cyan [MK9] | 60,000/s | 2,621,440/s |
| Green [MK10] | 240,000/s | 10,485,760/s |
| Lime [MK11] | 960,000/s | 41,943,040/s |
| Yellow [MK12] | 3,840,000/s | 167,772,160/s |
| Orange [MK13] | 15,360,000/s | 671,088,640/s |
| White [MK14] | 61,440,000/s | 2,684,354,560/s |
| Fading [MK15] | 245,760,000/s | 10,737,418,240/s |
| The Final Relay | 1,000,000,000,000/s | unlimited |

Like the collectors, every number here is config-driven and the block's tooltip shows what is
actually in use.

## Relays do not feed relays

A relay skips other relays when it looks for somewhere to send EMC. Two relays facing each other
would otherwise pass the same EMC back and forth, each charging a bonus for it, which is an
infinite generator made of two blocks.

If you need to cover distance, that is what the transfer rate is for on a single relay, or use a
higher tier. Chaining relays is not the intended shape and will not work.

## Mixing with ProjectE

ProjectE's own relays accept EMC from these collectors and get their bonus too. The reverse also
holds: a ProjectE collector will feed one of these relays. The two mods' blocks are
interchangeable in a chain, so an existing setup does not have to be torn out.
