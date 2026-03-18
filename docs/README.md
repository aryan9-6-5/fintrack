# Vibe Coding Framework v3 — 10/10 System
# 11 docs + self-updating AI workflow

## What's new in v3
+ STATE.md      — live session state, AI reads first every time
+ TESTING.md    — prevents shipping broken code
+ CONSTRAINTS.md — stops AI adding expensive/heavy things
+ Self-updating  — STATE.md, PLAN.md, ISSUES.md update automatically

## The Full System

Doc               | Role                                    | When AI reads it
------------------|-----------------------------------------|------------------
PRD.md            | What it is + hard scope limits          | Every session
TECH.md           | Approved stack only                     | Before any install
ARCHITECTURE.md   | File placement law                      | Before any new file
AIRULES.md        | Non-negotiable AI rules                 | Every session
PLAN.md           | Full roadmap + stage checklist          | Session start + end
DESIGN.md         | Visual system — colors, fonts, spacing  | Before any UI
ISSUES.md         | Bug log + solutions + gotchas           | Before debugging
SKILLS.md         | AI workflows + prompt templates         | When starting a task
STATE.md          | Live NOW state — step, blockers, next   | FIRST every session
TESTING.md        | Test strategy + what must be tested     | Before shipping anything
CONSTRAINTS.md    | Budget + infra + complexity limits      | Before adding services
LLM_INSTRUCTIONS  | Master synthesis → goes into IDE        | Always loaded

## Folder Structure

/your-project
├── /docs
│   ├── PRD.md
│   ├── TECH.md
│   ├── ARCHITECTURE.md
│   ├── AIRULES.md
│   ├── PLAN.md
│   ├── DESIGN.md
│   ├── ISSUES.md
│   ├── SKILLS.md
│   ├── STATE.md          ← NEW
│   ├── TESTING.md        ← NEW
│   └── CONSTRAINTS.md    ← NEW
├── /system
│   └── LLM_INSTRUCTIONS.md
├── /src
└── /tests

## The Generation Order

1.  PRD.md         — define the product
2.  TECH.md        — lock the stack
3.  CONSTRAINTS.md — set the limits (do this early, it shapes everything)
4.  ARCHITECTURE.md — define the structure
5.  AIRULES.md     — set the rules
6.  TESTING.md     — set the test strategy
7.  PLAN.md        — build the roadmap
8.  DESIGN.md      — define the look
9.  STATE.md       — set the starting state
10. ISSUES.md      — pre-seed with stack gotchas
11. SKILLS.md      — define AI workflows
    LLM_INSTRUCTIONS.md — synthesize everything (no Q&A, pure output)

## The Self-Updating System

After every task, AI automatically:

  1. Updates STATE.md
     → Last Completed, Current Step, Next Action, Recent Changes

  2. Updates PLAN.md
     → Marks completed step ✅, moves pointer to next step

  3. Updates ISSUES.md
     → Logs any bugs found or solved this session

  4. Posts SESSION SUMMARY
     → Done / Now at / Next / Open blockers

This means:
- You never lose track of where you are
- You can stop mid-project and resume with full context
- Every AI session ends with the project more organized than it started

## Loading Into Your IDE

Cursor      → copy LLM_INSTRUCTIONS.md → paste into .cursorrules
Windsurf    → copy into rules / system prompt
Antigravity → paste at session start
Claude.ai   → paste at top of new conversation

## The "You Decide" Rule

On any question you're unsure about — say "you decide".
AI picks the industry best practice for your stack, states
the choice, and moves on. You can override. No 40-question interrogation.
