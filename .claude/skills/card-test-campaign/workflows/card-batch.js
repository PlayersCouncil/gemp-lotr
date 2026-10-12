export const meta = {
  name: 'card-test-batch',
  description: 'Author the unit tests of one batch of cards, review them, then land them: file what was found, apply the corrections, commit',
  whenToUse: 'One card, or a batch of up to four, of the card test campaign. args: {repo, cards: [ids], folder, notes}; add fixes (a numbered list per card) to run a fixer on that list in place of the author and reviewer. Returns only the lander\'s few lines; the full reports are in the run\'s journal',
  phases: [
    { title: 'Author', detail: 'blind plan, then the tests and the conformance check (the session model unless args say otherwise)' },
    { title: 'Review', detail: 'an independent review against the Card Test Standard; the reviewer fixes what it finds (Sonnet by default)' },
    { title: 'Fix', detail: 'only with args.fixes: a list from the orchestrator worked through' },
    { title: 'Land', detail: 'findings, questions and rig requests numbered and filed, corrections applied, what was learned worked into the documents, one commit (the session model)' },
  ],
}

const { repo, cards, folder } = args
const notes = args.notes || ''
const roles = repo + '/.claude/skills/card-test-campaign/roles/'
// Models: the author runs on the session's model and the reviewer and fixer on Sonnet: the arrangement Ketura
// chose on 2026-10-08 after the replay rounds, in which Opus authors found more of the known items than Sonnet
// authors did; review is where the cheaper model is being tried.
// args.model sets both roles; args.authorModel / args.reviewModel set one ('inherit' = the session's model).
// args.authorEffort / args.reviewEffort set the reasoning effort ('low' ... 'max'); unset = the session's.
const pick = (m) => (m === 'inherit' ? undefined : m)
const authorModel = pick(args.authorModel || args.model)
const reviewModel = pick(args.reviewModel || args.model || 'sonnet')
// The lander edits definitions and writes what Ketura reads: the session's model unless args.landModel says otherwise.
const landModel = pick(args.landModel || 'inherit')
// Slim-down A1 (Ketura on Q-0296): the agents run as the custom type `card-worker` (.claude/agents/card-worker.md),
// which carries only the file and shell tools instead of every tool of the session.  args.agentType: "" turns it off.
const AGENT_TYPE = args.agentType === undefined ? 'card-worker' : args.agentType
const withModel = (opts, m, e) => Object.assign({}, opts, m ? { model: m } : {}, e ? { effort: e } : {}, AGENT_TYPE ? { agentType: AGENT_TYPE } : {})

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

const LANDED = {
  type: 'object',
  required: ['cards', 'commit'],
  properties: {
    cards: {
      type: 'array',
      items: {
        type: 'object',
        required: ['id', 'status'],
        properties: {
          id: TEXT,
          status: { type: 'string', enum: ['ready', 'parked', 'held'], description: 'held: not landed' },
          on: { type: 'string', description: 'For parked: the numbers it is parked on. For held: one line why' },
        },
      },
    },
    fixed: { type: 'array', items: TEXT, description: 'Numbers only: findings fixed at this landing' },
    open: { type: 'array', items: TEXT, description: 'Numbers only: findings filed as confirmed or candidate' },
    reports: { type: 'array', items: TEXT, description: 'Numbers only: findings of kind ruling that need no answer' },
    questions: { type: 'array', items: TEXT, description: 'Numbers only: questions filed for Ketura (Q- numbers, and F- numbers asked about)' },
    rig: { type: 'array', items: TEXT, description: 'Numbers only: rig requests filed' },
    needs_orchestrator: { type: 'array', items: TEXT, description: 'Only what somebody must act on and no register holds. One line each. Usually empty' },
    commit: { type: 'string', description: 'The head as landing.py finish printed it' },
  },
}

const where = `Work in the repository at ${repo}. `
// The last stage of every passage: the lander is given both reports whole and returns a few lines.  What this
// script returns is what lands in the orchestrator's context, so it returns the lander's lines and nothing else;
// the reports themselves are in the run's journal (scripts/orchestrator/wf.py show <folder>).
const land = async (reports) => {
  phase('Land')
  const landed = await agent(
    where + `Read ${roles}lander.md and do what it says.\n\n` +
    `The cards: ${cards.join(', ')}\nThe batch's folder name: ${folder}\nYour working folder name: ${folder}-l\n` +
    (notes ? `\nFrom the orchestrator:\n${notes}\n` : '') +
    `\nThe reports of the agents before you, whole:\n${JSON.stringify(reports, null, 1)}\n`,
    withModel({ label: `land ${folder}`, phase: 'Land', schema: LANDED }, landModel, args.landEffort))
  if (!landed) return { folder, cards, failed: 'land' }
  return { folder, cards, landed }
}
const numbered = (items) => (items || []).map((t, i) => `${i + 1}. ${t}`).join('\n')

// A list from the orchestrator (a ruling to apply, a definition that changed) worked through by a fixer.
if (args.fixes) {
  phase('Fix')
  const fix = await agent(
    where + `Read ${roles}fixer.md and do what it says.\n\n` +
    `The cards and the list for each:\n\n${args.fixes}\n\nYour working folder name: ${folder}-f\n`,
    withModel({ label: `fix ${folder}`, phase: 'Fix', schema: FIXED }, reviewModel, args.reviewEffort))
  if (!fix) return { folder, cards, failed: 'fix' }
  return await land({ fix })
}

// A batch whose author finished and whose review did not (a run cut off): args.reported is the author's account,
// built by the orchestrator from that run's journal.  (Since the Workflow tool can resume a run, relaunching with
// resumeFromRunId and the same args is the simpler way: finished stages come back from the journal at once.)
if (args.reported) {
  phase('Review')
  const review = await agent(
    where + `Read ${roles}review-and-fix.md and do what it says.\n\n` +
    `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-r2\n\n` +
    `What the author reported as findings, questions and rig requests (the rest of the author's report is kept from you on purpose):\n${args.reported}\n`,
    withModel({ label: `review ${folder}`, phase: 'Review', schema: REVIEW }, reviewModel, args.reviewEffort))
  if (!review) return { folder, cards, failed: 'review' }
  return await land({ author_reported: args.reported, review })
}

phase('Author')
const author = await agent(
  where + `Read ${roles}author.md and do what it says.\n\n` +
  `Your cards: ${cards.join(', ')}\nYour working folder name: ${folder}-a\n` +
  (notes ? `\nFrom the orchestrator:\n${notes}\n` : ''),
  withModel({ label: `author ${folder}`, phase: 'Author', schema: AUTHOR }, authorModel, args.authorEffort))
if (!author) return { folder, cards, failed: 'author' }

const reported = (author.cards || []).map(c =>
  `${c.id} (${c.state})` +
  (c.findings && c.findings.length ? `\n  findings:\n` + c.findings.map(f => `  - ${f.summary} [${f.kind}]${f.parked_test ? ' parked test: ' + f.parked_test : ''}`).join('\n') : '') +
  (c.questions && c.questions.length ? `\n  questions:\n` + c.questions.map(q => `  - ${q.question}${q.readings ? ' Readings: ' + q.readings : ''}${q.parked_test ? ' parked test: ' + q.parked_test : ''}`).join('\n') : '') +
  (c.rig_requests && c.rig_requests.length ? `\n  rig requests:\n` + c.rig_requests.map(r => `  - ${r}`).join('\n') : '')
).join('\n')

phase('Review')
const review = await agent(
  where + `Read ${roles}review-and-fix.md and do what it says.\n\n` +
  `The cards: ${cards.join(', ')}\nYour working folder name: ${folder}-r\n\n` +
  `What the author reported as findings, questions and rig requests (the rest of the author's report is kept from you on purpose):\n${reported}\n`,
  withModel({ label: `review ${folder}`, phase: 'Review', schema: REVIEW }, reviewModel, args.reviewEffort))
if (!review) return { folder, cards, failed: 'review' }

return await land({ author, review })
