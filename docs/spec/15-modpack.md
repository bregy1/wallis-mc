# 15 — Modpack composition

**Area code:** PACK
**Owner:** bregy1
**Status of this file:** draft

## Summary
Wallis Ultimate is a modpack plus our own mod, `wallis_ultimate`. Each system in the pack comes from exactly one mod, chosen for fit, maintenance and license, and everything else is built in our mod. Mods we can't legally modify are used unmodified; mods we may need to change must allow forking. Research was done on 2026-11-01 against Modrinth, CurseForge and the mods' repositories; versions and licenses must be re-checked when the pack is assembled.

## Requirements

### PACK-001 — One mod per system
**Status:** approved
**Priority:** must

**Description:** Every system in the pack (quests, power, guns…) comes from exactly one mod or from our own mod. No overlapping mods.

**Acceptance criteria:**
- [ ] No two mods in the pack cover the same system.
- [ ] Features of included mods that overlap with another system are disabled.

**Open questions:**
**Notes:** Pillar "curated, not piled".

### PACK-002 — License rules
**Status:** approved
**Priority:** must

**Description:** Every mod's license is checked before it goes into the pack. Mods without a forking-friendly license are only ever used unmodified. Forks stay under their original license, as separate jars.

**Acceptance criteria:**
- [ ] Every mod in the pack has its license (code and assets) recorded in the table below.
- [ ] Mods marked "unmodified only" are never forked or changed.
- [ ] Our own mod `wallis_ultimate` is licensed MIT. No GPL or LGPL code is ever copied into it; forks of GPL/LGPL mods keep their license and ship as separate jars.
- [ ] Our mod and every fork are shared as their own jars. Forks credit and link the original and say what changed; GPL/LGPL forks make their source available (PACK-007).
- [ ] `CREDITS.md` lists every mod used, forked or borrowed from, with author, URL and license.

**Open questions:**
**Notes:** Rules from the `minecraft-design` skill. MIT chosen 2026-11-01 (answer to Q-092). With MIT, others may reuse our code, assets and story with credit.

### PACK-003 — No lag
**Status:** approved
**Priority:** must

**Description:** The pack ships with a performance base and avoids mods known to cause lag over time.

**Acceptance criteria:**
- [ ] Embeddium, ModernFix and FerriteCore are in the pack. Entity Culling isn't yet: it was dropped only because its license forbids earning money with it, which no longer applies (PACK-007), so it can come back if you say so.
- [ ] Every new mod is checked for known performance problems before it's added.

**Open questions:**
**Notes:** Pillar "we don't want lag". Entity Culling's license checked 2026-11-01 (Q-094): fine only without monetization, so it was dropped when you chose to join the Rewards Program. Create was rejected partly for this reason (2026-11-01).

### PACK-004 — Core mod list
**Status:** approved (Milestone 1: the mods Chapter 1 needs; other picks stay draft)
**Priority:** must

**Description:** The mods below cover the systems the spec needs. "Unmodified only" means we may include the official jar but never change or fork it.

| System | Pick | License (code / assets) | How we use it | Spec | Alternatives |
|---|---|---|---|---|---|
| Quests | FTB Quests | No license file; treat as unmodified only | Unmodified | PROG-003 | Odyssey Quests (MIT, 1.20.1 build stale since 2024) |
| Teams | FTB Teams | No license file; treat as unmodified only | Unmodified | PROG-006 | Open Parties and Claims (LGPL, no shared progression) |
| Item locking per team | History Stages (6.1.1 for Forge 1.20.1; requires Lootr) | Custom: official jar allowed in packs; no forks or modified copies | Unmodified, configured: individual stages shared with the FTB team; pickup allowed for every locked entry; machines lock only "place", never their GUI; loot and container access allowed; loot replacement off. Our mod adds the rest (PACK-005) | PROG-002 | AStages (ARR, no team scope); Game Stages + Item Stages (drops held items, clashes with PROG-002) |
| Recipe viewer | JEI | MIT | Unmodified | PROG-002 | EMI (MIT) |
| NPCs with dialogue, reply choices and trading | Easy NPC | MIT | Unmodified or fork | STORY-002, STORY-003, ECON-002 | interactEntity (MIT, small project) |
| Cutscenes | ImmersiveCinematics | MIT | Unmodified, plus our glue for team-wide playback | STORY-010 | CMDCam (LGPL-2.1, more manual) |
| Trinket slots | Curios API | LGPL-3.0 | Library dependency | CMBT-003 | None serious |
| Guns | TaCZ | Code GPL-3.0; assets CC BY-NC-ND 4.0 (non-commercial) | Unmodified, with our own small gun pack using only our own art (Q-116) | GEAR-004 | Scorched Guns 2 (GPL-3.0) |
| Melee weapons | Spartan Weaponry (candidate) | Apache-2.0 code; asset terms unverified | Decide together with the weapon picks (Q-064) | GEAR-002 | Simply Swords (custom license) |
| Backpacks | Traveler's Backpack | LGPL-3.0 | Unmodified; integrates with Curios | UTIL | Sophisticated Backpacks (ARR) |
| Day length | Hourglass | LGPL-3.0 | Unmodified; its sleep speed-up is accepted (SURV-005) | SURV-007 | None found |
| Minimap and world map | Xaero's Minimap + World Map | ARR, pack use allowed with credit | Unmodified | WORLD-003 | JourneyMap (ARR, pack terms unclear) |
| Performance | Embeddium, ModernFix, FerriteCore | LGPL-3.0, LGPL-3.0, MIT | Unmodified | PACK-003 | Entity Culling (dropped: no money allowed), Canary (stale) |
| Storage | Tom's Simple Storage | MIT | Unmodified; its recipes locked by tier (PROG-002) | TECH-004 | Applied Energistics 2 (complex, non-commercial textures); Refined Storage (Forge 1.20.1 build stale since 2023) |
| Flying vehicles | Immersive Aircraft | GPL-3.0-only | Trimmed to 1–2 aircraft, armed Warship disabled; electric engines via a small GPL fork as a separate jar, or fuel mapped to a charged battery item (Q-102) | TRANS-003 | Our own aircraft |
| Food | Farmer's Delight | MIT (asset terms unverified) | Unmodified; check its Nourishment buff (Q-103) | SURV-008 | Vanilla food only |
| Diet | A Balanced Diet | LGPL-3.0-or-later | Unmodified; effects set in its data files (Q-105) | SURV-009 | Spice of Life Onion (variety only, LGPL) |
| Personal loot chests | Lootr (0.7.35.94 for Forge 1.20.1) | Code MIT; assets All Rights Reserved; modpack terms not stated (check CurseForge's distribution setting or ask the author) | Unmodified; History Stages requires it anyway. Works automatically for every loot-table container, including our structures' chests | WORLD-011 | Our own per-player loot |
| Mob animations | GeckoLib (4.8.4 for Forge 1.20.1) | MIT | Library dependency | ENEMY-012 | Vanilla's keyframe animations (as used by the Warden) |
| Structure decoration: palisades, beams, supports, braziers, chains | Decorative Blocks (4.1.3 for Forge 1.20.1) | MIT | Unmodified; its items stay locked (Settler stage), used in structures | SURV-006, WORLD-005 | Picked by Claude (Q-148) |
| Structure decoration: furniture for interiors | Another Furniture (3.0.4 for Forge 1.20.1) | Code MIT, assets All Rights Reserved | Unmodified; its items stay locked, used in structures | SURV-006, WORLD-005 | Picked by Claude (Q-148); Macaw's Furniture (ARR), Handcrafted (code MIT, assets ARR) |
| Structure decoration: boarded-up windows and doors | Just Barricades (1.0.0 for Forge 1.20.1) | MIT | Unmodified; "zombies destroy barricades" switched off in its config (regular zombies never damage blocks, ENEMY-005); its items stay locked | SURV-006, ENEMY-005 | Picked by Claude (Q-148) |
| Food values on screen | AppleSkin | The Unlicense (public domain) | Unmodified | SURV-009 | None needed |

**Acceptance criteria:**
- [ ] Every system in the table has one confirmed pick before the pack is assembled.
- [ ] Each pick's Forge 1.20.1 version is recorded when the pack is assembled.

**Open questions:** Q-064, Q-091, Q-093
**Notes:** Picks are recommendations from the 2026-11-01 research; confirm or swap. Tech and power were first decided 2026-11-01 as Energized Power for both; changed 2026-11-03 to our own (answer to Q-159, see PACK-005, TECH-005 and TECH-007).
Dev setup (2026-11-02): all mods above that Chapter 1 needs load in the dev runs (GeckoLib comes with ENEMY-012), at their latest Forge 1.20.1 releases. Those our code works with: FTB Quests 2001.4.22, FTB Teams 2001.3.2, History Stages 6.1.1-1.20.1, Lootr 0.7.35.94, Curios API 5.14.1+1.20.1, JEI 15.74.0.229 (from 15.74.0.230 on, History Stages' JEI fluid patch fails to load and the game crashes as soon as a world loads, checked 2026-11-02). FTB Quests and Teams also need FTB Library (2001.2.13) and Architectury (9.2.14), so the pack needs those two as well; their licenses are not yet checked. Versions live in `gradle.properties`.

### PACK-005 — Built in our own mod
**Status:** approved
**Priority:** must

**Description:** These systems have no fitting mod (wrong behavior, wrong license, or nothing for Forge 1.20.1), so `wallis_ultimate` builds them.

| System | Spec | Why our own |
|---|---|---|
| Locked-item handling on top of History Stages: pickup only into the 27 main slots (stays on the ground if full), locked items in the hotbar or hands move back to the main inventory, grayed-out rendering, and optionally "unlocked by…" info in JEI | PROG-002 | History Stages doesn't do these (checked 2026-11-01, Q-091; it does refuse locked items in the offhand and armor slots itself, found 2026-11-02) and can't be forked; our code uses its API to ask whether an item is locked for a player |
| Districts as dimensions, with per-district world borders | WORLD-002 | Datapack dimensions work, but vanilla only sends the Overworld's border to players (bug MC-213713), and no Forge 1.20.1 mod fixes it |
| District map and safe spot travel | WORLD-003, WORLD-004 | Map mods are ARR and can't be extended |
| Unminable blocks and hardening | WORLD-006, ENEMY-005 | Our own content |
| The Nether overhaul | WORLD-008 | Incendium is unmodified-only and clashes with unique structures and gear curation (your choice, 2026-11-01) |
| Downed state and revive | SURV-001, SURV-002 | No mod has crawling, the downed pistol or enemies ignoring downed players. PlayerRevive (LGPL) can serve as reference |
| Graves | SURV-004 | GraveStone is ARR and its expiry is unverified; a small own system is simpler (proposal) |
| Forward-only roll | CMBT-002 | Combat Roll (GPL-3.0) rolls in any direction; either a small GPL fork as a separate jar or our own code |
| Enemies, specials, bosses, enemy tracker, noise and horde nights | ENEMY-001–011 | Zombie Awareness is ARR and hasn't been updated since 2024 |
| Utility points, player leveling, specialisations | PROG-005, PROG-009, PROG-011 | Our own systems |
| Bed and safe spot rules | SURV-005, SURV-006 | Our own rules |
| Story glue (team cutscene triggers, quest hooks) | STORY | Connects FTB Quests, Easy NPC and ImmersiveCinematics |
| Our invented weapons and the downed pistol | GEAR, SURV-001 | Our own content |
| Garages: store vehicles and hand them out at safe spots | TRANS-004 | Our own building; it has to save and restore Immersive Aircraft vehicles |
| Generators, batteries, wire, pipes and machines | TECH-005, TECH-007 | The tech mods we may legally use bring far more than a few machines, and the closest match can't go in a pack that earns money (research in TECH-005, answer to Q-159) |
| Workers and job tables | WORK-001–003 | Villager Workers is All Rights Reserved and needs Recruits; MineColonies is a whole town-builder |
| Coins | ECON-001 | Just one plain item; Lightman's Currency brings wallets, ATMs and banks we don't need |
| The rocket (built as a structure) and the Moon dimension with low gravity | PROG-010, STORY-017, PLANET | Northstar needs Create; Ad Astra's rocket is likely a crafted item and its assets can't be changed. Decided 2026-11-01 |

**Acceptance criteria:**
- [ ] Each system above is either built in `wallis_ultimate` or moved to a mod by a spec change.

**Open questions:**
**Notes:**

### PACK-007 — Shared with friends, not published
**Status:** approved 2026-11-04 (answer to Q-175)
**Priority:** must

**Description:** The pack is only shared with friends. It isn't uploaded anywhere and earns no money. Decided 2026-11-04 (answer to Q-175); before that the plan was to publish on CurseForge and join its Rewards Program.

**Acceptance criteria:**
- [ ] Friends get the pack as a list of mods from their official download pages (for example a launcher profile or a manifest), plus our own mod. A mod whose license doesn't allow passing on its files is never passed on directly.
- [ ] Anyone who gets a GPL or LGPL fork from us can also get its source (PACK-002).
- [ ] There's one pack, with the parody names (STORY-019).
- [ ] Licenses only need to allow private use: the rule that every mod must allow a pack that earns money is gone. Credits stay in CREDITS.md.
- [ ] If the pack is ever uploaded after all, this has to be checked again first: the parody names, nudity and every mod's license (see Notes).

**Open questions:**
**Notes:** Your words (2026-11-03): "I dont really plan on loading it up somewhere, but just sharing with my friends." Answered "Friends only" on 2026-11-04 (Q-175). Older notes on licenses in this file were written for a pack that earns money; those reasons no longer apply, though the decisions they led to stand until someone changes them. The earlier plan (answers to Q-115) was no nudity and fictional names in a public pack, a local uncut add-on, and the Rewards Program.

### PACK-006 — Inspiration only
**Status:** approved
**Priority:** could

**Description:** Mods we learn from but don't include.

| Mod | Why not included |
|---|---|
| L_Ender's Cataclysm | Boss-fight inspiration; CC-BY-NC-ND forbids reuse |
| Incendium | Unmodified only; random structures and its own weapons clash with our rules |
| Better Combat, Epic Fight | Replace vanilla combat, against CMBT-001 |
| Create | Rejected 2026-11-01: gets messy and laggy over time |
| Create: Northstar Redux | Needs Create |
| Ad Astra | Not chosen: we build our own rocket and Moon |
| Mekanism, Industrial Foregoing | Lean overpowered |
| Energized Power | Dropped 2026-11-03 (answer to Q-159): far too much to trim. MIT, so its code can still be reused with credit |
| Villager Workers, Recruits, MineColonies | Ideas for workers only: the first two are All Rights Reserved, and MineColonies is a whole town-builder |
| Lightman's Currency | Far more than a coin and a merchant |
| Applied Energistics 2, Refined Storage | Storage picked: Tom's Simple Storage (simpler, can't snowball) |
| Sophisticated Storage, Ultimate Car Mod | All Rights Reserved; the car mod isn't electric |
| Diet (original), Spice of Life variants | Diet is stale since 2023 (A Balanced Diet continues it); Spice of Life rewards variety only; Carrot Edition is ARR |

**Acceptance criteria:**
- [ ] None of these are in the pack.

**Open questions:**
**Notes:**
