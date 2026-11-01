# Wallis Ultimate — Project Instructions

## Project facts (fill these in — never guess them)

| Field | Value |
|---|---|
| Minecraft version | 1.20.1 |
| Mod loader + version | Forge (exact Forge version TODO) |
| Mappings | Parchment |
| Java version | 17 |
| Mod ID | `wallis_ultimate` (display name "Wallis Ultimate") |
| Distribution | Modpack + this custom mod; selected existing mods may be forked (license permitting) |
| Base package | `wallis_ultimate` |
| License (our mod) | MIT — never copy GPL/LGPL code into it |
| Dependencies / APIs | See `docs/spec/15-modpack.md` (draft) |
| Repository | https://github.com/bregy1/wallis-mc (project name "wallis-ultimate") |

If any field above still says TODO, ask me before writing code that depends on it.

## Non-negotiable rules

1. **The spec is the source of truth.** The spec lives in `docs/spec/`. Never implement anything that isn't in an approved spec section. If a task needs something the spec doesn't cover, stop and propose a spec change first. Follow the `spec-driven` skill.
2. **Ask, don't assume.** If something is ambiguous, missing, or could reasonably go two ways, ask me before acting. Don't fill gaps with your own preferences. Follow the `ask-first` skill.
3. **Never invent Minecraft or loader APIs.** Check the decompiled sources, the loader docs, or existing code in this repo before using any class or method you're not certain exists in *this* Minecraft version. Follow the `minecraft-modding` skill.
4. **Design decisions are mine.** For anything affecting gameplay, balance, visuals or player experience, present options with trade-offs and let me choose. Follow the `minecraft-design` skill.
5. **Small, verified steps.** Build after every meaningful change. Don't stack untested changes.
6. **Story content follows the `story` skill.** Plot, characters, dialogue, quests, cutscenes and lore.

## Workflow for every task

1. Read the relevant spec section(s) and quote the requirement IDs you're working on.
2. List anything unclear or not covered → ask me (batch the questions).
3. Propose a short plan → wait for my OK on anything non-trivial.
4. Implement in small steps.
5. Run `./gradlew build` (and `runClient` / `runServer` when relevant). Fix all errors.
6. Report back: what changed, which spec IDs are now covered, anything left open.

## Commands

- Build: `./gradlew build`
- Run client: `./gradlew runClient`
- Run server: `./gradlew runServer`  (use this to catch client-only code leaking onto the server)
- Data generation: `./gradlew runData` (task name varies by loader — check `build.gradle`)
- Decompiled sources: TODO (e.g. `./gradlew genSources` for Fabric Loom)

## Project structure

TODO — fill in once the skeleton exists. Example:

```
src/main/java/<package>/
  MyMod.java            # common entrypoint
  client/               # client-only code (renderers, screens, keybinds)
  registry/             # all registration (blocks, items, entities, etc.)
  block/  item/  entity/  network/  worldgen/  datagen/
src/main/resources/
  assets/<modid>/       # textures, models, blockstates, lang, sounds
  data/<modid>/         # recipes, loot tables, tags, worldgen (prefer datagen)
docs/spec/              # the specification
```

## Conventions

- All registration goes through the `registry/` package — no registering from random classes.
- Client-only code lives only in `client/` and is only referenced from the client entrypoint.
- Prefer data generation over hand-written JSON.
- Mixins are a last resort; every mixin needs a comment explaining why no event/API works.
- All player-facing text goes through lang files, never hardcoded strings.
- Commit messages reference spec IDs, e.g. `feat(ITEM-003): add copper wrench`.
