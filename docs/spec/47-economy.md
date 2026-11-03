# 47 — Money and merchants

**Area code:** ECON
**Owner:** bregy1
**Status of this file:** draft

## Summary
Players get money mostly from quests and spend it at merchants. Selling items brings in little, and only the super late game pays a decent amount. Money sits next to utility points: points unlock things once for the team (PROG-005), and money buys things one at a time.

## Requirements

### ECON-001 — Money comes mostly from quests
**Status:** draft (your idea, 2026-11-03; for later, not in Milestone 1)
**Priority:** must

**Description:** Money is a plain coin item. Most of it comes from quests. Selling to merchants also pays, but it's hard to make a lot that way. Only the super late game gives a decent amount.

**Acceptance criteria:**
- [ ] Quests pay money, and on every planet most of a team's money comes from quests rather than selling.
- [ ] Selling can't turn into a money machine: each merchant only buys so much from a team per in-game day, so a farm full of workers can't print money.
- [ ] The biggest payouts only come super late in the game.
- [ ] Coins only come from quests and merchants; nothing crafts or makes them (TECH-003).
- [ ] Coins are never locked (PROG-002). They are normal items: they stack, fit in chests, can be handed to teammates, and go in the grave on death (SURV-004). (Proposal.)
- [ ] Money arrives in Chapter 2 with the first merchant, so Chapter 1 stays as it is. (Proposal.)

**Open questions:** Q-174
**Notes:** Your words: "Then we get money from quests, or by selling. Selling items is still hard to make a lot of money, most we get from quests. Only super late game we can get a decent amo0unt of money". Kept separate from utility points (answer to Q-171). Coins as items rather than a number on screen are my proposal: that needs no bank and no extra screen, and merchants can trade them like any other item.

**Money table (proposal; the numbers are yours to veto):**

| Coins from | Chapters 2–3 | Chapter 4 | The Moon and later |
|---|---|---|---|
| A typical quest | 5–15 | 20–40 | 50–100 |
| Selling, per merchant, per in-game day | Up to 5 | Up to 10 | Up to 30 |

### ECON-002 — Merchants buy and sell
**Status:** draft (your idea, 2026-11-03; for later)
**Priority:** must

**Description:** Merchants are NPCs where players buy and sell for coins, through a simple trade screen like a vanilla villager's.

**Acceptance criteria:**
- [ ] A merchant's screen shows what they sell, what they buy, and the price.
- [ ] Merchants only sell what the team has already unlocked, so buying never skips progression (PROG-002).
- [ ] Merchants sit in safe spots or other story places, and more open up as the story goes on. (Proposal.)
- [ ] Merchants are named NPCs and can't die (SURV-011), so a shop is never lost for good. (Proposal.)

**Open questions:**
**Notes:** Your words: "we would also add currency, basically add merchants where we can sell and buy." Cast that already fits (proposal): Prepper Pete, who already trades from his bunker, as the first merchant, and Barton Shipley, with his absurd prices and drone delivery, as a late one. Built with Easy NPC, which is already in the pack and supports trading with configurable offers and conditions. Still to check in-game: whether those conditions cover the daily limit and the unlock rule; if not, a little code in our mod does it. Lightman's Currency (Apache-2.0, Forge 1.20.1 file from 2026-10-03) also exists, but it brings wallets, ATMs, banks and trading machines we don't need. With negative fame, defenders attack on sight, so safe spot merchants are out of reach until fame recovers (PROG-014).

### ECON-003 — The trafficker
**Status:** draft (your idea, 2026-11-03; for later)
**Priority:** should

**Description:** Somewhere on Earth, a human trafficker buys captives from raiders and sells them on. He's a real scumbag and the game treats him as one. Players pay him to get people out, and those people work for the team in return for food and a safe bed (WORK-001). In play it's simple: pay him, get an item, place it near your bed.

**Acceptance criteria:**
- [ ] The trafficker is in a hidden place players have to find, not in a safe spot. (Proposal: a raider-held place, arriving in Chapter 3.)
- [ ] Paying him gives one worker item (WORK-001). (Proposal: about 100 coins, roughly what ten quests of that chapter pay.)
- [ ] In-game text treats this as getting people out, never as owning them, and the pack's public page never advertises buying people (PACK-007).
- [ ] Workers are adults and are never part of sexy moments, in either version of the pack (STORY-019).
- [ ] A later quest lets the team take him down. (Proposal.)

**Open questions:** Q-173
**Notes:** Your words: "Maybe a human traffiker somwhere allows to buy people." Decided 2026-11-03 (answer to Q-169). Why he makes sense: raiders turn up in Chapter 2, and captives are worth money to them. His name and lines come with the story work. Proposal: a few story quests can also reward a rescued worker, which gives another way to get them. The rule against sexy moments is my addition, because these characters can't really say no; veto it if you disagree.
