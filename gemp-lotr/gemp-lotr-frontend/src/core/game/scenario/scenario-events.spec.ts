import { messagePlace, parseScenarioEvent, ScenarioInfo, ScenarioMessage, ScenarioStep } from './scenario-events';

/** An info event as the server sends it for the first tutorial. */
const INFO = {
  type: 'info',
  id: 'basics-01',
  version: 1,
  kind: 'tutorial',
  title: 'The Basics: Companions and Skirmishes',
  coverage: 'strict',
  human: 'asdf',
  humanSeat: 'freeps',
  opponent: '~Scenario',
  opponentName: 'Basic Shadow',
  autoAccept: true,
  autoDismissDeckReveal: true,
  animationSpeed: null,
  bubbleGap: null,
  resumedAt: null,
  campaign: {
    id: 'basics',
    title: 'Learn to Play',
    position: 1,
    count: 2,
    next: { id: 'basics-02-items-damage', title: 'The Basics: Items and Damage' },
  },
  author: false,
  draftTest: false,
};

/** A step's message as the server sends it. */
const MESSAGE = {
  header: 'Welcome',
  html: '<p>Hello</p>',
  anchor: 'card:frodo',
  anchors: ['card:frodo'],
  gap: null,
  highlight: ['card:frodo'],
  highlightStyle: 'gold',
  avoid: ['ui:alertBox'],
  side: 'auto',
  advance: 'next',
  advanceCard: null,
  choices: [],
  on: null,
  zoom: null,
};

const STEP = {
  type: 'step',
  step: 1,
  sub: 0,
  name: null,
  seat: 'human',
  acknowledge: false,
  pause: false,
  checkpoint: false,
  messages: [MESSAGE],
  parts: { 'frodo:strength': [5, 60, 12, 10] },
  allow: { answers: ['12'], pass: false, otherwise: null, hints: { frodo: '<p>Not him.</p>' } },
  cards: { frodo: 7 },
};

const parse = (value: unknown) => parseScenarioEvent(JSON.stringify(value));

describe('parseScenarioEvent', () => {
  it('reads who the scenario is: its kind, the seats, the opponent and its shown name, the fixed settings, the campaign', () => {
    expect(parse(INFO)).toEqual<ScenarioInfo>({
      type: 'info',
      id: 'basics-01',
      kind: 'tutorial',
      title: 'The Basics: Companions and Skirmishes',
      humanSeat: 'freeps',
      opponent: '~Scenario',
      opponentName: 'Basic Shadow',
      settings: { autoAccept: true, autoDismissDeckReveal: true, animationSpeed: null },
      bubbleGap: null,
      resumedAt: null,
      campaign: {
        id: 'basics',
        title: 'Learn to Play',
        position: 1,
        count: 2,
        next: { id: 'basics-02-items-damage', title: 'The Basics: Items and Damage' },
      },
      author: false,
      draftTest: false,
      twoSided: false,
      opening: 'board',
      mode: 'play',
      level: 'none',
      tools: [],
      canStop: false,
      canStart: false,
      slots: [],
      undo: null,
    });
  });

  it('fills in what an info event leaves out, and reads a puzzle played from a checkpoint by its author', () => {
    expect(parse({ type: 'info', id: 'p1' })).toEqual({
      type: 'info',
      id: 'p1',
      kind: 'tutorial',
      title: 'p1',
      humanSeat: 'freeps',
      opponent: '',
      opponentName: '',
      settings: { autoAccept: null, autoDismissDeckReveal: null, animationSpeed: null },
      bubbleGap: null,
      resumedAt: null,
      campaign: null,
      author: false,
      draftTest: false,
      twoSided: false,
      opening: 'board',
      mode: 'play',
      level: 'none',
      tools: [],
      canStop: false,
      canStart: false,
      slots: [],
      undo: null,
    });
    expect(
      parse({
        type: 'info',
        id: 'p2',
        kind: 'puzzle',
        humanSeat: 'shadow',
        opponent: 'bot',
        animationSpeed: -2,
        bubbleGap: '20px',
        resumedAt: 4,
        author: true,
        draftTest: true,
        campaign: { id: 'c', next: { id: 'n' } },
      }),
    ).toMatchObject({
      kind: 'puzzle',
      humanSeat: 'shadow',
      opponentName: 'bot',
      settings: { animationSpeed: -2 },
      bubbleGap: '20px',
      resumedAt: 4,
      author: true,
      draftTest: true,
      campaign: { id: 'c', title: 'c', position: 1, count: 1, next: { id: 'n', title: 'n' } },
    });
    // A kind or seat it doesn't know is the usual one; a campaign without an id, or a next without one, is none
    expect(parse({ type: 'info', id: 'x', kind: 'raid', humanSeat: 'both', campaign: { title: 't' } })).toMatchObject({
      kind: 'tutorial',
      humanSeat: 'freeps',
      campaign: null,
    });
    expect(parse({ type: 'info', id: 'x', campaign: { id: 'c', next: { title: 't' } } })).toMatchObject({
      campaign: { next: null },
    });
    expect(parse({ type: 'info', id: 'x', campaign: 'c' })).toMatchObject({ campaign: null });
    // No id: not an info event the page can use
    expect(parse({ type: 'info', title: 'nameless' })).toBeNull();
  });

  it("reads a step: its messages, the parts of the cards, what it allows and the cards' ids as text", () => {
    expect(parse(STEP)).toEqual<ScenarioStep>({
      type: 'step',
      step: 1,
      sub: 0,
      seat: 'human',
      pause: false,
      role: null,
      turnOf: null,
      turn: null,
      lastCheckpoint: null,
      messages: [
        {
          index: null,
          header: 'Welcome',
          html: '<p>Hello</p>',
          anchors: ['card:frodo'],
          gap: null,
          highlight: ['card:frodo'],
          highlightStyle: 'gold',
          avoid: ['ui:alertBox'],
          side: 'auto',
          advance: 'next',
          advanceCard: null,
          choices: [],
          on: null,
          zoom: null,
        },
      ],
      parts: { 'frodo:strength': [5, 60, 12, 10] },
      allow: { answers: ['12'], pass: false, otherwise: null, hints: { frodo: '<p>Not him.</p>' } },
      cards: { frodo: '7' },
    });
  });

  it('fills in what a step leaves out, and leaves out what it cannot read', () => {
    expect(
      parse({
        type: 'step',
        step: 3,
        seat: 'opponent',
        pause: true,
        messages: [MESSAGE, 'not a message', null],
        parts: { 'Gandalf:Name': [1, 2, 3, 4], short: [1, 2, 3], words: ['a', 'b', 'c', 'd'], flat: 5 },
        allow: 'anything',
        cards: { sam: '8', ring: true, bad: null },
      }),
    ).toMatchObject({
      step: 3,
      sub: 0,
      seat: 'opponent',
      pause: true,
      messages: [{ header: 'Welcome' }],
      parts: { 'gandalf:name': [1, 2, 3, 4] },
      allow: null,
      cards: { sam: '8' },
    });
    expect(parse({ type: 'step', step: 1, seat: 'nobody', messages: 'none', parts: [], cards: [] })).toMatchObject({
      seat: 'human',
      pause: false,
      messages: [],
      parts: {},
      cards: {},
    });
    // A step without a number is none
    expect(parse({ type: 'step', messages: [] })).toBeNull();
    expect(parse({ type: 'step', step: Number.POSITIVE_INFINITY })).toBeNull();
  });

  it("reads what a step allows: its answers, passing, what to say of other clicks, and each card's hint", () => {
    const step = parse({
      ...STEP,
      allow: {
        answers: ['1', 2, '', '3'],
        pass: true,
        otherwise: "<p>Not yet <img src='images/cultures/gondor.png'/></p>",
        hints: { frodo: '<p>Not him.</p>', sam: 7 },
      },
    }) as ScenarioStep;
    expect(step.allow).toEqual({
      answers: ['1', '', '3'],
      pass: true,
      otherwise: "<p>Not yet <img src='/gemp-lotr/images/cultures/gondor.png'/></p>",
      hints: { frodo: '<p>Not him.</p>' },
    });
    expect((parse({ ...STEP, allow: {} }) as ScenarioStep).allow).toEqual({
      answers: [],
      pass: false,
      otherwise: null,
      hints: {},
    });
  });

  it('reads every field of a message, the pictures in its HTML from the legacy pages', () => {
    const step = parse({
      ...STEP,
      messages: [
        {
          html: "<img src='images/icons/twilight.png'>",
          anchors: ['card:frodo', 3, 'ui:twilightPool'],
          gap: '3',
          highlight: ['zone:minions'],
          highlightStyle: 'red',
          avoid: [],
          side: 'left',
          advance: 'inspect',
          advanceCard: 'frodo',
          choices: [{ name: 'yes', label: 'Yes!' }, { name: 'no' }, { label: 'nameless' }, 'bad'],
          on: { moment: 'assigned', card: 'frodo' },
          zoom: {
            blueprint: '1_290',
            image: null,
            magnify: 0.5,
            focus: [{ name: 'strength', rect: [5, 60, 12, 10] }, { rect: [1, 2, 3] }, 'bad', { rect: [1, 2, 3, 4] }],
          },
        },
      ],
    }) as ScenarioStep;
    expect(step.messages[0]).toEqual<ScenarioMessage>({
      index: null,
      header: null,
      html: "<img src='/gemp-lotr/images/icons/twilight.png'>",
      anchors: ['card:frodo', 'ui:twilightPool'],
      gap: '3',
      highlight: ['zone:minions'],
      highlightStyle: 'red',
      avoid: [],
      side: 'left',
      advance: 'inspect',
      advanceCard: 'frodo',
      choices: [
        { name: 'yes', label: 'Yes!' },
        { name: 'no', label: 'no' },
      ],
      on: { moment: 'assigned', card: 'frodo' },
      zoom: {
        blueprint: '1_290',
        image: null,
        // (never smaller than the card itself)
        magnify: 1,
        focus: [
          { name: 'strength', rect: [5, 60, 12, 10] },
          { name: null, rect: [1, 2, 3, 4] },
        ],
      },
    });
  });

  it('gives a message the usual look and behaviour for what it leaves out or names wrongly', () => {
    const step = parse({
      ...STEP,
      messages: [
        { highlightStyle: 'purple', side: 'under', advance: 'never', on: { moment: 'later' }, zoom: { magnify: 3 } },
        // (a game recorded before a message could point at several things names the one)
        { anchor: 'ui:alertBox', anchors: [] },
        { anchor: 7 },
      ],
    }) as ScenarioStep;
    expect(step.messages[0]).toEqual<ScenarioMessage>({
      index: null,
      header: null,
      html: '',
      anchors: [],
      gap: null,
      highlight: [],
      highlightStyle: 'gold',
      avoid: [],
      side: 'auto',
      advance: 'next',
      advanceCard: null,
      choices: [],
      on: { moment: 'start', card: null },
      zoom: { blueprint: null, image: null, magnify: 3, focus: [] },
    });
    expect(step.messages[1]?.anchors).toEqual(['ui:alertBox']);
    expect(step.messages[2]?.anchors).toEqual([]);
  });

  it("reads a two-sided game, and one that plays the game's own opening", () => {
    expect(parse({ ...INFO, twoSided: true, opening: 'game' })).toMatchObject({ twoSided: true, opening: 'game' });
    // (anything else is the usual: one-sided, the board laid out)
    expect(parse({ ...INFO, twoSided: 'yes', opening: 'bid' })).toMatchObject({ twoSided: false, opening: 'board' });
  });

  it('reads the role the person plays at the step, whose turn it is, which, and the last checkpoint reached', () => {
    expect(parse({ ...STEP, role: 'shadow', turnOf: 'opponent', turn: 2, lastCheckpoint: 5 })).toMatchObject({
      role: 'shadow',
      turnOf: 'opponent',
      turn: 2,
      lastCheckpoint: 5,
    });
    expect(parse({ ...STEP, role: 'freeps', turnOf: 'human', turn: 0, lastCheckpoint: null })).toMatchObject({
      role: 'freeps',
      turnOf: 'human',
      turn: 0,
      lastCheckpoint: null,
    });
    // (a game recorded before these were said, or anything it can't read: not said)
    expect(parse({ ...STEP, role: 'both', turnOf: 'me', turn: '2', lastCheckpoint: 'x' })).toMatchObject({
      role: null,
      turnOf: null,
      turn: null,
      lastCheckpoint: null,
    });
  });

  it("knows each message by its place in the file, which the step's list no longer is once one is left out", () => {
    const step = parse({
      ...STEP,
      messages: [
        { ...MESSAGE, index: 1 },
        { ...MESSAGE, index: 3 },
      ],
    }) as ScenarioStep;
    expect(step.messages.map((message) => message.index)).toEqual([1, 3]);
    expect(step.messages.map((message) => messagePlace(step.messages, message))).toEqual([1, 3]);
    // A game recorded before a message could be left out says nothing (nor what can't be read): each is in its place
    const old = parse({ ...STEP, messages: [MESSAGE, { ...MESSAGE, index: -1 }, { ...MESSAGE, index: 1.5 }] });
    const messages = (old as ScenarioStep).messages;
    expect(messages.map((message) => message.index)).toEqual([null, null, null]);
    expect(messages.map((message) => messagePlace(messages, message))).toEqual([0, 1, 2]);
    // (one that isn't among them has no place)
    expect(messagePlace(messages, step.messages[0]!)).toBe(-1);
  });

  it('reads that the game has left its script, with or without a word of its own', () => {
    expect(parse({ type: 'offscript', message: "<p>Off <img src='images/x.png'/></p>" })).toEqual({
      type: 'offscript',
      message: "<p>Off <img src='/gemp-lotr/images/x.png'/></p>",
    });
    expect(parse({ type: 'offscript' })).toEqual({ type: 'offscript', message: null });
  });

  it('reads how the scenario ended: the result, its messages and the last checkpoint', () => {
    expect(
      parse({ type: 'end', result: 'complete', messages: [], message: null, restart: false, checkpoint: null }),
    ).toEqual({
      type: 'end',
      result: 'complete',
      messages: [],
      message: null,
      checkpoint: null,
      restart: false,
      winner: null,
      reason: null,
      reset: false,
      score: null,
      unlisted: false,
    });
    expect(
      parse({ type: 'end', result: 'failed', messages: [MESSAGE], message: '<p>Again?</p>', checkpoint: 5 }),
    ).toMatchObject({ result: 'failed', messages: [{ header: 'Welcome' }], message: '<p>Again?</p>', checkpoint: 5 });
    for (const result of ['off_script', 'abandoned', 'error']) {
      expect(parse({ type: 'end', result })).toMatchObject({ result, restart: false });
    }
    // Left its script, to be played again (outcome.offScript.result: restart), or a failure (fail)
    expect(parse({ type: 'end', result: 'off_script', restart: true })).toMatchObject({ restart: true });
    expect(parse({ type: 'end', result: 'off_script', restart: 'yes' })).toMatchObject({ restart: false });
    // A result it doesn't know is an error
    expect(parse({ type: 'end', result: 'won' })).toMatchObject({ result: 'error', messages: [] });
  });

  it("reads a table changed by hand: the mode, the level, the tools, Stop and Start (a sandbox's, as the server sends it)", () => {
    expect(
      parse({
        type: 'info',
        id: 'sandbox',
        kind: 'sandbox',
        mode: 'edit',
        level: 'sandbox',
        tools: ['add', 'copy', 'paint', 'modify'],
        canStop: false,
        canStart: true,
      }),
    ).toMatchObject({
      kind: 'sandbox',
      mode: 'edit',
      level: 'sandbox',
      tools: ['add', 'copy', 'modify'],
      canStart: true,
    });
    expect(parse({ type: 'info', id: 'p', mode: 'paused', level: 'puzzle', canStop: 'yes' })).toMatchObject({
      mode: 'play',
      level: 'puzzle',
      canStop: false,
    });
    // (the author's level is the page's own, never the server's)
    expect(parse({ type: 'info', id: 'p', level: 'author' })).toMatchObject({ level: 'none' });
  });

  it("reads a sandbox's end: who won and why, and that it can be reset", () => {
    expect(
      parse({
        type: 'end',
        result: 'ended',
        messages: [],
        message: null,
        restart: false,
        checkpoint: null,
        winner: 'opponent',
        reason: 'The Ring-Bearer is dead',
        reset: true,
      }),
    ).toMatchObject({ result: 'ended', winner: 'opponent', reason: 'The Ring-Bearer is dead', reset: true });
    expect(parse({ type: 'end', result: 'ended', winner: 'both', reason: 3, reset: 'yes' })).toMatchObject({
      winner: null,
      reason: null,
      reset: false,
    });
    expect(parse({ type: 'end', result: 'ended', winner: 'human' })).toMatchObject({ winner: 'human' });
  });

  it("reads a puzzle's info: its slots and whether it can be taken back (ep-notes.md P3, P6)", () => {
    const info = parse({
      type: 'info',
      id: 'p10',
      kind: 'puzzle',
      mode: 'edit',
      level: 'puzzle',
      undo: false,
      slots: [{ id: 'ally', zone: 'support', label: 'a Rohan ally', comes: { tokens: 1, wounds: 0 } }],
    }) as ScenarioInfo;
    expect(info.undo).toBe(false);
    expect(info.slots).toEqual([
      expect.objectContaining({ id: 'ally', zone: 'support', label: 'a Rohan ally', comes: { tokens: 1, wounds: 0 } }),
    ]);
    expect(parse({ type: 'info', id: 'p', undo: 'no' })).toMatchObject({ undo: null, slots: [] });
  });

  it("reads a puzzle's end: its score, and whether the solution is one its author did not list (P5, P7)", () => {
    expect(
      parse({
        type: 'end',
        result: 'complete',
        score: { value: 1160, label: "Sméagol's strength", better: 'higher', best: 1160, newBest: true },
        unlisted: true,
      }),
    ).toMatchObject({
      score: { value: 1160, label: "Sméagol's strength", better: 'higher', best: 1160, newBest: true },
      unlisted: true,
    });
    expect(parse({ type: 'end', result: 'complete', score: 'high', unlisted: 'yes' })).toMatchObject({
      score: null,
      unlisted: false,
    });
  });

  it('reads nothing from what is not a scenario event', () => {
    expect(parseScenarioEvent('')).toBeNull();
    expect(parseScenarioEvent('{not json')).toBeNull();
    expect(parse(null)).toBeNull();
    expect(parse([INFO])).toBeNull();
    expect(parse('info')).toBeNull();
    expect(parse({ type: 'author', id: 'x' })).toBeNull();
    expect(parse({ id: 'x' })).toBeNull();
  });
});
