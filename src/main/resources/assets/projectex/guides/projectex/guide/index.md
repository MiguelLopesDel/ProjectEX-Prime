---
navigation:
  title: ProjectEX Prime
  icon: basic_collector
  position: 0
---

# ProjectEX Prime

ProjectEX extends ProjectE. Where a Transmutation Table lets you turn what you already have into
EMC, this mod is about producing EMC on its own, in quantities that grow by a factor of four with
every tier, and about moving that EMC between the world and your personal balance.

Everything here needs ProjectE installed and works on top of its EMC values.

## Where to start

Place a <ItemLink id="projectex:basic_collector" /> next to a
<ItemLink id="projectex:basic_relay" />, and the relay next to a
<ItemLink id="projectex:energy_link" />. That is the whole loop: the collector makes EMC, the
relay carries it, the link puts it in your Transmutation Table.

Once that works, the rest of the mod is the same three ideas at larger and larger numbers.

## Pages

<SubPages />

## A note on timing

Collectors, relays, links and power flowers all act **once a second**, not once a tick. That is a
deliberate difference from most EMC generation: twenty times fewer operations, so a base with
hundreds of these blocks does not drag the server down. The rates shown on the tooltips are
already per second, so nothing is lost by it.
