// The scenarios the tutorial checks bring with them (tutorial.mjs `bring`): the check server has only the ones that
// ship with the game, which are Ketura's and are not added to for a check. Each is the file a scenario would be, as
// an object (the authoring service takes its parts as JSON). They are the engine's test fixtures
// (gemp-lotr-server/src/test/resources/scenarios), which were written for the headless runner, with what a person
// playing them on the page needs:
// - every step of the person's that has an `answer` and no `allow` is given the allow of its answer: without one the
//   page takes any click there, and anything but the answer leaves the script (the runner always gives the answer);
// - a skirmish's message outlines `ui:skirmish:selfStrength`, the person's own side of it by the role they play.

/** Two-sided, the person in the first seat: Free Peoples on turns 1, 3, 5, Shadow on 2, 4, 6 (two-sided-first). */
export const TWO_SIDED_FIRST = {
  title: 'Two sides, the person first',
  format: 'scenario',
  cards: {
    freeps: { frodo: '10_121', aragorn: '4_109', ravager: '4_15', freca: '9_2' },
    shadow: { oppFrodo: '10_121', boromir: '0_65', orc1: '1_178', orc2: '1_178' },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  sites: { freeps: '1_323, 1_331, 1_340, 2_118', shadow: '1_323, 1_331, 1_340, 2_118' },
  options: {
    autoPass: {
      freeps: 'fellowship, maneuver, archery, assignment, regroup',
      shadow: 'maneuver, archery, assignment, regroup',
    },
  },
  opponent: {
    fallback: 'pass',
    leftovers: 'ringBearer',
    orders: [
      {
        note: "The opponent's minions, on the person's turns.",
        when: 'opponent, action, phase(shadow), as(shadow)',
        answer: 'play(orc1), play(orc2)',
      },
    ],
  },
  setup: {
    freeps: { characters: 'aragorn', hand: 'ravager, freca' },
    shadow: { characters: 'boromir', hand: 'orc1, orc2' },
  },
  steps: [
    {
      id: 'firstAssign',
      when: 'human, assign, as(freeps), turn(1)',
      messages: [
        'Your turn: you are the Free Peoples player.  Aragorn takes the first Goblin; the other goes to Frodo.',
      ],
      allow: 'assign(aragorn: orc1)',
    },
    {
      id: 'fights',
      when: 'turn(1)',
      until: 'phase(regroup)',
      anyOrder: [
        {
          when: 'human, nextSkirmish',
          allow: 'choose(aragorn), choose(frodo)',
          answer: 'choose(aragorn), choose(frodo)',
        },
        {
          once: true,
          when: 'human, action, inSkirmish(aragorn)',
          messages: [
            {
              text: 'Aragorn is {selfStrength} against {oppStrength}.',
              anchor: 'ui:skirmish:selfStrength',
              highlight: 'ui:skirmish:selfStrength',
            },
          ],
          allow: 'pass',
        },
        { when: 'human, action, phase(skirmish)', allow: 'pass', answer: 'pass' },
      ],
    },
    { when: 'human, anotherMove, turn(1)', allow: 'option(No)', answer: 'option(No)' },
    { when: 'human, reconcile, turn(1)', allow: 'pass', answer: 'pass' },
    {
      id: 'theirTurn',
      checkpoint: true,
      when: 'human, action, phase(shadow), as(shadow)',
      do: 'twilight(10)',
      messages: ["Now it is the opponent's turn, and you are the Shadow player.  Play the Ravager, then Freca."],
      allow: 'play(ravager)',
    },
    { when: 'human, action, phase(shadow), turn(2)', allow: 'play(freca)' },
    {
      id: 'control',
      when: 'human, optionalResponses, as(shadow)',
      messages: ['Both fellowships are past site 1: Freca can take control of it.'],
      allow: 'use(freca)',
    },
    { when: 'human, action, phase(shadow), turn(2)', allow: 'pass' },
    {
      pause: true,
      when: 'human, assign, as(shadow)',
      messages: ['The Free Peoples player has assigned nothing.  The minions are yours to assign.'],
    },
    { when: 'human, assign, as(shadow)', allow: 'assign(boromir: freca, oppFrodo: ravager)' },
    {
      when: 'turn(2)',
      until: 'phase(regroup)',
      skirmishes: true,
      anyOrder: [
        {
          when: 'human, action, phase(skirmish), as(shadow)',
          messages: [
            {
              text: 'Your minion is {selfStrength} against {oppStrength}.',
              anchor: 'ui:skirmish:selfStrength',
              highlight: 'ui:skirmish:selfStrength',
            },
          ],
          allow: 'pass',
          answer: 'pass',
        },
      ],
    },
    { when: 'human, anotherMove, turn(3)', allow: 'option(No)', answer: 'option(No)' },
    {
      when: 'human, action, phase(shadow), turn(4)',
      messages: ['Your hand is empty: nothing to play this time.'],
      allow: 'pass',
    },
    {
      id: 'sanctuary',
      when: 'human, sanctuary, as(freeps)',
      messages: ['Frodo starts the turn at a sanctuary.'],
      allow: 'choose(frodo)',
      answer: 'choose(frodo)',
    },
    { when: 'human, anotherMove, turn(5)', allow: 'option(No)', answer: 'option(No)' },
    { when: 'human, action, phase(shadow), turn(6)', allow: 'pass', end: 'complete' },
  ],
};

/**
 * Two-sided, the person in the second seat, down the path (a late start at site 3): Shadow on turn 1 (a fierce Uruk
 * assigned twice), Free Peoples on turn 2 (two-sided-second).
 */
export const TWO_SIDED_SECOND = {
  title: 'Two sides, the person second',
  format: 'scenario',
  human: 'shadow',
  cards: {
    freeps: { oppFrodo: '10_121', boromir: '0_65', ravager: '4_15', freca: '9_2' },
    shadow: { frodo: '10_121', aragorn: '4_109', runner1: '1_178', uruk: '4_187' },
  },
  ringBearer: { freeps: 'oppFrodo', shadow: 'frodo' },
  sites: { freeps: '1_323, 1_331, 1_340, 2_118, 1_349', shadow: '1_323, 1_331, 1_340, 2_118, 1_349' },
  options: {
    autoPass: { freeps: 'maneuver, archery, assignment, regroup', shadow: 'maneuver, archery, assignment, regroup' },
  },
  opponent: {
    fallback: 'pass',
    leftovers: 'ringBearer',
    orders: [
      {
        id: 'minions',
        note: "The opponent's minions, on the person's turn.",
        when: 'opponent, action, phase(shadow), as(shadow)',
        answer: 'play(ravager), play(freca)',
      },
      { id: 'takeControl', once: true, when: 'opponent, optionalResponses, as(shadow)', answer: 'use(freca)' },
    ],
  },
  setup: {
    startAt: { site: 3, opponentSite: 2 },
    freeps: { characters: 'boromir', hand: 'ravager, freca' },
    shadow: { characters: 'aragorn', hand: 'runner1, uruk' },
  },
  steps: [
    {
      id: 'theirTurn',
      when: 'human, action, phase(shadow), as(shadow)',
      messages: [
        "The opponent's fellowship has moved on to site 4.  You are the Shadow player: play your Goblin and your Uruk.",
      ],
      allow: 'play(runner1)',
    },
    { when: 'human, action, phase(shadow), turn(1)', allow: 'play(uruk)' },
    { when: 'human, action, phase(shadow), turn(1)', allow: 'pass' },
    { when: 'human, assign, as(shadow), turn(1)', allow: 'assign(oppFrodo: uruk, boromir: runner1)' },
    {
      when: 'turn(1)',
      until: 'phase(regroup)',
      skirmishes: true,
      anyOrder: [
        {
          when: 'human, assign, as(shadow)',
          messages: ['The Uruk is fierce: it is assigned again.'],
          allow: 'assign(boromir: uruk)',
        },
        { when: 'human, action, phase(skirmish)', allow: 'pass', answer: 'pass' },
      ],
    },
    {
      id: 'ourTurn',
      checkpoint: true,
      when: 'human, action, phase(fellowship), as(freeps)',
      do: 'twilight(6)',
      messages: ['Your turn: you are the Free Peoples player now, at site 2.'],
      allow: 'pass',
    },
    {
      id: 'assigning',
      when: 'human, assign, as(freeps)',
      messages: ['The opponent took control of site 1.  Aragorn takes Freca; Frodo the Ravager.'],
      allow: 'assign(aragorn: freca, frodo: ravager)',
    },
    {
      when: 'turn(2)',
      until: 'phase(regroup)',
      anyOrder: [
        {
          when: 'human, nextSkirmish',
          allow: 'choose(aragorn), choose(frodo)',
          answer: 'choose(aragorn), choose(frodo)',
        },
        {
          when: 'human, action, phase(skirmish), as(freeps)',
          messages: [
            {
              text: 'The skirmish: {selfStrength} against {oppStrength}.',
              anchor: 'ui:skirmish:selfStrength',
              highlight: 'ui:skirmish:selfStrength',
            },
          ],
          allow: 'pass',
          answer: 'pass',
        },
      ],
    },
    { when: 'human, anotherMove, turn(2)', allow: 'option(No)', end: 'complete' },
  ],
};

/**
 * The game's own opening, played (real-opening): the bid, going first, the starting fellowship, the mulligan (the
 * checkpoint, step 5), then the first turn.
 */
export const REAL_OPENING = {
  title: 'How a game is set up',
  format: 'scenario',
  cards: {
    freeps: {
      frodo: '10_121',
      aragorn: '4_109',
      gandalf: '0_64',
      servant1: '1_83',
      swordarm: '1_116',
      boromir: '0_65',
    },
    shadow: { oppFrodo: '10_121', runner1: '1_178', runner2: '1_178', bowman: '2_60' },
  },
  ringBearer: { freeps: 'frodo', shadow: 'oppFrodo' },
  sites: { freeps: '1_323, 1_331, 1_340, 2_118', shadow: '1_323, 1_331, 1_340, 2_118' },
  options: { autoPass: true },
  opponent: { fallback: 'pass' },
  setup: { opening: 'game', freeps: { deck: 'boromir' } },
  steps: [
    {
      id: 'bid',
      when: 'human, bid',
      messages: ['Each player bids burdens for the right to choose who goes first.  Bid one.'],
      allow: 'number(1)',
    },
    { when: 'opponent, bid', answer: 'number(0)' },
    {
      id: 'first',
      when: 'human, goFirst',
      messages: ['You bid more, so you choose: go first.'],
      allow: 'option(Go first)',
    },
    {
      id: 'fellowship',
      when: 'human, startingFellowship',
      until: 'human, mulligan',
      messages: ['Your starting fellowship: up to 4 twilight of companions.  Start with Aragorn.'],
      allow: 'choose(aragorn), pass',
      answer: 'choose(aragorn), pass',
    },
    {
      id: 'mulligan',
      checkpoint: true,
      when: 'human, mulligan',
      messages: ['Your opening hand is drawn: {hand} cards, as the deck is small.  Keep them.'],
      allow: 'option(No)',
    },
    {
      when: 'human, action, phase(fellowship), turn(1)',
      messages: ['The first turn: yours.  You start with {burdens} burden(s): the one you bid.'],
      allow: 'pass',
      end: 'complete',
    },
  ],
};

/**
 * One step whose middle message has a condition that doesn't hold (no Gandalf in play), so the server leaves it out:
 * the page says the other two, counted as two. The rest is a new draft's own (Frodo, one-sided).
 */
export const LEFT_OUT = {
  title: 'A message left out',
  steps: [
    {
      when: 'freeps, action, phase(fellowship), turn(1)',
      messages: [
        'The first of two.',
        { text: 'Never said: there is no Gandalf.', require: { type: 'canSpot', filter: 'name(Gandalf)' } },
        { text: 'The second of two: Frodo is here.', require: { type: 'canSpot', filter: 'name(Frodo)' } },
      ],
      allow: 'pass',
      end: 'complete',
    },
  ],
};
