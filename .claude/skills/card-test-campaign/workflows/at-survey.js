export const meta = {
  name: 'at-survey',
  description: 'Survey the old at/ test files: what each test guards and where its replacement belongs (read-only)',
  whenToUse: 'The `at` phase of the card test campaign, before any conversion. args: {repo, groups: [{key, files: [names], note?}]}; one agent per group, one after another. Writes orchestrator/at-survey/<File>.md and .tsv; returns a few lines per group',
  phases: [{ title: 'Survey', detail: 'one agent for each group of files, in order' }],
}
const { repo, groups } = args
const TEXT = { type: 'string' }
const RESULT = {
  type: 'object',
  properties: {
    files: { type: 'array', items: TEXT, description: 'One line per at file: its name, how many methods, how many of each kind, how many covered / part / new' },
    needs_orchestrator: { type: 'array', items: TEXT, description: 'Only what the orchestrator must decide. One line each. Usually short' },
  },
  required: ['files', 'needs_orchestrator'],
}
const at = 'gemp-lotr/gemp-lotr-server/src/test/java/com/gempukku/lotro/at/'
phase('Survey')
const results = []
for (const g of groups) {
  const r = await agent(
    `Work in the repository at ${repo}; change nothing in it. Read .claude/skills/card-test-campaign/roles/at-survey.md and do what it says.\n\n` +
    `Your files, all in ${at}:\n${g.files.map(f => '- ' + f).join('\n')}\n` + (g.note ? `\n${g.note}\n` : ''),
    { label: `survey ${g.key}`, phase: 'Survey', schema: RESULT })
  if (!r) {
    results.push({ group: g.key, status: 'died' })
    log(`${g.key} did not finish; stopping here.`)
    break
  }
  results.push(Object.assign({ group: g.key }, r))
}
return { results }
