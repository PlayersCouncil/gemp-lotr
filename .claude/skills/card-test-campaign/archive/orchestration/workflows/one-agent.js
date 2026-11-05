export const meta = {
  name: 'one-agent',
  description: 'Run one agent on a brief read from a file (args: {briefPath, label})',
  phases: [{ title: 'Work' }],
}
const r = await agent(`Your brief is the file ${args.briefPath}. Read the whole of it first and follow it.`, { label: args.label || 'agent', phase: 'Work' })
return { result: r }
