/**
 * The words of the question asked before a scenario is submitted for review (the legacy scenarioSubmit.js
 * ScenarioSubmit), the same in the authoring panel (gd-game-author-submit) and in the hall's My Scenarios.
 */

/** What to see to before submitting a scenario for review (the legacy scenarioSubmit.js GUIDELINES). */
export const SUBMIT_GUIDELINES: readonly string[] = [
  'Play it through yourself first, as a player would. The server plays it through too: one that does not play through cannot be submitted.',
  'Give it a clear title and a summary that says what the player will do or learn, and put your name in Author.',
  'Keep the messages friendly, on topic and accurate: what they say about cards and rules has to be true of the game as Gemp plays it.',
  'A tutorial teaches one thing at a time; a puzzle has a solution a player can find from what they are shown.',
];

/** What submitting does (the legacy WHAT_HAPPENS). */
export const SUBMIT_CONSEQUENCES: readonly string[] = [
  'It is locked: you cannot change, rename or delete it until an admin has approved or rejected it. A draft of it open in the authoring tool is closed. To keep working on it in the meantime, copy it in My Scenarios.',
  'An admin plays it, and either approves it or sends it back with the changes they would like, which you will see in My Scenarios.',
  'Approved, it becomes a public scenario, offered to every player. If it is a version of a public scenario (copied from one), it replaces that one.',
  'A public scenario is part of the game: the admins may change it, disable it or take it down, and it may be kept with the game’s own scenarios. It stays credited to whoever its Author says.',
];

export const SUBMIT_TITLE = (title: string | null) =>
  `Submit “${title !== null && title !== '' ? title : 'this scenario'}” for review?`;
export const SUBMIT_BEFORE_HEADING = 'Before you submit';
export const SUBMIT_DOES_HEADING = 'What submitting does';
export const SUBMIT_ASK = 'Are you sure you want to submit it?';
export const SUBMIT_YES = 'Submit for review';
