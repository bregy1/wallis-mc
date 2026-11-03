# 10 — Progression

**Area code:** PROG
**Owner:** bregy1
**Status of this file:** draft

## Summary
Progression is the backbone of Wallis Ultimate. It is mainly story-driven: completing story quests unlocks new items, recipes, areas and gear. A secondary point currency lets a team buy special utility items. Progression is organised into ordered tiers grouped by planet; new planets add tiers on top of the existing ones. All progression belongs to a team. Enemy spawns depend mainly on location, with some enemy types gated by tier; difficulty never rises from time alone.

## Requirements

### PROG-001 — Ordered tiers grouped by planet
**Status:** approved
**Priority:** must

**Description:** Progression is a single ordered list of tiers. Every tier belongs to exactly one planet, and a planet has one or more tiers. Adding a planet appends its tiers after the existing ones without changing earlier tiers.

**Acceptance criteria:**
- [ ] Each team has exactly one current tier at any time, starting at the first Earth tier.
- [ ] Tiers are defined in data (not hard-coded), so a new planet's tiers can be added without changing existing tier definitions.
- [ ] Adding new tiers to an existing world does not reset or change any team's progress.
- [ ] Earlier planets stay visitable after moving on. Returning to Earth after the Moon is a special case (STORY-018).

**Open questions:**
**Notes:** Matches the "planets stack" decision (open-ended planet count).

### PROG-002 — Content is locked by default
**Status:** approved (partly implemented 2026-11-02: locks run on History Stages with the stage files in pack/config; slot rules verified by GameTests; slot rules, the locked look and tooltip, crafting, the recipe book and JEI's lock mark verified in a real client by the smoke test; quests that unlock stages not built yet)
**Priority:** must

**Description:** At the start, only a small starter set of items can be crafted and used (e.g. basic swords and bows). Everything else is locked until the team unlocks it. A locked item can be obtained and carried, but not used or crafted.

**Acceptance criteria:**
- [ ] A new team can craft and use only the starter set: wood and stone tools and weapons, bows and arrows, crafting table, furnace, torches, chests, campfire and basic food. Iron and everything beyond unlocks through the story.
- [x] Locked items can be picked up. They go only into the 27 main inventory slots, never the hotbar. If those slots are full, the item stays on the ground.
- [x] If a locked item ends up in the main hand (e.g. moved to the hotbar and selected), it moves back into the main inventory, or drops at the player's feet if that is full.
- [x] Offhand and armor slots don't accept locked items, and locked blocks can't be placed (right-click does nothing).
- [ ] Locked items can't be used from the inventory either, and are shown grayed out there: a gray tint over the item plus a lock icon, and a tooltip naming the stage that unlocks it (answer to Q-128).
- [ ] Recipes that produce locked items can't be crafted. JEI shows them marked as locked with info on what unlocks them; the vanilla recipe book hides them (answer to Q-129).
- [x] Creative and spectator players are exempt from the hotbar and main-hand rules (answer to Q-130).
- [ ] Locking is per team: an item unlocked by one team stays locked for members of another team.
- [ ] Locking also applies to items from other mods in the pack, not only our own.
- [ ] Machines (furnaces and other mods' machines included) are gated by being locked items themselves. Once placed, a machine processes its recipes without checking locks.
- [ ] No machine available in a tier has a recipe whose output belongs to a later tier (machine recipes are curated per tier).

**Unlocked from the start (allow-list):** everything in the pack is locked except this list (answer to Q-124).

| Group | Items |
|---|---|
| Starter set (Q-004) | Wooden and stone swords, pickaxes, axes, shovels and hoes; bow, arrow; crafting table, furnace, torch, chest, campfire |
| Early extras (Q-127) | Leather armor, boats and chest boats (all wood types), fishing rod, wooden and stone buttons and pressure plates |
| Basic food | Bread, apple, carrot, potato, baked potato, beetroot, melon slice, sweet berries, raw and cooked beef, pork, chicken, mutton, rabbit, cod and salmon, mushroom stew, beetroot soup, pumpkin pie, cookie, dried kelp, rotten flesh |
| Natural blocks | Dirt, coarse dirt, gravel, sand, red sand, clay and clay balls, stone, cobblestone, mossy cobblestone, andesite, diorite, granite, sandstone, red sandstone, snowballs, snow block |
| Wood (all wood types) | Logs, planks, slabs, stairs, fences, fence gates, doors, trapdoors, signs, saplings, leaves, sticks, ladders |
| Stone building | Cobblestone, stone and stone brick slabs, stairs and walls; stone bricks; smooth stone and its slab; bricks and brick slabs, stairs and walls; sandstone slabs, stairs and walls |
| Glass and wool | Glass, glass panes, all wool and carpets |
| Materials | Coal, charcoal, string, feather, flint, leather, paper, sugar cane, sugar, bone, bone meal, wheat, egg, seeds (wheat, beetroot, pumpkin, melon), pumpkin, melon |
| Story-given | Beds (all colors; they can't be crafted, SURV-005), Bandage (SURV-010), FTB Quests' quest book |

**Unlock steps (answer to Q-125):** each step is a stage, unlocked for the whole team by a quest reward.

| Stage | Unlocks | When |
|---|---|---|
| Iron | Iron and copper ore, raw metal, ingots, nuggets and blocks; iron tools, sword and armor | A Chapter 1 quest (which one: the storyline) |
| Settler | Everything else for now; split into Settler, Gunsmith and Launch when tiers 2–4 are specified | Tier 2 |
| Gunsmith, Launch | Empty placeholders | Tiers 3 and 4 |

**Open questions:**
**Notes:** Starter set decided 2026-11-01 (answer to Q-004). Allow-list instead of a literal starter set (2026-11-02, answer to Q-124), so basic building works from day one; anything not on the list, including new mods' items, stays locked until we allow it. Early extras added 2026-11-02 (answer to Q-127). How it is built: the Settler stage locks whole mods (its `mods` list) minus the allow-list, so every new pack mod that adds items must be added to that list, or its items stay unlocked. Decided 2026-11-01 (answers to Q-003, Q-012, Q-016–019): obtain yes, use no, craft no, shown grayed with info; pickup into main inventory only; main hand bounces back to inventory; offhand/armor/placing refused; machines are gated by locking the machine, not by checking each recipe. "Lock the machine" was chosen because it works with every mod in the pack without forking; the trade-off is that machine recipes must be curated per tier. Checked in a real client on 2026-11-02 by the smoke test (`tools/smoketest.sh`): pickups, main hand, offhand and armor, gray tint with lock icon, the tooltip ("Required Individual Progress: • Iron"), crafting refused, the recipe book without the iron pickaxe, JEI's recipe view marked "Locked", unlocking and relocking the Iron stage, creative exempt. History Stages refuses the offhand and armor slots itself and puts the item into the first free inventory slot, which can be in the hotbar (the main-hand rule always uses the main inventory). Placing can't happen because a locked block can't stay in the hand. Still open in the criteria above: using locked items from the inventory, and JEI's info on what unlocks a recipe (the recipe view shows "Locked" only).

### PROG-003 — Story quests unlock content
**Status:** approved
**Priority:** must

**Description:** Completing story quests is the main way to unlock content: recipes, items, gear and areas.

**Acceptance criteria:**
- [ ] Completing a story quest applies its listed unlocks to the whole team immediately.
- [ ] Every non-starter item that isn't a utility-point unlock (PROG-005) is unlocked by at least one story quest.
- [ ] Unlocks persist across logouts, server restarts and team members going offline.

**Open questions:**
**Notes:** Quest content and story structure live in the story spec. Quests run on FTB Quests (PACK-004).

### PROG-004 — Story milestones open the next tier
**Status:** approved
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
**Notes:** Your call: "mainly story driven, with some points needed for special utility items." Decided 2026-11-01 (answers to Q-006, Q-007): all four extra sources and all four categories. Mobility and base defense extras are the categories most likely to need tier limits (Q-024). Money (ECON-001) is separate: points unlock things once for the team, and money buys things one at a time (answer to Q-171).

### PROG-006 — Progression is owned by the team
**Status:** approved (partly implemented 2026-11-02: unlocks only. History Stages shares them across the FTB team, including offline members, and copies them on leaving; a joiner loses unlocks the team lacks, verified by GameTests. Tier, story progress and points not built yet)
**Priority:** must

**Description:** Tier, story progress, unlocks and utility points belong to a team. A solo player is a team of one.

**Acceptance criteria:**
- [ ] All team members see the same tier, unlocks and point balance.
- [ ] An unlock earned by one member applies to all members, including offline ones.
- [ ] A player who joins a team takes on the team's tier, story progress, unlocks and points. Unlocks the joiner had that the team doesn't are removed, so all members always match (answer to Q-126).
- [ ] A player who leaves a team starts a new solo team with a copy of the team's tier, story progress and unlocks at that moment (points: Q-028).
- [ ] A player's own level (PROG-009) stays with them when joining or leaving.

**Open questions:** Q-028
**Notes:** Inspiration: FTB Teams-style parties. Decided 2026-11-01 (answer to Q-009): "keep a copy". Known, accepted weak spot: a player can join an advanced team and leave with its unlocks.

### PROG-007 — Difficulty comes from location and progress, not time
**Status:** approved
**Priority:** must

**Description:** Which enemies spawn is mainly decided by location, not by team strength. Some enemy types only start spawning once a certain tier has been reached. Difficulty never rises just because time passes.

**Acceptance criteria:**
- [ ] The enemies that spawn at a place depend mainly on its location (planet, and areas within it: Q-029).
- [ ] Some enemy types spawn only after a defined tier has been reached (whose tier counts: Q-010).
- [ ] A team that idles for many in-game days without progressing doesn't face rising difficulty.
- [ ] The game is optimised for three players and supports any reasonable team size, from solo to four or more; enemy and boss strength scales per player.
- [ ] Scaling counts every real player taking part in the fight (for bosses: everyone inside the arena; for other enemies: players near the fight, radius set with balancing). NPCs never count.
- [ ] What scales is defined per enemy or boss type (e.g. health for one boss, damage for another), whatever suits that type.

**Open questions:** Q-010, Q-029, Q-030 (balancing details; don't block Milestone 1)
**Notes:** Changed 2026-11-01 (answer to Q-010). Previously "difficulty follows the team's tier"; now spawns are location-first, with tier-gated extras. Progress still makes things harder, because progressing takes players to more dangerous places and adds new enemy types. Exact numbers belong in the enemy and balancing spec.

### PROG-008 — Earth has four tiers
**Status:** approved (Milestone 1: the tier structure and tier 1)
**Priority:** must

**Description:** Earth is split into four tiers, one district each (WORLD-003). Each tier takes about 4–6 hours of story progress, leaving plenty of time for building and playing normal Minecraft. More content comes later as new planets, rather than stretching tiers.

| # | Name | Focus | Enemies |
|---|---|---|---|
| 1 | Scavenger | Melee weapons, bows, basic shelter | Plain zombies |
| 2 | Settler | Fortified base, farming | First specialised zombies |
| 3 | Gunsmith | Hard-to-craft guns | Stronger specials, hordes |
| 4 | Launch | Tech and automation, building the rocket | Launch defense siege (PROG-010) |

**Acceptance criteria:**
- [ ] Earth defines exactly four tiers, in the order above.
- [ ] Each tier has one milestone that opens the next (PROG-004).
- [ ] A team focused on the story gets through a tier in about 4–6 hours (checked in playtesting).

**Open questions:**
**Notes:** Decided 2026-11-01 (answers to Q-005, Q-011, Q-020). Your words on pacing: "4-6 hours is enough. The player should still be able to spend plenty of time building, or playing normal minecraft too! We keep it simple and may add new planets later on. Lets not risk content to get boring!"

### PROG-009 — Player leveling grants utility points and skill points
**Status:** draft
**Priority:** should

**Description:** Each player has a level, raised by earning XP from combat and quests. Every level-up adds utility points to the team pool and gives the player one skill point for their skill tree (PROG-012). Levels themselves give no stats; stat growth comes only through the capped skill tree.

**Acceptance criteria:**
- [ ] Players earn XP from combat and quests (sources and amounts: Q-021).
- [ ] Each level-up adds a defined number of utility points to the team's pool.
- [ ] Each level-up gives the player one skill point (PROG-012).
- [ ] Level and XP are tracked per player; utility points belong to the team, skill points to the player.
- [ ] On death, a player loses 50% of their progress toward the next level. Levels already reached are never lost, so skill points are never taken away.

**Open questions:** Q-021
**Notes:** Decided 2026-11-01 (answer to Q-008). Changed 2026-11-01 (answer to Q-109): previously "levels give no stat bonuses"; now levels give a skill point for the capped skill tree.

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

### PROG-011 — Specialist sub-trees, two per player
**Status:** draft
**Priority:** should

**Description:** The skill tree (PROG-012) contains four specialist sub-trees. During the game, specific quests let each player unlock two of them in total. A solo player must still be able to make it through the whole game; teams simply cover more specialisations between them.

**Acceptance criteria:**
- [ ] There are four specialist sub-trees (themes and names: Q-042).
- [ ] Each player can unlock at most two of them, each through a specific quest.
- [ ] A solo player can complete all main story content with any two specialist trees.
- [ ] Content tied to a specific specialisation (e.g. optional shortcuts or extra rewards) is never required for the main story.

**Open questions:** Q-042
**Notes:** Originally: "balanced around 2 player teams. 3 player teams dont really benefit from it later." Changed 2026-11-01 with the three-player target, then reshaped (answer to Q-110): "Balance wise it is important that single player can still make it! So i would go with specialist trees which get unlocked during the game. So out of maybe 4 we can unlock 2 in total with specific quests." The rule that main content never needs a specific tree follows from "single player can still make it".

### PROG-013 — Unlocks match what the district demands
**Status:** draft
**Priority:** must

**Description:** Items and tools arrive when the world logically calls for them. A district's terrain and threats explain what the team unlocks there, for example a flying machine in a district full of lava holes.

**Acceptance criteria:**
- [ ] Every major unlock (vehicles, key tools, story machines) names the district challenge that motivates it.
- [ ] A district never demands a tool or vehicle that hasn't been unlocked by the time the team gets there.

**Open questions:**
**Notes:** Your words: "In general, we want the items to be logically matched, say you get a fly machine once there is lot of lava holes or whatever in a district."

### PROG-012 — Character skill tree
**Status:** draft
**Priority:** should

**Description:** Each player grows their character through a compact skill tree that raises health, regeneration, movement speed and other character stats. The tree is split into sub-trees, one per category.

**Acceptance criteria:**
- [ ] Each player has their own skill tree.
- [ ] The tree has several sub-trees, each covering one category (e.g. health, regeneration, movement).
- [ ] Skills raise character stats such as max health, regeneration and movement speed.
- [ ] Each player level-up gives that player one skill point (PROG-009).
- [ ] Total bonuses are capped at roughly +6 hearts of max health, +15% movement speed and +50% regeneration; other stats and exact caps are set with balancing.
- [ ] Four specialist sub-trees are part of the tree (PROG-011).
- [ ] Players can reset their skill tree at any time and freely redistribute all points.
- [ ] A specialist sub-tree, once unlocked, stays unlocked; resetting only frees the points spent in it.

**Open questions:**
**Notes:** Your words: "We also want a scaling aspect, so we want a skill tree for making health, regen, move speed and other character stats! I think a compact tree with like different sub trees works fine. Each sub tree being of a category." Decided 2026-11-01 (answers to Q-109, Q-110): skill points from levels, noticeable but capped. PROG-009 changed to match. Your words on resets: "the skilltree to be revertible at any point. Special trees have to be unlocked and are unlocked then, but points can be freely distributed again!"

### PROG-014 — Fame on every planet
**Status:** draft (your idea, 2026-11-02; for later, not in Milestone 1)
**Priority:** should

**Description:** Every player has their own fame bar on every planet. Your standing rises as you do good there and falls if you turn on its people, and it can swing up and down fast. With high fame and everything on a planet finished, you get special leader abilities for that planet.

**Acceptance criteria:**
- [ ] Each player has their own fame bar on each planet, and can see it. Fame belongs to the player, not the team.
- [ ] Completing quests and killing bosses raises fame.
- [ ] Killing NPCs lowers fame.
- [ ] With negative fame, defenders attack you on sight, so safe spots become unsafe too.
- [ ] Getting killed by NPCs raises your fame a bit again.
- [ ] Killing hordes and zombies on Earth gives some fame back, so nobody can get locked out for good.
- [ ] With high fame and everything on a planet finished, an item grants that planet's special leader abilities, for example having NPCs dropped off where you want them, or an evacuation.
- [ ] Leader abilities have a somewhat long cooldown. It only counts down while playing, and logging out doesn't reset it.

**Open questions:**
**Notes:** Answers to Q-160: "fame belongs to player only, with negative fame defenders literally attack you. When killed by npcs, fame gets a bit higher again, so this is something going up and down fast, could also be a wanted level maybe." Decided afterwards: it stays a fame system, with no separate wanted level ("okay keep the fame system"). Your words on the idea as a whole: "as the quest progresses, we get higher fame on earth. So on each planet we have a fame bar. Completing quests and killing bosses gets higher fame, killing npcs reduces fame, so with negative fame safe spots start to be unsafe too! So careful. Also killing hordes and zombies on earth gives fame back a bit. So no hard lock. With high fame, and all finished on a planet, there is special leader abilities per planet. So acquired through an item, there can be drop offs of npcs placed. There can be evacuation. Of course this with somewhat long cooldown. Cooldown does only go down while playing. Logging out does not reset etc." Builds on the safe spot defenders and their aircraft (SURV-011).

