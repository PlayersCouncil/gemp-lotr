export const meta = {
  name: 'card-test-batch',
  description: 'Author the unit tests of one batch of cards, then review them and put right what the review finds',
  whenToUse: 'One card, or a batch of up to four, of the card test campaign. args: {repo, cards: [ids], folder, notes}; add fixes (a numbered list per card) to run a fixer on that list and nothing else',
  phases: [
    { title: 'Author', detail: 'blind plan, then the tests, the conformance check and the stock mistakes, once' },
    { title: 'Review', detail: 'an independent review against the Card Test Standard; the reviewer fixes what it finds' },
    { title: 'Fix', detail: 'only with args.fixes: a list from the orchestrator worked through' },
  ],
}

const { repo, cards, folder } = args
const notes = args.notes || ''
const roles = repo + '/.claude/skills/card-test-campaign/roles/'

const TEXT = { type: 'string' }
const LIST = { type: 'array', items: TEXT }

const AUTHOR = {
  type: 'object',
  required: ['cards'],
  properties: {
    cards: {
      type: 'array',
      items: {
        type: 'object',
        required: ['id', 'state', 'tests_before', 'tests_after'],
        properties: {
          id: TEXT,
          name: TEXT,
          state: { type: 'string', enum: ['READY', 'PARKED'] },
          parked_on: { type: 'string', description: 'What is holding the card, if PARKED' },
          tests_before: { type: 'integer' },
          tests_after: { type: 'integer' },
          lint: { type: 'string', description: 'Last line or two of lint.py output' },
          run: { type: 'string', description: 'Last line of cardtest.py run output' },
          existing_tests: { type: 'string', description: 'What happened to each test that existed before' },
          mutation_survivors: { type: 'string', description: 'Each survivor: gap closed, or why it makes no difference to play' },
          findings: {
            type: 'array',
            items: {
              type: 'object',
              required: ['summary', 'kind'],
              properties: {
                summary: TEXT,
                text_says: TEXT,
                expected: TEXT,
                happened: TEXT,
                kind: { type: 'string', description: 'typo in the definition, oversight in the implementation, wrong displayed text, engine bug, rig bug, wrong sheet row' },
                parked_test: { type: 'string', description: 'Name of the test parked on it, if any' },
              },
            },
          },
          questions: {
            type: 'array',
            items: {
              type: 'object',
              required: ['question'],
              properties: {
                question: TEXT,
                readings: { type: 'string', description: 'Each reading and what would follow from it' },
                existing_test_takes: { type: 'string', description: 'The reading an existing test already asserts, if one does' },
                parked_test: TEXT,
              },
            },
          },
          rig_requests: LIST,
        },
      },
    },
    documentation_issues: { type: 'array', items: TEXT, description: 'Anything in the standard, the rig reference or the tools that was wrong, unclear or missing' },
  },
}

const REVIEW = {
  type: 'object',
  required: ['cards'],
  properties: {
    cards: {
      type: 'array',
      items: {
        type: 'object',
        required: ['id', 'verdict'],
        properties: {
          id: TEXT,
          verdict: { type: 'string', enum: ['PASS', 'FIXED', 'PARK'], description: 'PASS: changed nothing. FIXED: changed something and the card is now right. PARK: something holds it that you could not put right' },
          changes: { type: 'array', items: TEXT, description: 'For FIXED: each change made: what was wrong, the evidence, what you did' },
          parked_on: TEXT,
          ledger_differences: { type: 'string', description: 'Where your own reading of the card differed from the ledger file, and why' },
          findings_and_questions: { type: 'string', description: "Each of the author's findings and questions: stands, or refuted with the citation" },
          survivor_claims: { type: 'string', description: "Only where you disagree with one of the author's no-difference claims: which, and what you found" },
          lost_tests: { type: 'string', description: 'Anything the old tests asserted that the new ones no longer do' },
          new_findings: LIST,
          new_questions: LIST,
          new_rig_requests: LIST,
          lint: TEXT,
          run: TEXT,
        },
      },
    },
    standard_issues: { type: 'array', items: TEXT, description: 'Anything in the standard that let a bad test through or had to be interpreted' },
  },
}

const FIXED = {
  type: 'object',
  required: ['cards'],
  properties: {
    cards: {
      type: 'array',
      items: {
        type: 'object',
        required: ['id', 'state', 'items'],
        properties: {
          id: TEXT,
          state: { type: 'string', enum: ['READY', 'PARKED'] },
          parked_on: TEXT,
          items: {
            type: 'array',
            items: {
              type: 'object',
              required: ['number', 'outcome'],
              properties: {
                number: { type: 'integer' },
                outcome: { type: 'string', enum: ['done', 'refused'] },
                note: TEXT,
              },
            },
          },
          lint: TEXT,
          run: TEXT,
          new_findings: LIST,
          new_questions: LIST,
          new_rig_requests: LIST,
        },
      },
    },
  },
}

const where = `Work in the repository at ${repo}. `
const numbered = (items) => (items || []).map((t, i) => `${i + 1}. ${t}`).join('\n')

// A list from the orchestrator (a ruling to apply, a definition that changed) worked through by a fixer.
if (args.fixes) {
  phase('Fix')
  const fix = await agent(
    where + `Read ${roles}fixer.md and do what it says.\n\n` +
    `The cards and the list for each:\n\n${args.fixes}\n\nYour working folder name: ${folder}-f\n`,
    { label: `fix ${folder}`, phase: 'Fix', schema: FIXED })
  return { folder, cards, fix }
}

// A batch whose author finished and whose review did not (a run cut off): args.reported is the author's account,
// built by the orchestrator from that run's journal.
if (args.reported) {
  phase('Review')
  const review = await agent(
    where + `Read ${roles}review-and-fix.md and do what it says.\n\n` +
    `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-r2\n\n` +
    `What the author reported as findings, questions and rig requests, and its account of the stock mistakes its tests did not notice (the rest of the author's report is kept from you on purpose):\n${args.reported}\n`,
    { label: `review ${folder}`, phase: 'Review', schema: REVIEW })
  if (!review) return { folder, cards, failed: 'review' }
  return { folder, cards, review }
}

phase('Author')
const author = await agent(
  where + `Read ${roles}author.md and do what it says.\n\n` +
  `Your cards: ${cards.join(', ')}\nYour working folder name: ${folder}-a\n` +
  (notes ? `\nFrom the orchestrator:\n${notes}\n` : ''),
  { label: `author ${folder}`, phase: 'Author', schema: AUTHOR })
if (!author) return { folder, cards, failed: 'author' }

const reported = (author.cards || []).map(c =>
  `${c.id} (${c.state})` +
  (c.findings && c.findings.length ? `\n  findings:\n` + c.findings.map(f => `  - ${f.summary} [${f.kind}]${f.parked_test ? ' parked test: ' + f.parked_test : ''}`).join('\n') : '') +
  (c.questions && c.questions.length ? `\n  questions:\n` + c.questions.map(q => `  - ${q.question}${q.readings ? ' Readings: ' + q.readings : ''}${q.parked_test ? ' parked test: ' + q.parked_test : ''}`).join('\n') : '') +
  (c.rig_requests && c.rig_requests.length ? `\n  rig requests:\n` + c.rig_requests.map(r => `  - ${r}`).join('\n') : '') +
  `\n  stock mistakes that survived, by the author's account:\n  ${c.mutation_survivors || '(none reported)'}`
).join('\n')

phase('Review')
const review = await agent(
  where + `Read ${roles}review-and-fix.md and do what it says.\n\n` +
  `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-r\n\n` +
  `What the author reported as findings, questions and rig requests, and its account of the stock mistakes its tests did not notice (the rest of the author's report is kept from you on purpose):\n${reported}\n`,
  { label: `review ${folder}`, phase: 'Review', schema: REVIEW })
if (!review) return { folder, cards, author, failed: 'review' }

return { folder, cards, author, review }
