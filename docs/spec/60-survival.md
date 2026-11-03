# 60 — Survival systems

**Area code:** SURV
**Owner:** bregy1
**Status of this file:** draft

## Summary
Vanilla survival systems stay as they are unless this file says otherwise (pillar "stay a Minecraft game"). Changes so far: players go down instead of dying and can be revived by teammates; items go into a private grave on death; beds become rare heal and respawn points, found in safe spots or given by the story; days last 16 minutes and nights 8.

## Requirements

### SURV-001 — Downed instead of dead
**Status:** approved (partly implemented 2026-11-02: server logic verified by GameTests; controls and a placeholder HUD built, to check in-game)
**Priority:** must

**Description:** When a player would die, they go down instead. A downed player lies on the ground, crawls slowly and defends themselves with a simple pistol, like Left 4 Dead's incapacitated state. Enemies ignore downed players. Teammates have 90 seconds to revive them, or the player can give up and respawn right away.

**Acceptance criteria:**
- [x] Lethal damage puts the player into the downed state instead of killing them. Falling into the void, dying in lava and /kill kill instantly. (Lava also kills a player who is already downed.)
- [ ] A downed player lies on the ground and can crawl at vanilla crawling speed. (Implemented: forced crawl pose, no sprinting or jumping; to check in-game.)
- [ ] While downed, the player has a simple pistol with one 10-shot magazine. Its damage is OK-ish for the early game and can be raised by trinkets or items. (10 shots, 4 damage, 24 blocks verified by GameTests; the attack key fires one shot per click, to check in-game; raising the damage comes with trinkets.)
- [x] Enemies ignore downed players and don't attack them. (Enemies drop a downed target, can't pick one, and downed players take no damage except the instant-death cases.)
- [ ] A downed player can press a button to give up and respawn right away (SURV-003, SURV-004). (Giving up verified by GameTests; the key, G by default, to check in-game.)
- [x] If not revived within 90 seconds, the player dies (SURV-003, SURV-004).
- [ ] Teammates see a marker over a downed player (visible through walls), hear a call-for-help sound, and see the remaining time on screen. (Implemented: glowing outline, an alert sound for teammates when someone goes down plus a help sound at the spot every 5 seconds, and a list of downed teammates with their time on screen; to check in-game.)

**Open questions:**
**Notes:** From dying.md, refined 2026-11-01 (answers to Q-048, Q-049, Q-050). dying.md said "like 30 seconds"; 90 seconds is the only timer (answer to Q-047). Instant-death cases decided (answer to Q-051). Solo players follow the same rules; the give-up button means they don't have to wait out the timer. The downed pistol exists only while downed, so it doesn't replace the hard-to-craft guns of tier 3. The details picked during implementation (no damage while downed except void, lava and /kill; pistol 4 damage, 24 blocks, one shot per click; outline visible to everyone; give-up key H (first G, but Curios opens its screen on G, found 2026-11-02); bell alert plus hurt sound every 5 s; no jumping; plain-text HUD placeholder) were confirmed 2026-11-02 (answer to Q-121).

### SURV-002 — Reviving a teammate
**Status:** approved (partly implemented 2026-11-02: server logic verified by GameTests; holding the use key and the progress bar to check in-game)
**Priority:** must

**Description:** A team member revives a downed player by holding the use key on them for 3–4 seconds while standing still. Taking damage interrupts the revive, so reviving in the middle of a big group of enemies is hard.

**Acceptance criteria:**
- [x] Only members of the downed player's team can revive them. (Teams come from FTB Teams; a solo player has no teammates. The reviver must be up and within 3 blocks.)
- [ ] Reviving takes 3.5 seconds of holding the use key. (70 ticks verified by GameTests; letting go stops it; holding the key to check in-game.)
- [ ] While reviving, the reviver can't move, attack or use items. (Movement lock verified by GameTests; attack and item blocking to check in-game.)
- [x] If the reviver takes damage, the revive is cancelled and its progress is lost.
- [x] A revived player gets back up with half health.

**Open questions:**
**Notes:** Not chosen (2026-11-01): coming back weak, "second down is final", and hits on a downed player shortening the timer.

### SURV-003 — Respawn at your bed
**Status:** approved
**Priority:** must

**Description:** A player who isn't revived within 90 seconds dies and respawns at their bed, their "last save".

**Acceptance criteria:**
- [x] A player who isn't revived within 90 seconds respawns at their bed. (The 90 seconds are checked by a GameTest; dying and coming back at the bed in a real client by the smoke test, 2026-11-03.)
- [ ] A player without a valid bed respawns at world spawn, as in vanilla.

**Open questions:**
**Notes:** Your words: "After 90 seconds he teleports to last save, which is literally a bed."

### SURV-004 — Graves keep your items
**Status:** approved (implemented 2026-11-02: verified by GameTests and in a real client by the smoke test, including a real death, the take-only grave screen and the location message; the look is a placeholder)
**Priority:** must

**Description:** Death works like vanilla, except the player's items go into a grave instead of dropping on the ground. Only the player who died can open it. A grave with items lasts 60 minutes; after that it's gone, with everything in it. An empty grave is removed right away. Dying also costs some level progress (PROG-009).

**Acceptance criteria:**
- [x] On death, the player's items go into a grave at the place they died instead of dropping. (A grave holds 54 stacks; more items get a second grave next to it. If there's no room at all, items drop as in vanilla.)
- [x] If the player died in lava or the void, the grave appears at the last safe spot they stood on.
- [x] Only the player who died can take items out of their grave. (Right-click opens a chest-like screen for the owner only. Graves can't be mined, blown up or emptied by hoppers.)
- [x] The grave is removed as soon as it's empty.
- [x] A grave that still holds items lasts 60 minutes, then disappears together with its contents. (World time, so it only counts while the server runs.)
- [x] Items can only be taken out of a grave, never put in (answer to Q-122). (Its own take-only screen; verified by GameTests and the client smoke test.)
- [x] On death, a chat message tells the player where their grave is (answer to Q-122).

**Open questions:**
**Notes:** First "like 20 minutes"; changed 2026-11-01 (answer to Q-053): "make it 60 minutes but it is lost after." Details decided 2026-11-02 (answer to Q-122): chest screen kept, take-only, 60 minutes of server time, a chat message with the grave's coordinates on death, placeholder look for now.

### SURV-005 — Beds are rare heal and respawn points
**Status:** approved (partly implemented 2026-11-02: bed recipes removed and one placed bed per planet, verified by GameTests and the client smoke test; beds from quests, safe spot beds and the Hourglass night not built yet)
**Priority:** must

**Description:** Beds are the heal and respawn points. Each player has one bed per planet, plus the beds already placed in safe spots (SURV-006). Beds can't be crafted. On every planet the bed comes from a quest, sometimes earlier, sometimes later; on Earth it's early, for the team's first home. Sleeping fully heals you, and sleeping through the night still works as in vanilla.

**Acceptance criteria:**
- [ ] A bed sets the player's respawn point, as in vanilla.
- [x] Sleeping in a bed fully heals the player, when they wake up in the morning after sleeping through the night. Leaving the bed early gives nothing (answer to Q-123). (Verified in a real client with Hourglass, which wakes players at dawn.)
- [x] Beds can't be crafted. (The 16 bed recipes are switched off; dyeing a bed you already have still works, see Q-123.)
- [ ] Each player can own one bed per planet; it comes from a quest on that planet. (The limit used to be forgotten on death, so dying let you place a second bed; fixed 2026-11-03 and checked by a GameTest. The quests come with the story work.)
- [ ] On Earth, every player receives their bed early, used to set up the team's first home (quest "create first home").
- [x] Safe spots have beds already placed (SURV-006); they don't count toward the one-bed limit.
- [ ] A player can pick up their bed and place it again, which moves their home (WORLD-012).
- [x] When players sleep, the night races by in a few seconds (Hourglass) instead of being skipped instantly.
- [x] A player can't place a second bed on a planet while their own bed there stands; they pick up the old one first (answer to Q-144). (Creative players are exempt so they can build; verified by GameTests and the client smoke test.)

**Open questions:**
**Notes:** Your words: "Beds cannot be placed everywhere and are not easy to obtain. Beds can still be used to sleep through a night." Refined 2026-11-01 (answers to Q-052): "each player to have one bed per planet + the safe spot beds. On the new planet the bed is achievable through quest always." Picking up and moving the bed follows from "home bed decides" (WORLD-012). Dyeing kept 2026-11-02 (answer to Q-123): ownership belongs to the placed bed, so recoloring the item changes nothing. Second bed (answer to Q-144): refused while your own bed on that planet stands.

### SURV-006 — Safe spots
**Status:** approved (partly implemented 2026-11-02: four safe spots with beds and signposts, verified by GameTests and in a real client by the smoke test. Defenders: SURV-011)
**Priority:** must

**Description:** Safe spots along the way have beds already placed, working as respawn and heal points. Some safe spots on Earth are guarded by friendly redneck defenders. They're heavily fortified, easily withstand attacks, and only fall when the story says so.

**Acceptance criteria:**
- [x] Each district has about 3–4 safe spots along its main routes, each with a bed already placed that works as a respawn and heal point. (District 1.)
- [x] Any player can use a safe spot's bed and its travel point.
- [ ] Some safe spots on Earth are guarded by friendly redneck defenders, who give quests and trade.
- [ ] No safe spot can be overrun by enemies or hordes, except where the story makes it fall (ENEMY-007, SURV-011).

**Open questions:**
**Notes:** Your words: "They only would be overrun when the story tells so." Count, access and defenders decided 2026-11-01 (answer to Q-062). Placement of safe spots belongs in the world spec (WORLD). Placement and look decided 2026-11-02 (answers to Q-138, Q-146): district 1 has four safe spots placed when the world is created, about 350–600 blocks from spawn in the four directions, with Hog Daddy's farm between two of them. Each looks different: bigger ones are fortified bases built from found materials with lots of apocalypse decoration and detail; smaller ones can be a truck or a few tents. Built (2026-11-02), placeholders by Claude, names are my proposals: Earl's Salvage to the north (a junkyard fort with a palisade, stacked wrecks, two lookout towers), Gas-N-Guns to the west (a fortified gas station), Camp Tarp to the east (three tents around a fire), The Stranded Hauler to the south (a broken-down semi to sleep in). Rules inside a safe spot: SURV-011 (answers to Q-155, Q-156).

### SURV-011 — Safe spot defenders
**Status:** approved 2026-11-02 (your answers to Q-155, Q-156; the numbers and looks are my proposals)
**Priority:** must

**Description:** Safe spots aren't protected by magic. They have real walls or barricades and armed NPCs, and anything that gets in starts a fight. When a horde reaches one, a big battle breaks out: many defenders die, reinforcements fly in by aircraft and land in visible drop-offs, and the place is patched up afterwards.

**Acceptance criteria:**
- [ ] Every safe spot is closed in by some kind of wall or barricade, with a way in, and has a clear theme of its own (your words: "please design them better themed").
- [ ] Ordinary defenders are armed with weapons that are okay-ish, and fight any enemy that comes near or gets in.
- [ ] Ordinary defenders can die. Main NPCs (the named ones who give quests) can't.
- [ ] When a horde reaches a safe spot, a big fight breaks out around it, without the players having to do anything.
- [ ] Lost defenders are replaced by supporting troops, who fly in by aircraft with a visible drop-off.
- [ ] Blocks can be destroyed. Defenders slowly repair everything back to how it was.
- [ ] Players can mine there, but defenders then turn on that player and try to kill them.
- [ ] An NPC that gets stuck teleports to the closest free spot; no NPC stays trapped in a hole. This goes for all NPCs, not only defenders.
- [ ] Enemies never spawn inside a safe spot's area: a fight there always comes from outside. (Built 2026-11-02, verified by a GameTest.)

**Open questions:**
**Notes:** Your words: "I like if a player breaks stuff the npcs start killing them, so mining is fine, but the npcs slowly fix it up anyways. In general, if npcs get stuck, they should teleport to the closest spot, i dont want npcs to be trapped in holes and stuff." And: "There should be some sort of barricade or wall or whatever. Everything which intrudes still will actually trigger a fight. The weapons they have should be still okayish. If a horde arrives to a safe spot a big fight should break out. So basically lot of npcs die, but there is also supporting troops flying in by the aircraft with a visual mechanic like drop offs. So safe spots npcs (if not the main ones) actually can die and fights happen around them for fun. Stuff can be destroyed, but is slowly patched up by the builders which are basically the npcs."

### SURV-007 — Longer days, shorter nights
**Status:** implemented 2026-11-02 (Hourglass config in pack/defaultconfigs; speeds measured in a real client by the smoke test)
**Priority:** must

**Description:** A full day lasts 24 minutes: 16 minutes of day and 8 minutes of night (vanilla: about 10 minutes of day and 7 of night).

**Acceptance criteria:**
- [x] Daytime lasts 16 real minutes. (Measured 0.680 game ticks per tick; 0.677 is 16 minutes.)
- [x] Nighttime lasts 8 real minutes. (Measured 1.150 game ticks per tick; 1.146 is 8 minutes.)
- [x] Sleeping still gets players through the night, by speeding it up (SURV-005).

**Open questions:**
**Notes:** Decided 2026-11-01. Vanilla has no setting for day length; Hourglass covers it (PACK-004). Zombie behavior by time of day is in ENEMY-001 to ENEMY-003.

### SURV-008 — Food: vanilla hunger plus cooking
**Status:** approved (partly implemented 2026-11-02: Farmer's Delight in the pack, Nourishment weakened, verified by a GameTest; balance still to check; the Cooking stage is in place, its quest comes with the story)
**Priority:** should

**Description:** Hunger works as in vanilla. Farmer's Delight adds cooking (cooking pot, cutting board), new crops and hearty meals, in vanilla style.

**Acceptance criteria:**
- [x] Hunger and saturation work as in vanilla.
- [x] Farmer's Delight is the only food mod in the pack (PACK-001).
- [ ] No food buff is overpowered.
- [x] Farmer's Delight's Nourishment buff only slows hunger drain; it never stops it. (Half the drain still counts; verified by a GameTest.)

**Open questions:**
**Notes:** Decided 2026-11-01. Fits Chef Angus Roast's food quests (cast proposal). Food automation (TECH-003) comes from workers (WORK-002). What's free (2026-11-02, answer to Q-152): crops, simple foods and hand-made meals from the start; the cooking pot, skillet, stove and cutting board unlock through the Cooking stage, from an early Chapter 1 quest by Lurleen.

### SURV-009 — Diet: what you eat matters
**Status:** approved (partly implemented 2026-11-02: our diet suite gives every group's benefit and penalty, verified in a real client by the smoke test and by GameTests; decay speed and Farmer's Delight's groups still to check)
**Priority:** must

**Description:** Food falls into five groups: fruits, vegetables, grains, proteins and sugars. Eating a balanced diet gives real benefits; neglecting groups gives medium penalties, so players actually have to pay attention. Penalties weaken the player but are never slowing, blinding or otherwise super annoying. This makes the early game even harder, on purpose.

**Acceptance criteria:**
- [x] The diet system is A Balanced Diet, with five food groups (PACK-004).
- [x] Each group's level gives benefits when high and penalties when low. Starting values (tuned in playtesting):
  - Proteins: more attack damage / less attack damage
  - Vegetables: faster natural healing / slower natural healing
  - Fruits: +2 max health / -2 max health
  - Grains: more saturation / hunger drains faster
  - Sugars: a short speed boost after eating / nothing
- [x] Penalties only weaken (e.g. less attack damage, lower max health, slower natural healing). They never include Slowness, Mining Fatigue, Blindness, Nausea, Darkness or similar.
- [ ] Benefits stay below overpowered (pillar).
- [ ] Group levels change gradually, so benefits and penalties last a reasonable time instead of flickering on and off: a full group lasts about 2 in-game days without eating from it.
- [ ] Farmer's Delight foods count toward the right groups.
- [ ] Players can see food values and group levels (AppleSkin and the diet screen).

**Open questions:**
**Notes:** Values decided 2026-11-01 (answer to Q-105). Your words: "Mid tier penalties for bad diet is good! So we actually have to check for it. Makes early game even harder due to debuffs. Weakening debuffs but not slowing or blindness or something super annoying." How it is built (my numbers, tuned in playtesting): groups count as high from 70% and low below 30%, and new players start at 50% so they have no penalties at first. Proteins: +1 attack damage / 25% less attack damage (not vanilla Weakness, whose flat -4 would make fists and wooden swords do nothing). Vegetables: Hearty (natural healing x1.5) / Frail (x0.5). Fruits: +2 / -2 max health. Grains: Filling (+25% saturation from meals) / the Hunger effect. Sugars: Sugar Buzz (Speed I for 5 seconds after eating). The diet mod applies these every few seconds. "Lasts about 2 in-game days" depends on how fast hunger drops, so it needs a playtest. Dying costs every group 20% (2026-11-02, answer to Q-153).

### SURV-010 — Healing items
**Status:** approved (partly implemented: Bandage, verified by GameTests 2026-11-02; Medkit later)
**Priority:** must

**Description:** Besides sleeping, players can heal with items. Early healing items are weak; stronger ones come later.

**Acceptance criteria:**
- [x] Bandage: available in tier 1, heals 3 hearts over 5 seconds. (Right-click applies a visible "Bandaged" effect: 1 HP every 16 ticks for 100 ticks; only usable when hurt.)
- [x] Bandages are crafted from 3 string or wool plus 1 paper, and stack to 16 (answer to Q-119). (One Bandage per craft; string and wool can be mixed.)
- [ ] Bandages are also found in loot (with the Chapter 1 structures).
- [ ] Medkit: unlocks later (proposal: tier 3), heals much more.
- [ ] Exact heal amounts are tuned in playtesting.

**Open questions:**
**Notes:** Your words: "should be able to get items for healing, weaker in the beginning." Two items rather than four (answer to Q-108: "Fewer items").
