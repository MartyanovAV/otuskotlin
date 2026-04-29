# Gate 1 Review Report — FitBridge Business Documentation Package

**Review Date:** 2026-04-29
**Reviewer:** Reviewer Agent
**Scope:** `docs/business/` — BUSINESS_VISION.md, CUSTOMER_PERSONAS.md, CJM.md, GTM_PLAN.md, MONETIZATION_STRATEGY.md, RISK_REGISTER.md, BR/ (8 files)
**Review Type:** Approval-style Gate 1 — internal consistency, business realism, measurability, GTM coherence, monetization validity, scope control, approval-readiness

---

## 1. Findings by Severity

### CRITICAL (Approval Blockers)

**C-01: Contradictory BR document status**
- **Files:** All 8 BR files (`BR/BR-001` through `BR/008`)
- **Location:** Status table, line 7–8 in each file
- **Issue:** Every BR file shows `Статус: ⬜ Черновик` (Draft) AND `Дата утверждения: 2026-04-29` (Approval Date) simultaneously. A document cannot be both unapproved draft and approved on the same date.
- **Impact:** Creates ambiguity about whether requirements are baselined. Blocks handoff to Architect/Executor.
- **Fix:** Set status to "Черновик" with no approval date until Gate 1 is passed, or set to "Утверждён" with the actual approval date.

**C-02: GTM phase targets conflict with revenue plan account counts**
- **Files:** `GTM_PLAN.md` §2 (line 10) vs `BUSINESS_VISION.md` §7 (line 146)
- **Location:** GTM_PLAN.md line 10: "3–6 месяцев → 35+ платящих"; BUSINESS_VISION.md line 146: "Q2 → 60 платящие аккаунты"
- **Issue:** GTM plan targets 35+ paying accounts by month 6, but the revenue plan assumes 60 by end of Q2 (month 6). The 71% gap is unexplained.
- **Impact:** Financial projections and GTM resource planning are misaligned. One of the two documents must be wrong.
- **Fix:** Reconcile the numbers. Either lower the Q2 revenue projection or raise the GTM phase target, with explicit rationale.

**C-03: Market sizing methodology is opaque**
- **File:** `BUSINESS_VISION.md` §3 (lines 36–42)
- **Location:** Lines 36–42
- **Issue:** TAM "≈ ₽1,15 млрд" is derived from "рынке автоматизации фитнеса в РФ менее ₽1 млрд и добавлении русскоязычного внешнего сегмента около 15%." The base figure is not stated, the 15% addition is not sourced, and SAM (₽300–360M) has no derivation from TAM. SOM (₽22–36M) is not explicitly derived from SAM.
- **Impact:** Investors and stakeholders cannot validate the market opportunity. The entire revenue model rests on an unverified market size.
- **Fix:** Provide explicit TAM → SAM → SOM funnel with source citations and percentage assumptions at each step.

### HIGH (Must Resolve Before Approval)

**H-01: "Active client" definition missing from pricing model**
- **Files:** `BUSINESS_VISION.md` §7 (lines 122–127), `MONETIZATION_STRATEGY.md` §2 (lines 10–15)
- **Location:** Pricing tier definitions
- **Issue:** Coach Start is "до 15 активных клиентов", Coach Pro is "15–50 клиентов", but "active client" is never defined. Is it a client with a session in 30 days? 60 days? Assigned a program? This is the core unit of the pricing ladder and is undefined.
- **Impact:** Pricing is unenforceable without this definition. Competitors (TrueCoach, PT Distinction) define it explicitly.
- **Fix:** Add a precise definition of "active client" to the pricing section and BR-007.

**H-02: CAC assumptions appear unrealistically low**
- **File:** `MONETIZATION_STRATEGY.md` §5 (lines 53–58)
- **Location:** CAC hypothesis table
- **Issue:** PLG/referral CAC of ₽1,500–2,500 for B2B SaaS in the Russian fitness market is extremely aggressive. Even with PLG motion, trainer acquisition typically requires content investment, partnership overhead, and sales assist. The implied LTV/CAC ratios (6.8–15x) are best-in-class SaaS numbers, not startup-year-1 numbers.
- **Impact:** If actual CAC is 2–3x higher, the unit economics still work but the payback period extends significantly, affecting cash flow planning.
- **Fix:** Add a sensitivity analysis showing LTV/CAC at 2x and 3x CAC assumptions. Flag current CAC as optimistic hypothesis requiring validation in first 90 days.

**H-03: No absolute GTM budget stated**
- **File:** `GTM_PLAN.md` §8 (lines 79–86)
- **Location:** Budget allocation table
- **Issue:** Budget is expressed only as percentages (30% content, 20% partnerships, etc.) with no absolute ruble amount. Without a total budget, the percentages are meaningless for planning.
- **Impact:** Cannot determine if the GTM plan is adequately funded or if CAC targets are achievable at the implied spend level.
- **Fix:** State total Year 1 GTM budget in rubles and show the ruble allocation per channel.

**H-04: Trial-to-paid conversion target of 30% is aggressive**
- **Files:** `BUSINESS_VISION.md` §4 (line 69), `GTM_PLAN.md` §6 (line 64), `MONETIZATION_STRATEGY.md` §9 (line 100)
- **Location:** All three documents cite ≥30% trial-to-paid by month 12
- **Issue:** Industry average for B2B SaaS trial-to-paid is 15–25%. A 30% target is top-decile and requires exceptional product-market fit and onboarding. No rationale is given for why FitBridge would outperform the category.
- **Impact:** Revenue projections are inflated if conversion lands at industry average.
- **Fix:** Add a scenario showing revenue at 20% and 25% conversion. Justify the 30% target with specific product differentiators or pilot data.

**H-05: Revenue structure by Year 3 is not reconciled with account counts**
- **File:** `BUSINESS_VISION.md` §7 (lines 150–154)
- **Location:** Revenue structure breakdown
- **Issue:** Claims 63% solo, 22% studio, 10% add-ons, 5% onboarding = 100% of ₽29.9M ARR. But with 1,000 paying accounts at blended ARPU ₽2,490, the solo trainer revenue (₽18.8M) implies an average solo ARPU that isn't reconciled with the Start/Pro tier split. The mix of Start vs Pro accounts is not stated.
- **Impact:** Cannot validate whether the blended ARPU of ₽2,490 is achievable given the tier pricing.
- **Fix:** Show the assumed mix of Start/Pro/Team accounts that produces the blended ARPU and revenue structure.

### MEDIUM (Should Resolve, Not Blockers)

**M-01: LTV calculation rounding error for Coach Start**
- **File:** `MONETIZATION_STRATEGY.md` §5 (line 48)
- **Location:** Line 48
- **Issue:** ₽1,490 × 0.82 × 14 = ₽17,105.2, but document states ₽17,115. The ₽10 discrepancy is minor but indicates calculation was not verified.
- **Fix:** Correct to ₽17,105 or show the rounding logic.

**M-02: No baseline measurements for success metrics**
- **Files:** `BR/BR-001` (line 54), `BR/BR-004` (line 53), `BR/BR-005` (lines 49–50), `BR/BR-006` (lines 66–68)
- **Issue:** Metrics like "D30 retention ≥ 35%", "≥ 55% trainers create first program in 7 days" have no baseline. For a pre-launch product this is expected, but the documents should explicitly state "baseline: TBD — to be established in pilot."
- **Fix:** Add "baseline: TBD" notation to all success metrics in BR files.

**M-03: International competitor prices not converted to RUB for comparison**
- **File:** `MONETIZATION_STRATEGY.md` §2 (lines 10–15)
- **Location:** Competitor pricing table
- **Issue:** TrueCoach ($26.34–$136.99), PT Distinction ($19.90–$89.90) are shown in USD without RUB equivalent. The reader cannot immediately assess the price gap. At ~₽90/USD, TrueCoach Start is ~₽2,370 vs FitBridge Start at ₽1,490 — a 37% discount. This should be explicit.
- **Fix:** Add RUB equivalents at a stated exchange rate and show the percentage discount.

**M-04: Risk Register and Business Vision risk matrix are duplicated with different structures**
- **Files:** `BUSINESS_VISION.md` §11 (lines 219–232) vs `RISK_REGISTER.md` (lines 4–19)
- **Issue:** The same risks appear in both documents with different formats. BUSINESS_VISION.md uses qualitative probability (Средняя/Высокая) while RISK_REGISTER.md uses numeric (1–5). This creates maintenance burden and potential drift.
- **Fix:** Keep the detailed risk register as the single source of truth. Reference it from BUSINESS_VISION.md instead of duplicating.

**M-05: No explicit MVP scope summary document**
- **Files:** All BR files individually
- **Issue:** Each BR file states MVP vs Phase 2 scope, but there is no consolidated MVP scope document. The Architect and Executor need a single source of truth for what ships in v1.
- **Fix:** Create a consolidated MVP scope summary that aggregates all MVP items from BR-001 through BR-008.

**M-06: CJM mermaid diagrams use inconsistent section naming**
- **File:** `CJM.md` (lines 3–21, 32–50, 61–79, 90–108, 119–136)
- **Issue:** Irina's CJM uses Russian section names (Осознание, Оценка, Активация...), Alexey's uses mixed English/Russian (Триггер, Evaluation, Start, Use, Expansion), Maria's uses Russian, Nikita's uses Russian, Olga's uses Russian. Inconsistency reduces professionalism.
- **Fix:** Standardize all CJM section names to one language (preferably Russian for consistency with the rest of the package).

### LOW (Nice to Have)

**L-01: No explicit definition of North Star Metric calculation**
- **File:** `BUSINESS_VISION.md` §4 (line 47)
- **Issue:** "Количество активных клиент-тренерских связок с еженедельной активностью" — what constitutes "weekly activity"? A logged workout? A program view? A message?
- **Fix:** Add operational definition of "weekly activity."

**L-02: SWOT "Возможности" includes "Рост фитнес-рынка" without data**
- **File:** `BUSINESS_VISION.md` §6 (line 113)
- **Issue:** Claims market growth but the only source is TAdviser. Add a specific growth rate (e.g., "CAGR X% 2024–2027 per TAdviser").
- **Fix:** Add the specific growth figure with citation.

**L-03: BR system constraints lack justification**
- **Files:** All BR files, System Constraints tables
- **Issue:** Latency targets (800ms–1500ms) and throughput (15–30 req/sec) are stated without rationale. Are these based on expected load, UX research, or arbitrary?
- **Fix:** Add a note on how these numbers were derived or mark as "TBD — to be validated in load testing."

---

## 2. Open Questions / Approval Blockers

| # | Question | Blocking? | Related Finding |
|---|----------|-----------|-----------------|
| Q1 | What is the precise definition of "active client" for pricing tier enforcement? | **YES** | H-01 |
| Q2 | Which is correct: 35 or 60 paying accounts by month 6? | **YES** | C-02 |
| Q3 | What is the total Year 1 GTM budget in rubles? | **YES** | H-03 |
| Q4 | What is the explicit TAM → SAM → SOM derivation with sources? | **YES** | C-03 |
| Q5 | Are all BR documents approved or still draft? | **YES** | C-01 |
| Q6 | What is the assumed Start/Pro/Team account mix that produces ₽2,490 blended ARPU? | No (but important) | H-05 |
| Q7 | What exchange rate was used for competitor price comparison? | No | M-03 |
| Q8 | Is there pilot data or customer interviews supporting the 30% trial-to-paid target? | No (but important) | H-04 |
| Q9 | Where is the consolidated MVP scope document for Architect/Executor handoff? | No (but important) | M-05 |

---

## 3. Strengths Noted

The package demonstrates several areas of high quality:

1. **Differentiated positioning:** The client-owned data model is clearly articulated and genuinely differentiates from both international coach apps and Russian club CRMs.
2. **Realistic scope discipline:** BR-003 (multi-specialist) and BR-008 (team management) are correctly deferred to Phase 2, showing good scope control.
3. **Comprehensive risk awareness:** The risk register covers technical, business, regulatory, and operational risks with owners, timelines, and early warning indicators.
4. **Honest hypothesis labeling:** MONETIZATION_STRATEGY.md §10 explicitly separates confirmed facts from hypotheses — this is rare and valuable.
5. **Persona-CJM alignment:** All 5 personas have matching CJMs with consistent pain points and opportunities.
6. **Regulatory awareness:** 152-ФЗ compliance is properly identified with specific requirements (Article 10, data localization, consent flows).
7. **GTM channel diversification:** The plan avoids over-reliance on any single channel, with a 35% cap on any one partner.

---

## 4. Residual Risks (Even After Fixes)

| Risk | Description |
|------|-------------|
| Market validation gap | No evidence of 20+ problem interviews or pricing interviews being completed. The entire plan rests on unvalidated assumptions about trainer willingness to pay. |
| Cash flow timing | Revenue projections are annual/quarterly but no monthly cash flow model is provided. Early-stage SaaS typically burns cash for 12–18 months before positive unit economics. |
| Competitive response | Russian club CRM players (1С:Фитнес, FitBase, Mobifitness) could launch solo-trainer tiers with existing distribution. The moat (client-owned data) takes time to build. |
| Regulatory uncertainty | Health data classification under 152-ФЗ Article 10 may require additional licensing or restrictions that could limit the product scope. |

---

## 5. Verdict

### **REJECT — with clear path to approval**

The business documentation package is **well-structured, internally coherent in most areas, and demonstrates strong strategic thinking**. However, **5 critical issues** must be resolved before Gate 1 approval can be granted:

1. **C-01:** Resolve the draft/approved status contradiction in all BR files.
2. **C-02:** Reconcile GTM phase targets with revenue plan account counts.
3. **C-03:** Provide transparent TAM → SAM → SOM derivation.
4. **H-01:** Define "active client" for pricing enforcement.
5. **H-03:** State absolute GTM budget in rubles.

Additionally, **H-02** (CAC realism), **H-04** (conversion target justification), and **H-05** (revenue structure reconciliation) should be addressed to ensure the financial model is defensible.

**Estimated effort to resolve:** 1–2 days of focused revision by the Product Owner.

**Recommendation:** Return to Product Owner for revision. Re-submit for Gate 1 review after addressing all CRITICAL and HIGH findings.
