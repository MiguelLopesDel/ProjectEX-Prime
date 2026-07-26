---
navigation:
  parent: index.md
  title: Stars
  icon: magnum_star_ein
  position: 55
item_ids:
  - projectex:magnum_star_ein
  - projectex:magnum_star_zwei
  - projectex:magnum_star_drei
  - projectex:magnum_star_vier
  - projectex:magnum_star_sphere
  - projectex:magnum_star_omega
  - projectex:colossal_star_ein
  - projectex:colossal_star_zwei
  - projectex:colossal_star_drei
  - projectex:colossal_star_vier
  - projectex:colossal_star_sphere
  - projectex:colossal_star_omega
  - projectex:final_star
  - projectex:final_star_shard
---

# Magnum, Colossal and Final Stars

ProjectE's Klein Stars stop at the Omega. These carry on from there.

## Magnum and Colossal

Twelve stars, six of each, and every one holds four times the one before it. A Magnum Ein is four
Klein Star Omegas; each step up is four of the step below.

<RecipeFor id="projectex:magnum_star_ein" />

They behave exactly like a Klein Star: hold them and they charge from a
<ItemLink id="projectex:basic_relay" /> or from ProjectE's own, and anything that draws on a Klein
Star draws on these. The durability bar shows how full one is.

Crafting four stars into the next one up keeps whatever they were holding, so there is never a
reason to empty them first. Four full stars make one full star, because each step up holds exactly
four times as much.

| Star | Capacity |
| --- | --- |
| Magnum Ein | 204,800,000 |
| Magnum Zwei | 819,200,000 |
| Magnum Drei | 3,276,800,000 |
| Magnum Vier | 13,107,200,000 |
| Magnum Sphere | 52,428,800,000 |
| Magnum Omega | 209,715,200,000 |
| Colossal Ein | 838,860,800,000 |
| Colossal Zwei | 3,355,443,200,000 |
| Colossal Drei | 13,421,772,800,000 |
| Colossal Vier | 53,687,091,200,000 |
| Colossal Sphere | 214,748,364,800,000 |
| Colossal Omega | 858,993,459,200,000 |

## The Final Star

<RecipeFor id="projectex:final_star" />

It is always full and never empties. Anything that pulls EMC from a Klein Star can pull from this
one forever, and nothing can charge it, because there is nothing left to charge.

### On a pedestal

Put it on a Dark Matter Pedestal and it stops being a battery and becomes a duplicator. Once a
second it takes one of the items lying on the pedestal and pushes a full stack of it into an
inventory placed against any side of the pedestal except the top.

Three config options govern it, and a pack may well have changed them:

- `final_star_update_interval` — how often it copies, in ticks. **Set it to 0 to turn copying off
  entirely.**
- `final_star_copy_any_item` — off means it only copies items that have an EMC value.
- `final_star_copy_nbt` — off, by default, means copies come out plain: damage reset and tags
  stripped, except the tags ProjectE itself charges EMC for.
