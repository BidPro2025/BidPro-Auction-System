import { spawn } from 'node:child_process';
import { existsSync, mkdirSync } from 'node:fs';
import { dirname, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import { loadEnvFile } from 'node:process';

export const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
if (existsSync(resolve(root, '.env'))) loadEnvFile(resolve(root, '.env'));
const windows = process.platform === 'win32';

export function runApi(args = ['spring-boot:run'], options = {}) {
  mkdirSync(resolve(root, '.cache/m2'), { recursive: true });
  // Prefer Maven already installed on PATH. Set BIDPRO_USE_MAVEN_WRAPPER=1
  // when only the checked-in wrapper is available.
  const wrapper = windows ? 'apps\\api\\mvnw.cmd' : './apps/api/mvnw';
  const command = process.env.BIDPRO_USE_MAVEN_WRAPPER === '1' ? wrapper : 'mvn';
  const mavenArgs = [
    '-B',
    '-ntp',
    '-Ddebug=false',
    '-Dspring-boot.run.arguments=--debug=false',
    '-Dmaven.repo.local=.cache/m2',
    '-f',
    'apps/api/pom.xml',
    ...(args.includes('spring-boot:run') ? ['-Dbidpro.build.directory=target-dev'] : []),
    ...args,
  ];
  // Windows executes Maven's .cmd launcher through cmd.exe. Accept only literal
  // Maven goals/properties so command arguments never become shell syntax.
  if (windows && mavenArgs.some((arg) => !/^[A-Za-z0-9_./\\:=,+@-]+$/.test(arg))) {
    throw new Error('Use literal Maven goals/properties without spaces or shell characters.');
  }
  const launchOptions = {
    cwd: root,
    stdio: 'inherit',
    windowsHide: true,
    ...options,
  };
  return windows
    ? spawn([command, ...mavenArgs].join(' '), { ...launchOptions, shell: true })
    : spawn(command, mavenArgs, launchOptions);
}

if (process.argv[1] && resolve(process.argv[1]) === fileURLToPath(import.meta.url)) {
  const child = runApi(process.argv.slice(2));
  child.on('error', (error) => {
    console.error(
      `Cannot start Maven: ${error.message}. Install Maven or set BIDPRO_USE_MAVEN_WRAPPER=1.`,
    );
    process.exitCode = 1;
  });
  child.on('exit', (code) => {
    process.exitCode = code ?? 1;
  });
}
