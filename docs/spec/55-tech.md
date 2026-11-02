# 55 — Tech and automation

**Area code:** TECH
**Owner:** bregy1
**Status of this file:** draft

## Summary
Tech runs on electricity. Energized Power provides the general machines and the power system (generators, batteries, cables, trimmed to a few tiers). Our own mod adds story machines: the machines a chapter needs, arriving when the story needs them. Automation stays small and purposeful (ammo, fuel, food), never endless item factories.

## Requirements

### TECH-001 — Energized Power is the machine and power base
**Status:** draft
**Priority:** must

**Description:** General processing machines (powered furnace, crusher, sawmill, presses, alloy smelter, auto crafter, fluid machines, growth chamber…) and power (generators, batteries, cables) come from Energized Power.

**Acceptance criteria:**
- [ ] Energized Power is in the pack (PACK-004).
- [ ] Its power blocks are trimmed to a few tiers, and unused machines are disabled (which: Q-096).

**Open questions:** Q-096
**Notes:**

### TECH-002 — Story machines arrive when the story needs them
**Status:** draft
**Priority:** should

**Description:** Our mod adds machines that each solve a concrete problem of the current chapter, unlocked by that chapter's story. That gives a machine experience that grows along the way instead of a big tech tree to grind.

**Acceptance criteria:**
- [ ] Every story machine names the problem it solves and the chapter that unlocks it.
- [ ] Which story machines exist follows Q-100 (idea pool below).

**Open questions:** Q-100
**Notes:** Your question: "Any ideas for a fulfilling machine-like experience along the way?"

### TECH-003 — Small, purposeful automation
**Status:** draft
**Priority:** must

**Description:** Automation exists for concrete needs like ammo, fuel and food. Nothing in the pack duplicates items, and no machine creates resources from nothing beyond what's defined on purpose (farms, generators).

**Acceptance criteria:**
- [ ] No machine or combination of machines in the pack duplicates items.
- [ ] Every renewable resource source is listed in this spec on purpose.

**Open questions:**
**Notes:** Your words: "no crazy create pipelines creating the same item x times."

### TECH-004 — Storage scales slowly
**Status:** draft
**Priority:** must

**Description:** Storage comes from Tom's Simple Storage: it links normal chests into one searchable network. It has no power use and no autocrafting, and grows only as fast as players build chests. Its parts unlock by progression like every other item.

**Acceptance criteria:**
- [ ] Tom's Simple Storage is the only storage network mod in the pack (PACK-001).
- [ ] Its terminal, connectors and other parts are locked until their tier (PROG-002; which tier: Q-101).
- [ ] The storage network doesn't autocraft.

**Open questions:** Q-101
**Notes:** Your words: "a storage mod, not something broken, it has to be scaled slowly." Autocrafting, if any, comes only from Energized Power's auto crafters, which are locked by tier as well.

### TECH-005 — A few machines that combine
**Status:** draft (your idea, 2026-11-02; for later: Earth's late game, then on through the other planets)
**Priority:** must

**Description:** Machines are where automation lives, and the fun comes from piping them together. So there are only 3–4 basic components, each doing one classic job, that combine into pipelines. Not many machines: too many and it just gets boring. No Create. Most likely we invent our own, maybe pieced together from existing tech mods.

**Acceptance criteria:**
- [ ] There are only 3–4 basic machines, and every chain in the game is built from them.
- [ ] Any machine's output can be piped into another machine's input.
- [ ] Ammo and fuel for batteries can be fully automated this way.
- [ ] It starts in Earth's late game and more can be added on later planets, without changing what's there.

**Open questions:** Q-159
**Notes:** Your words: "It has definitely be automated but not by create, i think that is always just overpowered. Maybe we have very basic machines? [...] So i think we go with machines the most, but fun arrives when machines can be piped together anyways. Problem to be careful is having too many machines, then it just gets boring. We need 3-4 base composable components doing pipelines. The classic ones i guess, crushers, and fuel mixers etc. Not too much and no create as said. We will have to invent our own. this will be part of earth late game and go on in other planets, so we can still add it later." My first sketch, to discuss later: a **Crusher** (ore and scrap into dust and parts), a **Mixer** (dust and liquids into fuel and propellant), a **Press** (dust and parts into casings, plates and ammo), and a **Generator** (burns fuel, charges batteries), joined by one kind of pipe. So scrap goes through crusher and press to ammo, and crops or coal go through crusher, mixer and generator to full batteries. This changes TECH-001, which picked Energized Power (Q-159).

### TECH-006 — Turrets and base defense
**Status:** draft (your idea, 2026-11-02; for later)
**Priority:** must

**Description:** Players can defend their bases with turrets and similar defenses, kept in balance. They run on ammo and on batteries, which come out of the machines (TECH-005).

**Acceptance criteria:**
- [ ] Turrets and other base defenses exist, and help without winning a horde night on their own (pillar: nothing completely overpowered).
- [ ] They need ammo or battery power, so a defense depends on the base's supply chain.

**Open questions:**
**Notes:** Your words: "We will also need items like turrets and base defense. in balanced manner." Goes with Chargers and Brutes, who can break through weak walls (ENEMY-005).

## Story machine idea pool (proposals)

*My proposals for TECH-002. Each one solves something the story or survival actually needs. Generic processing stays with Energized Power.*

| Machine | Chapter | Solves | Power |
|---|---|---|---|
| Scrap Salvager | 1 Scavenger | Breaks junk loot into parts and utility points (PROG-005 salvaging). Hand-cranked | None |
| Floodlight | 2 Settler | Lights the base at night; Crawlers and the Weeper avoid light | Electric |
| Electric Fence | 2 Settler | Shocks zombies at the base perimeter; a base defense extra (PROG-005) | Electric |
| Alarm | 2 Settler | Warns when enemies approach the base on horde nights | Electric |
| Ammo Press | 3 Gunsmith | Turns metal and powder into ammo for the few TaCZ guns; the main automation goal of tier 3 | Electric |
| Scanner Station | 3 Gunsmith | Studies enemy samples to fill the enemy tracker (ENEMY-010); a Morris Byte tie-in | Electric |
| Radio Tower | 3 Gunsmith | Picks up broadcasts that point to new districts ("Radio voices") | Electric |
| Fuel Refinery | 4 Launch | Refines the Nether ingredient into rocket fuel; the big automation chain before the launch | Electric |
| Launch Pad Controller | 4 Launch | Assembles and checks the built rocket, starts the launch defense | Electric |
| Vehicle Workshop | Later | Builds and charges electric flying vehicles (TRANS-003) | Electric |
| Hardening Infuser | Later | Applies the alien hardening material to blocks (ENEMY-005) | Electric |
