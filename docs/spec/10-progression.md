# 10 — Progression

**Area code:** PROG
**Owner:** bregy1
**Status of this file:** draft

## Summary
Progression is the backbone of Wallis Ultimate. It is mainly story-driven: completing story quests unlocks new items, recipes, areas and gear. A secondary point currency lets a team buy special utility items. Progression is organised into ordered tiers grouped by planet; new planets add tiers on top of the existing ones. All progression belongs to a team. Enemy spawns depend mainly on location, with some enemy types gated by tier; difficulty never rises from time alone.

## Requirements

### PROG-001 — Ordered tiers grouped by planet
**Status:** draft
**Priority:** must

**Description:** Progression is a single ordered list of tiers. Every tier belongs to exactly one planet, and a planet has one or more tiers. Adding a planet appends its tiers after the existing ones without changing earlier tiers.

**Acceptance criteria:**
- [ ] Each team has exactly one current tier at any time, starting at the first Earth tier.
- [ ] Tiers are defined in data (not hard-coded), so a new planet's tiers can be added without changing existing tier definitions.
- [ ] Adding new tiers to an existing world does not reset or change any team's progress.

**Open questions:** Q-005, Q-011, Q-014
**Notes:** Matches the "planets stack" decision (open-ended planet count).

### PROG-002 — Content is locked by default
**Status:** draft
**Priority:** must

**Description:** At the start, only a small starter set of items can be crafted and used (e.g. basic swords and bows). Everything else is locked until the team unlocks it. A locked item can be obtained and carried, but not used or crafted.

**Acceptance criteria:**
- [ ] A new team can craft and use only the starter set defined for the first tier.
- [ ] Locked items can be picked up. They go only into the 27 main inventory slots, never the hotbar. If those slots are full, the item stays on the ground.
- [ ] If a locked item ends up in the main hand (e.g. moved to the hotbar and selected), it moves back into the main inventory, or drops at the player's feet if that is full.
- [ ] Offhand and armor slots don't accept locked items, and locked blocks can't be placed (right-click does nothing).
- [ ] Locked items can't be used from the inventory either, and are shown grayed out there.
- [ ] Recipes that produce locked items can't be crafted. They are shown grayed out with info on what unlocks them.
- [ ] Locking is per team: an item unlocked by one team stays locked for members of another team.
- [ ] Locking also applies to items from other mods in the pack, not only our own.
- [ ] Machines (furnaces and other mods' machines included) are gated by being locked items themselves. Once placed, a machine processes its recipes without checking locks.
- [ ] No machine available in a tier has a recipe whose output belongs to a later tier (machine recipes are curated per tier).

**Open questions:** Q-004
**Notes:** Decided 2026-11-01 (answers to Q-003, Q-012, Q-016–019): obtain yes, use no, craft no, shown grayed with info; pickup into main inventory only; main hand bounces back to inventory; offhand/armor/placing refused; machines are gated by locking the machine, not by checking each recipe. "Lock the machine" was chosen because it works with every mod in the pack without forking; the trade-off is that machine recipes must be curated per tier.

### PROG-003 — Story quests unlock content
**Status:** draft
**Priority:** must

**Description:** Completing story quests is the main way to unlock content: recipes, items, gear and areas.

**Acceptance criteria:**
- [ ] Completing a story quest applies its listed unlocks to the whole team immediately.
- [ ] Every non-starter item that isn't a utility-point unlock (PROG-005) is unlocked by at least one story quest.
- [ ] Unlocks persist across logouts, server restarts and team members going offline.

**Open questions:** Q-013
**Notes:** Quest content and story structure will get their own spec area.

### PROG-004 — Story milestones open the next tier
**Status:** draft
**Priority:** must

**Description:** A team advances to the next tier only by completing that tier's story milestone (for example a boss fight).

**Acceptance criteria:**
- [ ] Each tier defines exactly one milestone that advances the team to the next tier.
- [ ] There is no way to advance a tier without the milestone (outside creative/commands).

**Open questions:**
**Notes:** Earth's final milestone is the launch defense (PROG-010).

### PROG-005 — Utility points buy special utility items
**Status:** draft
**Priority:** should

**Description:** Teams earn utility points (name TBD) and spend them to unlock special utility items that sit outside the main story unlocks. Points come from leveling, quests, enemy discoveries, exploration and salvaging. Point unlocks cover four categories: quality of life, mobility, scouting and info, and base defense extras.

**Acceptance criteria:**
- [ ] Teams earn points from: player level-ups (PROG-009), quest rewards, the first discovery of each enemy type, exploration finds, and salvaging found items.
- [ ] Each exploration find pays out once per team.
- [ ] Salvaging can't be farmed endlessly (limit rule: Q-027).
- [ ] Teams can spend points to unlock items from a defined list of point unlocks.
- [ ] Every point unlock belongs to one category: quality of life (e.g. backpacks, item magnet), mobility (e.g. grappling hook, glider), scouting and info (e.g. enemy scanner, horde radar), or base defense extras (e.g. traps, alarms).
- [ ] Points and point unlocks are shared by the team.

**Open questions:** Q-006 (name), Q-024, Q-026, Q-027
**Notes:** Your call: "mainly story driven, with some points needed for special utility items." Decided 2026-11-01 (answers to Q-006, Q-007): all four extra sources and all four categories. Mobility and base defense extras are the categories most likely to need tier limits (Q-024).

### PROG-006 — Progression is owned by the team
**Status:** draft
**Priority:** must

**Description:** Tier, story progress, unlocks and utility points belong to a team. A solo player is a team of one.

**Acceptance criteria:**
- [ ] All team members see the same tier, unlocks and point balance.
- [ ] An unlock earned by one member applies to all members, including offline ones.
- [ ] A player who joins a team takes on the team's tier, story progress, unlocks and points.
- [ ] A player who leaves a team starts a new solo team with a copy of the team's tier, story progress and unlocks at that moment (points: Q-028).
- [ ] A player's own level (PROG-009) stays with them when joining or leaving.

**Open questions:** Q-028
**Notes:** Inspiration: FTB Teams-style parties. Decided 2026-11-01 (answer to Q-009): "keep a copy". Known, accepted weak spot: a player can join an advanced team and leave with its unlocks.

### PROG-007 — Difficulty comes from location and progress, not time
**Status:** draft
**Priority:** must

**Description:** Which enemies spawn is mainly decided by location, not by team strength. Some enemy types only start spawning once a certain tier has been reached. Difficulty never rises just because time passes.

**Acceptance criteria:**
- [ ] The enemies that spawn at a place depend mainly on its location (planet, and areas within it: Q-029).
- [ ] Some enemy types spawn only after a defined tier has been reached (whose tier counts: Q-010).
- [ ] A team that idles for many in-game days without progressing doesn't face rising difficulty.

**Open questions:** Q-010, Q-029, Q-030
**Notes:** Changed 2026-11-01 (answer to Q-010). Previously "difficulty follows the team's tier"; now spawns are location-first, with tier-gated extras. Progress still makes things harder, because progressing takes players to more dangerous places and adds new enemy types. Exact numbers belong in the enemy and balancing spec.

### PROG-008 — Earth has four tiers
**Status:** draft
**Priority:** must

**Description:** Earth is split into four tiers. The names and contents below are a first outline; details come in the Earth spec.

| # | Name (placeholder) | Focus | Enemies |
|---|---|---|---|
| 1 | Scavenger | Melee weapons, bows, basic shelter | Plain zombies |
| 2 | Settler | Fortified base, farming | First specialised zombies |
| 3 | Gunsmith | Hard-to-craft guns | Stronger specials, hordes |
| 4 | Launch | Tech and automation, building the rocket | Launch defense siege (PROG-010) |

**Acceptance criteria:**
- [ ] Earth defines exactly four tiers, in the order above.
- [ ] Each tier has one milestone that opens the next (PROG-004).

**Open questions:** Q-011, Q-020
**Notes:** Decided 2026-11-01 (answer to Q-005).

### PROG-009 — Player leveling grants utility points
**Status:** draft
**Priority:** should

**Description:** Each player has a level, raised by earning XP from combat and quests. Every level-up adds utility points to the player's team pool. Levels give no stat bonuses, so power stays in gear and tier.

**Acceptance criteria:**
- [ ] Players earn XP from combat and quests (sources and amounts: Q-021).
- [ ] Each level-up adds a defined number of utility points to the team's pool.
- [ ] A player's level gives no bonus to health, damage, armor or any other stat.
- [ ] Level and XP are tracked per player; the points they produce belong to the team.

**Open questions:** Q-021
**Notes:** Decided 2026-11-01 (answer to Q-008).

### PROG-010 — Earth finale: launch defense
**Status:** draft
**Priority:** must

**Description:** Earth's final milestone. The team builds the rocket during the Launch tier. Starting the launch triggers a large horde siege on the base that lasts until liftoff, like a Left 4 Dead campaign finale. Surviving until liftoff completes Earth and advances the team to the next planet's first tier (the Moon, per plans.md).

**Acceptance criteria:**
- [ ] Starting the launch begins a horde siege that lasts until liftoff (duration: Q-022).
- [ ] Reaching liftoff completes the Launch tier's milestone and advances the team to the next tier.
- [ ] A failed siege follows the rules in Q-022.

**Open questions:** Q-022, Q-023
**Notes:** Decided 2026-11-01 (answer to Q-015). Ties together story, base defense and special zombies. Siege makeup belongs in the enemy spec.
