# LogLens

A local incident-triage CLI for answering “which log entry should I investigate first?” It sorts events across services, labels known fault patterns, and points back to the original input line. All example services and events are fictional.

## Try the outage

From this folder with Node.js 20 or newer:

```sh
node loglens.mjs
node --test loglens.test.mjs
```

The first line of the demo is a gateway timeout. The analyzer reconstructs the timeline and points to line 3, where the database reported a failed write seven seconds before the timeout was logged.

## Bring your own logs

Use one JSON object per line with string fields `time` (ISO timestamp with timezone), `service`, and `message`. See [the sample](demo.jsonl). Run `node loglens.mjs events.jsonl --json` for machine-readable output, or pass `-` as the filename to read standard input. Malformed rows appear by line number under `rejected`. An input with zero valid events returns exit status 1.

## How classification works

A nearest-neighbor text classifier compares each message with twelve labeled examples using binary term frequency, inverse document frequency and cosine similarity. A match needs two shared terms and similarity of at least 0.28. It labels disk capacity, memory pressure, certificate expiry and downstream symptoms. The earliest matched fault becomes an investigation lead. Similarity measures word overlap and has no probability interpretation.

The tests exercise unseen phrasings, unrelated events, sparse input, shuffled timestamps, malformed rows and a symptom-only incident. This small synthetic suite checks behavior. Production accuracy is unmeasured. Negation, recovery messages and coincident incidents can produce misleading leads. Read the quoted evidence before acting.

The CLI uses Node built-ins and runs offline, with no install step or account.
