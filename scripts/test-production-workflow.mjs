import { readFileSync, mkdtempSync, mkdirSync, writeFileSync, rmSync } from 'node:fs';
import { tmpdir } from 'node:os';
import { join, dirname, resolve, delimiter } from 'node:path';
import { spawnSync } from 'node:child_process';
import { fileURLToPath } from 'node:url';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..');
const workflow = readFileSync(join(root, '.github/workflows/deploy-with-docker-prod.yml'), 'utf8');
const compose = readFileSync(join(root, 'docker-compose-prod.yml'), 'utf8');
const bash = process.env.DEPLOY_TEST_BASH || (process.platform === 'win32'
  ? join(process.env.LOCALAPPDATA, 'Programs', 'Git', 'bin', 'bash.exe') : '/bin/bash');

test('deployment is restricted to main and a trusted host key is required', () => {
  assert.match(workflow, /push:\s*\n\s*branches: \[main\]/);
  assert.match(workflow, /github\.ref == 'refs\/heads\/main'.*github\.event_name == 'push'.*github\.event_name == 'workflow_dispatch'/);
  assert.doesNotMatch(workflow, /refs\/heads\/dev/);
  assert.match(workflow, /cancel-in-progress: false/);
  assert.match(workflow, /PROD_SERVER_SSH_FINGERPRINT/);
  assert.equal((workflow.match(/fingerprint: \$\{\{ secrets\.PROD_SERVER_SSH_FINGERPRINT \}\}/g) || []).length, 2);
  assert.match(workflow, /test -n "\$(?:HOST_FINGERPRINT|KNOWN_HOSTS)"/);
  assert.match(workflow, /steps\.image\.outputs\.digest/);
  assert.match(compose, /image: \$\{IMAGE_REF:\?/);
  assert.match(compose, /MANAGEMENT_HEALTH_DB_ENABLED=true/);
  assert.match(compose, /MANAGEMENT_HEALTH_REDIS_ENABLED=true/);
});

test('production dotenv encoder treats secret interpolation and quotes literally', () => {
  const dir = mkdtempSync(join(tmpdir(), 'memonogi-env-test-'));
  try {
    const names = ['DB_HOST', 'DB_PORT', 'DB_SCHEMA', 'DB_USER', 'DB_PASSWORD', 'REDIS_HOST',
      'REDIS_PORT', 'REDIS_PASSWORD', 'JWT_SECRET_KEY', 'MINIO_ENDPOINT', 'MINIO_ACCESS_KEY',
      'MINIO_SECRET_KEY', 'MINIO_BUCKET', 'KAFKA_BOOTSTRAP_SERVERS', 'KAFKA_CONSUMER_GROUP_ID'];
    const env = { ...process.env, SERVICE_PORT: '2222', DOCKER_PASSWORD: 'fixture',
      ...Object.fromEntries(names.map(name => [name, 'fixture'])), DB_PASSWORD: "a'$HOME # fixture" };
    const run = spawnSync(process.execPath, [join(root, 'scripts', 'stage-production-env.mjs')],
      { cwd: dir, env, encoding: 'utf8' });
    assert.equal(run.status, 0, run.stderr);
    assert.match(readFileSync(join(dir, '.deploy-secrets', '.env.candidate'), 'utf8'), /DB_PASSWORD='a\\'\$HOME # fixture'/);
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
});

function scenario({ failCandidate = false, noPrevious = false, missingDigest = false, failRecovery = false, failRollback = false } = {}) {
  const dir = mkdtempSync(join(tmpdir(), 'memonogi-deploy-test-'));
  try {
    const bin = join(dir, 'bin');
    mkdirSync(bin);
    mkdirSync(join(dir, 'candidate'));
    writeFileSync(join(dir, 'candidate', 'docker-compose-prod.yml'), 'new compose\n');
    writeFileSync(join(dir, '.env.candidate'), 'SERVICE_PORT=2222\nMARKER=new\n');
    if (!noPrevious) {
      writeFileSync(join(dir, '.env.prod'), 'SERVICE_PORT=1111\nMARKER=old\n');
      writeFileSync(join(dir, 'docker-compose-prod.yml'), 'old compose\n');
    }
    writeFileSync(join(bin, 'docker'), `#!/bin/sh
case "$*" in
  "inspect "*) [ "$NO_PREVIOUS" = 1 ] && exit 1; echo old-id ;;
  "image inspect "*) [ "$MISSING_DIGEST" = 1 ] && exit 1; echo 'registry/repo@sha256:old' ;;
  *".previous-compose.yml"*) echo "$*" >> "$FIXTURE_ROOT/docker.log"; cat .env.prod >> "$FIXTURE_ROOT/docker.log"; [ "$FAIL_ROLLBACK" = 1 ] && exit 1; touch "$FIXTURE_ROOT/recovered" ;;
  *"compose "*"up -d"*) echo "$*" >> "$FIXTURE_ROOT/docker.log" ;;
esac
`, { mode: 0o755 });
    writeFileSync(join(bin, 'curl'), `#!/bin/sh
echo "$*" >> "$FIXTURE_ROOT/curl.log"
if [ "$FAIL_RECOVERY" = 1 ] || { [ "$FAIL_CANDIDATE" = 1 ] && [ ! -f "$FIXTURE_ROOT/recovered" ]; }; then exit 1; fi
echo '{"status":"UP"}'
`, { mode: 0o755 });
    const env = { ...process.env, PATH: bin + delimiter + process.env.PATH,
      IMAGE_REF: 'registry/repo@sha256:new', DOCKERHUB_USERNAME: 'registry',
      DOCKER_USERNAME: 'registry', DOCKER_REPO: 'repo', HEALTH_ATTEMPTS: '1',
      HEALTH_DELAY: '0', CURL_CMD: join(bin, 'curl').replaceAll('\\', '/'), FIXTURE_ROOT: dir, FAIL_CANDIDATE: failCandidate ? '1' : '0',
      NO_PREVIOUS: noPrevious ? '1' : '0', FAIL_RECOVERY: failRecovery ? '1' : '0',
      FAIL_ROLLBACK: failRollback ? '1' : '0', MISSING_DIGEST: missingDigest ? '1' : '0' };
    const run = spawnSync(bash, [join(root, 'scripts', 'deploy-production.sh')],
      { cwd: dir, env, encoding: 'utf8' });
    const read = file => { try { return readFileSync(join(dir, file), 'utf8'); } catch { return ''; } };
    return { status: run.status, error: run.error?.message, output: run.stdout + run.stderr,
      env: read('.env.prod'), compose: read('docker-compose-prod.yml'), docker: read('docker.log'),
      curl: read('curl.log'), recorded: read('.last-deployed-image-ref') };
  } finally {
    rmSync(dir, { recursive: true, force: true });
  }
}

test('successful rollout uses candidate environment and records digest after health', () => {
  const result = scenario();
  assert.equal(result.status, 0, result.error || result.output);
  assert.match(result.env, /MARKER=new/);
  assert.equal(result.compose, 'new compose\n');
  assert.match(result.curl, /:2222\/actuator\/health/);
  assert.match(result.recorded, /sha256:new/);
  assert.doesNotMatch(result.docker, /--no-deps spring-app/);
});

test('failed rollout restores previous environment and compose and checks recovered health', () => {
  const result = scenario({ failCandidate: true });
  assert.notEqual(result.status, 0, result.output);
  assert.match(result.env, /MARKER=old/);
  assert.equal(result.compose, 'old compose\n');
  assert.match(result.docker, /\.previous-compose\.yml.*\.rollback-image\.yml.*--env-file \.previous-env/);
  assert.match(result.docker, /MARKER=old/);
  assert.match(result.curl, /:1111\/actuator\/health/);
  assert.match(result.output, /Previous deployment restored and healthy/);
  assert.equal(result.recorded, '');
});

test('first rollout failure reports that rollback is unavailable', () => {
  const result = scenario({ failCandidate: true, noPrevious: true });
  assert.notEqual(result.status, 0, result.output);
  assert.match(result.output, /automatic rollback unavailable/);
  assert.equal(result.recorded, '');
});

test('missing previous digest restores saved files but reports rollback unavailable', () => {
  const result = scenario({ failCandidate: true, missingDigest: true });
  assert.notEqual(result.status, 0, result.output);
  assert.match(result.output, /automatic rollback unavailable/);
  assert.match(result.env, /MARKER=old/);
  assert.equal(result.compose, 'old compose\n');
});

test('rollback health failure is reported without marking deployment successful', () => {
  const result = scenario({ failCandidate: true, failRecovery: true });
  assert.notEqual(result.status, 0, result.output);
  assert.match(result.output, /Rollback health check failed/);
  assert.equal(result.recorded, '');
});

test('rollback application failure is reported without marking deployment successful', () => {
  const result = scenario({ failCandidate: true, failRollback: true });
  assert.notEqual(result.status, 0, result.output);
  assert.match(result.output, /Rollback application failed/);
  assert.match(result.env, /MARKER=old/);
  assert.equal(result.recorded, '');
});
