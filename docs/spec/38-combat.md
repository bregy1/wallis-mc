# 38 — Combat

**Area code:** CMBT
**Owner:** bregy1
**Status of this file:** draft

## Summary
Fighting is real-time and built on vanilla Minecraft combat. On top of that, players get a dodge roll and extra equipment slots for trinkets that add fighting abilities or powers. Which weapons, tools and gadgets exist is covered in the gear spec (`40-gear.md`). Magic is left out for now; it only exists on a later planet (STORY-012).

## Requirements

### CMBT-001 — Vanilla combat is the base
**Status:** draft
**Priority:** must

**Description:** Melee and ranged combat work like vanilla Minecraft 1.20.1 unless this spec says otherwise.

**Acceptance criteria:**
- [ ] Vanilla attack timing, critical hits, shields, knockback and bows/crossbows behave as in vanilla 1.20.1, except where another requirement changes them.

**Open questions:**
**Notes:** Your words: "Default minecraft mechanics with like an additional roll to use and dodge."

### CMBT-002 — Dodge roll
**Status:** draft
**Priority:** must

**Description:** Players can do a quick forward roll to get out of the way of attacks.

**Acceptance criteria:**
- [ ] Pressing the roll key makes the player roll forward (distance, cooldown and cost: Q-055).
- [ ] The roll key defaults to T and can be rebound.
- [ ] The pack moves vanilla's "open chat" default from T to Enter, so the two don't clash.
- [ ] Whether the roll avoids damage while it happens follows Q-055.
- [ ] A downed player (SURV-001) can't roll.

**Open questions:** Q-055
**Notes:** Decided 2026-11-01: "just a roll, going forward. Bound on T in beginning."

### CMBT-003 — Trinkets add fighting abilities
**Status:** draft
**Priority:** should

**Description:** Players have many extra equipment slots for trinkets, organised by body part, provided by an existing trinket mod. Trinkets are found throughout the world and give additional fighting abilities or powers.

**Acceptance criteria:**
- [ ] Players have about 20 trinket slots by body part, in addition to vanilla armor and offhand slots, provided by an existing trinket mod (which one: Q-056).
- [ ] Trinkets are found in the world (where exactly: gear spec).
- [ ] An equipped trinket grants its ability or power; removing it takes the ability away.
- [ ] Locked trinkets can't be equipped (PROG-002).
- [ ] Some trinkets can boost the downed pistol (SURV-001).

**Open questions:** Q-056
**Notes:** Your words: "Some minecraft mods have already in use a trinket mod which gives like 20 body slots." The well-known one for Forge 1.20.1 is Curios API; check its license before deciding (minecraft-design skill). Individual trinkets belong in the gear spec.
