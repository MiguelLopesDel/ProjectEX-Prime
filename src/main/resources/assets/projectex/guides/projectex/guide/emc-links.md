---
navigation:
  parent: index.md
  title: EMC Links
  icon: personal_link
  position: 40
item_ids:
  - projectex:energy_link
  - projectex:personal_link
  - projectex:refined_link
  - projectex:compressed_refined_link
---

# EMC Links

A link is the door between the world and your personal EMC balance. EMC pushed into it from a
collector or a relay lands in your Transmutation Table, wherever you are.

Every link has an owner, set when it is placed, and everything it does is done to that owner's
balance.

There are four, and they differ only in how many item slots they have.

## Basic Energy EMC Link

<RecipeFor id="projectex:energy_link" />

No slots at all. Point a <ItemLink id="projectex:basic_relay" /> at it and the EMC becomes yours.
This is the cheapest way to turn a collector farm into transmutation EMC, and for a long time it
is all you need.

## Personal EMC Link

<RecipeFor id="projectex:personal_link" />

Eighteen input slots and one output slot, on top of everything the energy link does.

## Refined EMC Link

<RecipeFor id="projectex:refined_link" />

One input slot and nine output slots. It also **learns** every item it eats, which the personal
link does not do — feed it something new and it appears in your Transmutation Table.

## Compressed Refined EMC Link

<RecipeFor id="projectex:compressed_refined_link" />

The same as the refined link with fifty-four output slots instead of nine. It is meant for
storage systems: one link can stand in for fifty-four separate infinite chests.

# Selling: the input slots

Anything put in an input slot is eaten once a second and paid for in EMC.

What it pays is the item's **sell value**, the same figure the Transmutation Table would give you,
so ProjectE's covalence loss applies here as well. A link is not a way around the pack's economy.

You can feed the slots by hand, or with a hopper, a pipe, or anything else that can insert items —
the link exposes its slots the way a chest does.

# Buying: the output slots

An output slot does not hold items. It holds a **template**: an item you have chosen, which the
link then produces from your EMC on demand.

**To set one**, pick up the item you want and click it onto an empty output slot. The item is
learned in the process, so you do not need to know it beforehand.

**To clear one**, shift and right click the slot.

The count you see on a template is not stock, it is how many you can currently afford. It rises as
your balance rises and falls as you spend. Taking items out charges you the moment they leave.

A pipe or a storage network sees exactly the same thing: a slot that always has as many as your
EMC covers. That is what makes a link work as an infinite chest in an automated system.

## What a template can be

A template is stored as the plain item. Damage, enchantments and other tags are stripped unless
ProjectE charges EMC for them, so setting a worn or enchanted item as a template gives you the
plain version at the plain price.

If your pack has blocked an item from being learned or from being put in a Condenser, it cannot be
set as a template either.

# While you are logged out

Links keep working. Items in the input slots are still eaten and paid for, and the EMC is written
to your player data, so it is waiting in your Transmutation Table when you come back.

Output slots go quiet while you are away, because nothing is there to ask for items — but a hopper
or a pipe pulling from one will still be served, and still charged.
