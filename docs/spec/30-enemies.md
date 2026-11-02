# 30 — Enemies and bosses

**Area code:** ENEMY
**Owner:** bregy1
**Status of this file:** draft

## Summary
Zombies are slow by day and faster at night, and early nights are deadly. Only some zombies burn in sunlight. Enemies carry status effects: weak ones on easy enemies, stronger and combined ones on harder enemies and bosses. Block breaking is super rare: regular zombies can't, and in version 1 only the Brute can, and only weak blocks. Hordes come from noise, the story, and horde nights against your base. Safe spots are always safe. Special zombies each have an ability and a counter, which players look up in the enemy tracker. Besides zombies there are raiders, cult members and, later, mini chicken aliens with guns. Bosses have real mechanics, not just more health.

## Requirements

### ENEMY-001 — Slow by day, faster at night
**Status:** approved (partly implemented 2026-11-02: regular zombie day and night speed, measured by GameTests; Rallier buff and per-district balancing not built yet)
**Priority:** must

**Description:** During the day, regular zombies are always slower than a walking player. At night they're slightly faster than a walking player but still slower than a sprinting one. Special zombies like the Rallier buff them, which is what makes groups really dangerous.

**Acceptance criteria:**
- [x] By day, regular zombies are slower than a walking player.
- [x] At night, regular zombies are slightly faster than a walking player and slower than a sprinting one.
- [ ] While a Rallier is alive, zombies within 8 blocks move 25% faster.
- [ ] At night, buffed zombies can outrun an early-game player's sprint. Players catch up through speed trinkets (CMBT-003) and the skill tree (PROG-012). Once a player has progressed well in a district, buffed zombies there can no longer outrun them.
- [ ] Zombie speed is balanced per district and planet against how fast players can be at that point, and re-checked whenever districts, planets or speed sources are added.

**Open questions:**
**Notes:** Your idea: "a version which buffs zombies around it to be a bit faster. So one dude literally." Speeds decided 2026-11-01 (answer to Q-078): "slower than player running but slightly faster than player normal walking in night. Otherwise always slower as player. Important to have the special zombies to buff them!" Changed 2026-11-01 (Q-113): first "never faster than sprint", then "In the night zombies might oversprint u with buffs! So there is trinkets required or levels to get more speed. When advanced in a district zombies should not be able to get faster anymore, but early on in night they do!" Day and night lengths are in SURV-007. Speeds set 2026-11-02 (answer to Q-132): 80% of a walking player's speed by day, 110% at night (walking is about 4.3 m/s, sprinting 5.6 m/s), so a Rallier-buffed zombie at night (about 137%) outruns an early sprint. How it is built: a mob's ground speed grows with the square of its speed attribute, so "+25% faster" means about +11.8% on the attribute (see RegularZombie). Night counts from when beds work until morning in clear weather; thunderstorms don't make it night for zombies.

### ENEMY-002 — Only some zombies burn in sunlight
**Status:** approved (partly implemented 2026-11-02: Sunburn on about 15% of regular zombies, hidden; verified by GameTests and the client smoke test; the reveal trinket not built yet)
**Priority:** must

**Description:** Zombies don't burn in daylight by default. About 15% of regular zombies carry a "burns in sunlight" effect, which naturally thins them out during the day.

**Acceptance criteria:**
- [x] About 15% of regular zombies spawn with the "burns in sunlight" effect.
- [x] All other zombies don't burn in daylight.
- [ ] Players can't see which zombies carry the effect, unless they wear the trinket that reveals enemies' effects (answer to Q-134).

**Open questions:**
**Notes:** Decided 2026-11-01. The effect is called Sunburn. Hidden by default (2026-11-02, answer to Q-134), your words: "normally not, but ofcourse there is a trinket for that which we get later. The trinket reveals effect on ememies" (see the trinket ideas in 40-gear.md).

### ENEMY-003 — Early nights are deadly
**Status:** approved
**Priority:** must

**Description:** At the start of the game, nights aren't survivable out in the open without gear, and they stay hard even with gear. Players are expected to shelter at home or in a safe spot, or sleep through the night. A key item earned from a quest, likely a trinket, makes night enemies about 10% weaker.

**Acceptance criteria:**
- [ ] In the first districts, a player without gear can't survive a night in the open.
- [ ] With gear, nights stay hard.
- [ ] A key item earned from a quest (likely a trinket, CMBT-003) makes night enemies about 10% weaker for the player who has it.
- [ ] Shelter (the team's first home, safe spots) and sleeping are real ways to get through an early night.

**Open questions:**
**Notes:** Your words: "In the beginning night should not really be survivable!" and (answer to Q-086) "without gear not possible. Should stay hard." Fits the early bed from the story (SURV-005) and safe spots (SURV-006).

### ENEMY-004 — Enemies carry status effects
**Status:** approved (partly implemented 2026-11-02: tier 1 spawn effects on regular zombies, 10% with one hidden weak effect, verified by GameTests; other enemy types and tiers come with them)
**Priority:** must

**Description:** Enemies can spawn with status effects from the potion effect system. Easy enemies get weaker effects; harder enemies get stronger effects, sometimes several at once. Wild combinations appear later in the game but stay fairly rare. Bosses can have effects too. New effects can be added (e.g. "burns in sunlight").

**Acceptance criteria:**
- [ ] Each enemy type defines which effects it can spawn with, how strong, and how likely.
- [ ] Starting rates (tuned with balancing): tier 1, 10% of enemies get one weak effect (Strength I, Resistance I, Regeneration I or Fire Resistance; no Speed, Q-143); tier 2, 20%, sometimes level II; tier 3, 30%, with two-effect combos at 5%; tier 4, 40%, combos at 10%.
- [ ] Effect strength and number of effects rise with enemy difficulty.
- [ ] Enemies with several effects at once are rare.
- [ ] Bosses have fixed effects, defined per boss.

**Open questions:**
**Notes:** Your words: "We stick to the potion brewing mechanic to get effects. Can always add more." Players keep getting effects through vanilla brewing (Q-025). Tier 1 pool decided 2026-11-02 (answer to Q-143): Strength I, Resistance I, Regeneration I and Fire Resistance (which also keeps a Sunburn zombie from burning); no Speed in tier 1, because it would make day zombies faster than walking (ENEMY-001). Speed effects start in tier 2. Spawn effects are hidden like Sunburn (my reading of Q-134: "the trinket reveals effect on enemies"), so players see them only with the reveal trinket.

### ENEMY-005 — Only specials break blocks
**Status:** approved (partly implemented 2026-11-02: regular zombies break no doors and trample no farmland or turtle eggs, verified by GameTests; the Brute and hardened blocks not built yet)
**Priority:** must

**Description:** Block breaking is super rare. Regular zombies can't break blocks, and only very few specials can, and only weak blocks. Nothing can break safe spot blocks or unminable blocks. Later, players can harden blocks with an alien material so enemies can't break them. Humans never invented this material, so it only comes from the aliens, and it isn't an early-game item.

**Acceptance criteria:**
- [x] Regular zombies can't break or damage blocks.
- [ ] In version 1, the Brute is the only enemy that breaks blocks, and only wood and cobblestone (proposal).
- [ ] No enemy can break safe spot blocks or unminable blocks (WORLD-006).
- [ ] Hardened blocks can't be broken by enemies.
- [ ] The hardening material comes only from the aliens and isn't available early (details: Q-090, to discuss later).

**Open questions:** Q-090
**Notes:** Your words: "make sure to not add too much block breaking!! it has to be super rare." Base fortification details belong in the base spec (BASE).

### ENEMY-006 — Hordes
**Status:** approved
**Priority:** must

**Description:** Hordes come from three sources: noise attracts nearby zombies, the story sends hordes at key moments, and horde nights send big attacks at bases. There are no randomly wandering hordes.

**Acceptance criteria:**
- [ ] Only loud actions pull zombies toward the noise: gunshots (about 32 blocks), explosions (about 48), chainsaws (about 24), breaking glass and doors (about 12). Walking, sprinting and mining are quiet.
- [ ] Scripted story hordes happen at key moments (e.g. the launch defense, PROG-010).
- [ ] Every 7th in-game night is a horde night, sending a big attack at a player's base (which base: WORLD-012).
- [ ] The horde night counter runs per player and only counts in-game days while that player is online.
- [ ] When team members play together, the counter closest to its horde night decides. When that horde night hits, the counters of everyone present reset.
- [ ] In a given district, a horde night is equally strong each time (PROG-007).

**Open questions:**
**Notes:** Matches gear traits like the loud chainsaw and the pipe bomb lure (40-gear.md).

### ENEMY-007 — Safe spots are safe
**Status:** approved
**Priority:** must

**Description:** Safe spots never have trouble with enemies or hordes. They're literally safe, except when the story makes one fall.

**Acceptance criteria:**
- [ ] No enemy or horde can harm players inside a safe spot or overrun it, except in a story event (SURV-006).

**Open questions:**
**Notes:** Your words: "a safe place doesnt have any issues dealing with hordes. They are literally safe."

### ENEMY-008 — Not only zombies
**Status:** draft
**Priority:** must

**Description:** Besides zombies, the team faces raiders (hostile humans), cult members (human collaborators), and later mini chicken aliens with guns picked up on Earth. Disguised alien leaders appear as story enemies.

**Acceptance criteria:**
- [ ] Raiders and cult members exist as human enemies (STORY-007).
- [ ] Mini chicken aliens with guns don't appear in the first districts (STORY-011).

**Open questions:** Q-085
**Notes:** Details per enemy go in the tables below once picked.

### ENEMY-009 — Special zombies with abilities and counters
**Status:** approved (the rules and the first specials Rallier, Crawler, Hound, which start in tier 2 (Q-139), so they come after Milestone 1; tiers 2–3 stay draft)
**Priority:** must

**Description:** Special zombies each have one clear ability and a counter. They're what makes fighting hard, and they arrive gradually by tier (first specials in the Settler tier, PROG-008).

**Acceptance criteria:**
- [ ] Every special zombie has a written ability and at least one counter.
- [ ] Specials appear according to their first tier, and only occasionally (spawn rates set with balancing).
- [ ] The Rallier is in the game (your idea).
- [ ] The version 1 set is the ten specials designed below (my picks on your request; veto any, Q-082).
- [ ] Specials that pin a player can always be escaped alone by attacking repeatedly; a teammate frees the player instantly. This keeps them fair for solo players and any team size.

**Open questions:** Q-082
**Notes:** Inspired mostly by Left 4 Dead's special infected, redneck-flavored. Decided 2026-11-02 (answer to Q-139): specials start in tier 2. Rallier, Crawler and Hound come first, in the Settler tier; Chapter 1 has only regular zombies, plus Hog Daddy and his piglets.

### ENEMY-010 — Enemy tracker
**Status:** draft
**Priority:** must

**Description:** An in-game tracker where players look up enemies they've met, with their abilities and counters.

**Acceptance criteria:**
- [ ] The tracker lists every enemy type the team has encountered.
- [ ] Each entry shows the enemy's abilities and counters.
- [ ] How entries fill in and what the tracker looks like follow Q-083.
- [ ] Discovering a new enemy type gives utility points (PROG-005).

**Open questions:** Q-083
**Notes:** From features.md: "Need a system like a tracker or smth where enemies can be looked up and counter mechanics can be seen." Story hooks if picked: the deserter chicken and Morris Byte's scanner add details.

### ENEMY-011 — Bosses have mechanics
**Status:** approved (Milestone 1: the rules and Hog Daddy; other bosses stay draft)
**Priority:** must

**Description:** Bosses are fights with mechanics, not just enemies with more health. Minecraft isn't a boss-fight game, so every boss is built on three layers: a sealed arena, attacks telegraphed on the ground, and one signature twist. For some bosses the twist is a minigame-style phase. Normal hitting still matters, but the mechanics decide the fight.

**Acceptance criteria:**
- [ ] Every boss fight happens in a sealed arena built from unminable blocks (WORLD-006); players can't pillar up or dig out.
- [ ] Every boss's big attacks are telegraphed on the ground (markers show where they'll land) and can be dodged, including with the dodge roll (CMBT-002).
- [ ] Every boss has one signature twist; some use a minigame-style phase (chase, dodging, tower defense, stealth).
- [ ] Every boss has at least two phases and at least one weakness players can exploit.
- [ ] Team mechanics are designed for three players and adjust for other team sizes (e.g. longer timing windows for fewer players).
- [ ] Boss strength scales per player as defined for that boss (PROG-007).
- [ ] Downed players are ignored by bosses too (SURV-001).

**Open questions:** Q-084
**Notes:** From the Final Fantasy vs. modpack comparison: "fights have mechanics, not just more health." Approach approved in principle 2026-11-01 (answer to Q-089): "Boss fights as u suggest the 3 points." Inspiration for Minecraft boss fights: L_Ender's Cataclysm (Forge 1.20.1).

### ENEMY-012 — Scarier zombies
**Status:** approved (partly implemented 2026-11-02: our own zombie type with vanilla's look replaces vanilla zombies in natural spawns, no babies; verified by GameTests and the client smoke test; own look and animations not built yet)
**Priority:** must

**Description:** Zombies are scarier than vanilla ones: new looks and a few new animations, while still fitting Minecraft's style (pillar "it still looks like Minecraft").

**Acceptance criteria:**
- [ ] Regular zombies have their own look, clearly different from vanilla zombies (direction approved, Q-151: varied rural victims; see notes).
- [ ] They have a few new animations that make them feel more threatening (e.g. lurching walk, lunging attack).
- [ ] Looks and animations stay in vanilla's art style.
- [x] Regular zombies are our own zombie type, which replaces vanilla zombies, husks, drowned and zombie villagers in natural spawns (answers to Q-131, Q-135).
- [x] There are no baby zombies (answer to Q-133).

**Open questions:**
**Notes:** Your words: "Zombies in general have to be scarier than normal zombie. So maybe few new animations and different looks." Custom animations likely need an animation library; picking one (and checking its license) is a technical check for the modpack spec. Look chosen 2026-11-02 (answer to Q-151): varied rural victims (farmers in overalls, hunters in camo, flannel, work clothes), gray-green skin, torn clothes, hollow dark eyes, slightly hunched; a lurching walk and a lunge attack.

## Special zombie designs (proposals)

*Ten specials for version 1: the Rallier is yours, the rest are my picks and designs. Every one is a different threat that needs a different answer. Numbers are proposals, measured against a vanilla zombie (20 health, 3 damage on Normal). "Scales by" is what grows per extra player (PROG-007). All looks are in vanilla style.*

### First specials (from tier 2, Settler; answer to Q-139)

**Rallier** *(your idea)*
- **Look:** a zombie football coach with a whistle and a torn cap.
- **Behavior:** stays behind other zombies and avoids melee. Blows its whistle every few seconds; while it's alive, zombies within 8 blocks move 25% faster.
- **Counter:** the whistle gives it away. Kill it first, at range if you can.
- **Numbers:** 14 health, 2 damage. **Breaks blocks:** no. **Spawns:** mixed into groups of 5 or more, day and night. **Scales by:** buff radius.

**Crawler**
- **Look:** a pale, legless zombie dragging itself along, with glowing eyes.
- **Behavior:** spawns only in dark caves. Hides in the dark and lunges from 3 blocks away, causing a moment of blindness.
- **Counter:** light up caves; it won't come into light.
- **Numbers:** 12 health, 4 damage. **Breaks blocks:** no. **Spawns:** underground, in darkness. **Scales by:** how many spawn.

**Hound**
- **Look:** an infected dog with exposed ribs.
- **Behavior:** packs of 3–4, 50% faster than zombies. They circle and bite from different sides.
- **Counter:** back against a wall; sword sweep attacks; shield.
- **Numbers:** 10 health each, 2 damage. **Breaks blocks:** no. **Spawns:** open terrain, more at night. **Scales by:** pack size.

### Tier 2: Settler, further specials

**Bloater**
- **Look:** a swollen, greenish hillbilly in overalls.
- **Behavior:** slow; walks straight at players and bursts when killed or when it reaches you. Players within 3 blocks get "Bile" for 15 seconds: every zombie within 24 blocks targets them.
- **Counter:** kill it from more than 4 blocks away. Water washes Bile off.
- **Numbers:** 16 health. **Breaks blocks:** no. **Spawns:** with groups. **Scales by:** Bile duration.

**Pouncer**
- **Look:** a hooded, crouching zombie.
- **Behavior:** hides, then leaps up to 8 blocks at a player who isn't facing it and pins them. A pinned player can't move or attack normally and takes damage over time.
- **Counter:** a raised shield blocks the leap. A teammate frees you instantly; alone, attack repeatedly to break free (ENEMY-009).
- **Numbers:** 20 health, 2 damage per second while pinned. **Breaks blocks:** no. **Spawns:** hidden spots, ambushes. **Scales by:** health.

**Moonshiner**
- **Look:** a red-nosed zombie clutching a moonshine jug.
- **Behavior:** stumbles unpredictably, sometimes takes a swig and sprints. When killed, it bursts into a 3 x 3 fire patch for 5 seconds.
- **Counter:** kill it at range, or in water. Fire resistance helps.
- **Numbers:** 18 health, 3 damage. **Breaks blocks:** no; its fire doesn't spread to blocks. **Spawns:** countryside, near redneck places. **Scales by:** fire patch size.

### Tier 3: Gunsmith

**Strangler**
- **Look:** a tall, thin zombie with a long tongue.
- **Behavior:** from up to 20 blocks away, it lashes its tongue at a player, drags them in and chokes them.
- **Counter:** any hit on the tongue or the Strangler breaks the grab. A teammate can do it, or the grabbed player alone by attacking repeatedly. Breaking line of sight stops it from grabbing.
- **Numbers:** 20 health, 2 damage per second while choking. **Breaks blocks:** no. **Spawns:** rooftops and high ground. **Scales by:** tongue range.

**Spitter**
- **Look:** a hunched zombie with a glowing green throat.
- **Behavior:** keeps its distance and spits an arcing acid glob that leaves a 3-block acid pool for 6 seconds. The acid hurts players.
- **Counter:** keep moving; don't fight in corners; close the distance fast.
- **Numbers:** 18 health, 2 damage per second in acid. **Breaks blocks:** no (changed so block breaking stays super rare). **Spawns:** with groups and during horde nights. **Scales by:** pool duration.

**Brute**
- **Look:** a huge zombie, twice normal size. A former lumberjack.
- **Behavior:** slow walk. Smashes weak blocks in its way. Telegraphed ground slam (raises its arms for 1 second) knocks back everything within 4 blocks. Throws debris at range.
- **Counter:** roll through or away from the slam; fire does extra damage; keep your distance and focus fire as a team.
- **Numbers:** 150 health, 10 damage. **Breaks blocks:** yes, the only special that does: wood and cobblestone only; never safe spot, unminable or hardened blocks. **Spawns:** rarely in the open, often on horde nights. **Scales by:** health.

**Weeper**
- **Look:** a pale zombie in a torn nightgown, sitting and sobbing.
- **Behavior:** her crying gives her away. If a player comes within 6 blocks with a light source, sprints, makes noise or attacks her, she rages: very fast, and two hits down a player. Then she flees.
- **Counter:** sneak past with no light, or ambush her with heavy burst damage.
- **Numbers:** 40 health, 12 damage. **Breaks blocks:** no. **Spawns:** dark buildings, at night. **Scales by:** health.

### Reserve (not in version 1)
- **Screamer:** screams to call zombies from far away. Overlaps with the noise system (ENEMY-006).
- **Bull:** charges in a straight line and slams. Overlaps with the Brute.

## Boss designs (proposals)

*You left the design to me (2026-11-01), so these are my proposals; veto or change anything. Every boss follows ENEMY-011: sealed arena, ground telegraphs, one signature twist. Each design names the game it borrows from. Health numbers come later, with balancing.*

### Hog Daddy, Chapter 1 milestone (tier 1)
*The rednecks' neighbor, a pig farmer who mutated into a hulking hog-man. He smashes into the cabin.*
- **Look (answer to Q-150):** about 3 blocks tall, a pig's head with broken tusks, torn dungarees over a huge gut, a meat hook for a hand, one rubber boot, flies buzzing around him.
- **Arena:** his farm: pig pen, barn, mud, ringed by wrecked cars and hay bales made of unminable blocks.
- **Phase 1, the chase (minigame):** he bursts through the cabin wall and the team runs through the cornfield to the barn. Red lines on the ground show where his charges will go; dodge them, and duck under falling hay. *Inspired by: Crash Bandicoot's boulder chases, Resident Evil 4's village.*
- **Phase 2, the pen:** he charges in straight lines (a red line telegraph, 1 second). If he slams into a fence post or the feeding trough, he's stunned for 4 seconds and his back is exposed; that's when hits really count. Zombie piglets keep coming. With three players, one baits the charges while the others clear piglets.
- **Phase 3, enraged (half health):** belly flops send mud shockwave rings across the pen; roll or jump over them. Each stun post breaks after one hit, so the team has to plan which ones to use. *Inspired by: Cuphead's ring attacks.*
- **Weakness:** stuns from obstacles; fire burns his bristles.
- **Scales by:** health, plus one extra piglet per player.

### The Raider King, Chapter 2, optional
*A monster-truck champion turned warlord. He runs his junkyard like a wrestling show.*
- **Arena:** the junkyard, walled with unminable scrap.
- **Phase 1, the truck:** he circles the junkyard in an armored monster truck, and tire-track lines warn where he'll ram. His crew shoots from scrap towers. Shoot out the three tires to stop the truck (three players, three tires).
- **Phase 2, the ring:** a wrestling-ring duel. He fights with a tire iron and wears scrap armor plates; heavy hits break plates, and only then does he take damage. Before his big swing he flexes for 1 second; roll behind him for a free hit. *Inspired by: Punch-Out!! (read the tell, punish the opening).*
- **Ending:** at low health he yields. Recruit him or finish him (STORY-005 choice).
- **Weakness:** his flex tell; the truck's tires.
- **Scales by:** number of armor plates.

### General Hargrove, the traitor, Chapter 3 milestone, stage 1
*The general who secretly serves the cult (name is a proposal).*
- **Arena:** a military base courtyard sealed with unminable concrete barriers.
- **Phase 1, the barrage (dodging minigame):** he hides in a glass command bunker and calls artillery. Target circles appear on the ground before every shell lands. Three radio relays stand around the courtyard; destroy them all to cut his artillery and open the bunker. With three players, the team splits up. *Inspired by: Undertale and bullet-hell dodging, moved to 3D.*
- **Phase 2, the gunner:** he steps out with a heavy machine gun. A sweeping line shows where his fire will go; hide behind sandbags and flank him while he reloads.
- **Weakness:** the relays; attacks from the side.
- **Scales by:** health and how often shells fall.

### Sister Velvet, true form, Chapter 3 milestone, stage 2
*The cult's seductive recruiter. A comedic reveal cutscene shows what she really is: a chicken alien (STORY-007).*
- **Arena:** the cult's ritual chamber, walls of alien stone (unminable).
- **Phase 1, the pillars (team puzzle):** she teleports between three ritual pillars and is shielded while any pillar stands. All three have to break within 5 seconds of each other, or they regrow. Built for three players; the window grows for smaller teams. *Inspired by: World of Warcraft raid mechanics.*
- **Phase 2, chicken form:** egg bombs (landing circles), feather storms that push players around, summoned cultists. She clucks mid-sentence.
- **Weakness:** fire.
- **Scales by:** health and the pillar timing window.

### The Mountain, Chapter 4 launch defense
*A gigantic Brute that goes for the rocket during the launch siege (PROG-010).*
- **Arena:** the launch site, sealed.
- **Phase 1, the march (tower defense):** he walks a fixed path toward the rocket while the horde attacks. The team slows him with launch-site defenses and base defense extras. *Inspired by: Plants vs. Zombies, Orcs Must Die!*
- **Phase 2, the cranes:** lure him under one of three cargo cranes and drop a container on him. He drops to his knees, exposing a glowing tumor on his back: the weak spot. One player runs the crane, the others bait and defend. *Inspired by: Zelda (use the environment), Shadow of the Colossus (weak spots).*
- **Phase 3, last stand:** before liftoff he goes berserk. Ground slam rings, thrown cars with landing circles.
- **Weakness:** the tumor; fire.
- **Scales by:** health and the number of thrown cars.

### The Ashen Hunter, the Nether guardian
*A blind, towering skeleton beast that leads the running skeletons and guards the rocket fuel ingredient (WORLD-008). The scary one.*
- **Arena:** its lair in the Nether, sealed.
- **Phase 1, stealth (minigame):** it can't be hurt yet. It's blind and hunts by sound, so sneak to the fuel ingredient, avoid noise, and use cover. *Inspired by: Amnesia, Alien: Isolation, and Minecraft's own Warden.*
- **Phase 2, the escape (chase):** grabbing the ingredient wakes everything. Run through collapsing lava corridors with running skeleton packs on your heels.
- **Phase 3, the gate:** a showdown at the exit. Flares from the flare gun blind it for a few seconds and open it up to damage.
- **Weakness:** bright light (flares); silence.
- **Scales by:** health and skeleton pack size.

### Colonel Cluck, Chapter 5, the Moon (end of version 1)
*The alien chicken commander of the Moon base, armed with Earth guns.*
- **Arena:** a dome in the Moon base, sealed. Low gravity.
- **Phase 1, the jetpack:** he hovers and fires Earth guns in spread patterns; lanes on the floor show where the shots will go. Shoot his three fuel tanks to bring him down.
- **Phase 2, low-gravity brawl:** long stomping leaps with landing circles. In low gravity players jump high to clear the shockwave rings. *Inspired by: Super Mario Galaxy.*
- **Ending:** as he falls he clucks something; the deserter chicken translates it as "home is... further". The cliffhanger for after version 1 (STORY-016, STORY-017).
- **Weakness:** the fuel tanks; he's slow to turn on the ground.
- **Scales by:** health and how many guns fire at once.
