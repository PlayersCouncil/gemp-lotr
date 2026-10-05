export const meta = {
  name: 'card-test-batch',
  description: 'Author, review, fix and re-check the unit tests of one batch of cards',
  whenToUse: 'One card, or a batch of up to four, of the card test campaign. args: {repo, cards: [ids], folder, notes}; add fixes (a numbered list per card) to skip straight to fix and re-check',
  phases: [
    { title: 'Author', detail: 'blind plan, then the tests, the conformance check and the mutation check' },
    { title: 'Review', detail: 'an independent review against the Card Test Standard' },
    { title: 'Fix', detail: 'only when the review asks for changes' },
    { title: 'Re-check', detail: 'only after a fix' },
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
          verdict: { type: 'string', enum: ['PASS', 'FIX', 'PARK'] },
          fixes: { type: 'array', items: TEXT, description: 'For FIX: each change that must be made, specific enough to act on without asking' },
          parked_on: TEXT,
          ledger_differences: { type: 'string', description: 'Where your own reading of the card differs from the ledger file, and why' },
          mutation_survivors: { type: 'string', description: 'Each survivor marked gap or no-difference' },
          findings_and_questions: { type: 'string', description: 'Each finding and question: stands, or refuted with the citation' },
          lost_tests: { type: 'string', description: 'Anything the old tests asserted that the new ones no longer do' },
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

// A batch that has been through review already and only needs a list worked through: args.fixes is that list.
if (args.fixes) {
  phase('Fix')
  const fix = await agent(
    where + `Read ${roles}fixer.md and do what it says.\n\n` +
    `The cards and the list for each:\n\n${args.fixes}\n\nYour working folder name: ${folder}-f\n`,
    { label: `fix ${folder}`, phase: 'Fix', schema: FIXED })
  if (!fix) return { folder, cards, failed: 'fix' }
  const account = (fix.cards || []).map(c =>
    `${c.id}:\n` + (c.items || []).map(i => `${i.number}. ${i.outcome}${i.note ? ': ' + i.note : ''}`).join('\n')).join('\n\n')
  phase('Re-check')
  const recheck = await agent(
    where + `Read ${roles}reviewer.md and do what it says. This is a re-check: see its section "Re-checking after a fix".\n\n` +
    `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-c\n\n` +
    `The list:\n\n${args.fixes}\n\nThe fixer's account of each item:\n\n${account}\n`,
    { label: `re-check ${folder}`, phase: 'Re-check', schema: REVIEW })
  return { folder, cards, fix, recheck }
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
  (c.rig_requests && c.rig_requests.length ? `\n  rig requests:\n` + c.rig_requests.map(r => `  - ${r}`).join('\n') : '')
).join('\n')

phase('Review')
const review = await agent(
  where + `Read ${roles}reviewer.md and do what it says.\n\n` +
  `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-r\n\n` +
  `What the author reported as findings, questions and rig requests (the rest of the author's report is kept from you on purpose):\n${reported}\n`,
  { label: `review ${folder}`, phase: 'Review', schema: REVIEW })
if (!review) return { folder, cards, author, failed: 'review' }

const toFix = (review.cards || []).filter(c => c.verdict === 'FIX' && c.fixes && c.fixes.length)
if (!toFix.length) return { folder, cards, author, review }

const fixList = toFix.map(c => `${c.id}:\n${numbered(c.fixes)}`).join('\n\n')

phase('Fix')
const fix = await agent(
  where + `Read ${roles}fixer.md and do what it says.\n\n` +
  `The cards and the reviewer's list for each:\n\n${fixList}\n\nYour working folder name: ${folder}-f\n`,
  { label: `fix ${folder}`, phase: 'Fix', schema: FIXED })
if (!fix) return { folder, cards, author, review, failed: 'fix' }

const account = (fix.cards || []).map(c =>
  `${c.id}:\n` + (c.items || []).map(i => `${i.number}. ${i.outcome}${i.note ? ': ' + i.note : ''}`).join('\n')).join('\n\n')

phase('Re-check')
const recheck = await agent(
  where + `Read ${roles}reviewer.md and do what it says. This is a re-check: see its section "Re-checking after a fix".\n\n` +
  `The cards: ${toFix.map(c => c.id).join(', ')}\nYour working folder name: ${folder}-c\n\n` +
  `The first review's list:\n\n${fixList}\n\nThe fixer's account of each item:\n\n${account}\n`,
  { label: `re-check ${folder}`, phase: 'Re-check', schema: REVIEW })

return { folder, cards, author, review, fix, recheck }
