# 70 — Presentation

**Area code:** PRES
**Owner:** bregy1
**Status of this file:** draft

## Summary
How the game sounds and looks around the gameplay: music, voices, textures, the main menu and the loading screen. The loading screen and menu are for later; the rest is being built now, after your first play (2026-11-04).

## Requirements

### PRES-001 — Themed background music
**Status:** approved 2026-11-04 for Earth (your note after your first play: "Also no music yet :)"); other planets come with them
**Priority:** should

**Description:** Every theme in the game has some background music that fits it, in the spirit of "every planet follows a theme and has a vibe" (see Pillars in the overview). Nothing too crazy, just some background music that's nice to have on.

**Acceptance criteria:**
- [ ] Each planet has background music matching its theme.
- [ ] It stays in the background: quiet, not constant, and it never gets in the way of hearing the game.
- [ ] The music is open, free and already made, not made for us (answer to Q-161).
- [ ] Every track's license allows use in a pack shared with friends that earns nothing (PACK-007). So CC0 and CC BY are fine, and so is "non-commercial" and, if the track stays unchanged, "no derivatives". Each track and its author are listed in CREDITS.md.

**Open questions:**
**Notes:** Your words: "also, overall we want also music for the themings, nothing too crazy but some background music is nice." And on the source: "yeah for music we want open and free already built ideally." On Earth it could share its style with the menu music in PRES-002: redneck, but still apocalyptic.

### PRES-002 — Own loading screen and main menu
**Status:** draft (your idea, 2026-11-02; for later)
**Priority:** should

**Description:** The loading screen and main menu set the mood: darker, grayer colors, slow fight music in a redneck style that's still apocalyptic, and a nice title image.

**Acceptance criteria:**
- [ ] The loading screen and main menu are our own, in darker, grayer colors.
- [ ] The menu plays slow fight music, redneck in style but still apocalyptic.
- [ ] The title image shows a group of rednecks sitting around a fire, with materials and half-destroyed futuristic machines around them, one with a guitar, one with a gun, and a few dead around who got shot, undead and alive.

**Open questions:**
**Notes:** The music follows the same rule as in PRES-001: open, free and already made. Your words: "We want some slow fight music, giving it a redneck style but still apocalyptic. We want darker, grayer colors and a nice title image. Maybe a group of rednecks sitting around a fire, then some materials or futuristic, half destroyed machines around. One with a guitar, one with a gun, maybe a few deads around, which got shot. Undead and alive etc." The fiction disclaimer on the title screen stays (STORY-014).

### PRES-003 — You can hear them talk
**Status:** approved 2026-11-04 (your words; which voices and how it works are my proposals)
**Priority:** should

**Description:** People in cutscenes and conversations actually speak out loud, each with a nice voice of their own. The text stays on screen too.

**Acceptance criteria:**
- [ ] Every spoken line in the opening cutscene, in conversations and on the radio plays as voice audio, in time with its text.
- [ ] Every character has their own voice that fits them, and they sound good rather than robotic as far as open tools allow.
- [ ] Moving on to the next line, or leaving a conversation, stops the voice.
- [ ] Voices have their own volume setting and can be turned off.
- [ ] Changing a line's text and running one tool makes its new audio. Nobody records anything by hand.
- [ ] Nobody's real voice is copied. The parodies (STORY-020) get a generic voice with a funny delivery, never a clone of the real person.
- [ ] The tool and voice models have licenses that fit PACK-007, and they're listed in CREDITS.md.

**Open questions:**
**Notes:** Your words: "The cutscenese, would be nice if you could hear them talking actually. Please use some nice voice !." Claude can't hear the result, so you judge it by ear and describe what to change.

### PRES-004 — A better looking game
**Status:** approved 2026-11-04 (your words; which pack is my pick, for you to veto)
**Priority:** should

**Description:** The whole game gets a nice texture pack, so it looks less blocky while it still looks like blocks. It fits the end of the world (WORLD-015).

**Acceptance criteria:**
- [ ] A texture pack covers the vanilla blocks and is on by default in a new install.
- [ ] It still looks like Minecraft: blocks stay blocks (pillar "stay a Minecraft game").
- [ ] Our own blocks and the pack's decoration mods don't look out of place next to it.
- [ ] It causes no noticeable lag (PACK-003, Q-148).
- [ ] Its license allows use with friends. If it can't be passed on directly, friends get it from its official page (PACK-007).

**Open questions:**
**Notes:** Your words: "Then general textures, do you think we can overhaul the whole thing with a nice texture pack ? So it looks less blocky, still block like but better." Mods a pack needs, such as connected textures or shaders, are new mods, so they need your OK first.
