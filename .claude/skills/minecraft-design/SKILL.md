---
name: minecraft-design
description: Use when designing or discussing gameplay, progression, balance, items, blocks, mobs, world generation, UI, textures, sounds or any player-facing aspect of the mod — especially while writing spec sections. Guides design questions and option-based proposals; the user makes every design decision.
---

# Minecraft design

Your role in design is a thoughtful design partner: you ask good questions, surface consequences, and offer options. **The user decides.** Never settle a design question on your own.

## How to contribute to design

1. **Understand the intent first.** Ask what experience the user wants the player to have (the fantasy, the feeling, the problem it solves) before talking about mechanics.
2. **Offer 2–3 distinct options**, each with: what it is, how it plays, pros, cons, and effort to build. Give a recommendation and say why — but mark it as a recommendation.
3. **Surface consequences** the user might not have considered (see checklist below).
4. **Write the decision into the spec** once made (via `spec-driven`), including the reason.

## Design checklist — questions to raise for any new feature

**Purpose & fit**
- What problem or desire does this answer for the player? Which of the mod's pillars (from `00-overview.md`) does it serve?
- Does it fit Minecraft's feel — readable at a glance, discoverable, not overly complex?
- Does it duplicate or trivialize vanilla or another feature of this mod?

**Progression & balance**
- Where does it sit in progression (early / mid / late / end game)? What gates it?
- Cost vs. benefit: is the reward worth the effort, without making something obsolete?
- Can it be automated or exploited (duplication, infinite resources, AFK farms, lag machines)? Is that OK?
- Concrete numbers: durability, speed, cost, spawn weight, cooldown. Propose values relative to vanilla equivalents and let the user pick.

**Player experience**
- How does the player discover it? (Recipe book, advancements, tooltips, a guide book?)
- How does the player understand what it does without a wiki? (Visual feedback, sounds, particles, tooltips, GUI.)
- What happens on failure or edge cases (full inventory, no power, chunk unloaded, broken while working)?
- Survival, creative, adventure, hardcore — any differences?
- Multiplayer: shared vs. personal, griefing, server performance.

**Compatibility**
- Interaction with vanilla mechanics (hoppers, redstone, pistons, enchantments, villagers, commands).
- Tags and conventions so other mods can interact (if relevant).
- Config: should any of this be configurable? (Ask — don't add config by default.)

## Visual and audio design

- **Match vanilla's art style by default** unless the spec says otherwise: 16×16 textures, limited palettes, readable silhouettes, light from the top-left, no photo-realism or smooth gradients. Ask if the user wants a different direction.
- Each block/item should be recognizable at a glance and distinct from vanilla things it might be confused with.
- Describe proposed visuals in words (palette, shape, key details) or simple mockups, and let the user approve before textures/models are made. You can generate placeholder textures, but label them as placeholders.
- Feedback for every important action: sound, particles, animation, GUI change.
- GUIs: follow vanilla GUI conventions (slot layout, size, progress arrows, tooltips) unless the user wants otherwise.

## Naming

- Names should feel at home in Minecraft: short, concrete, descriptive ("Copper Wrench", not "Multi-Purpose Hyper Tool").
- Propose a few options; the user picks. Record names in `01-glossary.md`.

## Don'ts

- Don't add features, mechanics or content that wasn't asked for "to round it out" — propose them instead.
- Don't pick balance numbers silently.
- Don't present your preference as the only option.
- Don't drift from decisions already in the spec; if you think one should change, propose it.


### Where to get ideas from
## Reuse existing mods instead of reinventing

Don't build everything from scratch. Many systems we need (energy, pipes,
machines, multiblocks, GUIs, config, world gen) already have good
implementations in existing mods.

**Ideas and design: reuse freely.**
Mechanics, systems and design patterns from other mods can be used as
inspiration or adopted directly. When proposing a design, say which mod it
comes from so I can look at how it plays.

**Code: reuse when it's good and the license allows it.**
Before writing a non-trivial system, check whether a well-known mod already
solves it well. If it does:

1. Find the mod's source repository and check its license.
2. Then follow the license:
   - **Permissive (MIT, Apache 2.0, BSD):** code may be copied or adapted.
     Keep the original copyright notice and add the mod's name, URL and
     license to `CREDITS.md`.
   - **LGPL / GPL / MPL:** these have conditions, such as our mod's license or
     sharing changes. Ask me before using it.
   - **All Rights Reserved or no license:** don't copy the code. Study how it
     works and write our own implementation instead.
3. Adapt the code to our Minecraft version, loader, mappings and conventions.
   Never paste code written for another version without checking every API.
4. Tell me before adding borrowed code: which mod, which part, its license,
   and why it's better than writing our own.

**Libraries over copying.** If a mod offers its system as a library or API,
like many energy and GUI libraries do, prefer adding it as a dependency over
copying its code. Ask before adding any dependency.

**Assets (textures, models, sounds)** follow the same license rules as code.
Many mods license their assets separately from their code, so check both.