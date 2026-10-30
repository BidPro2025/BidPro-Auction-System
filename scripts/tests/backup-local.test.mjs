import assert from 'node:assert/strict';
import { mkdtemp, mkdir, writeFile, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import { resolve } from 'node:path';
import { test } from 'node:test';
import { createLocalBackup, verifyLocalBackup } from '../backup-local.mjs';

test('offline backup preserves database and uploads, verifies contents, and detects corruption', async () => {
  const directory = await mkdtemp(resolve(tmpdir(), 'bidpro-backup-test-'));
  try {
    const source = resolve(directory, 'source');
    const backup = resolve(directory, 'backup');
    await mkdir(resolve(source, 'uploads'), { recursive: true });
    await writeFile(resolve(source, 'bidpro.mv.db'), 'database fixture');
    await writeFile(resolve(source, 'uploads/item.png'), 'image fixture');
    assert.equal((await createLocalBackup(source, backup)).files, 2);
    assert.equal((await verifyLocalBackup(backup)).files, 2);
    await writeFile(resolve(backup, 'data/bidpro.mv.db'), 'corrupted');
    await assert.rejects(verifyLocalBackup(backup), /integrity check failed/);
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
});
test('backup refuses nested destinations and active database lock files', async () => {
  const directory = await mkdtemp(resolve(tmpdir(), 'bidpro-backup-guard-'));
  try {
    await writeFile(resolve(directory, 'bidpro.mv.db'), 'database fixture');
    await assert.rejects(
      createLocalBackup(directory, resolve(directory, 'nested')),
      /outside the source/,
    );
    await writeFile(resolve(directory, 'bidpro.lock.db'), 'active');
    await assert.rejects(createLocalBackup(directory, `${directory}-backup`), /Database lock file/);
  } finally {
    await rm(directory, { recursive: true, force: true });
  }
});
