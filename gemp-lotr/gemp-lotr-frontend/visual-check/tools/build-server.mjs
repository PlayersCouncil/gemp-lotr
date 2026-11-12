#!/usr/bin/env node
// Builds the Java server into .local-server/jar-build and runs its web tests, from a copy of the checkout and inside a
// container, so the web.jar a running development server uses is never replaced. `check:all` tests with this build.
import { spawnSync } from 'node:child_process';
import fs from 'node:fs';
import path from 'node:path';
import { fileURLToPath } from 'node:url';

const HERE = path.dirname(fileURLToPath(import.meta.url));
const REPOSITORY = path.resolve(HERE, '../../../..');
const BUILD_DIR = path.resolve(HERE, '../.local-server/jar-build');
export const BUILT_JAR = path.join(BUILD_DIR, 'gemp-lotr/gemp-lotr-async/target/web.jar');

function run(command, args, options = {}) {
  const result = spawnSync(command, args, { stdio: 'inherit', maxBuffer: 1 << 30, ...options });
  if (result.status !== 0) {
    throw new Error(`${command} ${args[0]} failed (exit ${result.status})`);
  }
  return result.stdout;
}

/** Only tracked and new files: no build output, no card images, and of the frontend only what Maven reads. */
function copySources() {
  run('docker', [
    'run',
    '--rm',
    '-v',
    `${path.dirname(BUILD_DIR)}:/parent`,
    'node:24-alpine',
    'rm',
    '-rf',
    '/parent/jar-build',
  ]);
  fs.mkdirSync(BUILD_DIR, { recursive: true });
  const files = run(
    'git',
    [
      'ls-files',
      '-co',
      '--exclude-standard',
      '-z',
      '--',
      'gemp-lotr',
      ':!gemp-lotr/gemp-lotr-async/src/main/web/images',
      ':!gemp-lotr/gemp-lotr-frontend',
    ],
    { cwd: REPOSITORY, stdio: ['ignore', 'pipe', 'inherit'] },
  );
  const frontendPom = run('git', ['ls-files', '-z', '--', 'gemp-lotr/gemp-lotr-frontend/pom.xml'], {
    cwd: REPOSITORY,
    stdio: ['ignore', 'pipe', 'inherit'],
  });
  const archive = path.join(path.dirname(BUILD_DIR), 'sources.tar');
  run('tar', ['--null', '-T', '-', '--ignore-failed-read', '-cf', archive], {
    cwd: REPOSITORY,
    input: Buffer.concat([files, frontendPom]),
    stdio: ['pipe', 'inherit', 'inherit'],
  });
  run('tar', ['-xf', archive, '-C', BUILD_DIR]);
  fs.rmSync(archive);
}

function buildAndTest() {
  const script = [
    'mvn -q package -Dskip.frontend=true -DskipTests -pl gemp-lotr-async -am',
    // Only the web server's own tests: the card tests of the other modules take far longer and aren't affected
    'mvn -q surefire:test -DskipTests=false -Dsurefire.failIfNoSpecifiedTests=false ' +
      "-Dtest='com.gempukku.lotro.async.**' -pl gemp-lotr-async -am",
  ].join(' && ');
  const owner = `${process.getuid()}:${process.getgid()}`;
  run('docker', [
    'run',
    '--rm',
    '-v',
    `${path.join(BUILD_DIR, 'gemp-lotr')}:/build`,
    '-v',
    'gemp_m2:/root/.m2',
    '-w',
    '/build',
    'gemp_app',
    'sh',
    '-c',
    `status=0; (${script}) || status=$?; chown -R ${owner} /build; exit $status`,
  ]);
}

if (process.argv[1] === fileURLToPath(import.meta.url)) {
  copySources();
  buildAndTest();
  console.log(`Built and tested: ${BUILT_JAR}`);
}
