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

// Slim-down A1 (Q-0296): agents run as `card-worker` (file and shell tools only); args.agentType: "" turns it off.
const AGENT_TYPE = args.agentType === undefined ? 'card-worker' : args.agentType
phase('Items')
const results = []
for (const item of items) {
  const brief = roles + (item.brief || 'engine.md')
  const r = await agent(
    `Work in the repository at ${repo}. Read ${brief} and do what it says.\n\n` +
    `Your item is the section "## ${item.key}" of the file ${queue}: read the whole of that section, and the lines above the first section.\n` +
    `This is engine pass ${pass}. Your working folder name: ep${pass}-${item.key}\n`,
    Object.assign({ label: `${item.key} (pass ${pass})`, phase: 'Items', schema: RESULT }, AGENT_TYPE ? { agentType: AGENT_TYPE } : {}))
  if (!r) {
    // The agent died (a usage limit, most likely) and may have left the tree changed: nobody follows it.
    results.push({ item: item.key, status: 'died' })
    log(`${item.key} did not finish; stopping here. Relaunch with resumeFromRunId once the tree has been looked at.`)
    break
  }
  results.push(Object.assign({ item: item.key }, cut(r)))
}
return { pass, results }
