# 15 — Modpack composition

**Area code:** PACK
**Owner:** bregy1
**Status of this file:** draft

## Summary
Wallis Ultimate is a modpack plus our own mod, `wallis_ultimate`. Each system in the pack comes from exactly one mod, chosen for fit, maintenance and license, and everything else is built in our mod. Mods we can't legally modify are used unmodified; mods we may need to change must allow forking. Research was done on 2026-11-01 against Modrinth, CurseForge and the mods' repositories; versions and licenses must be re-checked when the pack is assembled.

## Requirements

### PACK-001 — One mod per system
**Status:** draft
**Priority:** must

**Description:** Every system in the pack (quests, power, guns…) comes from exactly one mod or from our own mod. No overlapping mods.

**Acceptance criteria:**
- [ ] No two mods in the pack cover the same system.
- [ ] Features of included mods that overlap with another system are disabled.

**Open questions:**
**Notes:** Pillar "curated, not piled".

### PACK-002 — License rules
**Status:** draft
**Priority:** must

**Description:** Every mod's license is checked before it goes into the pack. Mods without a forking-friendly license are only ever used unmodified. Forks stay under their original license, as separate jars.

**Acceptance criteria:**
- [ ] Every mod in the pack has its license (code and assets) recorded in the table below.
- [ ] Mods marked "unmodified only" are never forked or changed.
- [ ] Forks of GPL/LGPL mods keep their license and ship as separate jars, so our own mod's license stays our choice (Q-092).
- [ ] `CREDITS.md` lists every mod used, forked or borrowed from, with author, URL and license.

**Open questions:** Q-092
**Notes:** Rules from the `minecraft-design` skill.

### PACK-003 — No lag
**Status:** draft
**Priority:** must

**Description:** The pack ships with a performance base and avoids mods known to cause lag over time.

**Acceptance criteria:**
- [ ] Embeddium, ModernFix and FerriteCore are in the pack.
- [ ] Every new mod is checked for known performance problems before it's added.

**Open questions:** Q-094
**Notes:** Pillar "we don't want lag". Create was rejected partly for this reason (2026-11-01).

### PACK-004 — Core mod list
**Status:** draft
**Priority:** must

**Description:** The mods below cover the systems the spec needs. "Unmodified only" means we may include the official jar but never change or fork it.

| System | Pick | License (code / assets) | How we use it | Spec | Alternatives |
|---|---|---|---|---|---|
| Quests | FTB Quests | No license file; treat as unmodified only | Unmodified | PROG-003 | Odyssey Quests (MIT, 1.20.1 build stale since 2024) |
| Teams | FTB Teams | No license file; treat as unmodified only | Unmodified | PROG-006 | Open Parties and Claims (LGPL, no shared progression) |
| Item locking per team | History Stages | Custom: official jar allowed in packs; no forks or modified copies | Unmodified; changes only by asking the author | PROG-002 | AStages (ARR, no team scope); Game Stages + Item Stages (drops held items, clashes with PROG-002) |
| Recipe viewer | JEI | MIT | Unmodified | PROG-002 | EMI (MIT) |
| NPCs with dialogue and reply choices | Easy NPC | MIT | Unmodified or fork | STORY-002, STORY-003 | interactEntity (MIT, small project) |
| Cutscenes | ImmersiveCinematics | MIT | Unmodified, plus our glue for team-wide playback | STORY-010 | CMDCam (LGPL-2.1, more manual) |
| Trinket slots | Curios API | LGPL-3.0 | Library dependency | CMBT-003 | None serious |
| Guns | TaCZ | GPL-3.0 | Unmodified, with our own small gun pack | GEAR-004 | Scorched Guns 2 (GPL-3.0) |
| Melee weapons | Spartan Weaponry (candidate) | Apache-2.0 code; asset terms unverified | Decide together with the weapon picks (Q-064) | GEAR-002 | Simply Swords (custom license) |
| Backpacks | Traveler's Backpack | LGPL-3.0 | Unmodified; integrates with Curios | UTIL | Sophisticated Backpacks (ARR) |
| Day length | Hourglass | LGPL-3.0 | Unmodified (sleep behavior: Q-093) | SURV-007 | None found |
| Minimap and world map | Xaero's Minimap + World Map | ARR, pack use allowed with credit | Unmodified | WORLD-003 | JourneyMap (ARR, pack terms unclear) |
| Performance | Embeddium, ModernFix, FerriteCore | LGPL-3.0, LGPL-3.0, MIT | Unmodified | PACK-003 | Entity Culling (terms unclear, Q-094) |
| Tech machines and power (generators, batteries, cables) | Energized Power | MIT; some textures credited to flashbulbs (terms unverified); pack use explicitly allowed | Unmodified or MIT fork; power trimmed to a few tiers (which: Q-096) | TECH | Mekanism, Industrial Foregoing (both lean overpowered); own power system |

**Acceptance criteria:**
- [ ] Every system in the table has one confirmed pick before the pack is assembled.
- [ ] Each pick's Forge 1.20.1 version is recorded when the pack is assembled.

**Open questions:** Q-064, Q-091, Q-093, Q-096
**Notes:** Picks are recommendations from the 2026-11-01 research; confirm or swap. Tech and power decided 2026-11-01: Energized Power for both, power trimmed. Its Forge Energy compatibility is unverified; Forge 1.20.1 itself provides the `IEnergyStorage` capability.

### PACK-005 — Built in our own mod
**Status:** draft
**Priority:** must

**Description:** These systems have no fitting mod (wrong behavior, wrong license, or nothing for Forge 1.20.1), so `wallis_ultimate` builds them.

| System | Spec | Why our own |
|---|---|---|
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
| The rocket (built as a structure) and the Moon dimension with low gravity | PROG-010, STORY-017, PLANET | Northstar needs Create; Ad Astra's rocket is likely a crafted item and its assets can't be changed. Decided 2026-11-01 |

**Acceptance criteria:**
- [ ] Each system above is either built in `wallis_ultimate` or moved to a mod by a spec change.

**Open questions:**
**Notes:**

### PACK-006 — Inspiration only
**Status:** draft
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

**Acceptance criteria:**
- [ ] None of these are in the pack.

**Open questions:**
**Notes:**
