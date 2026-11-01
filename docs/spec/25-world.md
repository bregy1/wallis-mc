# 25 — World and districts

**Area code:** WORLD
**Owner:** bregy1
**Status of this file:** draft

## Summary
The world keeps the vanilla feel: generated terrain you can explore, mine and build in. It's split into districts. Each district is its own size-limited dimension with its own biomes, enemies and tone, and every structure in it is placed on purpose, exactly once. Districts open up through the story, and players travel between them by choosing a district on a map at a safe spot, or by hopping from safe spot to safe spot. Key structures are built from special unminable blocks that look different from everything else, so players can see what's protected. The End is gone; the Nether becomes a scary underworld that opens near the end of Earth.

## Requirements

### WORLD-001 — Vanilla-style terrain you can explore and mine
**Status:** draft
**Priority:** must

**Description:** Inside a district, terrain is generated in vanilla style. Players explore, mine and build as in normal Minecraft.

**Acceptance criteria:**
- [ ] District terrain is generated (not hand-built), in vanilla style.
- [ ] Players can mine and place blocks as in vanilla, except for protected structures (WORLD-006).

**Open questions:** Q-068
**Notes:** Your words: "I still want to keep exploring and vanilla theme... The ground can still be mined."

### WORLD-002 — Districts are size-limited dimensions
**Status:** draft
**Priority:** must

**Description:** Each district is its own dimension with a size limit. It has its own biomes, enemies (PROG-007) and tone (STORY-009).

**Acceptance criteria:**
- [ ] Each district is a separate dimension.
- [ ] Each district is 2,000 x 2,000 blocks, and players can't go past its edge (size may change later).
- [ ] Each district defines its own biomes, enemy spawns and tone.

**Open questions:** Q-065, Q-066
**Notes:** Decided 2026-11-01. Chosen for control, clean size limits and performance (districts nobody is in don't need to run). Downside: travel feels a bit more "level-based" than one open world.

### WORLD-003 — Districts open through the story
**Status:** draft
**Priority:** must

**Description:** A district becomes available when the story opens it. Players actively choose to go there from a map.

**Acceptance criteria:**
- [ ] Earth has four districts, one per tier (PROG-008).
- [ ] Each district has a story condition that opens it for a team.
- [ ] At a safe spot, players can open a map of districts and travel to any district their team has opened.
- [ ] Districts that aren't open yet are visible on the map but can't be selected.

**Open questions:** Q-065
**Notes:** Your words: "I still want the player to actively change to a district on the map."

### WORLD-004 — Travel between safe spots
**Status:** draft
**Priority:** must

**Description:** Players can travel from one safe spot (SURV-006) to another.

**Acceptance criteria:**
- [ ] From a safe spot, a player can travel to any other safe spot their team has visited.
- [ ] Travel is free.
- [ ] Travel isn't possible while in combat (hit or attacking in the last 10 seconds).

**Open questions:**
**Notes:** Your words: "safe spot to other safe spot travel is possible."

### WORLD-005 — Every structure is unique
**Status:** draft
**Priority:** must

**Description:** Every structure in a district is placed on purpose, exactly once. Nothing repeats, not even small things like wrecked cars or ruined houses.

**Acceptance criteria:**
- [ ] Each structure exists exactly once, at a defined place in a defined district.
- [ ] No structure is generated randomly or more than once.
- [ ] Vanilla structures (villages, mineshafts, dungeons, temples, strongholds…) don't generate.

**Open questions:**
**Notes:** Decided 2026-11-01: "Everything unique." Chosen for the strongest sense of place; it means a lot of build work.

### WORLD-006 — Protected structures use unminable blocks
**Status:** draft
**Priority:** must

**Description:** Key structures are built from several special block types that players can't mine. These blocks look clearly different from all vanilla blocks, so players can see at a glance which structures are protected. Other structures and the ground stay minable.

**Acceptance criteria:**
- [ ] There are several unminable block types (names, looks and rules: Q-069).
- [ ] They can't be broken in survival mode.
- [ ] Each one is visually distinct from every vanilla block.
- [ ] Key structures use them; ordinary structures and terrain don't.

**Open questions:** Q-069
**Notes:** Your words: "add several unminable materials for the player which connect to proper structures so it is visible."

### WORLD-007 — No End
**Status:** draft
**Priority:** must

**Description:** The End dimension is removed from the game.

**Acceptance criteria:**
- [ ] The End can't be reached in survival.
- [ ] Items that only come from the End are handled as defined in Q-076.

**Open questions:** Q-076
**Notes:** Your words: "Get rid of the end!"

### WORLD-008 — The Nether: a scary underworld
**Status:** draft
**Priority:** must

**Description:** The Nether stays, overhauled into a different, scary underworld: its own dimension, medium-hard, with running skeletons and other evil creatures. Players can't go there early; it opens close to building the rocket. It provides important last materials for the rocket, including something for the fuel.

**Acceptance criteria:**
- [ ] The Nether is a separate dimension that can't be entered before the Launch tier (PROG-008) (how players get in: Q-075).
- [ ] It's harder than the early districts (exact difficulty: balancing spec).
- [ ] It has new, scary creatures, including skeletons that run.
- [ ] At least one material needed for the rocket fuel comes only from the Nether.

**Open questions:** Q-075
**Notes:** Your words: "The nether can be a nice underworld overhaul... It definitely sticks to providing some important last materials for the rocket! Something for the fuel!!" Decided 2026-11-01: we build our own Nether in `wallis_ultimate` (PACK-005), because Incendium can only be used unmodified and clashes with WORLD-005 and GEAR-003.

### WORLD-009 — No negative depth
**Status:** draft
**Priority:** should

**Description:** The underground stops at Y 0, so there are no negative layers. Upward, the normal build height stays.

**Acceptance criteria:**
- [ ] The lowest block in a district is at Y 0; nothing generates or can be placed below it.
- [ ] Players can build up to vanilla's normal limit (Y 320).

**Open questions:**
**Notes:** Decided 2026-11-01 (answer to Q-073). Vanilla 1.20.1 runs from Y -64 to 320. Districts need their own terrain settings for this, and ores that vanilla places below Y 0 (e.g. diamonds) have to move up (WORLD-010).

### WORLD-010 — Ores are set per district and balanced with progression
**Status:** draft
**Priority:** must

**Description:** Ore generation is adjusted from vanilla. Each district defines which ores it has, how many, and at what depth, balanced together with the tier the district belongs to.

**Acceptance criteria:**
- [ ] Each district has its own ore settings (ore types, amounts, depths).
- [ ] Ore settings are part of the balancing spec, alongside locked items (PROG-002) and tiers.

**Open questions:** Q-074
**Notes:** Your question "we have to balance that in as well?": yes. What a district can mine paces progression just as much as what's locked.

### WORLD-011 — Personal loot chests
**Status:** draft
**Priority:** must

**Description:** Loot chests in structures are personal: every player gets their own contents from each chest, so nobody misses out because a teammate opened it first.

**Acceptance criteria:**
- [ ] Each player who opens a loot chest sees their own contents, rolled for them.
- [ ] What one player takes doesn't change what another player finds in the same chest.
- [ ] Whether loot chests ever refill follows Q-106.

**Open questions:** Q-106
**Notes:** Your words: "please personalise loot chests".

### WORLD-012 — Home bases
**Status:** draft
**Priority:** must

**Description:** The team builds its first home in district 1 (the "create first home" quest). After that, teams may build bases in any district they've opened. Each player's home is wherever their bed is (SURV-005).

**Acceptance criteria:**
- [ ] The first home quest takes place in district 1.
- [ ] Teams can build in any opened district.
- [ ] A player's horde night (ENEMY-006) attacks the base around that player's bed. When team members are online together, it attacks the team's shared base: the bed most of them use.

**Open questions:**
**Notes:** Decided 2026-11-01 (answers to Q-066, Q-107). Since every structure exists only once (WORLD-005), personal loot keeps exploring worthwhile for every team member. Candidate mod: Lootr (license still to check).
