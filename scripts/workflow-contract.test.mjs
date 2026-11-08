import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import { secretKinds } from './secret-scan.mjs';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
const pin = '698f7b25995156af3f2df7f17171e5c3f5e77d76';
const ci = read('.github/workflows/pull-request-test.yml');
const prod = read('.github/workflows/spring-prod.yml');
const build = read('build.gradle.kts');
const redisIntegrationTest = read('src/test/java/until/the/eternity/dcs/common/notification/RedisNotificationIntegrationTest.java');
const minioHealthIndicator = read('src/main/java/until/the/eternity/dcs/common/infrastructure/MinioHealthIndicator.java');

test('PR caller targets the approved reusable Community CI', () => {
  assert.match(ci, /pull_request:\s*\n\s*branches: \[dev, main\]/);
  assert.match(ci, /workflow_dispatch:/);
  assert.match(ci, new RegExp(`spring-ci\\.yml@${pin}`));
  assert.doesNotMatch(ci, /spring-prod\.yml|\bpush:/);
  assert.match(ci, /node --test scripts\/workflow-contract\.test\.mjs/);
  assert.match(ci, /ci:\s*\n\s*needs: workflow-contract/);
});

test('production caller only permits main push or manual dispatch', () => {
  assert.match(prod, /push:\s*\n\s*branches: \[main\]/);
  assert.match(prod, /workflow_dispatch:/);
  assert.match(prod, /if: github\.ref == 'refs\/heads\/main' && \(github\.event_name == 'push' \|\| github\.event_name == 'workflow_dispatch'\)/);
  assert.doesNotMatch(prod, /pull_request:/);
  assert.match(prod, new RegExp(`spring-prod\\.yml@${pin}`));
  assert.match(prod, /permissions:\s*\n\s*contents: write/);
  assert.match(prod, /concurrency:\s*\n\s*group: community-production\s*\n\s*cancel-in-progress: false/);
  assert.match(prod, /production:\s*\n\s*needs: workflow-contract/);
  assert.match(prod, /deployment-target: prod2/);
  assert.match(prod, /health-timeout: '300'/);
  assert.match(prod, /platform: linux\/amd64/);
  assert.match(prod, /secrets: inherit/);
  assert.match(build, /^version = "1\.0\.12-SNAPSHOT"$/m);
  assert.match(build, /tasks\.register<Test>\("redisIntegrationTest"\)/);
  assert.match(build, /tasks\.test[\s\S]*excludeTags\("redis-integration", "outfit-mysql", "guild-mysql"\)/);
  assert.match(build, /tasks\.register<Test>\("guildMysqlTest"\)/);
  assert.match(read('.github/workflows/outfit-mysql.yml'), /gradlew guildMysqlTest --no-daemon/);
  assert.match(build, /includeTags\("redis-integration"\)/);
  assert.match(redisIntegrationTest, /@Tag\("redis-integration"\)/);
});

test('callers agree on Community inputs and Compose requires the image', () => {
  for (const workflow of [ci, prod]) {
    for (const line of ['service-profile: community-redis', 'service-name: community',
      'dockerfile: Dockerfile', 'compose-file: docker-compose-prod.yml']) {
      assert.ok(workflow.includes(line), line);
    }
  }
  assert.match(read('docker-compose-prod.yml'), /backend:\s*\n\s*container_name: devnogi-community-server\s*\n\s*image: \$\{IMAGE_REF:\?IMAGE_REF is required\}/);
  assert.match(read('docker-compose-prod.yml'), /ports:\s*\n\s*- "127\.0\.0\.1:\$\{SERVICE_PORT:-8093\}:8093"/);
  assert.match(read('docker-compose-prod.yml'), /KAFKA_BOOTSTRAP_SERVERS=kafka:29092/);
  assert.doesNotMatch(read('docker-compose-prod.yml'), /KAFKA_BOOTSTRAP_SERVERS=\$\{KAFKA_BOOTSTRAP_SERVERS\}/);
  for (const key of ['MINIO_ENDPOINT', 'MINIO_ACCESS_KEY', 'MINIO_SECRET_KEY', 'MINIO_BUCKET']) {
    assert.match(read('docker-compose-prod.yml'), new RegExp(`${key}=\\$\\{${key}\\}`));
  }
  assert.match(minioHealthIndicator, /implements HealthIndicator/);
  assert.match(minioHealthIndicator, /minioClient\.bucketExists/);
});

test('secret scan rejects hardcoded JWT environment defaults', () => {
  const synthetic = '${JWT_SECRET_KEY:' + 'a'.repeat(64) + '}';
  assert.deepEqual(secretKinds(synthetic), ['jwt-default-secret']);
});
