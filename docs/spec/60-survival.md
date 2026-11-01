# 60 — Survival systems

**Area code:** SURV
**Owner:** bregy1
**Status of this file:** draft

## Summary
Vanilla survival systems stay as they are unless this file says otherwise (pillar "stay a Minecraft game"). Changes so far: players go down instead of dying and can be revived by teammates; items go into a private grave on death; beds become rare heal and respawn points, found in safe spots or given by the story; days last 16 minutes and nights 8.

## Requirements

### SURV-001 — Downed instead of dead
**Status:** draft
**Priority:** must

**Description:** When a player would die, they go down instead. A downed player lies on the ground, crawls slowly and defends themselves with a simple pistol, like Left 4 Dead's incapacitated state. Enemies ignore downed players. Teammates have 90 seconds to revive them, or the player can give up and respawn right away.

**Acceptance criteria:**
- [ ] Lethal damage puts the player into the downed state instead of killing them. Falling into the void, dying in lava and /kill kill instantly.
- [ ] A downed player lies on the ground and can crawl at vanilla crawling speed.
- [ ] While downed, the player has a simple pistol with one 10-shot magazine. Its damage is OK-ish for the early game and can be raised by trinkets or items.
- [ ] Enemies ignore downed players and don't attack them.
- [ ] A downed player can press a button to give up and respawn right away (SURV-003, SURV-004).
- [ ] If not revived within 90 seconds, the player dies (SURV-003, SURV-004).
- [ ] Teammates see a marker over a downed player (visible through walls), hear a call-for-help sound, and see the remaining time on screen.

**Open questions:**
**Notes:** From dying.md, refined 2026-11-01 (answers to Q-048, Q-049, Q-050). dying.md said "like 30 seconds"; 90 seconds is the only timer (answer to Q-047). Instant-death cases decided (answer to Q-051). Solo players follow the same rules; the give-up button means they don't have to wait out the timer. The downed pistol exists only while downed, so it doesn't replace the hard-to-craft guns of tier 3.

### SURV-002 — Reviving a teammate
**Status:** draft
**Priority:** must

**Description:** A team member revives a downed player by holding the use key on them for 3–4 seconds while standing still. Taking damage interrupts the revive, so reviving in the middle of a big group of enemies is hard.

**Acceptance criteria:**
- [ ] Only members of the downed player's team can revive them.
- [ ] Reviving takes 3.5 seconds of holding the use key.
- [ ] While reviving, the reviver can't move, attack or use items.
- [ ] If the reviver takes damage, the revive is cancelled and its progress is lost.
- [ ] A revived player gets back up with half health.

**Open questions:**
**Notes:** Not chosen (2026-11-01): coming back weak, "second down is final", and hits on a downed player shortening the timer.

### SURV-003 — Respawn at your bed
**Status:** draft
**Priority:** must

**Description:** A player who isn't revived within 90 seconds dies and respawns at their bed, their "last save".

**Acceptance criteria:**
- [ ] A player who isn't revived within 90 seconds respawns at their bed.
- [ ] A player without a valid bed respawns at world spawn, as in vanilla.

**Open questions:**
**Notes:** Your words: "After 90 seconds he teleports to last save, which is literally a bed."

### SURV-004 — Graves keep your items
**Status:** draft
**Priority:** must

**Description:** Death works like vanilla, except the player's items go into a grave instead of dropping on the ground. Only the player who died can open it. A grave with items lasts 60 minutes; after that it's gone, with everything in it. An empty grave is removed right away. Dying also costs some level progress (PROG-009).

**Acceptance criteria:**
- [ ] On death, the player's items go into a grave at the place they died instead of dropping.
- [ ] If the player died in lava or the void, the grave appears at the last safe spot they stood on.
- [ ] Only the player who died can take items out of their grave.
- [ ] The grave is removed as soon as it's empty.
- [ ] A grave that still holds items lasts 60 minutes, then disappears together with its contents.

**Open questions:**
**Notes:** First "like 20 minutes"; changed 2026-11-01 (answer to Q-053): "make it 60 minutes but it is lost after."

### SURV-005 — Beds are rare heal and respawn points
**Status:** draft
**Priority:** must

**Description:** Beds are the heal and respawn points. Each player has one bed per planet, plus the beds already placed in safe spots (SURV-006). Beds can't be crafted. On every planet the bed comes from a quest, sometimes earlier, sometimes later; on Earth it's early, for the team's first home. Sleeping fully heals you, and sleeping through the night still works as in vanilla.

**Acceptance criteria:**
- [ ] A bed sets the player's respawn point, as in vanilla.
- [ ] Sleeping in a bed fully heals the player.
- [ ] Beds can't be crafted.
- [ ] Each player can own one bed per planet; it comes from a quest on that planet.
- [ ] On Earth, every player receives their bed early, used to set up the team's first home (quest "create first home").
- [ ] Safe spots have beds already placed (SURV-006); they don't count toward the one-bed limit.
- [ ] A player can pick up their bed and place it again, which moves their home (WORLD-012).
- [ ] Sleeping through the night works as in vanilla.

**Open questions:** Q-107
**Notes:** Your words: "Beds cannot be placed everywhere and are not easy to obtain. Beds can still be used to sleep through a night." Refined 2026-11-01 (answers to Q-052): "each player to have one bed per planet + the safe spot beds. On the new planet the bed is achievable through quest always." Picking up and moving the bed follows from "home bed decides" (WORLD-012).

### SURV-006 — Safe spots
**Status:** draft
**Priority:** must

**Description:** Safe spots along the way have beds already placed, working as respawn and heal points. Some safe spots on Earth are guarded by friendly redneck defenders. They're heavily fortified, easily withstand attacks, and only fall when the story says so.

**Acceptance criteria:**
- [ ] Each district has about 3–4 safe spots along its main routes, each with a bed already placed that works as a respawn and heal point.
- [ ] Any player can use a safe spot's bed and its travel point.
- [ ] Some safe spots on Earth are guarded by friendly redneck defenders, who give quests and trade.
- [ ] No safe spot, defended or not, can be overrun by enemies or hordes, except where the story makes it fall (ENEMY-007).

**Open questions:**
**Notes:** Your words: "They only would be overrun when the story tells so." Count, access and defenders decided 2026-11-01 (answer to Q-062). Placement of safe spots belongs in the world spec (WORLD).

### SURV-007 — Longer days, shorter nights
**Status:** draft
**Priority:** must

**Description:** A full day lasts 24 minutes: 16 minutes of day and 8 minutes of night (vanilla: about 10 minutes of day and 7 of night).

**Acceptance criteria:**
- [ ] Daytime lasts 16 real minutes.
- [ ] Nighttime lasts 8 real minutes.
- [ ] Sleeping still skips the night (SURV-005).

**Open questions:**
**Notes:** Decided 2026-11-01. Vanilla has no setting for day length; Hourglass covers it (PACK-004). Zombie behavior by time of day is in ENEMY-001 to ENEMY-003.

### SURV-008 — Food: vanilla hunger plus cooking
**Status:** draft
**Priority:** should

**Description:** Hunger works as in vanilla. Farmer's Delight adds cooking (cooking pot, cutting board), new crops and hearty meals, in vanilla style.

**Acceptance criteria:**
- [ ] Hunger and saturation work as in vanilla.
- [ ] Farmer's Delight is the only food mod in the pack (PACK-001).
- [ ] No food buff is overpowered (Nourishment check: Q-103).

**Open questions:** Q-103
**Notes:** Decided 2026-11-01. Fits Gordon Damsay's food quests (cast proposal) and Energized Power's growth chamber for food automation (TECH-003).

### SURV-009 — Diet: what you eat matters
**Status:** draft
**Priority:** must

**Description:** Food falls into five groups: fruits, vegetables, grains, proteins and sugars. Eating a balanced diet gives real benefits; neglecting groups gives medium penalties, so players actually have to pay attention. Penalties weaken the player but are never slowing, blinding or otherwise super annoying. This makes the early game even harder, on purpose.

**Acceptance criteria:**
- [ ] The diet system is A Balanced Diet, with five food groups (PACK-004).
- [ ] Each group's level gives defined benefits when high and penalties when low (values: Q-105).
- [ ] Penalties only weaken (e.g. Weakness, lower max health, slower natural healing). They never include Slowness, Mining Fatigue, Blindness, Nausea, Darkness or similar.
- [ ] Benefits stay below overpowered (pillar).
- [ ] Group levels change gradually, so benefits and penalties last a reasonable time instead of flickering on and off (rates: Q-105).
- [ ] Farmer's Delight foods count toward the right groups.
- [ ] Players can see food values and group levels (AppleSkin and the diet screen).

**Open questions:** Q-105
**Notes:** Your words: "Mid tier penalties for bad diet is good! So we actually have to check for it. Makes early game even harder due to debuffs. Weakening debuffs but not slowing or blindness or something super annoying."

### SURV-010 — Healing items
**Status:** draft
**Priority:** must

**Description:** Besides sleeping, players can heal with items. Early healing items are weak; stronger ones come later.

**Acceptance criteria:**
- [ ] At least one healing item is available in tier 1.
- [ ] Stronger healing items unlock in later tiers.
- [ ] Which items exist, and how much they heal, follow Q-108.

**Open questions:** Q-108
**Notes:** Your words: "should be able to get items for healing, weaker in the beginning."
