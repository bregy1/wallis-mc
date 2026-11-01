# 40 — Weapons, guns, armor, tools and trinkets

**Area code:** GEAR
**Owner:** bregy1
**Status of this file:** draft

## Summary
Players start with vanilla Minecraft tools and weapons. On top of that come weapons from carefully picked existing weapon mods, plus our own inventions. Every item has to earn its place: a unique trait or a clear upgrade, no stat clones. Guns come in tier 3 and are hard to craft. How fighting itself works is in `38-combat.md`.

## Requirements

### GEAR-001 — Vanilla tools and weapons stay
**Status:** draft
**Priority:** must

**Description:** All vanilla tools and weapons remain in the game and work as in vanilla.

**Acceptance criteria:**
- [ ] Vanilla swords, axes, pickaxes, shovels, hoes, bows, crossbows and tridents exist and work as in vanilla (subject to locking, PROG-002).

**Open questions:**
**Notes:** Your words: "The basic tools of course of minecraft."

### GEAR-002 — Weapons from existing mods, curated
**Status:** draft
**Priority:** must

**Description:** Melee and ranged weapons from existing weapon mods are used where they fit. Only the weapons that pass GEAR-003 make it into the pack.

**Acceptance criteria:**
- [ ] Each weapon mod in the pack covers a weapon category no other mod in the pack covers (pillar "curated, not piled").
- [ ] Weapons from those mods that fail GEAR-003 are removed or disabled.

**Open questions:** Q-064
**Notes:** Candidates to check for Forge 1.20.1 availability and license: TaCZ (guns), Simply Swords and Spartan Weaponry (melee). Gun mods usually bring dozens of guns; we'd keep only a few.

### GEAR-003 — Every item earns its slot
**Status:** draft
**Priority:** must

**Description:** Every weapon and armor piece either has a unique trait or is a clear upgrade over what came before. No items with near-identical stats.

**Acceptance criteria:**
- [ ] Each weapon and armor piece in the gear list has a written unique trait, or names the item it upgrades.
- [ ] No two items in the same tier differ only by small stat changes.

**Open questions:**
**Notes:** From features.md: "We dont need 10 weapons or armor with all the same stats."

### GEAR-004 — Guns are tier 3 and hard to craft
**Status:** draft
**Priority:** must

**Description:** Guns become available in the Gunsmith tier and are not easy to craft. The downed pistol (SURV-001) is the only exception, since it exists only while a player is downed.

**Acceptance criteria:**
- [ ] No craftable gun is unlocked before the Gunsmith tier (PROG-008).
- [ ] Gun recipes need materials that take real effort to get (exact costs: later, with the balancing spec).

**Open questions:**
**Notes:** From plans.md: "Guns exist, not too easy to craft."

## Weapon idea pool (proposals)

*All of these are my proposals, mixing ideas from other games. Nothing here is decided. Pick, change or drop, and the keepers become requirements. Tiers follow PROG-008.*

| Name | Tier | Type | Unique trait | Inspired by |
|---|---|---|---|---|
| Nail Bat | 1 Scavenger | Melee | Hits cause bleeding (damage over a few seconds) | The Walking Dead, Dead Rising |
| Shovel | 1 Scavenger | Melee and tool | Heavy swing knocks enemies back and briefly stuns them; still digs | Dead Rising |
| Pitchfork | 1 Scavenger | Melee | Long reach; hits two enemies standing in a line | Farm horror films |
| Frying Pan | 1 Scavenger | Melee | Held in the offhand, blocks one hit like a weak shield, with a loud "bong" | Left 4 Dead 2 |
| Moonshine Molotov | 1 Scavenger | Throwable | Leaves a burning patch that blocks a path for a few seconds | Left 4 Dead |
| Pipe Bomb | 1 Scavenger | Throwable | Beeps and lures zombies to it before exploding | Left 4 Dead |
| Slingshot | 1 Scavenger | Ranged | Silent, cheap gravel ammo; a shot can make noise somewhere to distract enemies | The Last of Us, Far Cry |
| Machete | 2 Settler | Melee | Fast; chance to instantly kill weak zombies | Dead Island |
| Chainsaw | 2 Settler | Melee | Cuts through hordes, runs on fuel, but it's loud and attracts zombies | Left 4 Dead 2, Evil Dead |
| Compound Bow | 2 Settler | Ranged | Silent; arrows can be picked back up; bonus damage on enemies that haven't noticed you | The Last of Us |
| Bile Jar | 2 Settler | Throwable | The splashed enemy becomes the target of every zombie nearby | Left 4 Dead 2 |
| Weapon tinkering | 2 Settler | System | Combine a weapon with parts at a workbench, e.g. Nail Bat + Moonshine = Flaming Nail Bat | Dead Rising, Dying Light |
| "Ol' Betsy" double-barrel | 3 Gunsmith | Gun | Huge close-range spread, 2 shells, slow reload | Redneck classic |
| Hunting Rifle | 3 Gunsmith | Gun | Long range; the bullet goes through several enemies in a line | Left 4 Dead |
| Revolver | 3 Gunsmith | Gun | 6 shots, high damage per shot | Westerns, Hunt: Showdown |
| Scrap SMG | 3 Gunsmith | Gun | Very fast fire, inaccurate, cheap scrap ammo | Fallout pipe guns |
| Nail Gun | 3 Gunsmith | Gun and tool | Pins small enemies in place for a moment | Dead Rising, Fallout |
| Flare Gun | 3 Gunsmith | Gun | Sets targets on fire and lights up an area at night | Hunt: Showdown |
| Plasma Cutter | 4 Launch | Tech | Cuts through armor; doubles as a fast mining tool | Dead Space |
| Tesla Coil Gun | 4 Launch | Tech | Lightning jumps between grouped enemies | Fallout, Ratchet & Clank |
| Rocket-Fuel Flamethrower | 4 Launch | Tech | Cone of fire, fueled by rocket fuel from the launch project | Fallout, Wolfenstein |

Magic (the special book) comes later, on the magic planet (STORY-012).
