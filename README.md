# Token Bucket Rate Limiter

A rate limiter built from scratch with Spring Boot, Redis, and JWT — enforcing per-client request limits with tiered access (higher limits for authenticated users) using an atomic token bucket implemented in Lua.

Live at: [https://atul9043.github.io/Rate-Limiter/](https://atul9043.github.io/Rate-Limiter/) (Backend might be paused to conserve hosting credits — email me (atulkr9043@gmail.com) or message me and I'll spin it back up within a few minutes)

## What this demonstrates

- Atomic read-modify-write operations under concurrency, using Redis + Lua scripting (not a library)
- JWT-based authentication with a real user database, integrated into the same request pipeline as the rate limiter
- Custom Spring Security filter chain design, including filter ordering and the tradeoffs of pre-auth vs post-auth checks
- Tiered rate limiting: anonymous requests are limited by IP, authenticated requests by user identity, with separate quotas

## Architecture

```
Request
   │
   ▼
RLFilter — extracts identity (JWT username if present, else IP)
   │       — calls Redis via a Lua script to atomically check/decrement tokens
   │
   ├─ blocked → 429, X-RateLimit-Remaining header, stop
   │
   └─ allowed → continue
   │
   ▼
JwtFilter — full token validation, sets SecurityContext
   │
   ▼
Controller
```

Redis stores each client's bucket as a Hash (`ratelimit:{ip|user}` → `tokens`, `last_refill`), refilled lazily at request time rather than via a background timer.

## Key design decisions

- **Lua scripting for atomicity.** A plain "read tokens, then write tokens" as two separate Redis calls has a race condition: two concurrent requests can both read the same token count before either writes back, allowing both through even when only one token remains. Redis executes a Lua script as a single atomic operation server-side, closing that gap.
- **Rate limiting runs before JWT validation**, not after. Validating a JWT (signature check, DB lookup for user details) is real work; rejecting an over-limit request should happen before paying that cost. The tradeoff: the rate limiter can't use Spring Security's `SecurityContext` (it isn't populated yet), so it extracts the username directly from the raw JWT itself — a cheap decode, not full validation.
- **Token bucket over fixed-window counting.** A fixed window (e.g. "10 requests per 60s, resetting on the minute") allows a burst at the boundary — up to 2x the intended limit in a short window as one period ends and the next begins. Token bucket refills continuously, avoiding this.
- **JWT over session-based auth**, specifically because it's stateless — the rate limiter needs a cheap way to identify the caller without a DB/session lookup, which a bare JWT decode provides and a session ID does not.
- **Secrets in environment variables**, not committed to the repo. JWT signing key and DB credentials are read from `.env` via placeholders in `application.properties`.

## Known limitations

- Rate limit tiers (`MAX_TOKENS`, refill rate/interval) are hardcoded constants rather than externalized config — a quick fix, just not done yet.
- Auth failures currently surface as `403` rather than the more semantically correct `401` — a known Spring Security default behavior, not customized.
- No CORS restriction beyond `*` for local development — would need tightening for any real deployment.

## Running it locally

Requires: Java 17+, Maven, Redis, MySQL.

```bash
# start Redis and MySQL locally, create the database
git clone <repo-url>
cd rate-limiter
# create a .env file — see .env.example
mvn spring-boot:run
```

Open `index.html` (the frontend) in a browser, or test directly via Postman:
- `POST /api/register`, `POST /api/login` — public
- `GET /api/test` — rate-limited demo endpoint, public
- `GET /api/profile` — requires a valid JWT

## Tech stack

Java 17, Spring Boot, Spring Security, Redis (Lua scripting), MySQL, JWT (jjwt), vanilla HTML/JS frontend
