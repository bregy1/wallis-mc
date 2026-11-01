---
name: ask-first
description: Use at the start of every task and whenever a decision is not explicitly covered by the spec or the user's instructions. Defines how to ask clarifying questions instead of making assumptions.
---

# Ask first, never assume

The user wants to be asked. A question costs them ten seconds; a wrong assumption buried in code costs them an hour. When in doubt, ask.

## What counts as an assumption

You are assuming whenever you pick something the user didn't specify, including:

- Names (mod IDs, item names, class names, lang keys, file locations)
- Numbers (durability, damage, crafting costs, spawn rates, cooldowns, sizes)
- Behaviour (what happens on right-click, in multiplayer, in creative, on edge cases)
- Scope (adding "helpful" extras, refactoring nearby code, extra config options)
- Visuals (colors, textures, models, particles, sounds)
- Technical choices (which library, mixin vs. event, where data is stored, sync strategy)
- Interpreting vague words ("make it better", "like vanilla", "balanced", "cool")

If the spec or the user's message doesn't pin it down, it's a question.

## Facts vs. decisions

- **Facts you can look up yourself → don't ask, look.** Which Minecraft version the project uses, how an existing class works, what vanilla does, whether a method exists. Read the code, `CLAUDE.md`, the spec or the decompiled sources first.
- **Decisions → always ask.** Anything that is a matter of preference, design, or scope belongs to the user.

## How to ask

1. **Batch your questions.** Collect them and ask in one go rather than one at a time across many turns. Use the `AskUserQuestion` tool when it fits (up to 4 multiple-choice questions); otherwise a numbered list.
2. **Give options and a recommendation.** "Should the wrench break blocks? (a) No, utility only — *recommended, matches the spec's 'non-combat tool' note* (b) Yes, like a pickaxe (c) Only mod blocks."
3. **Explain why it matters** in one short line, if it isn't obvious.
4. **Separate blocking from non-blocking.** Say which answers you need before starting and which can wait.
5. **Don't start the part that depends on the answer.** You may do clearly independent prep work (reading code, scaffolding unaffected files) while waiting.

## Hard stops — always stop and ask when

- The spec is silent, ambiguous, or contradicts itself or the user's latest message.
- A change would touch more files or systems than the task obviously implies.
- Something would be hard to undo (deleting files, renaming registry IDs — this breaks existing worlds, changing save data formats, changing networking packets).
- You're about to add a dependency, a mixin, or a config option.
- A build fails and the fix would change behaviour, not just syntax.
- You notice a bug or a better approach outside the current task — mention it, don't fix it unasked.

## Banned habits

- Silently picking a "sensible default".
- Phrases like "I assumed…", "I went ahead and…", "I also added…" in your report. If you'd write that, you should have asked.
- Treating your earlier guess as settled because the user didn't object.

## At the end of every task

List any decision you made that wasn't explicitly given, so the user can confirm or overturn it. This list should usually be empty.
