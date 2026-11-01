---
name: spec-driven
description: Use for every implementation task and whenever writing or changing the specification in docs/spec/. Keeps all work tied to the spec written together with the user, and defines how the spec is written, approved and changed.
---

# Spec-driven development

The specification in `docs/spec/` is the contract between you and the user. Code implements the spec — nothing more, nothing less.

## The golden rules

1. **No spec, no code.** Only implement requirements that exist in the spec with status `approved`.
2. **The spec wins over your opinion.** If you think the spec is wrong or could be better, say so and propose a change — but keep implementing what it says until the user changes it.
3. **The user's latest explicit instruction wins over the spec**, but then the spec must be updated so they match again. Never let code and spec drift apart silently.
4. **You never approve your own spec changes.** Only the user sets a requirement to `approved`.

## Spec layout

```
docs/spec/
  00-overview.md        # vision, pillars, target audience, scope, out-of-scope
  01-glossary.md        # every mod-specific term, defined once
  <area>.md             # one file per feature area (e.g. 10-blocks.md, 20-machines.md, 30-worldgen.md)
  changelog.md          # every spec change, dated, with reason
  open-questions.md     # unresolved questions, each with an ID (Q-001…)
```

Use `docs/spec/TEMPLATE.md` when creating a new area file.

## Requirement format

Every requirement has a stable ID and a status:

```markdown
### MACH-004 — Crusher processes ores
**Status:** draft | proposed | approved | implemented | deprecated
**Priority:** must | should | could

**Description:** One or two sentences, in plain language.

**Acceptance criteria:**
- [ ] Placing 1 raw iron in the input produces 2 crushed iron after 200 ticks.
- [ ] Works with hoppers on all sides except the front.
- [ ] Stops and keeps progress when power runs out.

**Open questions:** Q-012
**Notes:** design rationale, links to related IDs.
```

- IDs are `AREA-NNN`, never reused, never renumbered. Deprecated requirements stay in the file marked `deprecated`.
- Acceptance criteria must be concrete and testable in-game. "Feels good" is not a criterion — turn it into something checkable, together with the user.
- Numbers (ticks, damage, costs) belong in the spec, not only in code.

## Writing the spec together

When the user wants to spec a new feature:

1. Ask what they're imagining before proposing anything (follow `ask-first`). Let them describe it in their own words.
2. Ask targeted questions to fill gaps: behaviour, edge cases, multiplayer, creative mode, interaction with vanilla and other features, numbers. Use the `minecraft-design` skill for design questions.
3. Write a draft with status `draft`. Mark every gap as an open question rather than filling it in yourself.
4. Show the draft and walk through open questions.
5. Iterate until the user says it's approved; then set status to `approved` and log it in `changelog.md`.

## Implementing from the spec

1. Before starting, name the requirement IDs you're implementing and quote their acceptance criteria.
2. If a requirement is not `approved`, or has open questions that affect the work → stop and ask.
3. If implementation reveals something the spec doesn't cover (an edge case, a technical constraint) → stop, describe it, propose a spec addition, and wait for approval.
4. Don't add anything beyond the acceptance criteria: no bonus features, no extra config options, no "while I was here" changes.
5. When done, tick the acceptance criteria you verified, set status to `implemented`, and say how each criterion was checked (build, `runClient`, manual steps the user should do).
6. Reference spec IDs in commit messages.

## Changing the spec

- Propose changes as a short diff: what changes, why, and which code is affected.
- On approval: edit the requirement, add an entry to `changelog.md` (date, IDs, reason), and list any implemented code that now needs updating.
- If a change affects saved worlds (registry IDs, NBT/data components, world gen), point that out explicitly.

## Drift check

When asked to "check the spec", or before a release: compare code against the spec and report (a) implemented code with no requirement, (b) approved requirements with no implementation, (c) mismatches between code and acceptance criteria. Report only — don't fix without approval.
