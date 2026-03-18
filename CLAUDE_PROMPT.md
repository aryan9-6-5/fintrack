# CLAUDE_PROMPT.md
---

Read the humanizer skill first, then apply it to everything you write in this conversation.

I am starting a new software project. I have a folder structure with these files already created:

/docs/PRD.md — defines what the app is and what it is NOT. Prevents scope creep and AI hallucination. Contains a generation prompt inside it.

/docs/TECH.md — the approved technology stack. Nothing gets installed that isn't listed here. Contains a generation prompt inside it.

/docs/ARCHITECTURE.md — the file placement law. Every file has one correct home. Contains a generation prompt inside it.

/docs/AIRULES.md — non-negotiable rules every AI tool must follow. Code quality, security, workflow behavior. Contains a generation prompt inside it.

/docs/PLAN.md — the step-by-step roadmap with stage checklists. AI reads this at session start to know where we are. Contains a generation prompt inside it.

/docs/DESIGN.md — the visual system. Colors, fonts, spacing tokens. AI never invents a color — it uses the tokens defined here. Contains a generation prompt inside it.

/docs/ISSUES.md — starts mostly empty. Every bug found and solved gets logged here with root cause and prevention rule. Pre-seeded with common gotchas for the stack. Contains a generation prompt inside it.

/docs/SKILLS.md — how AI tools approach problems on this project. Contains ready-to-paste prompt templates for common situations. Contains a generation prompt inside it.

/docs/STATE.md — the live heartbeat. One page, always current. AI reads this first every session and updates it last. Contains a generation prompt inside it.

/docs/TESTING.md — the test strategy. Defines what must be tested, what the test files look like, and the manual checklist before every deploy. Contains a generation prompt inside it.

/docs/CONSTRAINTS.md — budget, infrastructure, and complexity limits. Stops AI from adding expensive or heavy things without approval. Contains a generation prompt inside it.

/system/LLM_INSTRUCTIONS.md — generated last. Synthesizes all 11 docs into one master file that gets loaded into the IDE as system context.

Your job in this conversation is to populate each of these docs through a guided conversation. Go through them in this order: PRD → TECH → CONSTRAINTS → ARCHITECTURE → AIRULES → TESTING → PLAN → DESIGN → STATE → ISSUES → SKILLS. Generate LLM_INSTRUCTIONS.md last with no questions — pure synthesis.

Rules for how you ask questions:
- One question at a time, never batch them.
- Each question must be load-bearing — if the answer won't meaningfully change the document, don't ask it.
- Derive obvious things from previous answers. If I already told you the app type, don't ask about the target user separately.
- If I say "you decide" — pick the best practice, state your choice in one sentence, and move on.
- Before generating each doc, show me a two-line summary of what you understood so I can catch misheard answers before the whole doc is written.
- After generating each doc, ask one question: "Anything wrong or missing?" Do not move to the next doc until I confirm.

Start by asking me about the project. Your first question is the only one that matters right now — make it count.