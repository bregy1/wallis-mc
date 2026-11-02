# 38 — Combat

**Area code:** CMBT
**Owner:** bregy1
**Status of this file:** draft

## Summary
Fighting is real-time and built on vanilla Minecraft combat. On top of that, players get a dodge roll and extra equipment slots for trinkets that add fighting abilities or powers. Which weapons, tools and gadgets exist is covered in the gear spec (`40-gear.md`). Magic is left out for now; it only exists on a later planet (STORY-012).

## Requirements

### CMBT-001 — Vanilla combat is the base
**Status:** approved
**Priority:** must

**Description:** Melee and ranged combat work like vanilla Minecraft 1.20.1 unless this spec says otherwise.

**Acceptance criteria:**
- [ ] Vanilla attack timing, critical hits, shields, knockback and bows/crossbows behave as in vanilla 1.20.1, except where another requirement changes them.

**Open questions:**
**Notes:** Your words: "Default minecraft mechanics with like an additional roll to use and dodge."

### CMBT-002 — Dodge roll
**Status:** approved (partly implemented 2026-11-02: server logic verified by GameTests; in-game feel and chat key still open)
**Priority:** must

**Description:** Players can do a quick forward roll to get out of the way of attacks.

**Acceptance criteria:**
- [ ] Pressing the roll key makes the player roll about 3 blocks forward. (Implemented as a 1.36 ground impulse in the look direction; distance to check in-game.)
- [x] The roll has a 2-second cooldown and costs a little hunger, like a sprint jump. (40 ticks; 0.2 exhaustion.)
- [ ] The roll key defaults to T and can be rebound. (Implemented; to check in-game.)
- [ ] The pack moves vanilla's "open chat" default from T to Enter, so the two don't clash. (Pack config, done when the pack is assembled.)
- [x] The player is immune to damage for a split second (about 0.25 seconds) during the roll. (5 ticks; void and /kill still hit.)
- [x] A downed player (SURV-001) can't roll.

**Open questions:** Q-120
**Notes:** Decided 2026-11-01: "just a roll, going forward. Bound on T in beginning." Built in our own mod (PACK-005), no fork. No roll animation yet (presentation, later).

### CMBT-003 — Trinkets add fighting abilities
**Status:** approved
**Priority:** should

**Description:** Players have many extra equipment slots for trinkets, organised by body part, provided by an existing trinket mod. Trinkets are found throughout the world and give additional fighting abilities or powers.

**Acceptance criteria:**
- [ ] Players have about 20 trinket slots by body part, in addition to vanilla armor and offhand slots, provided by Curios (PACK-004).
- [ ] Most trinkets give passive bonuses; a few rare ones have an active ability on a key, with a cooldown.
- [ ] Trinkets are found in the world (where exactly: gear spec).
- [ ] An equipped trinket grants its ability or power; removing it takes the ability away.
- [ ] Locked trinkets can't be equipped (PROG-002).
- [ ] Some trinkets can boost the downed pistol (SURV-001).

**Open questions:**
**Notes:** Your words: "Some minecraft mods have already in use a trinket mod which gives like 20 body slots." Passive vs active decided 2026-11-01 (answer to Q-056). The well-known one for Forge 1.20.1 is Curios API; check its license before deciding (minecraft-design skill). Individual trinkets belong in the gear spec.
