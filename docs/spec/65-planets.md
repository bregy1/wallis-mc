# 65 — Planets and travel

**Area code:** PLANET
**Owner:** bregy1
**Status of this file:** draft

## Summary
How planets work and how players travel between them. So far this file only holds the Moon's air and rocket fuel. The rocket build, the Moon itself, later planets and the other travel rules are still to be written (see the roadmap).

## Requirements

### PLANET-001 — No air on the Moon
**Status:** draft (your idea, 2026-11-03)
**Priority:** must

**Description:** The Moon has no air, so players need a breathing trinket. It stays simple: wear it and you can breathe. It never runs out and needs no charging.

**Acceptance criteria:**
- [ ] A player wearing the trinket keeps full air on the Moon for as long as they wear it.
- [ ] When a player dies, the trinket stays on them and doesn't go in the grave (SURV-004), so they respawn able to breathe.
- [ ] Without the trinket, a player on the Moon runs out of air as if drowning (vanilla's bubbles), then takes damage until they go down (SURV-001). (Proposal.)
- [ ] It's worn in a trinket slot (CMBT-003), so it takes up a place another trinket could use. (Proposal: a head slot.)
- [ ] The whole Moon counts as airless, inside the alien base and player bases too, so there are no sealed rooms to build or check. (Proposal.)
- [ ] Every player gets the trinket before the first launch, from a quest in the Launch tier that also unlocks it (PROG-002). (Proposal.)
- [ ] A player who respawns on the Moon without one gets a new one, so losing it can never become a death loop. (Proposal.)

**Open questions:** Q-166
**Notes:** Your words: "Then we havent talked about air, so on the moon player actually nees a trinket to breath or smth." Decided 2026-11-03 (answers to Q-162 and Q-163). A time limit on the air could still be added later without changing the rest. Name proposal: Rebreather (or Air Mask, Moon Mask). Later planets decide for themselves whether they have air. Build note: graves catch everything that drops on death (`Graves.onDrops`), so check how Curios can keep an item on death before building this.

### PLANET-002 — Rockets run on fuel
**Status:** draft (your idea, 2026-11-03)
**Priority:** must

**Description:** The rest of tech runs on electricity, but rockets stay on fuel. Every trip needs a set amount of fuel to start, in both directions. Fuel can be acquired on every planet, so a team can always fuel its next trip.

**Acceptance criteria:**
- [ ] A rocket trip only starts with the amount of fuel that trip needs; electricity can't replace it.
- [ ] Every trip uses fuel, including the way back to an earlier planet.
- [ ] Every planet has a way to get fuel, so nobody gets stranded.
- [ ] A planet's way to get fuel works with what players can have there. On the Moon, for example, nothing that burns can power it (TECH-007). (Proposal.)

**Open questions:** Q-168
**Notes:** Your words: "For rockets i still want to stick to fuel." and "Rockets, a trip literally neds a certain amount of fuel to be started. We have to make sure fuel can be acquired on every planet though." Decided 2026-11-03 (answer to Q-165). Amounts come with the rocket; proposal: longer trips need more. Rockets still fire on the Moon even though burners don't run there, because they carry what they need to burn. On Earth the fuel comes from the Fuel Refinery (pool in 55-tech.md) and needs a Nether ingredient (WORLD-008). STORY-018's rules for the first return to Earth still apply.
