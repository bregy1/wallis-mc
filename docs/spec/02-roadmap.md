# 02 — Spec roadmap

**Status of this file:** draft

All spec areas we expect to need, so we can see what's left before going deep on any single one. Numbers are just file order; the area code is used in requirement IDs (e.g. `STORY-001`).

| File | Area | Code | Covers | Related open questions | Status |
|---|---|---|---|---|---|
| `10-progression.md` | Progression | PROG | Tiers, locked items, story unlocks, utility points, teams, leveling, Earth tiers, launch finale, specialisations | Q-004, Q-006, Q-011, Q-013, Q-014, Q-020–024, Q-027–030, Q-042, Q-043 | draft |
| `15-modpack.md` | Modpack composition | PACK | One mod per system, license rules, performance base, core mod list, what our mod builds, inspiration-only mods | Q-064, Q-091–095 | draft |
| `20-story.md` | Story and characters | STORY | General story and storyline, alien origin and reveal, redneck opening, allies, factions, choices, random quests, human and hidden enemies, tone, cutscenes, the aliens, Elon Tusk and the Tusk group, slow build-up, the Moon, plot idea pool with picks, cast. Rules for story content live in the `story` skill | Q-013, Q-031–041, Q-044, Q-059–060, Q-070, Q-072, Q-077 | draft |
| `25-world.md` | World and districts | WORLD | Vanilla-style terrain, districts as size-limited dimensions, story-opened districts, safe spot travel, unique structures, unminable blocks, no End, Nether overhaul, world height, ores per district | Q-029, Q-065–069, Q-073–076 | draft |
| `30-enemies.md` | Enemies and bosses | ENEMY | Day/night zombie behavior, sunlight, status effects, block breaking, hordes, safe spots, human and alien enemies, special zombies and boss pools, enemy tracker | Q-010, Q-030, Q-078–086 | draft |
| `35-balancing.md` | Balancing | BAL | Damage, defense and enemy strength model; the balancing approach from plans.md | Q-030 | not started |
| `38-combat.md` | Combat | CMBT | Vanilla combat as the base, dodge roll, trinket slots | Q-055, Q-056 | draft |
| `40-gear.md` | Weapons, guns, armor, tools and trinkets | GEAR | Vanilla tools, curated weapon mods, every item earns its slot, guns in tier 3; weapon idea pool; individual trinkets | Q-064 | draft |
| `45-utility.md` | Utility items | UTIL | Point unlocks: quality of life (backpacks), mobility, scouting and info, base defense extras | Q-024, Q-026 | not started |
| `50-base.md` | Base and raids | BASE | Fortification, base attacks, how raids scale | Q-030 | not started |
| `55-tech.md` | Tech and automation | TECH | Energized Power machines and trimmed power, story machines, small purposeful automation, slow-scaling storage (Tom's Simple Storage) | Q-096, Q-100, Q-101 | draft |
| `60-survival.md` | Survival systems | SURV | Which vanilla systems stay unchanged (hunger confirmed); downed state and revive, graves, beds as rare heal and respawn points, safe spots, 16/8-minute day, food (Farmer's Delight), diet (A Balanced Diet) | Q-025, Q-047–054, Q-062, Q-103, Q-105 | draft |
| `65-planets.md` | Planets and travel | PLANET | Rocket built as a structure and our own Moon dimension with low gravity (PACK-005), the Moon as an alien mini base at the end of version 1, later planets including the magic planet, travel rules, going back to earlier planets | Q-014, Q-023, Q-070 | not started |
| `68-transport.md` | Transport | TRANS | Safe spot teleport, horses early, electric flying vehicles later on Earth (Immersive Aircraft, trimmed), garages at safe spots | Q-097, Q-098, Q-102, Q-104 | draft |
| `70-presentation.md` | Presentation | PRES | Music, sound, art style, UI and feedback | — | not started |
| `75-endgame.md` | Endgame and post-game | END | Optional superbosses, side content, mini-games, goals after the main story | Q-002 | not started |
| `80-magic.md` | Magic | MAGIC | The special book, spells, magic bombs; the magic chapter after the magic planet | Q-063 | after version 1 |

## Milestone 1: Chapter 1 playable

The first thing we build: the Scavenger tier in the first district, from the redneck opening to beating Hog Daddy. Only the requirements below need to be approved before coding starts.

**In Milestone 1**
- **Progression:** PROG-001–004, PROG-006, PROG-007, PROG-008 (tier 1 only)
- **Story:** STORY-002, STORY-003, STORY-010, STORY-014, STORY-016, Chapter 1 of the storyline
- **Survival:** SURV-001–009
- **Combat:** CMBT-001–003
- **Gear:** GEAR-001, GEAR-003, tier 1 weapons (Q-064)
- **World:** WORLD-001–007, WORLD-009–011
- **Enemies:** ENEMY-001–007, ENEMY-009 (tier 1 specials), ENEMY-011 (Hog Daddy)
- **Transport:** TRANS-001, TRANS-002
- **Modpack:** PACK-001–006, with only the mods Chapter 1 needs (FTB Quests and Teams, History Stages, JEI, Easy NPC, ImmersiveCinematics, Curios, Hourglass, Xaero's maps, Farmer's Delight, A Balanced Diet, AppleSkin, Lootr, performance mods)

**Later milestones:** utility points and leveling (PROG-005, PROG-009), the skill tree and specialist sub-trees (PROG-011, PROG-012), factions and raiders, the enemy tracker, guns, tech and storage, the Nether, rocket, Moon, aircraft and garages.

**Open questions blocking Milestone 1, in batches**
1. Foundations: Q-004 starter set, Q-065 districts per tier, Q-066 home base, Q-082/Q-084 tier 1 enemies and Hog Daddy
2. Downed and beds: Q-047, Q-049, Q-051, Q-052
3. Graves, safe spots, travel: Q-053, Q-054, Q-062, Q-067
4. World: Q-069, Q-074, Q-076, Q-106
5. Enemies: Q-078, Q-080, Q-081, Q-087
6. Combat, gear, food: Q-055, Q-056, Q-064 (tier 1), Q-093, Q-103, Q-105
7. Story and project: Q-011, Q-035, Q-041, Q-071/Q-077 (Chapter 1 cast and plots), Q-092 (our mod's license)

Technical checks Claude does before coding (no decision needed from you): Q-091 (History Stages behavior), Lootr's license, Q-094 (Entity Culling terms).

After the batches: you approve the Milestone 1 requirements, and we start building.

## Suggested next (your call)
1. **Story and characters**: it gives every other area its reasons (why you go somewhere, who you meet, which fights matter).
2. **Enemies and bosses**: it defines what Earth's first tiers actually feel like to play.
3. **Modpack composition**: early enough to pick one mod per system before we spec around a mod we won't use.
