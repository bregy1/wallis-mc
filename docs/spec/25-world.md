# 25 — World and districts

**Area code:** WORLD
**Owner:** bregy1
**Status of this file:** draft

## Summary
The world keeps the vanilla feel: generated terrain you can explore, mine and build in. It's split into districts. Each district is its own size-limited dimension with its own biomes, enemies and tone, and every structure in it is placed on purpose, exactly once. Districts open up through the story, and players travel between them by choosing a district on a map at a safe spot, or by hopping from safe spot to safe spot. Key structures are built from special unminable blocks that look different from everything else, so players can see what's protected. The End is gone; the Nether becomes a scary underworld that opens near the end of Earth.

## Requirements

### WORLD-001 — Vanilla-style terrain you can explore and mine
**Status:** implemented (district 1, verified 2026-11-02)
**Priority:** must

**Description:** Inside a district, terrain is generated in vanilla style. Players explore, mine and build as in normal Minecraft.

**Acceptance criteria:**
- [x] District terrain is generated (not hand-built), in vanilla style.
- [x] Players can mine and place blocks as in vanilla, except for protected structures (WORLD-006).

**Open questions:** Q-068
**Notes:** Your words: "I still want to keep exploring and vanilla theme... The ground can still be mined."

### WORLD-002 — Districts are size-limited dimensions
**Status:** approved (partly implemented: district 1 dimension and border, verified 2026-11-02)
**Priority:** must

**Description:** Each district is its own dimension with a size limit. It has its own biomes, enemies (PROG-007) and tone (STORY-009).

**Acceptance criteria:**
- [x] Each district is a separate dimension. (District 1 = the reshaped Overworld.)
- [x] Each district is 2,000 x 2,000 blocks, and players can't go past its edge (size may change later). (District 1.)
- [ ] Each district defines its own biomes, enemy spawns and tone.

**Open questions:** Q-065, Q-066
**Notes:** Decided 2026-11-01. Chosen for control, clean size limits and performance (districts nobody is in don't need to run). Downside: travel feels a bit more "level-based" than one open world. Implementation (2026-11-02): district 1 is the vanilla Overworld, reshaped (Y 0–320, 2,000-block border set once on a fresh world, no End in the world preset, only the default world type selectable). Districts 2–4 will be their own dimensions and need our own border sync (vanilla only sends the Overworld's border to players).

### WORLD-003 — Districts open through the story
**Status:** approved
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
**Status:** approved
**Priority:** must

**Description:** Players can travel from one safe spot (SURV-006) to another.

**Acceptance criteria:**
- [ ] From a safe spot, a player can travel to any other safe spot their team has visited.
- [ ] Travel is free.
- [ ] Travel isn't possible while in combat (hit or attacking in the last 10 seconds).

**Open questions:**
**Notes:** Your words: "safe spot to other safe spot travel is possible."

### WORLD-005 — Every structure is unique
**Status:** approved (partly implemented: vanilla structures removed, verified 2026-11-02)
**Priority:** must

**Description:** Every structure in a district is placed on purpose, exactly once. Nothing repeats, not even small things like wrecked cars or ruined houses.

**Acceptance criteria:**
- [ ] Each structure exists exactly once, at a defined place in a defined district.
- [ ] No structure is generated randomly or more than once.
- [x] Vanilla structures (villages, mineshafts, dungeons, temples, strongholds…) don't generate. (Overworld; Nether structures stay until WORLD-008.)

**Open questions:**
**Notes:** Decided 2026-11-01: "Everything unique." Chosen for the strongest sense of place; it means a lot of build work. Implementation (2026-11-02): Overworld structure tags are emptied, and dungeons, fossils, desert wells and amethyst geodes are removed. Nether structures stay vanilla until the Nether overhaul (WORLD-008), because fortresses are the only source of blaze rods for brewing (Q-118). Several teams (answer to Q-136, 2026-11-02): places stay single and work MMO-style. An NPC can appear in several places, and every team gets its own quests and story state there. Usually there is one team per server. Players start at the rednecks' cabin at world spawn (Q-137).

### WORLD-006 — Protected structures use unminable blocks
**Status:** approved (partly implemented: the three blocks, verified by GameTests 2026-11-02; structures use them once structures exist)
**Priority:** must

**Description:** Key structures are built from several special block types that players can't mine. These blocks look clearly different from all vanilla blocks, so players can see at a glance which structures are protected. Other structures and the ground stay minable.

**Acceptance criteria:**
- [x] There are three unminable block types: Reinforced Concrete (human and military buildings), Riveted Steel (bunkers, launch sites) and Alien Alloy (cult and alien sites).
- [x] They can't be broken in survival mode and are immune to explosions. (Also wither/dragon immune, can't be pushed by pistons.)
- [x] Players can never obtain them; for their own builds they get the alien hardening later (ENEMY-005). (No loot, no recipe; block items only in creative for building structures.)
- [x] Each one is visually distinct from every vanilla block. (Placeholder textures for now; real art later.)
- [ ] Key structures use them; ordinary structures and terrain don't.

**Open questions:**
**Notes:** Your words: "add several unminable materials for the player which connect to proper structures so it is visible." Implementation (2026-11-02): blocks `wallis_ultimate:reinforced_concrete`, `riveted_steel`, `alien_alloy`; GameTests `cannotbeminedorlooted`, `survivestnt`, `norecipemakesthem` pass.

### WORLD-007 — No End
**Status:** approved (partly implemented: no End dimension, verified 2026-11-02)
**Priority:** must

**Description:** The End dimension is removed from the game.

**Acceptance criteria:**
- [x] The End can't be reached in survival.
- [ ] Shulker boxes stay in the game, but are very rare: each player gets one shulker box per planet, as a quest reward.
- [ ] Elytra, end crystals and chorus fruit are removed.

**Open questions:**
**Notes:** Your words: "Get rid of the end!" Decided 2026-11-01 (answer to Q-076). Elytra as a very-late-game reward is parked as an idea (roadmap). Shulker boxes decided 2026-11-01 (answer to Q-111): "has to be quest reward... early we might get one shulker per player per planet. So rare af." A violet alien planet with shulker drops is parked for later.

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
**Status:** implemented (district 1, verified 2026-11-02)
**Priority:** should

**Description:** The underground stops at Y 0, so there are no negative layers. Upward, the normal build height stays.

**Acceptance criteria:**
- [x] The lowest block in a district is at Y 0; nothing generates or can be placed below it.
- [x] Players can build up to vanilla's normal limit (Y 320).

**Open questions:**
**Notes:** Decided 2026-11-01 (answer to Q-073). Vanilla 1.20.1 runs from Y -64 to 320. Districts need their own terrain settings for this, and ores that vanilla places below Y 0 (e.g. diamonds) have to move up (WORLD-010).

### WORLD-010 — Ores are set per district and balanced with progression
**Status:** approved (partly implemented: district 1 ores, verified 2026-11-02)
**Priority:** must

**Description:** Ore generation is adjusted from vanilla. Each district only has the ores its tier needs; later-tier ores appear only in later districts. New ores keep coming with later districts and planets, from mods or our own.

**Acceptance criteria:**
- [x] Each district has its own ore settings (ore types, amounts, depths). (District 1.)
- [x] A district's ores match its tier; ores for later tiers don't appear in earlier districts. (District 1: coal, copper, iron only.)
- [ ] Starting point (proposal, tuned with balancing): district 1 has coal, copper and iron; district 2 adds gold, redstone and lapis; district 3 adds rare diamonds; district 4 adds what the rocket needs.
- [ ] Later districts and planets can add new ores, from mods or our own, without changing earlier districts. (Not yet: district 1's ores are added to the shared vanilla biomes, so district 2 needs a per-dimension ore filter.)

**Open questions:**
**Notes:** Your question "we have to balance that in as well?": yes. What a district can mine paces progression just as much as what's locked. Decided 2026-11-01 (answer to Q-074): "make it we can go on and have more ores after district." Tools for every ore: GEAR-006. Implementation (2026-11-02): district 1 is the vanilla Overworld; vanilla coal and upper/small iron stay as they are; middle iron moved to Y 0–64 and copper to Y 0–112 (vanilla amounts); gold, redstone, diamond, lapis, emerald and large copper removed. Values to tune in playtesting.

### WORLD-011 — Personal loot chests
**Status:** approved
**Priority:** must

**Description:** Loot chests in structures are personal: every player gets their own contents from each chest, so nobody misses out because a teammate opened it first.

**Acceptance criteria:**
- [ ] Each player who opens a loot chest sees their own contents, rolled for them.
- [ ] What one player takes doesn't change what another player finds in the same chest.
- [ ] Loot chests never refill: each player loots each chest once.

**Open questions:**
**Notes:** Your words: "please personalise loot chests".

### WORLD-012 — Home bases
**Status:** approved
**Priority:** must

**Description:** The team builds its first home in district 1 (the "create first home" quest). After that, teams may build bases in any district they've opened. Each player's home is wherever their bed is (SURV-005).

**Acceptance criteria:**
- [ ] The first home quest takes place in district 1.
- [ ] Teams can build in any opened district.
- [ ] A player's horde night (ENEMY-006) attacks the base around that player's bed. When team members are online together, it attacks the team's shared base: the bed most of them use.

**Open questions:**
**Notes:** Decided 2026-11-01 (answers to Q-066, Q-107). Since every structure exists only once (WORLD-005), personal loot keeps exploring worthwhile for every team member. Candidate mod: Lootr (license still to check).

### WORLD-013 — Apocalypse props
**Status:** approved
**Priority:** should

**Description:** Our own decorative blocks for structures, for the apocalypse look no pack mod offers (answer to Q-154). Art by Claude as placeholders.

**Acceptance criteria:**
- [ ] Sandbags, barbed wire, oil drums, tarps and supply crates exist as blocks and are used in safe spots and other structures.
- [ ] Barbed wire hurts and slows anything walking through it, including players and zombies.
- [ ] Wrecked cars are built from several blocks, so they can be placed in any structure.
- [ ] Players can't craft or use them unless a stage unlocks them; they're for structures first (PROG-002).

**Open questions:**
**Notes:** Your words on safe spots (Q-146): "We definitely want to use some world ending theme modpack items for that too! deko and detail is impoirtant." Which props players may build with themselves comes with the base spec.
