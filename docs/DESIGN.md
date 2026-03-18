# DESIGN.md — Design System & Visual Language
# FinTrack: Personal Finance REST API

> Applies to three surfaces only: Swagger UI, README, and API error responses.
> AI never invents a color or pattern not defined here.

---

## 1. Design Philosophy

Internal fintech tooling restraint. One deliberate design decision: amber appears in exactly one place — the fraud flag. Nowhere else. When it shows up, it means something.

**Three-word filter:** Would this ship internally?

---

## 2. Color System

```css
:root {
  /* Primary */
  --color-primary:       #1e3a5f;  /* slate blue — all primary actions, headings */
  --color-primary-light: #2d5282;  /* hover states */
  --color-primary-muted: #e8eef5;  /* tinted backgrounds */

  /* Fraud flag — amber, used in exactly one place */
  --color-fraud:         #f59e0b;
  --color-fraud-bg:      #fffbeb;
  --color-fraud-border:  #fcd34d;

  /* Semantic */
  --color-success:       #16a34a;
  --color-success-bg:    #f0fdf4;
  --color-error:         #dc2626;
  --color-error-bg:      #fef2f2;

  /* Surfaces */
  --surface-page:        #f8fafc;
  --surface-card:        #ffffff;
  --surface-sunken:      #e2e8f0;

  /* Text */
  --text-primary:        #0f172a;
  --text-secondary:      #475569;
  --text-muted:          #94a3b8;
  --text-inverse:        #ffffff;

  /* Borders */
  --border-subtle:       #e2e8f0;
  --border-default:      #cbd5e1;
}
```

**Amber rule:** `--color-fraud` and its variants appear only on fraud flag badges and the `isFlagged: true` field in API responses. Never as decoration, never as a general accent.

---

## 3. Typography

No external font loads. System stack only — instant, native feel.

```css
--font-sans: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto,
             "Helvetica Neue", Arial, sans-serif;
--font-mono: "JetBrains Mono", "Fira Code", Consolas, "Courier New", monospace;
```

| Role | Weight | Size | Usage |
|---|---|---|---|
| Heading | 600 | 24px | README h2, section titles |
| Subheading | 600 | 18px | README h3 |
| Body | 400 | 15px | All prose |
| Label | 500 | 13px | Badges, table headers |
| Code | 400 | 13px | All code blocks, inline code |

---

## 4. Spacing & Radius

4px base grid. All spacing is a multiple of 4.

```css
--space-1: 4px;   --space-2: 8px;   --space-3: 12px;
--space-4: 16px;  --space-6: 24px;  --space-8: 32px;
--space-12: 48px; --space-16: 64px;

--radius-sm: 3px;   /* badges */
--radius-md: 6px;   /* cards, buttons, inputs */

--shadow-card: 0 1px 3px rgba(0,0,0,0.08), 0 1px 2px rgba(0,0,0,0.04);
```

---

## 5. Swagger UI

**App name:** `FinTrack API`
**Version:** `v1.0.0`
**Description:** `Personal finance REST API — JWT-authenticated, PostgreSQL-backed, deployed on AWS.`

**Tag order:**
1. `Authentication`
2. `Transactions`
3. `Summary`
4. `Health`

**JWT Authorize button** — configured in `OpenApiConfig.java`:
```java
.addSecurityItem(new SecurityRequirement().addList("Bearer Authentication"))
.components(new Components().addSecuritySchemes("Bearer Authentication",
    new SecurityScheme()
        .type(SecurityScheme.Type.HTTP)
        .scheme("bearer")
        .bearerFormat("JWT")))
```

Every endpoint requires:
- `@Operation(summary = "...")` — plain English, max 8 words
- `@ApiResponse` for 200, 400, 401 at minimum

---

## 6. README Structure

In this order, no deviation:

```
1. Project name + one-line description
2. Live Swagger URL          ← first clickable element, above the fold
3. Tech stack (one line of shields.io badges)
4. What it does              (5-item bullet list)
5. Quick start               (clone → .env → docker-compose up → Swagger)
6. Sample API call sequence  (register → login → transaction → summary → fraud flag)
7. Architecture              (package tree from ARCHITECTURE.md)
8. Running tests
9. Deployment notes
```

**Tone:**
- Active voice. Short sentences.
- No emoji except optionally one in the project name line.
- Every command in a fenced `bash` block. Every JSON in a fenced `json` block.
- Never use: "feel free to," "simply," "just," "easy," "straightforward," "This project demonstrates..."

---

## 7. API Error Response Shape

All errors from `GlobalExceptionHandler`:

```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "JWT token is expired or invalid",
  "timestamp": "2026-03-18T10:23:45Z"
}
```

- `message` is always present, always human-readable
- `timestamp` always ISO 8601 UTC
- No stack traces in responses
- No apologies, no vague messages ("Something went wrong")
- Correct HTTP status codes — never 200 for an error

---

## 8. Do Not

- Use amber anywhere except the fraud flag
- Use `--color-primary` as a background fill (text and borders only)
- Use rounded-full (pill) shapes — `--radius-md` maximum
- Use gradient backgrounds
- Add decorative shadows beyond `--shadow-card`
- Leave any Swagger endpoint with a default auto-generated title
- Use passive voice in README or error messages
- Include "TODO" in the published README