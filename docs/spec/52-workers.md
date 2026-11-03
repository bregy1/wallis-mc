# 52 — Workers

**Area code:** WORK
**Owner:** bregy1
**Status of this file:** draft

## Summary
Workers are NPCs who do jobs in the base, inspired by Palworld. You tell them what to make, and they fetch what they need from chests and make it. The workflow and the screen stay simple, and there aren't many per base. They are how food gets automated.

## Requirements

### WORK-001 — Workers live around your bed
**Status:** draft (your idea, 2026-11-03; for later)
**Priority:** must

**Description:** A worker is an item you place in your base, and only around your bed. Once placed, it becomes an NPC who lives and works there.

**Acceptance criteria:**
- [ ] A worker item only works near its owner's bed (SURV-005). Anywhere else it does nothing, and a message says why. (Proposal: within 32 blocks.)
- [ ] There are only a few workers per base. (Proposal: 3 per bed, and at most 6 in one base.)
- [ ] Workers can be picked up again as an item and placed somewhere else. Workers left too far from a moved bed stop working until they're moved too. (Proposal.)
- [ ] Anyone on the team can give them orders; other teams can't (PROG-006). (Proposal.)
- [ ] Workers need no wages, no food and no upkeep. (Proposal.)

**Open questions:**
**Notes:** Your words: "the idea is that we can buy workers somewhere , npcs, and they can be tasked in our base." and "They are simply items to put in the base around bed only." They come from the trafficker (ECON-003). Name proposal for the item: Worker Papers. Built in our own mod (PACK-005): Villager Workers plays close to this and its work areas are worth a look, but it's all rights reserved and needs Recruits; MineColonies is a whole town-builder.

### WORK-002 — Jobs and job tables
**Status:** draft (your idea, 2026-11-03; for later)
**Priority:** must

**Description:** A worker's job comes from the table they're assigned to. You pick a worker and an item, and they get on with it. There are three jobs to start with, and more only if something needs them.

| Job | Table (names are proposals) | What they do |
|---|---|---|
| Cook | Food Maker Table | Makes the chosen food at the table, including Farmer's Delight's cutting board and cooking pot meals |
| Farmer | Farmer's Post | Plants and harvests the chosen crop around the post, and puts it in a chest |
| Crafter | Crafter's Bench | Makes the chosen item from crafting table recipes |

**Acceptance criteria:**
- [ ] A job table has one simple screen: pick a worker, pick the item to make.
- [ ] The worker takes ingredients from chests near the table, works at the table, and puts the result in a chest nearby. (Proposal: within 8 blocks.)
- [ ] They keep going until ingredients or chest space run out, then stop, and the screen says why.
- [ ] Workers only make items the team has unlocked (PROG-002), and the screen only lists those.
- [ ] Recipes cost the same as by hand, so nothing gets duplicated (TECH-003).
- [ ] Workers are slower than a player. (Proposal: about 10 seconds per item.)
- [ ] The Crafter only uses crafting table recipes, so machines keep their jobs (TECH-005). (Proposal.)

**Open questions:**
**Notes:** Your words: "we have a food maker table where we can assign people we get in some way." and "So maybe they work on a cutting board or whatever. They would then select items they need from chests, So a simple workflow and interface. We just tell them what item to make and tehy do it, and there can also be jobs, So a farmer job, or a crafter job. We keep it still simple, not too many per base." Answers Q-167: food automation comes from workers, not a machine.

### WORK-003 — Danger, and being away
**Status:** draft (your answers, 2026-11-03; for later)
**Priority:** must

**Description:** Workers can get hurt when enemies reach the base, but they're never lost for good. They only run while someone is nearby, so they add no lag while you're away, and they catch up when you come back.

**Acceptance criteria:**
- [ ] Enemies can attack workers. A worker who runs out of health is knocked out and lies down, then goes back to work the next morning. Workers never die for good.
- [ ] Workers only run while their area is loaded, and nothing keeps it loaded for them (PACK-003).
- [ ] When someone comes back, workers have made what they would have made, up to about one in-game day's worth (24 minutes, SURV-007). (The amount is a proposal.)
- [ ] Catching up follows the same rules as working: it uses ingredients from the chests and stops when ingredients or chest space run out.
- [ ] A base with the most workers allowed shows no real lag when profiled (PACK-003).

**Open questions:**
**Notes:** Decided 2026-11-03 (answers to Q-170 and Q-172). Build note: crops don't grow while an area is unloaded, so the Farmer's catch-up has to count that growth itself.
