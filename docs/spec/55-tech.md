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

#### Q-159 options (proposal, nothing decided)

*My research for Q-159, checked 2026-11-03. TECH-001 still stands and Q-159 stays open until you choose. Nothing was run in-game or profiled: lag evidence comes from issue trackers and from reading the code. File dates match on Modrinth and CurseForge, except Pipez (Modrinth only). Re-check licenses when the pack is assembled.*

**The mods.** The pack joins CurseForge's Rewards Program, so a license must allow a pack that earns money (PACK-007).

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
| 3. Build our own | One machine base with item, fluid and energy handlers through [Forge capabilities](https://docs.minecraftforge.net/en/1.20.x/datastorage/capabilities/) (`ForgeCapabilities`, `IItemHandler`, `IFluidHandler`, `IEnergyStorage`), one recipe type in data files, then Crusher, Mixer, Press and Generator on top, and one pipe | About 2,000–4,000 lines plus art and recipes, so about a third of the mod today (9,703 lines in 3 days): very roughly 1–2 days plus balancing. Pipes are the biggest risk | Yes: nothing extra exists | In our hands, and we have to measure it | **Recommended** |

**Recommendation (proposal): path 3, our own machines, reusing what's free to reuse.** It's the only path that really ends at 3–4 machines. The mods we may legally use bring far more to hide and still no Mixer, and the one that matches the sketch can't go in a pack that earns money. "Pieced together from existing tech mods" survives as code instead of jars: Energized Power's machine classes and Refined Pipes (both MIT) and Ender IO (CC0) can be reused, with credit in `CREDITS.md`. No art from them (Energized Power's flashbulbs textures are unverified), and no code from Immersive Engineering, Thermal, Pipez or any GPL/LGPL mod (PACK-002).

- **Lag:** only machines tick, and they sleep when there's nothing to do. The pipe is a plain block with no block entity. A machine with output walks the connected pipes (cached until a pipe changes) and pushes into whatever accepts it, so recipes act as the filter. The cost: no items visibly moving through pipes. Machines only speak Forge's standard handlers, so Fast Pipes could replace our pipe later without changing a machine.
- **Later planets:** they add recipes, not machines, unless a chain truly needs one. We write every recipe, so TECH-003's no-duplication check is a list we can read.
- **Early electricity (the second half of Q-159):** it would change. With Energized Power gone, the Generator, one wire and the batteries are ours too, still on Forge Energy, so TRANS-003 and GEAR-005 keep working. The catch: the Floodlight, Electric Fence and Alarm (Chapter 2 in the pool below) need power long before Earth's late game, so the Generator would arrive early and the other three late. Your call.
- **If you accept, these change (nothing has yet):** TECH-001, Q-096 (closes), the pool below (Ammo Press and Fuel Refinery could become Press and Mixer recipes), the notes in TECH-004 and SURV-008 (auto crafters and growth chamber, so food automation needs a new answer), PACK-004, PACK-005, the transport summary and the roadmap row. Energized Power isn't in the dev build or `CREDITS.md`, so nothing has to be removed.
- **Effort is an estimate.** What I compared against, on Energized Power's 1.20.1 branch: Crusher 26 KB, Coal Engine 29 KB, fluid pipe 36 KB, shared machine classes 103 KB, the whole mod 3 MB. Whole pipe mods: Pipez 313 KB, Refined Pipes 332 KB, Fast Pipes 625 KB, all with tiers, filters and upgrades we wouldn't build.

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
