# 55 — Tech and automation

**Area code:** TECH
**Owner:** bregy1
**Status of this file:** draft

## Summary
Tech runs on electricity, and power is its biggest part: different generators, each with its own trade-off. All of it is in our own mod, with no tech mod in the pack. Machines are few and get dropped in along the way, when a chapter needs them, and each may just do one thing. Rockets are the exception and stay on fuel (PLANET-002). Automation stays small and purposeful (ammo, fuel, food), never endless item factories.

## Requirements

### TECH-001 — Energized Power is the machine and power base
**Status:** deprecated (2026-11-03, answer to Q-159: our own machines and power instead, see TECH-005 and TECH-007)
**Priority:** must

**Description:** General processing machines (powered furnace, crusher, sawmill, presses, alloy smelter, auto crafter, fluid machines, growth chamber…) and power (generators, batteries, cables) come from Energized Power.

**Acceptance criteria:**
- [ ] Energized Power is in the pack (PACK-004).
- [ ] Its power blocks are trimmed to a few tiers, and unused machines are disabled (which: Q-096).

**Open questions:**
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
**Notes:** Your words: "no crazy create pipelines creating the same item x times." Food is automated by workers rather than machines (WORK-002).

### TECH-004 — Storage scales slowly
**Status:** draft
**Priority:** must

**Description:** Storage comes from Tom's Simple Storage: it links normal chests into one searchable network. It has no power use and no autocrafting, and grows only as fast as players build chests. Its parts unlock by progression like every other item.

**Acceptance criteria:**
- [ ] Tom's Simple Storage is the only storage network mod in the pack (PACK-001).
- [ ] Its terminal, connectors and other parts are locked until their tier (PROG-002; which tier: Q-101).
- [ ] The storage network doesn't autocraft.

**Open questions:** Q-101
**Notes:** Your words: "a storage mod, not something broken, it has to be scaled slowly." With Energized Power dropped (Q-159), the only autocrafting left is the Crafter worker (WORK-002), who only makes what the team has unlocked.

### TECH-005 — A few machines, dropped in along the way
**Status:** draft (your idea, 2026-11-02; changed 2026-11-03 by your answers to Q-159)
**Priority:** must

**Description:** Machines are where automation lives, and the fun comes from piping them together. But there are only a few, and they're our own. We don't plan a whole system up front: a machine gets dropped in when something along the way needs it, and it's completely fine if it just does one thing and works. If a good system grows out of that, great. Too many machines and it just gets boring. No Create.

**Acceptance criteria:**
- [ ] There are only a few machines, and each one names the need it was added for.
- [ ] A machine can do just one thing; nothing has to fit a bigger system.
- [ ] Any machine's output can be piped into another machine's input.
- [ ] Ammo and fuel can be fully automated this way.
- [ ] Machines arrive along the way, and adding one never changes the ones already there.
- [ ] All machines live in `wallis_ultimate`, with no tech mod in the pack.

**Open questions:**
**Notes:** Your words: "It has definitely be automated but not by create, i think that is always just overpowered. Maybe we have very basic machines? [...] So i think we go with machines the most, but fun arrives when machines can be piped together anyways. Problem to be careful is having too many machines, then it just gets boring. We need 3-4 base composable components doing pipelines. The classic ones i guess, crushers, and fuel mixers etc. Not too much and no create as said. We will have to invent our own. this will be part of earth late game and go on in other planets, so we can still add it later." My first sketch, now a list of ideas rather than a plan: a **Crusher** (ore and scrap into dust and parts), a **Mixer** (dust and liquids into fuel and propellant), a **Press** (dust and parts into casings, plates and ammo), and a **Generator** (burns fuel, charges batteries), joined by one kind of pipe. So scrap goes through crusher and press to ammo, and crops or coal go through crusher, mixer and generator to full batteries. Your words, 2026-11-03: "lets not go too crazy on machines. We drop them in along the way and might end up with a good system. It is also compleytely okay if we only have few machines and they just do one thing and work." Proposal: the first machines to drop in are the pool's Ammo Press and Fuel Refinery (below), since each already does one thing.

#### Q-159 research (you chose path 3, 2026-11-03)

*My research for Q-159, checked 2026-11-03, kept as the reasons behind the choice. Nothing was run in-game or profiled: lag evidence comes from issue trackers and from reading the code. File dates match on Modrinth and CurseForge, except Pipez (Modrinth only). Re-check licenses before reusing any of these.*

**The mods.** Written when the pack was going to earn money through CurseForge. It's now only shared with friends (PACK-007), so "Out" below no longer holds for mods that only rule out earning money, such as Immersive Engineering. Your choice of our own machines stands unless you change it.

| Mod | License; pack that earns money? | Last Forge 1.20.1 file | Pipes | Down to 3–4 machines? | Lag evidence | Verdict |
|---|---|---|---|---|---|---|
| [Energized Power](https://modrinth.com/mod/energized-power) | MIT, [pack use welcomed](https://www.curseforge.com/minecraft/mc-mods/energized-power). Yes | 2.15.22, 2026-05-18 | Fluid pipes, item conveyor belts and cables: three systems | Hard: 118 blocks, and its [config](https://github.com/JDDev0/EnergizedPower/blob/1.20.1/src/main/java/me/jddev0/ep/config/ModConfigs.java) has no switch to turn a machine off. [No recipe type](https://github.com/JDDev0/EnergizedPower/tree/1.20.1/src/main/java/me/jddev0/ep/recipe) makes a liquid from dust and liquid, so no Mixer | [Cables took over half the server's ticks](https://github.com/JDDev0/EnergizedPower/issues/73) at 400 cables. Fixed on its 3.x line within days, but [1.20.1 has had no commit since May](https://github.com/JDDev0/EnergizedPower/commits/1.20.1), and there [pipes still rescan every tick](https://github.com/JDDev0/EnergizedPower/blob/1.20.1/src/main/java/me/jddev0/ep/block/entity/FluidPipeBlockEntity.java) | Best of the base mods, wrong shape |
| [Mekanism](https://modrinth.com/mod/mekanism) | MIT, [modpacks allowed](https://github.com/mekanism/Mekanism/blob/1.20.4/README.md). Yes | 10.4.16.80, 2025-05-21 | Yes, its own | Hard: about 12 MB of Java, and already rejected as overpowered (PACK-006) | [Logistical transporter lag report](https://github.com/mekanism/Mekanism/issues/7983) | No |
| [Thermal Expansion](https://www.curseforge.com/minecraft/mc-mods/thermal-expansion) | [All Rights Reserved](https://github.com/CoFH/ThermalExpansion/blob/1.20.x/README.md), nothing on packs. **Out** without written permission | 11.0.1, 2024-05-26 | Separate mod, [Thermal Dynamics](https://modrinth.com/mod/thermal-dynamics) | Modular; not checked further | Not checked | Out (license) |
| [Immersive Engineering](https://www.curseforge.com/minecraft/mc-mods/immersive-engineering) | Custom: [no monetizing and no publishing forks](https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/LICENSE), except CurseForge's "Points"; its page's modpack policy also rules out monetizing other people's content. **Out** without written permission | 10.2.0-183, 2025-02-18 | Yes, its own | Closest to the sketch: [Crusher, Mixer, Metal Press, Diesel Generator](https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/main/java/blusunrize/immersiveengineering/common/blocks/multiblocks/IEMultiblocks.java), and its [Mixer](https://github.com/BluSunrize/ImmersiveEngineering/blob/1.20.1/src/api/java/blusunrize/immersiveengineering/api/crafting/MixerRecipe.java) does turn dust and liquid into liquid. But they are big multiblocks, it brings its own guns (PACK-001), and no fork can remove them | No clear recent evidence found | Out (license); good to learn from |
| [Ender IO](https://modrinth.com/mod/enderio) | Public Domain: code is [CC0, but some sounds are CC BY or CC BY-NC](https://github.com/Team-EnderIO/EnderIO/blob/dev/1.20.1/README.md), to check. Probably | 6.2.18-beta, 2026-03-08 | Conduits for items, fluids and energy that share one block | Hard: also armor, weapons, tools, mobs and enchantments | A [third-party optimizer](https://modrinth.com/mod/ender-io-conduit-optimizer) exists because item conduits can lag at scale | No (too broad) |
| [Fast Pipes](https://www.curseforge.com/minecraft/mc-mods/fast-pipes) | MIT. Yes | 1.3.9, 2026-09-29 | Pipes only: items, fluids, energy, up to 5 tiers | No machines | Claims no needless ticking; unmeasured. [One author](https://github.com/bigenergy/fastpipes/tree/backport/1.20.1-forge), about 17,000 CurseForge downloads | Fallback for pipes |
| [Pipez](https://modrinth.com/mod/pipez) | [All Rights Reserved](https://www.curseforge.com/minecraft/mc-mods/pipez), nothing on packs. **Out** without written permission | 1.2.26, 2025-08-30 | Pipes only | No machines | By its page, only pipes that pull have block entities; one [2022 report](https://github.com/henkelmax/pipez/issues/139) of high tick time | Out (license); good idea to imitate |

Also checked: no Forge 1.20.1 files for [Modern Industrialization](https://modrinth.com/mod/modern-industrialization), [Tech Reborn](https://modrinth.com/mod/techreborn) or [Oritech](https://modrinth.com/mod/oritech). [Industrial Foregoing](https://modrinth.com/mod/industrial-foregoing) (MIT) is already rejected (PACK-006). [GregTech CEu](https://modrinth.com/mod/gregtechceu-modern) (LGPL) is the opposite of few machines. [Electrodynamics](https://github.com/aurilisdev/Electrodynamics/blob/1.20/LICENSE.txt) has a custom license, all rights reserved unless stated. [Powah](https://modrinth.com/mod/powah) (LGPL) is power only, with reactors. [Scrap Tech Workshop](https://modrinth.com/mod/scrap-tech-workshop) (MIT) fits our theme and is worth reading for ideas, but it is a recycler without pipes, a beta from a 2025 mod jam. Older pipes: [Pretty Pipes](https://modrinth.com/mod/pretty-pipes) (1.20.1 file from 2023), [LaserIO](https://www.curseforge.com/minecraft/mc-mods/laserio) (2024), [Refined Pipes](https://github.com/refinedmods/refinedpipes) (archived).

**The three paths.**

| Path | What it takes | Rough effort | Really 3–4 machines? | Lag | Verdict |
|---|---|---|---|---|---|
| 1. Trim one mod (Energized Power) | Remove recipes and hide about 110 blocks; add ammo and fuel recipes in data files (its machines take those, output from every face and use Forge Energy, which PACK-004 had as unverified). The Mixer still needs our code. A clean trim needs a fork: most of its 647 classes removed, as its own CurseForge project (PACK-002) | About a day of data work plus the Mixer; a fork is much more | Only on the surface: the rest stays registered | Depends on 1.20.1 code without October's fixes, unless a fork backports them | No |
| 2. Combine mods (for example Energized Power + Fast Pipes) | Path 1, plus a second jar to trim and two looks to match. Energized Power's own pipes and cables then overlap (PACK-001) | Path 1 plus fitting them together | Worse: more to hide | Two mods to profile | No |
| 3. Build our own | One machine base with item, fluid and energy handlers through [Forge capabilities](https://docs.minecraftforge.net/en/1.20.x/datastorage/capabilities/) (`ForgeCapabilities`, `IItemHandler`, `IFluidHandler`, `IEnergyStorage`), one recipe type in data files, then Crusher, Mixer, Press and Generator on top, and one pipe | About 2,000–4,000 lines plus art and recipes, so about a third of the mod today (9,703 lines in 3 days): very roughly 1–2 days plus balancing, now spread out because machines arrive one at a time. Pipes are the biggest risk | Yes: nothing extra exists | In our hands, and we have to measure it | **Chosen** |

**Outcome (your answer, 2026-11-03): path 3, our own machines, reusing what's free to reuse.** It's the only path that really keeps the machines few. The mods we may legally use bring far more to hide and still no Mixer, and the one that matches the sketch can't go in a pack that earns money. "Pieced together from existing tech mods" survives as code instead of jars: Energized Power's machine classes and Refined Pipes (both MIT) and Ender IO (CC0) can be reused, with credit in `CREDITS.md`. No art from them (Energized Power's flashbulbs textures are unverified), and no code from Immersive Engineering, Thermal, Pipez or any GPL/LGPL mod (PACK-002).

- **Lag (proposal):** only machines tick, and they sleep when there's nothing to do. The pipe is a plain block with no block entity. A machine with output walks the connected pipes (cached until a pipe changes) and pushes into whatever accepts it, so recipes act as the filter. The cost: no items visibly moving through pipes. Machines only speak Forge's standard handlers, so Fast Pipes could replace our pipe later without changing a machine.
- **Later planets (proposal):** new recipes first, and a new machine only when a need calls for it. We write every recipe, so TECH-003's no-duplication check is a list we can read.
- **Early electricity (the second half of Q-159):** it changes, and the first generator arrives early (TECH-007).
- **What changed with this:** TECH-001 and Q-096 are closed. The pool below, the notes in TECH-004 and SURV-008, PACK-004, PACK-005, PACK-006, the transport summary and the roadmap row are updated. Food automation moved from the growth chamber to workers (Q-167, WORK-002). Energized Power wasn't in the dev build or `CREDITS.md`, so nothing had to be removed.
- **Effort is an estimate.** What I compared against, on Energized Power's 1.20.1 branch: Crusher 26 KB, Coal Engine 29 KB, fluid pipe 36 KB, shared machine classes 103 KB, the whole mod 3 MB. Whole pipe mods: Pipez 313 KB, Refined Pipes 332 KB, Fast Pipes 625 KB, all with tiers, filters and upgrades we wouldn't build.

### TECH-006 — Turrets and base defense
**Status:** draft (your idea, 2026-11-02; for later)
**Priority:** must

**Description:** Players can defend their bases with turrets and similar defenses, kept in balance. They run on ammo and on batteries, which come out of the machines (TECH-005, TECH-007).

**Acceptance criteria:**
- [ ] Turrets and other base defenses exist, and help without winning a horde night on their own (pillar: nothing completely overpowered).
- [ ] They need ammo or battery power, so a defense depends on the base's supply chain.

**Open questions:**
**Notes:** Your words: "We will also need items like turrets and base defense. in balanced manner." Goes with Chargers and Brutes, who can break through weak walls (ENEMY-005).

### TECH-007 — Power is the biggest part: generators with trade-offs
**Status:** draft (your idea, 2026-11-03)
**Priority:** must

**Description:** Power is the biggest part of tech. Several generators exist, each with its own trade-off, so which one you build depends on what you need. The first one arrives early on Earth, long before most other machines. Power is our own, on Forge Energy, stored in batteries and carried by one kind of wire.

**Acceptance criteria:**
- [ ] The first generator arrives no later than the first machine that needs power (Chapter 2 in the pool below).
- [ ] Several generators exist, each with a clear trade-off in strength, fuel, noise or where it works (pool below).
- [ ] Loud generators pull zombies like other noise does (ENEMY-006).
- [ ] Generators that burn something don't run on the Moon, which has no air (PLANET-001).
- [ ] All power is Forge Energy, so aircraft (TRANS-003) and electric guns (GEAR-005) charge from it.
- [ ] Batteries store power and one kind of wire carries it.

**Open questions:**
**Notes:** Your words: "yeah generator kinda early then!" and "I think power is the biggest, we have different generators for different things." Trade-offs chosen over one generator per use or per planet (Q-164). Loud generators are inspired by Project Zomboid, where a running generator draws zombies.

**Generator pool (proposal; names and numbers are yours to veto):**

| Generator | Chapter | Runs on | Trade-off |
|---|---|---|---|
| Hand Crank | 2 Settler, first | A player turning it | Weak, but quiet and free; someone has to stand there |
| Burner | 2 Settler | Coal, wood or crops | Steady, but loud (pulls zombies from about 16 blocks) and needs feeding. Doesn't run on the Moon |
| Solar Panel | 3 Gunsmith | Daylight | Quiet and free, but weak and stops at night. Salvaged from the old civilisation's ruins. The only one that runs by itself on the Moon |
| Fuel Generator | 4 Launch | Refined fuel through pipes | Strongest, but very loud (about 32 blocks, like a gunshot), and the reason to automate fuel. Doesn't run on the Moon |

## Story machine idea pool (proposals)

*My proposals for TECH-002. Each one solves something the story or survival actually needs. This is also where machines get dropped in from (TECH-005); generic processing only arrives if something needs it.*

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
