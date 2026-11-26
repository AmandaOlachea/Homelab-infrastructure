import { readFileSync } from 'node:fs';
import { pathToFileURL } from 'node:url';

// Short synthetic training examples, grouped by diagnostic label.
const examples = {
  'Disk capacity': ['volume full no space left on device', 'disk quota exceeded write failed', 'filesystem exhausted free blocks'],
  'Memory pressure': ['out of memory killed process', 'allocation failed memory exhausted', 'oom killer terminated worker'],
  'Certificate expiry': ['tls certificate expired during handshake', 'x509 certificate past expiration', 'peer certificate validity period ended'],
  'Downstream symptom': ['upstream connection timed out', 'dependency unavailable retry scheduled', 'gateway returned 503 bad gateway']
};

const words = text => new Set(text.toLowerCase().match(/[a-z0-9]+/g) || []);
const corpus = Object.entries(examples).flatMap(([label, rows]) =>
  rows.map(row => ({label, terms: words(row)})));
const frequencies = new Map();
for (const {terms} of corpus) {
  for (const term of terms) frequencies.set(term, (frequencies.get(term) || 0) + 1);
}
const weight = term => (1 + Math.log((1 + corpus.length) / (1 + (frequencies.get(term) || 0)))) ** 2;
const mass = terms => [...terms].reduce((sum, term) => sum + weight(term), 0);

export function classify(message) {
  const query = words(message);
  let best = {label: 'Unclassified', similarity: 0};
  for (const {label, terms} of corpus) {
    const common = [...terms].filter(term => query.has(term));
    if (common.length < 2) continue;
    const score = mass(common) / Math.sqrt(mass(query) * mass(terms));
    if (score >= 0.28 && score > best.similarity) best = {label, similarity: score};
  }
  return best;
}

export function analyze(input) {
  const events = [], rejected = [];
  for (const [index, raw] of input.split(/\r?\n/).entries()) {
    if (!raw.trim()) continue;
    try {
      const {time, service, message} = JSON.parse(raw);
      if (![time, service, message].every(value => typeof value === 'string' && value.trim()) ||
          !/(Z|[+-]\d\d:\d\d)$/i.test(time) || !Number.isFinite(Date.parse(time))) throw Error();
      events.push({line: index + 1, time: new Date(time).toISOString(), service, message, ...classify(message)});
    } catch { rejected.push(index + 1); }
  }
  events.sort((a,b) => a.time.localeCompare(b.time) || a.line - b.line);
  const candidate = events.find(e => !['Downstream symptom','Unclassified'].includes(e.label)) ?? null;
  return {candidate, events, rejected};
}

if (process.argv[1] && import.meta.url === pathToFileURL(process.argv[1]).href) {
  try {
    const args = process.argv.slice(2);
    const path = args.find(arg => arg !== '--json');
    const report = analyze(readFileSync(path === '-' ? 0 : path || new URL('./demo.jsonl', import.meta.url), 'utf8'));
    if (args.includes('--json')) console.log(JSON.stringify(report, null, 2));
    else {
      const lead = report.candidate;
      console.log(lead ? `Investigate ${lead.label} at ${lead.service} (line ${lead.line})` : 'No supported cause identified');
      for (const e of report.events) console.log(`${e.time}  L${e.line}  ${e.service}  [${e.label}]  ${e.message}`);
      if (report.rejected.length) console.error('Skipped malformed lines:', report.rejected.join(', '));
    }
    if (!report.events.length) process.exitCode = 1;
  } catch (error) { console.error(error.message); process.exitCode = 1; }
}
