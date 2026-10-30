import { createHash, randomUUID } from 'node:crypto';
import { createReadStream } from 'node:fs';
import { chmod, cp, mkdir, readFile, readdir, stat, writeFile } from 'node:fs/promises';
import { createConnection } from 'node:net';
import { dirname, isAbsolute, relative, resolve, sep } from 'node:path';
import { fileURLToPath } from 'node:url';
import { root } from './api.mjs';

async function filesUnder(directory, prefix = '') {
  const files = [];
  for (const entry of await readdir(resolve(directory, prefix), { withFileTypes: true })) {
    if (entry.isSymbolicLink())
      throw new Error('Backup directories must not contain symbolic links.');
    const name = prefix ? `${prefix}/${entry.name}` : entry.name;
    if (entry.isDirectory()) files.push(...(await filesUnder(directory, name)));
    else if (entry.isFile()) files.push(name);
  }
  return files.sort();
}
async function digest(path) {
  const hash = createHash('sha256');
  for await (const block of createReadStream(path)) hash.update(block);
  return hash.digest('hex');
}
function within(directory, name) {
  const target = resolve(directory, name);
  const relation = relative(directory, target);
  if (!relation || relation === '..' || relation.startsWith(`..${sep}`) || isAbsolute(relation)) {
    throw new Error('Backup manifest contains an invalid file path.');
  }
  return target;
}
export async function createLocalBackup(sourceDirectory, backupDirectory) {
  const source = resolve(sourceDirectory);
  const destination = resolve(backupDirectory);
  const relation = relative(source, destination);
  if (
    !relation ||
    (!relation.startsWith(`..${sep}`) && relation !== '..' && !isAbsolute(relation))
  ) {
    throw new Error('Backup destination must be outside the source directory.');
  }
  const names = await filesUnder(source);
  if (!names.some((name) => name.endsWith('.mv.db')))
    throw new Error('No local H2 database was found.');
  if (names.some((name) => name.endsWith('.lock.db')))
    throw new Error('Database lock file exists. Stop all application processes before backing up.');
  await mkdir(dirname(destination), { recursive: true, mode: 0o700 });
  await mkdir(destination, { mode: 0o700 }); // Refuse to overwrite an existing backup.
  const entries = [];
  for (const name of names) {
    const original = within(source, name);
    const copy = within(resolve(destination, 'data'), name);
    await mkdir(dirname(copy), { recursive: true, mode: 0o700 });
    await cp(original, copy, { errorOnExist: true, force: false });
    await chmod(copy, 0o600);
    entries.push({ path: name, bytes: (await stat(copy)).size, sha256: await digest(copy) });
  }
  await writeFile(
    resolve(destination, 'manifest.json'),
    JSON.stringify(
      {
        format: 'bidpro-local-backup-v1',
        createdAt: new Date().toISOString(),
        files: entries,
      },
      null,
      2,
    ),
    { flag: 'wx', mode: 0o600 },
  );
  await verifyLocalBackup(destination);
  return { directory: destination, files: entries.length };
}
export async function verifyLocalBackup(directory) {
  const manifest = JSON.parse(await readFile(resolve(directory, 'manifest.json'), 'utf8'));
  if (
    manifest.format !== 'bidpro-local-backup-v1' ||
    !Array.isArray(manifest.files) ||
    !manifest.files.length
  ) {
    throw new Error('Unsupported or empty backup manifest.');
  }
  const actual = await filesUnder(resolve(directory, 'data'));
  const recorded = manifest.files.map((entry) => entry.path).sort();
  if (JSON.stringify(actual) !== JSON.stringify(recorded))
    throw new Error('Backup file inventory does not match its manifest.');
  for (const entry of manifest.files) {
    const path = within(resolve(directory, 'data'), entry.path);
    if ((await stat(path)).size !== entry.bytes || (await digest(path)) !== entry.sha256) {
      throw new Error(`Backup integrity check failed for ${entry.path}.`);
    }
  }
  return { files: manifest.files.length };
}
async function requireStopped(port) {
  if (!Number.isInteger(port) || port < 1 || port > 65535) throw new Error('PORT must be valid.');
  await new Promise((accept, reject) => {
    const socket = createConnection({ host: '127.0.0.1', port });
    socket.setTimeout(1000);
    socket.once('connect', () => {
      socket.destroy();
      reject(new Error('Stop the API before creating an offline database backup.'));
    });
    socket.once('error', (error) => {
      socket.destroy();
      if (error.code === 'ECONNREFUSED') accept();
      else reject(error);
    });
    socket.once('timeout', () => {
      socket.destroy();
      reject(new Error('Cannot establish whether the API is stopped.'));
    });
  });
}
if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  try {
    const args = process.argv.slice(2);
    if (args[0] === '--verify' && args.length === 2) {
      console.log('Backup verified:', await verifyLocalBackup(resolve(args[1])));
    } else {
      if (args.length && !(args[0] === '--output' && args.length === 2))
        throw new Error('Use --output <new-directory> or --verify <backup-directory>.');
      if ((process.env.SPRING_PROFILES_ACTIVE || 'local') !== 'local')
        throw new Error(
          'This tool backs up the offline local H2 database only. Use your database backup process for MySQL.',
        );
      await requireStopped(Number(process.env.PORT || 8080));
      const destination =
        args[1] ||
        resolve(
          root,
          '.cache/backups',
          `${new Date().toISOString().replace(/[:.]/g, '-')}-${randomUUID().slice(0, 8)}`,
        );
      console.log(
        'Offline backup created and verified:',
        await createLocalBackup(resolve(root, 'apps/api/data'), destination),
      );
    }
  } catch (error) {
    console.error(error.message);
    process.exitCode = 1;
  }
}
