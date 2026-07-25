# Kisaan Dost — Gemini proxy (Phase 0.1)

A minimal server that keeps the Gemini API key **off the Android device**. The app currently
embeds the key in the APK (`BuildConfig.GEMINI_API_KEY`), where it can be extracted and abused.
This proxy holds the key server-side and forwards the app's requests to Google unchanged.

## Why

- **Security (defect D1):** removes the extractable key from the shipped app.
- **Cost control (D8):** a single choke point where grounding, caching, quotas, and
  per-query cost logging can be added without an app release.

## Run locally

```bash
cd backend
cp .env.example .env      # then edit .env and set a real GEMINI_API_KEY
node server.js            # needs Node 18+ (built-in fetch); zero npm dependencies
```

Health check:

```bash
curl localhost:8080/health         # -> {"ok":true}
```

## How the app connects

The proxy mirrors Google's path shape, so the client needs **no request reshaping** — only a
base-URL change:

1. Deploy this server behind HTTPS.
2. In `app/build.gradle.kts`, set `GEMINI_API_BASE_URL` to your proxy origin
   (e.g. `https://api.kisaandost.pk/`).
3. Rebuild. The app's `?key=` query param is ignored by the proxy, which injects the real
   key itself. **Remove `GEMINI_API_KEY` from the app's `.env`** once the proxy is live — that
   is the step that actually closes D1.

## Endpoints

| Method | Path | Purpose |
| --- | --- | --- |
| `GET` | `/health` | Liveness probe |
| `POST` | `/v1beta/models/<model>:generateContent` | Forwarded to Google with the server key |

Built-in: 1 MB body cap, simple per-IP rate limit (60 req/min).

## Deliberately not built yet (later phases)

- Response cache keyed on (normalised question + district + crop + ISO week) — see plan §9 G2.
- Device-token auth issued at onboarding + per-farmer daily quota (G6).
- Request logging and cost-per-query / cost-per-farmer metrics (G7).
- Dedicated `/mandi`, `/weather`, `/content`, `/feedback` endpoints (plan §10.1).
