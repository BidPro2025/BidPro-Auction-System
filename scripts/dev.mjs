import { spawn } from 'node:child_process';
import { createRequire } from 'node:module';
import { resolve } from 'node:path';
import { root, runApi } from './api.mjs';

const require = createRequire(import.meta.url);
let vite;
try {
  vite = require.resolve('vite/bin/vite.js');
} catch {
  // Vite's export map excludes its CLI entry.
  try {
    vite = resolve(require.resolve('vite/package.json'), '../bin/vite.js');
  } catch {
    console.error('Install dependencies first: npm.cmd install');
    process.exit(1);
  }
}

const api = runApi();
const web = spawn(process.execPath, [vite, '--host', '127.0.0.1', '--strictPort'], {
  cwd: resolve(root, 'apps/web'),
  stdio: 'inherit',
});
const children = [api, web];
let stopping = false;

function stop(code = 0) {
  if (stopping) return;
  stopping = true;
  for (const child of children) {
    if (!child.pid || child.exitCode !== null) continue;
    if (process.platform === 'win32') {
      spawn('taskkill', ['/pid', String(child.pid), '/T', '/F'], {
        windowsHide: true,
        stdio: 'ignore',
      });
    } else {
      child.kill('SIGTERM');
    }
  }
  process.exitCode = code;
}

for (const [index, child] of children.entries()) {
  child.on('error', (error) => {
    console.error(`${index === 0 ? 'Backend' : 'Frontend'} failed: ${error.message}`);
    stop(1);
  });
  child.on('exit', (code) => {
    if (!stopping) stop(code ?? 1);
  });
}
process.on('SIGINT', () => stop());
process.on('SIGTERM', () => stop());
console.log('BidPro: frontend http://127.0.0.1:5173 | API http://127.0.0.1:8080/api');
