import test from 'node:test';
import assert from 'node:assert/strict';
import {readFileSync} from 'node:fs';
import {classify, analyze} from './loglens.mjs';

for (const [message, expected] of [
  ['snapshot failed: disk quota exceeded', 'Disk capacity'],
  ['allocator reports out of memory', 'Memory pressure'],
  ['tls handshake rejected: certificate expired yesterday', 'Certificate expiry'],
  ['backend retry scheduled after dependency unavailable', 'Downstream symptom'],
  ['worker finished batch successfully', 'Unclassified'],
  ['memory', 'Unclassified']
]) test(message, () => assert.equal(classify(message).label, expected));

test('orders shuffled events, retains evidence and reports bad input', () => {
  const sample = readFileSync(new URL('./demo.jsonl', import.meta.url),'utf8');
  const report = analyze(sample + 'broken json\n{}');
  assert.deepEqual(report.events.map(e => e.line), [2,3,1,4]);
  assert.equal(report.candidate.line, 3);
  assert.deepEqual(report.rejected, [5,6]);
});
test('symptoms alone cannot supply a cause', () => {
  const sample = readFileSync(new URL('./demo.jsonl', import.meta.url),'utf8');
  assert.equal(analyze(sample.split('\n')[0]).candidate, null);
});
