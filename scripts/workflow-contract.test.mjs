import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';

const read = (path) => readFileSync(new URL(`../${path}`, import.meta.url), 'utf8');
const pin = '2b4fbf8f7ab7403959f4641fe6d175b832ffe331';
const ci = read('.github/workflows/pull-request-test.yml');
const prod = read('.github/workflows/spring-prod.yml');
const build = read('build.gradle.kts');
const redisIntegrationTest = read('src/test/java/until/the/eternity/dcs/common/notification/RedisNotificationIntegrationTest.java');

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
  assert.match(build, /^version = "1\.0\.0-SNAPSHOT"$/m);
  assert.match(build, /tasks\.register<Test>\("redisIntegrationTest"\)/);
  assert.match(build, /tasks\.test[\s\S]*excludeTags\("redis-integration"\)/);
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
});
