# 40 — Weapons, guns, armor, tools and trinkets

**Area code:** GEAR
**Owner:** bregy1
**Status of this file:** draft

## Summary
Players start with vanilla Minecraft tools and weapons. On top of that come weapons from carefully picked existing weapon mods, plus our own inventions. Every item has to earn its place: a unique trait or a clear upgrade, no stat clones. Guns come in tier 3 and are hard to craft. How fighting itself works is in `38-combat.md`.

## Requirements

### GEAR-001 — Vanilla tools and weapons stay
**Status:** approved
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
**Status:** approved
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
- [ ] Guns may use TaCZ's default art as it is, since the pack earns no money (PACK-007). Its license (CC BY-NC-ND) rules out earning money and changing it, so anything we change is our own art instead.

**Open questions:**
**Notes:** From plans.md: "Guns exist, not too easy to craft."

### GEAR-005 — Later guns run on electricity
**Status:** draft
**Priority:** should

**Description:** Later in the game, guns move to electricity: energy weapons charged from the pack's power system.

**Acceptance criteria:**
- [ ] Electric guns exist from a later tier on (which: Q-098), charged with Forge Energy.
- [ ] Each electric gun follows GEAR-003 (unique trait or clear upgrade).

**Open questions:** Q-098
**Notes:** Your words: "Later guns as well." The idea pool's Tesla Coil Gun and Plasma Cutter fit here.

### GEAR-006 — Tools for every ore
**Status:** approved (partly implemented 2026-11-02: district 1's ores: copper tools and the copper tier, iron needs a copper pickaxe, verified by GameTests; gold and later ores come with their districts)
**Priority:** must

**Description:** Every ore material gets its own set of tools. Tools get faster with each new material, but later materials are also harder to mine, so you need the previous tier's tools to get them.

**Acceptance criteria:**
- [ ] Every ore material, vanilla and modded, has a full tool set (pickaxe, axe, shovel, hoe), including ones vanilla lacks, like copper.
- [ ] Tool tiers are ordered by material, with speed and durability scaled to match (e.g. copper between stone and iron; gold rebalanced to fit its place). Exact order and values come with balancing.
- [x] Each tool tier digs faster than the one before. (Wood to iron so far.)
- [x] Each new ore needs at least the previous tier's pickaxe to mine. (Copper a stone one, iron a copper one.)
- [ ] New ores added later (WORLD-010) come with their own tools.

**Open questions:**
**Notes:** Answer to Q-112: "Just all, scale accordingly." Your words: "We should literally add tools for every ore too! So tools get faster and faster after time. But materials also harder!" District 1's order (my starting values, tuned with balancing): wood, stone, copper, iron. Copper sits between stone and iron: 180 uses, mining speed 5, attack bonus 1.5 (stone: 131, 4, 1; iron: 250, 6, 2). Iron ore needs a copper pickaxe; copper ore a stone one. Copper tools unlock with the Iron stage, together with copper itself (Q-125).

### GEAR-007 — Tier 1 weapons
**Status:** implemented 2026-11-02 (all seven weapons, verified by GameTests and by really swinging, throwing and shooting in the client smoke test; placeholder icons; quests that unlock them come with the story)
**Priority:** must

**Description:** Chapter 1 gets seven weapons from the idea pool below, each with its own trait. They unlock through the Chapter 1 story (PROG-003).

**Acceptance criteria:**
- [x] Nail Bat: hits cause bleeding (damage over a few seconds).
- [x] Shovel: heavy swing knocks enemies back and briefly stuns them; still digs. (Called the Heavy Shovel, since vanilla has shovels.)
- [x] Pitchfork: long reach; hits two enemies standing in a line.
- [x] Frying Pan: held in the offhand, blocks one hit like a weak shield, with a loud "bong".
- [x] Moonshine Molotov: thrown, leaves a burning patch that blocks a path for a few seconds.
- [x] Pipe Bomb: thrown, beeps and lures zombies to it before exploding (counts as loud noise, ENEMY-006).
- [x] Slingshot: silent, cheap gravel ammo; a shot can make noise somewhere to distract enemies.
- [x] The Pipe Bomb and the Moonshine Molotov never damage blocks: no crater, and the molotov's flames never light blocks or spread (answer to Q-142).

**Open questions:**
**Notes:** Decided 2026-11-01 (answer to Q-064 for tier 1). Numbers come with balancing. Starting values (my proposals, tuned with balancing), against an iron sword's 6 damage and 1.6 attacks per second. Nail Bat: 5 damage, 1.4 attacks per second, Bleeding for 4 seconds (1 damage per second, doesn't stack). Shovel: 4 damage, 1.0 attacks per second, strong knockback, stuns for 1 second (Slowness IV), still digs like a stone shovel. Pitchfork: 4.5 damage, 1.2 attacks per second, reach +1.5 blocks, also hits one enemy behind the target. Frying Pan: offhand, blocks one hit every 5 seconds like a shield, with a bong that counts as noise (about 12 blocks). Moonshine Molotov: 8-block throw, a 3-block burning patch for 5 seconds, 2 damage per second plus fire; flames never light blocks. Pipe Bomb: 8-block throw, beeps for 4 seconds and draws zombies within 24 blocks, then explodes for 12 damage in 4 blocks (no block damage). Slingshot: gravel ammo, 3 damage at full draw, silent; a shot landing makes noise (about 12 blocks) that draws zombies. How players get them (my proposal): the Nail Bat, Heavy Shovel and Pitchfork unlock together through the Scavenger Weapons stage, from a Chapter 1 quest, and craft from flint, wood, stone and string, because iron is locked at first. The Moonshine Molotov and Slingshot are in that stage too, along with glass bottles. The Frying Pan and Pipe Bomb need iron, so they come with the Iron stage, along with gunpowder.

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

## Trinket ideas

*Trinkets come from Curios slots (CMBT-003). Ideas collected here until the trinket list is specified. The Chapter 1 set is designed by Claude as proposals you can veto (Q-141).*

- **Effect reveal** (your idea, answer to Q-134): shows the status effects enemies carry, for example which zombies have Sunburn (ENEMY-002) and will burn at sunrise. Found later in the game.

### Chapter 1 trinkets (proposals, Q-141)

*My designs around what the spec already needs; veto or change anything. Numbers are starting values. They're found in loot chests or given by quests (STORY) and stay on the allow-list, since Chapter 1 has no stage that unlocks them.*

| Trinket | Slot | Effect | Why |
|---|---|---|---|
| Granny's Rabbit Foot | Charm | At night you deal 10% more damage to enemies and take 10% less from them | The key item from ENEMY-003; a Chapter 1 quest reward |
| Jogging Headband | Head | +10% movement speed | The speed trinket from ENEMY-001, for outrunning zombies |
| Worn Spurs | Belt | Your horse runs 15% faster while you ride it | TRANS-002 |
| Lucky Horseshoe | Necklace | Your horse jumps 20% higher while you ride it | TRANS-002 |
| Spare Clip | Belt | +5 shots on the downed pistol | SURV-001 |
| Brass Knuckles | Hands | +1 attack damage | A plain early power boost |
| Thick Flannel | Body | +2 armor | A plain early defense boost |

Where they are (my proposal): since every structure exists once, each trinket has one home. Thick Flannel in the cabin, Lucky Horseshoe and Brass Knuckles at Earl's Salvage, Spare Clip at Gas-N-Guns, Jogging Headband at Camp Tarp, Worn Spurs at The Stranded Hauler, all guaranteed in that place's chest. Granny's Rabbit Foot stays a quest reward. Loot is personal (WORLD-011), so everyone in a team can get their own.
