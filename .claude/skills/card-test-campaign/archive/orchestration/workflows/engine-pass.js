export const meta = {
  name: 'engine-pass',
  description: 'Work through a queue of engine and rig items, one agent after another, each committing its own item',
  whenToUse: 'An engine pass of the card test campaign, between waves of card batches. args: {repo, pass, queue: <the file describing the items>, items: [{key, brief?}]}; brief defaults to engine.md. Returns a few lines for each item',
  phases: [
    { title: 'Items', detail: 'one agent for each item, in order: they share one build, so never two at once' },
  ],
}

const { repo, pass, queue, items } = args
const roles = repo + '/.claude/skills/card-test-campaign/roles/'
const TEXT = { type: 'string' }
const LIST = { type: 'array', items: TEXT }
const RESULT = {
  type: 'object',
  required: ['status'],
  properties: {
    status: { type: 'string', enum: ['done', 'proposal', 'blocked'], description: 'proposal: the change was too large to make unasked and is written up in the report. blocked: could not finish; the tree was put back' },
    commit: { type: 'string', description: 'The head after your commit, as landing.py printed it' },
    cards: { type: 'array', items: TEXT, description: 'Ids of the cards whose parked tests or waiting checks went live' },
    closed: { type: 'array', items: TEXT, description: 'Numbers only: the findings and rig requests closed' },
    filed: { type: 'array', items: TEXT, description: 'Numbers only: anything new filed' },
    corrected: { type: 'array', items: TEXT, description: "Other cards whose tests or definitions you put right, by id" },
    needs_orchestrator: { type: 'array', items: TEXT, description: 'Only what somebody must act on. One line each. Usually empty' },
  },
}

// What this script returns lands in the orchestrator's context and is re-read on every step of its session
// (pass 17 came back with a list of 171 card ids).  So every list in an item's result is cut to its first ten
// entries, with "and N more" as an eleventh; the whole of it is in the item's report and in the run's journal.
const KEEP = 10
const cut = (value) => {
  if (Array.isArray(value)) {
    const kept = value.slice(0, KEEP).map(cut)
    return value.length > KEEP ? kept.concat([`and ${value.length - KEEP} more`]) : kept
  }
  if (value && typeof value === 'object') {
    const out = {}
    for (const key of Object.keys(value)) out[key] = cut(value[key])
    return out
  }
  return value
}

// Slim-down A1 (Ketura on Q-0296): the agents run as the custom type `card-worker` (.claude/agents/card-worker.md),
// which carries only the file and shell tools.  A session that started before that file existed does not know the
// type (agent() throws "not found", 2026-10-12): then every agent runs as the default, once logged.
// args.agentType: "" turns it off.
let AGENT_TYPE = args.agentType === undefined ? 'card-worker' : args.agentType
const run = async (prompt, opts) => {
  if (AGENT_TYPE) {
    try {
      return await agent(prompt, Object.assign({}, opts, { agentType: AGENT_TYPE }))
    } catch (e) {
      if (!/not found/.test(String(e))) throw e
      log(`agent type ${AGENT_TYPE} is not known in this session: the default agent is used`)
      AGENT_TYPE = ''
    }
  }
  return await agent(prompt, opts)
}
phase('Items')
const results = []
for (const item of items) {
  const brief = roles + (item.brief || 'engine.md')
  const r = await run(
    `Work in the repository at ${repo}. Read ${brief} and do what it says.\n\n` +
    `Your item is the section "## ${item.key}" of the file ${queue}: read the whole of that section, and the lines above the first section.\n` +
    `This is engine pass ${pass}. Your working folder name: ep${pass}-${item.key}\n`,
    { label: `${item.key} (pass ${pass})`, phase: 'Items', schema: RESULT })
  if (!r) {
    // The agent died (a usage limit, most likely) and may have left the tree changed: nobody follows it.
    results.push({ item: item.key, status: 'died' })
    log(`${item.key} did not finish; stopping here. Relaunch with resumeFromRunId once the tree has been looked at.`)
    break
  }
  results.push(Object.assign({ item: item.key }, cut(r)))
}
return { pass, results }
