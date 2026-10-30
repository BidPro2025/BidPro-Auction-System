# BidPro web application

Run commands from the repository root:

```powershell
npm.cmd install
npm.cmd run dev:web
npm.cmd run lint
npm.cmd run test:web
npm.cmd run build
```

The maintained source lives in `src/app`, `src/features`, and `src/shared`.
The original incompatible prototype is retained under `legacy` for reference and
is excluded from the application build and lint.

Development uses Vite on port 5173 and forwards `/api` to the API on port 8080.
Copy `.env.example` to `.env` only when overriding the defaults.
`API_PROXY_TARGET` controls Vite's API proxy. `VITE_API_BASE_URL` controls the API
base URL embedded in a production build, including the `/api` path. Leave it empty
for same-origin requests. Product image URLs follow an explicitly configured API
origin as well.

See the [root README](../../README.md) for local accounts and full-stack checks.
