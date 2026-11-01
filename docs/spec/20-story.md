# 20 — Story and characters

**Area code:** STORY
**Owner:** bregy1
**Status of this file:** draft

## Summary
A group of rough, foul-mouthed rednecks didn't notice the world ended — until now. Earth is overrun by an infection that seems home-grown, but is really the work of an alien species that will later follow the survivors to other planets. Along the way the player meets allies, wins over (or turns against) factions like the military, fights raiders and a mysterious group, and makes choices with consequences. Tone mixes humor and seriousness depending on planet and district. Key moments are told through cutscenes that still look like Minecraft.

## Requirements

### STORY-001 — Alien-made apocalypse, revealed later
**Status:** draft
**Priority:** must

**Description:** Earth is overrun by an infection that turns people into zombies. At first everything points to an Earth-born cause. Later in the story the player learns that an alien species brought it. The aliens' home planet can be found later in the game.

**Acceptance criteria:**
- [ ] Early story content presents the outbreak as Earth-born (what it's blamed on: Q-031).
- [ ] The alien origin is revealed at a defined story point (when: Q-032).
- [ ] The aliens' home planet is planned as a later planet in the progression.

**Open questions:** Q-031, Q-032
**Notes:** Your words: "first we think it is just from earth, but later we find out someone has inflicted the earth."

### STORY-002 — Opening: the rednecks who missed the apocalypse
**Status:** draft
**Priority:** must

**Description:** The story starts with a group of rough rednecks who didn't notice the apocalypse happened at all. They talk a lot, swear a lot, and have funny lines. The opening plays their discovery of the new world for laughs. The players are members of this group and join in the conversations by picking replies.

**Acceptance criteria:**
- [ ] The story opens with the redneck group discovering that the world has ended.
- [ ] Redneck characters have humorous, rough dialogue, including swearing.
- [ ] Swearing is on by default; a client-side option replaces swear words with symbols (e.g. "#@!%").
- [ ] Player characters are members of the redneck group and look like normal Minecraft characters (their own skin).
- [ ] In conversations, players pick from short replies: some are just for fun, some make story choices (STORY-005).
- [ ] In a team, replies are picked as defined in Q-045.

**Open questions:** Q-045
**Notes:** Decided 2026-11-01 (answers to Q-033, Q-034). Before release, check the hosting platforms' content rules on swearing.

### STORY-003 — First ally: the redneck woman
**Status:** draft
**Priority:** must

**Description:** The first ally is an attractive, confident redneck woman. Unlike the normal-looking player characters, she has her own distinct look, made in Minecraft's art style.

**Acceptance criteria:**
- [ ] She is the first named ally the player meets.
- [ ] She has a unique model or skin that's recognizable at a glance and fits Minecraft's style.

**Open questions:** Q-035, Q-044
**Notes:**

### STORY-004 — Factions whose attitude changes
**Status:** draft
**Priority:** must

**Description:** Earth has organised factions, such as the military. They start out unfriendly and can become friendly over time. Players still build their own base; factions live in their own places.

**Acceptance criteria:**
- [ ] Each faction has an attitude toward each team (levels and effects: Q-036).
- [ ] The military starts unfriendly and can become friendly.
- [ ] Faction attitude is tracked per team (consistent with PROG-006).

**Open questions:** Q-036
**Notes:**

### STORY-005 — Optional quests with choices
**Status:** draft
**Priority:** should

**Description:** Some optional quests end in a decision that turns a character or faction into a friend or an enemy.

**Acceptance criteria:**
- [ ] Some optional quests offer a choice with a lasting effect on a character or faction.
- [ ] A choice applies to the whole team.

**Open questions:** Q-037
**Notes:**

### STORY-006 — Random quests and encounters
**Status:** draft
**Priority:** should

**Description:** Besides the main story and optional quests, random quests and random enemy encounters appear.

**Acceptance criteria:**
- [ ] Random quests are offered in addition to the main and optional quests (types: Q-038).
- [ ] Random enemy encounters can happen outside the main story.

**Open questions:** Q-038
**Notes:** Random encounters overlap with the enemy spec (ENEMY) and world spec (WORLD).

### STORY-007 — Human and hidden enemies
**Status:** draft
**Priority:** must

**Description:** Besides zombies there are raiders (hostile humans) and a strange unknown group. The unknown group is later identified as the ones who brought the infection.

**Acceptance criteria:**
- [ ] Raiders appear on Earth as a hostile human faction.
- [ ] The unknown group appears before the reveal without its true nature being obvious.
- [ ] After the reveal (STORY-001), the unknown group turns out to be a cult of human collaborators, led by aliens disguised as humans.
- [ ] The leaders' true alien form is revealed in a cutscene (STORY-010).

**Open questions:**
**Notes:** Decided 2026-11-01 (answer to Q-039). Mostly human enemies to build, plus one or a few alien models.

### STORY-008 — The aliens follow
**Status:** draft
**Priority:** should

**Description:** The alien species spreads the infection to other planets. At the start only Earth is infected; at some point the aliens follow the survivors to the new planets.

**Acceptance criteria:**
- [ ] Earth is the only infected planet at the start.
- [ ] At a defined story point, the infection reaches later planets (when and how: Q-040).

**Open questions:** Q-040
**Notes:**

### STORY-009 — Tone depends on planet and district
**Status:** draft
**Priority:** should

**Description:** The mix of humor and seriousness changes by planet and by district (area within a planet). Overall the game is a good mix of both.

**Acceptance criteria:**
- [ ] Each planet and district has a defined tone in its spec (e.g. comedic, grim, mixed).

**Open questions:** Q-029
**Notes:** Districts tie into the world spec (WORLD) and Q-029.

### STORY-010 — Cutscenes that still look like Minecraft
**Status:** draft
**Priority:** should

**Description:** Key story moments are shown as cutscenes, made in-game with Minecraft's graphics. Small extra effects in animations are fine, but anyone logging in should feel "this is still Minecraft".

**Acceptance criteria:**
- [ ] Cutscenes are made in-engine with the game's own graphics and models, not pre-rendered video.
- [ ] Extra effects stay within vanilla's look (see the overview pillar).
- [ ] All team members who are present take part in a cutscene together, and each can interact (e.g. pick replies).
- [ ] Some cutscenes only start when every team member is present (marked per cutscene).
- [ ] Skipping, and what happens in the world during a cutscene, follow Q-041.

**Open questions:** Q-041
**Notes:** Your words: "we both interact in those clips. Even the whole team might be needed to activate certain cutscenes." Cutscenes are expensive to build, so they should be saved for key moments. Tooling (an existing cutscene/camera mod or our own) to be picked in the modpack spec.
