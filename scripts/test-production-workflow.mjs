import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import assert from 'node:assert/strict';

const workflow = readFileSync(new URL('../.github/workflows/deploy-with-docker-prod.yml', import.meta.url), 'utf8');
const compose = readFileSync(new URL('../docker-compose-prod.yml', import.meta.url), 'utf8');

test('only main push or main manual dispatch can deploy, with non-cancelling concurrency', () => {
  assert.match(workflow, /push:\s*\n\s*branches: \[main\]/);
  assert.match(workflow, /github\.ref == 'refs\/heads\/main'.*github\.event_name == 'push'.*github\.event_name == 'workflow_dispatch'/);
  assert.doesNotMatch(workflow, /refs\/heads\/dev/);
  assert.match(workflow, /cancel-in-progress: false/);
});

test('deploys built digest and checks health before recording successful deployment', () => {
  assert.match(workflow, /steps\.image\.outputs\.digest/);
  assert.match(workflow, /docker pull "\$IMAGE_REF"/);
  assert.match(compose, /image: \$\{IMAGE_REF:\?/);
  assert.doesNotMatch(workflow, /docker compose[^\n]* down/);
  assert.match(workflow, /actuator\/health/);
  assert.match(workflow, /if \[ "\$healthy" != true \]; then rollback \|\| true; exit 1; fi/);
  assert.match(workflow, /No previous immutable image; automatic rollback unavailable/);
  assert.match(workflow, /\.previous-compose\.yml -f \.rollback-image\.yml/);
  assert.ok(workflow.indexOf('if [ "$healthy" != true ]') < workflow.indexOf('.last-deployed-image-ref'));
});
