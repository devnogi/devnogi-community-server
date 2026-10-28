import { mkdirSync, writeFileSync } from 'node:fs';

const names = [
  'SERVICE_PORT', 'DB_HOST', 'DB_PORT', 'DB_SCHEMA', 'DB_USER', 'DB_PASSWORD',
  'REDIS_HOST', 'REDIS_PORT', 'REDIS_PASSWORD', 'JWT_SECRET_KEY', 'MINIO_ENDPOINT',
  'MINIO_ACCESS_KEY', 'MINIO_SECRET_KEY', 'MINIO_BUCKET', 'KAFKA_BOOTSTRAP_SERVERS',
  'KAFKA_CONSUMER_GROUP_ID',
];
const lines = names.map(name => {
  const value = process.env[name];
  if (value === undefined || /[\r\n]/.test(value)) throw new Error(`Missing or multiline production setting: ${name}`);
  if (name === 'SERVICE_PORT') {
    if (!/^\d+$/.test(value)) throw new Error('SERVICE_PORT must be numeric');
    return `${name}=${value}`;
  }
  return `${name}='${value.replaceAll("'", "\\'")}'`;
});
if (!process.env.DOCKER_PASSWORD) throw new Error('Missing Docker password');
mkdirSync('.deploy-secrets', { mode: 0o700 });
writeFileSync('.deploy-secrets/.env.candidate', lines.join('\n') + '\n', { mode: 0o600 });
writeFileSync('.deploy-secrets/.docker-password', process.env.DOCKER_PASSWORD, { mode: 0o600 });
