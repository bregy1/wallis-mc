---
name: minecraft-modding
description: Use whenever writing, reviewing or debugging Minecraft mod code (Java, Gradle, mixins, registries, networking, data generation, resources). Covers version safety, client/server separation, registration, data and verification.
---

# Minecraft modding

Minecraft and its loaders change APIs constantly. Your training data mixes many versions, loaders and mapping sets. Treat your memory of Minecraft APIs as a hint, never as truth.

## 1. Know the target before writing code

Read the "Project facts" table in `CLAUDE.md`: Minecraft version, loader, mappings, Java version, mod ID. If any is TODO, stop and ask. Also check `build.gradle` / `gradle.properties` — if they disagree with `CLAUDE.md`, ask which is correct.

## 2. Verify every API

Before using a class, method, event or registry you're not certain exists in *this* version:

1. Search the existing code in this repo for how it's already done.
2. Search the decompiled Minecraft sources (Gradle cache / `genSources` output) and the loader's sources.
3. Check how vanilla does the equivalent thing (e.g. look at a vanilla block before writing a similar one).
4. Check the loader's official docs if needed.

If you can't verify it, say so and ask — don't guess a method name and "see if it compiles". Know the classic version pitfalls: e.g. NBT vs. data components on item stacks, registry/holder changes, renamed render and networking APIs, and differences between Mojmap and Yarn names. When in doubt, look it up.

## 3. Client vs. server — the #1 source of crashes

- Logic runs on the server; the client only displays. Never trust the client.
- Client-only classes (rendering, screens, keybinds, `Minecraft` client instance) live only in the `client/` package and are only touched from the client entrypoint or client-only events.
- Common code must never import client classes, even if it compiles. Run `runServer` to catch this.
- Check `level.isClientSide()` (or equivalent) before doing world changes.
- Anything the client needs to show (machine progress, energy, custom data) must be explicitly synced — via menus/containers data slots, block entity update packets, or custom payloads. Ask which approach if the spec doesn't say.

## 4. Registration

- All registration goes through the `registry/` package using the loader's standard mechanism (e.g. deferred registers / registry helpers). No static registration from random classes.
- Registry IDs are lowercase snake_case under the mod ID and are **permanent** — renaming one breaks existing worlds. Never rename or remove one without asking.
- Every registered thing needs its full set of assets/data: model, blockstate, item model, lang entry, loot table, recipe if applicable, tags. Missing ones are bugs. Prefer datagen to produce them.

## 5. Data, resources and datagen

- Prefer data generation over hand-written JSON for models, blockstates, loot tables, recipes, tags, lang, advancements.
- After changing datagen code, run the datagen task and check the generated files.
- Use tags (both vanilla and conventional/common tags) instead of hardcoding specific items where the spec allows.
- All player-facing text goes through lang keys.

## 6. Code structure and quality

- One feature area per package (`block/`, `item/`, `machine/`, …); follow the structure in `CLAUDE.md`.
- Keep game constants (ticks, costs, ranges) in one place per feature, matching the spec values, so balance changes are easy.
- Prefer events/loader APIs over mixins. A mixin needs a comment explaining why nothing else works, the narrowest possible injection, and the user's approval before adding it.
- Block entities: keep tick logic lean, avoid per-tick allocations and world scans; save/load all persistent state.
- Don't add libraries/dependencies without asking.
- Think about multiplayer, chunk unloading and world reload for anything stateful.

## 7. Verify after every change

1. `./gradlew build` — must pass with no errors. Read warnings; mention relevant ones.
2. `runData` if data/datagen changed.
3. `runServer` for anything touching common code, networking, or new registrations.
4. `runClient` for rendering, UI, interaction. You usually can't play the game yourself, so give the user a short, concrete in-game test checklist tied to the spec's acceptance criteria.
5. Read crash logs and `latest.log` fully before fixing; fix the cause, not the symptom. If the fix changes behaviour, ask first.

## 8. Reporting

After each task: files changed, spec IDs covered, how it was verified, the in-game test steps for the user, and any open questions or unverified APIs.
