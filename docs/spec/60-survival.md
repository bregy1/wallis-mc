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
- [ ] Lethal damage puts the player into the downed state instead of killing them (exceptions: Q-051).
- [ ] A downed player lies on the ground and can crawl slowly (speed: Q-049).
- [ ] While downed, the player has a simple pistol with one 10-shot magazine. Its damage is OK-ish for the early game and can be raised by trinkets or items.
- [ ] Enemies ignore downed players and don't attack them.
- [ ] A downed player can press a button to give up and respawn right away (SURV-003, SURV-004).
- [ ] If not revived within 90 seconds, the player dies (SURV-003, SURV-004).

**Open questions:** Q-047, Q-049, Q-051, Q-054
**Notes:** From dying.md, refined 2026-11-01 (answers to Q-048, Q-049, Q-050). dying.md said "like 30 seconds"; your latest answer says 90 seconds (Q-047). Solo players follow the same rules; the give-up button means they don't have to wait out the timer. The downed pistol exists only while downed, so it doesn't replace the hard-to-craft guns of tier 3.

### SURV-002 — Reviving a teammate
**Status:** draft
**Priority:** must

**Description:** A team member revives a downed player by holding the use key on them for 3–4 seconds while standing still. Taking damage interrupts the revive, so reviving in the middle of a big group of enemies is hard.

**Acceptance criteria:**
- [ ] Only members of the downed player's team can revive them.
- [ ] Reviving takes 3–4 seconds of holding the use key (exact value: Q-049).
- [ ] While reviving, the reviver can't move, attack or use items.
- [ ] If the reviver takes damage, the revive is cancelled and its progress is lost.
- [ ] A revived player gets back up with the health defined in Q-049.

**Open questions:** Q-049
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

**Description:** Death works like vanilla, except the player's items go into a grave instead of dropping on the ground. Only the player who died can open it. A grave with items lasts about 20 minutes; an empty grave is removed.

**Acceptance criteria:**
- [ ] On death, the player's items go into a grave at the place they died instead of dropping.
- [ ] Only the player who died can take items out of their grave.
- [ ] The grave is removed as soon as it's empty.
- [ ] A grave that still holds items lasts 20 minutes (what happens then: Q-053).

**Open questions:** Q-053
**Notes:** Your words: "Graveyard stays for like 20 minutes if items are in it."

### SURV-005 — Beds are rare heal and respawn points
**Status:** draft
**Priority:** must

**Description:** Beds are the heal and respawn points. At the start they can't be crafted at all: players find them already placed in safe spots along the way (SURV-006), and the story gives each player a bed early on to set up the team's first home. Beds can't be placed everywhere. Sleeping through the night still works as in vanilla.

**Acceptance criteria:**
- [ ] A bed sets the player's respawn point, as in vanilla.
- [ ] A bed heals the player (how: Q-052).
- [ ] At the start of the game, beds can't be crafted (when crafting unlocks, if ever: Q-052).
- [ ] Early in the story, every player receives a bed, used to set up the team's first home (STORY, quest "create first home").
- [ ] Safe spots have beds already placed (SURV-006).
- [ ] Beds can't be placed everywhere (rules: Q-052).
- [ ] Sleeping through the night works as in vanilla.

**Open questions:** Q-052
**Notes:** Your words: "Beds cannot be placed everywhere and are not easy to obtain. Beds can still be used to sleep through a night." Refined 2026-11-01 (answer to Q-052).

### SURV-006 — Safe spots
**Status:** draft
**Priority:** must

**Description:** Safe spots along the way have beds already placed, working as respawn and heal points. Some safe spots on Earth are guarded by friendly redneck defenders. They're heavily fortified, easily withstand attacks, and only fall when the story says so.

**Acceptance criteria:**
- [ ] Safe spots appear along the way, each with a bed already placed that works as a respawn and heal point.
- [ ] Some safe spots on Earth are guarded by friendly redneck defenders.
- [ ] No safe spot, defended or not, can be overrun by enemies or hordes, except where the story makes it fall (ENEMY-007).

**Open questions:** Q-062
**Notes:** Your words: "They only would be overrun when the story tells so." Placement of safe spots belongs in the world spec (WORLD).

### SURV-007 — Longer days, shorter nights
**Status:** draft
**Priority:** must

**Description:** A full day lasts 24 minutes: 16 minutes of day and 8 minutes of night (vanilla: about 10 minutes of day and 7 of night).

**Acceptance criteria:**
- [ ] Daytime lasts 16 real minutes.
- [ ] Nighttime lasts 8 real minutes.
- [ ] Sleeping still skips the night (SURV-005).

**Open questions:**
**Notes:** Decided 2026-11-01. Vanilla has no setting for day length, so this needs a mod or our own code. Zombie behavior by time of day is in ENEMY-001 to ENEMY-003.
