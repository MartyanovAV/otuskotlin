---
name: brainstorming
description: Use when users need to generate ideas, explore creative solutions, or brainstorm approaches before implementation.
---

# Brainstorming Skill

## Overview

This skill serves two critical purposes:

1. **Interactive Design Process:** Guides the AI through a natural, collaborative dialogue to turn ideas into fully formed designs and specs *before* any code is written.
2. **Comprehensive Ideation Framework:** Provides 30+ research-validated prompt patterns to help generate high-quality ideas across any domain.

> Do NOT invoke any implementation skill, write any code, scaffold any project, or take any implementation action until you have completed the brainstorming process, presented a design, and the user has approved it. This applies to EVERY project regardless of perceived simplicity.

## The Brainstorming Workflow

Complete these steps in order:

1. **Explore context** — check project state, files, docs, recent commits
2. **Ask clarifying questions** — one at a time, understand purpose/constraints/success criteria
3. **Propose 2-3 approaches** — with trade-offs and your recommendation
4. **Present design** — in sections scaled to complexity, get user approval after each section
5. **Document the result** — write the validated design to `docs/plans/YYYY-MM-DD-<topic>-design.md` and commit
6. **Transition** — invoke a planning or implementation skill only *after* approval

## Conversational Principles

- **One question at a time** — don't overwhelm with multiple questions
- **Multiple choice preferred** — easier than open-ended questions when possible
- **YAGNI ruthlessly** — remove unnecessary features from all designs
- **Explore alternatives** — always propose 2-3 approaches before settling
- **Incremental validation** — present the design, get approval before moving on
- **Be flexible** — go back and clarify when something doesn't make sense

## Pattern Categories for Ideation

When proposing approaches, use these 14 systematic categories:

1. **Perspective Multiplication** — ideas from multiple viewpoints and stakeholder angles
2. **Constraint Variation** — explore idea space through artificial constraints
3. **Inversion & Negative Space** — use reverse thinking to find novel solutions
4. **Analogical Transfer** — apply patterns from different domains
5. **Systematic Feature Decomposition** — SCAMPER and attribute-based ideation
6. **Scenario Exploration** — future-based and "what if" thinking
7. **Constraint-Based Structured Ideation** — build within hard constraints
8. **Chain-of-Thought Reasoning** — multi-step refinement processes
9. **Combination & Morphological Exploration** — force novel feature combinations
10. **Assumption Challenge** — question premises and invert assumptions
11. **Fill-in-the-Blank Templates** — structured completion formats
12. **Competitive Positioning** — differentiation matrix approaches
13. **Extreme Scaling** — 10x thinking and exponential scenarios
14. **Stakeholder & Empathy-Based** — customer journey and persona patterns

## Output Format

- Numbered list > bullet points (better for idea tracking)
- Table format: `Idea | Reasoning | Implementation | Trade-offs` (forces completeness)
- For each idea, explain your reasoning (increases quality significantly)
- Specify word count ranges (200-400 words prevents both brevity and verbosity)

## Notes for AI Instances

- Start by assessing if this is an **implementation design task** or a **content/marketing ideation task**
- ALWAYS enforce the workflow — never jump to code without an approved design
- When generating ideas/approaches, provide 2-3 concrete options with trade-offs
- Cite patterns used when generating ideas
