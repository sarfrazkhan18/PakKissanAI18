'use strict';

/**
 * Kisaan Dost — minimal Gemini proxy (Phase 0.1)
 *
 * Purpose: keep the Gemini API key OFF the device. The Android app ships the key inside
 * the APK today (BuildConfig.GEMINI_API_KEY), where anyone can extract it and bill the
 * account. This proxy holds the key server-side and forwards the app's requests to Google.
 *
 * It deliberately mirrors Google's path shape (/v1beta/models/<model>:generateContent) so
 * the Android client only needs its base URL repointed here — no request/response reshaping.
 * The client-supplied ?key= is ignored; the server injects its own.
 *
 * Zero dependencies: runs on Node 18+ (built-in fetch). `node server.js`.
 *
 * Next steps (later phases, intentionally not built here):
 *   - response cache keyed on (normalised question + district + crop + ISO week)
 *   - auth (device token issued at onboarding) and per-farmer quotas
 *   - request logging / cost-per-query metrics
 */

const http = require('http');

const GEMINI_API_KEY = process.env.GEMINI_API_KEY;
const PORT = parseInt(process.env.PORT || '8080', 10);
const UPSTREAM = 'https://generativelanguage.googleapis.com';
const MAX_BODY_BYTES = 1_000_000; // 1 MB — advisory prompts are small

if (!GEMINI_API_KEY || GEMINI_API_KEY === 'MY_GEMINI_API_KEY') {
  console.error('FATAL: GEMINI_API_KEY is not set. Copy .env.example and set a real key.');
  process.exit(1);
}

// --- tiny in-memory rate limiter (per IP, sliding window) -------------------
const RATE_LIMIT = 60; // requests
const RATE_WINDOW_MS = 60_000; // per minute
const hits = new Map(); // ip -> number[] (timestamps)

function rateLimited(ip) {
  const now = Date.now();
  const arr = (hits.get(ip) || []).filter((t) => now - t < RATE_WINDOW_MS);
  arr.push(now);
  hits.set(ip, arr);
  return arr.length > RATE_LIMIT;
}

function readBody(req) {
  return new Promise((resolve, reject) => {
    let size = 0;
    const chunks = [];
    req.on('data', (c) => {
      size += c.length;
      if (size > MAX_BODY_BYTES) {
        reject(new Error('payload too large'));
        req.destroy();
        return;
      }
      chunks.push(c);
    });
    req.on('end', () => resolve(Buffer.concat(chunks)));
    req.on('error', reject);
  });
}

function sendJson(res, status, obj) {
  const body = JSON.stringify(obj);
  res.writeHead(status, { 'Content-Type': 'application/json' });
  res.end(body);
}

const server = http.createServer(async (req, res) => {
  const ip = req.socket.remoteAddress || 'unknown';
  const url = new URL(req.url, `http://localhost:${PORT}`);

  if (req.method === 'GET' && url.pathname === '/health') {
    return sendJson(res, 200, { ok: true });
  }

  // Only proxy the generative-language surface the app actually uses.
  if (req.method !== 'POST' || !url.pathname.startsWith('/v1beta/')) {
    return sendJson(res, 404, { error: 'not found' });
  }

  if (rateLimited(ip)) {
    return sendJson(res, 429, { error: 'rate limit exceeded, slow down' });
  }

  let body;
  try {
    body = await readBody(req);
  } catch (e) {
    return sendJson(res, 413, { error: String(e.message || e) });
  }

  // Rebuild the upstream URL with OUR key; drop any client-supplied key.
  const upstreamUrl = new URL(UPSTREAM + url.pathname);
  upstreamUrl.searchParams.set('key', GEMINI_API_KEY);

  try {
    const upstream = await fetch(upstreamUrl.toString(), {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body,
    });
    const text = await upstream.text();
    res.writeHead(upstream.status, { 'Content-Type': 'application/json' });
    res.end(text);
  } catch (e) {
    console.error('Upstream error:', e);
    return sendJson(res, 502, { error: 'upstream request failed' });
  }
});

server.listen(PORT, () => {
  console.log(`Kisaan Dost Gemini proxy listening on :${PORT}`);
});
