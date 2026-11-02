# Wallis Ultimate — Project Instructions

## Project facts (fill these in — never guess them)

| Field | Value |
|---|---|
| Minecraft version | 1.20.1 |
| Mod loader + version | Forge 47.4.28 (recommended for 1.20.1), ForgeGradle 6 (`[6.0,6.2)`) |
| Mappings | Parchment `2023.09.03-1.20.1` (librarian plugin `org.parchmentmc.librarian.forgegradle`) |
| Java version | 17 (a JDK 17 must be installed; newer Java can't build Forge 1.20.1) |
| Mod ID | `wallis_ultimate` (display name "Wallis Ultimate") |
| Distribution | Modpack + this custom mod; selected existing mods may be forked (license permitting) |
| Base package | `wallis_ultimate` |
| License (our mod) | MIT — never copy GPL/LGPL code into it |
| Dependencies / APIs | See `docs/spec/15-modpack.md` (draft). Pack mods our code works with (FTB Quests/Teams/Library, Architectury, History Stages, Lootr, Curios, JEI) are Gradle dependencies in `build.gradle` (versions in `gradle.properties`), loaded in dev runs via `fg.deobf`; never drop jars into `run/mods` |
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
- Data generation: `./gradlew runData` (writes to `src/generated/resources/`)
- GameTests: `bash tools/gametest.sh` — starts the dev server, runs `/test runall` over RCON, prints `[GAMETEST] PASSED/FAILED` lines, exits with the number of failures. Tests live in `src/main/java/wallis_ultimate/gametest/` and use the `wallis_ultimate:platform` template. (Vanilla's separate GameTest server can't be used: it places tests at Y −60, below our Y 0 floor.)
- Hot reload: start `./gradlew runClient -PhotReload` (or `runServer -PhotReload`), edit code, then `bash tools/hotswap.sh` — changed classes (method bodies, new methods/fields/classes) are pushed into the running game over the local debug port 5005. Assets: F3+T after `./gradlew processResources`; data: `/reload`. Still needs a restart: new registry entries (blocks, items, entities), new event listeners, mixins, `mods.toml`, worldgen (also a new world). Needs JetBrains Runtime 17 at `C:/Users/Bregy/.jdks/jbrsdk-17.0.14-windows-x64-b1367.22` with `lib/hotswap/hotswap-agent.jar` (HotswapAgent 2.0.1), registered in `~/.gradle/gradle.properties` (`org.gradle.java.installations.paths`). VS Code can attach to port 5005 too.
- Server commands: `java tools/RconClient.java "<command>" ...` while `runServer` is running. Needs `enable-rcon=true`, `rcon.password=<random>` and `server-ip=127.0.0.1` in `run/server.properties` (git-ignored; never commit it).
- Decompiled sources: ForgeGradle sets them up when the project is imported into an IDE; run configs via `./gradlew genIntellijRuns` (or `genEclipseRuns` / `genVSCodeRuns`)

## Project structure

Skeleton created 2026-11-01 from the official Forge 1.20.1 MDK. Folders marked "(planned)" get created with the first feature that needs them.

```
src/main/java/wallis_ultimate/
  WallisUltimate.java   # common entrypoint (@Mod), MOD_ID constant, logger
  client/               # (planned) client-only code (renderers, screens, keybinds)
  registry/             # (planned) all registration (blocks, items, entities, etc.)
  worldgen/DistrictBorder.java  # WORLD-002: 2,000-block border on fresh worlds
  block/ item/ entity/ network/ datagen/   # (planned) one feature area per package
src/main/resources/
  META-INF/mods.toml    # mod metadata, filled from gradle.properties
  pack.mcmeta
  assets/wallis_ultimate/  # (planned) textures, models, blockstates, lang, sounds
  data/wallis_ultimate/    # our data: worldgen/placed_feature (district ores), forge/biome_modifier
  data/minecraft/          # vanilla overrides copied from vanilla 1.20.1 data: Overworld height Y 0–320
                           # (dimension_type, noise_settings), world preset without the End,
                           # world type list, emptied has_structure biome tags
src/generated/resources/   # datagen output (runData)
docs/spec/              # the specification
run/                    # dev game folder (git-ignored); run/eula.txt accepted for the dev server
```

Building needs JDK 17: run Gradle with `JAVA_HOME` pointing to `C:/Program Files/Java/jdk-17` (the default Java on this machine is newer).

## Conventions

- All registration goes through the `registry/` package — no registering from random classes.
- Client-only code lives only in `client/` and is only referenced from the client entrypoint.
- Prefer data generation over hand-written JSON.
- Mixins are a last resort; every mixin needs a comment explaining why no event/API works.
- All player-facing text goes through lang files, never hardcoded strings.
- Commit messages reference spec IDs, e.g. `feat(ITEM-003): add copper wrench`.
