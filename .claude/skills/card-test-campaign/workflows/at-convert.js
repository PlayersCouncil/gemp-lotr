export const meta = {
  name: 'at-convert',
  description: 'Convert one batch of the old at/ tests: write the replacements from the survey, delete the old methods, then check, file and commit',
  whenToUse: 'The conversion of the old at/ tests, one batch at a time. args: {repo, batch: <key of a file in orchestrator/at-convert/>, note?}. Returns only the lander\'s few lines; the converter\'s whole report is in the run\'s journal',
  phases: [
    { title: 'Convert', detail: 'the replacements written from their directives and run; the old methods deleted' },
    { title: 'Land', detail: 'checked against the directives; findings, questions and rig requests filed; corrections applied; committed' },
  ],
}
const { repo, batch } = args
const note = args.note ? `\n${args.note}\n` : ''
const roles = repo + '/.claude/skills/card-test-campaign/roles/'
const TEXT = { type: 'string' }
const LIST = { type: 'array', items: TEXT }
const CONVERTED = {
  type: 'object',
  required: ['homes'],
  properties: {
    homes: {
      type: 'array',
      items: {
        type: 'object',
        required: ['file', 'tests'],
        properties: {
          file: TEXT,
          tests: { type: 'array', items: TEXT, description: 'Each test written: its name, and "active" or "parked on F-0000 / Q-0000 / G-0000: why"' },
          deleted: { type: 'array', items: TEXT, description: 'Old methods deleted, as at-file: method' },
          lint: { type: 'string', description: 'Last line of lint.py --form' },
          run: { type: 'string', description: 'Last line of cardtest.py run' },
        },
      },
    },
    findings: { type: 'array', items: TEXT, description: 'Each whole: the card, what the text or rule says, what the game did, the correction or the engine lines, error or deliberate-looking' },
    questions: { type: 'array', items: TEXT, description: 'Each whole: the sentence, what is built, reading A, reading B, which test is parked' },
    rig: { type: 'array', items: TEXT, description: 'A reading the rig cannot give, and the method wanted' },
    left: { type: 'array', items: TEXT, description: 'Old methods not converted, and why' },
    documents: { type: 'array', items: TEXT, description: 'What in the documents was wrong, unclear or missing, one line each' },
  },
}
const LANDED = {
  type: 'object',
  required: ['tests', 'deleted', 'commit', 'needs_orchestrator'],
  properties: {
    tests: { type: 'string', description: 'How many tests landed, and the names of any parked, with their numbers' },
    deleted: { type: 'string', description: 'How many old methods were deleted; the names of any left' },
    fixed: { type: 'array', items: TEXT, description: 'Numbers only: findings fixed' },
    open: { type: 'array', items: TEXT, description: 'Numbers only: findings recorded and not fixed' },
    questions: { type: 'array', items: TEXT, description: 'Numbers only' },
    rig: { type: 'array', items: TEXT, description: 'Numbers only' },
    needs_orchestrator: { type: 'array', items: TEXT, description: 'Only what somebody must act on and no register holds. Usually empty' },
    commit: TEXT,
  },
}
const where = `Work in the repository at ${repo}. `

phase('Convert')
const converted = await agent(
  where + `Read ${roles}at-convert.md and do what it says.\n\nYour batch: ${batch}\nYour working folder name: at-${batch}\n` + note,
  { label: `convert ${batch}`, phase: 'Convert', schema: CONVERTED })
if (!converted) return { batch, failed: 'convert' }

phase('Land')
const landed = await agent(
  where + `Read ${roles}at-land.md and do what it says.\n\nYour batch: ${batch}\nYour working folder name: at-${batch}-l\n` + note +
  `\nThe converter's report:\n\n${JSON.stringify(converted, null, 1)}\n`,
  { label: `land ${batch}`, phase: 'Land', schema: LANDED })
if (!landed) return { batch, failed: 'land' }
return { batch, landed }
