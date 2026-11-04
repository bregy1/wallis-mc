# 68 — Transport

**Area code:** TRANS
**Owner:** bregy1
**Status of this file:** draft

## Summary
Getting around grows with progression: teleporting between safe spots from the start, horses early on, and electric flying vehicles later on Earth. Vehicles run on electricity, in line with the pack's electric tech (TECH-007). They can't travel between districts; garages at safe spots store them and hand them out again.

## Requirements

### TRANS-001 — Safe spot teleport
**Status:** approved
**Priority:** must

**Description:** Players can teleport between safe spots. Defined in WORLD-004; listed here so all transport is in one place.

**Acceptance criteria:**
- [x] See WORLD-004.

**Open questions:** Q-067
**Notes:**

### TRANS-002 — Horses early on
**Status:** approved
**Priority:** must

**Description:** Early in the game, players get around on horses.

**Acceptance criteria:**
- [ ] Horses (and donkeys and mules) can be found, tamed and ridden as in vanilla.
- [ ] Horses are the only early mount.
- [x] Some trinkets improve the player's horse (e.g. speed or jump). (Worn Spurs, Lucky Horseshoe.)

**Open questions:**
**Notes:** Your words: "something like horses" and (answer to Q-097) "horse is early mount, it can be tuned with trinkets too!"

### TRANS-003 — Electric flying vehicles later
**Status:** draft
**Priority:** must

**Description:** Later in the game, players get flying vehicles such as airplanes. They run on electricity.

**Acceptance criteria:**
- [ ] At least one flying vehicle exists, unlocked in district 2 or 3, in whichever district first has terrain that calls for it (PROG-013).
- [ ] Flying vehicles are powered by electricity (Forge Energy), charged from the pack's power system.
- [ ] Vehicles can't travel between districts; they move between safe spots only through garages (TRANS-004).

**Open questions:** Q-098, Q-102
**Notes:** Your words: "later flight thingies, like some airplanes or whatever. It should also be based on electricity." Decided 2026-11-01: Immersive Aircraft, trimmed to 1–2 aircraft with the armed Warship disabled, engines made electric (PACK-004). Aircraft come later on Earth, in version 1: "We want it to be accessible at some point." Then narrowed (answer to Q-097): "district 2 or 3 we should get flying thingies, as the district requires it."

### TRANS-004 — Garages at safe spots
**Status:** draft
**Priority:** must

**Description:** Vehicles can't travel between districts. Instead, safe spots have a garage building where vehicles are stored and taken out again. A garage has a door, a display showing all stored vehicles, and a terminal: pick a vehicle at the terminal and it appears in the open garage. Players unlock the garage key together with their first vehicle, later in the game. A quest has players power the garage, after which it can be used everywhere.

**Acceptance criteria:**
- [ ] Every safe spot has a garage with a door, a display of stored vehicles, and a terminal (or which safe spots: Q-104).
- [ ] The garage key unlocks together with the first vehicle.
- [ ] With the key, players can store a vehicle in a garage.
- [ ] At the terminal, players pick a stored vehicle, and it appears inside the open garage.
- [ ] A quest has the team power the garage. After that, any safe spot garage can hand out any of the team's stored vehicles.
- [ ] Stored vehicles are shared by the team, like shared storage.
- [ ] What garages can do before the power quest follows Q-104.

**Open questions:** Q-104
**Notes:** Your words: "Garage is just a display for storing all vehicles... We can just access garage terminal and given vehicle spawns in the open garage. One quest has also to be to power the garage, so it is usable everywhere." Built in our own mod (PACK-005).
