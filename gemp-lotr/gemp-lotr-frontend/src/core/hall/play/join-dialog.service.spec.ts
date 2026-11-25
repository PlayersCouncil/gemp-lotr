import { TestBed } from '@angular/core/testing';
import { settle } from '../../../testing/fake-notifier';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallBoard } from '../board/hall-board.service';
import { HallDialogs } from '../hall-dialogs.service';
import { MUST_SELECT_DECK } from '../tables/hall-tables-messages';
import { JoinDialog } from './join-dialog.service';

describe('JoinDialog', () => {
  let hall: HallTestBed;
  let join: JoinDialog;
  let dialogs: HallDialogs;
  const elves = { name: 'Elves', source: 'player' as const };

  beforeEach(() => {
    vi.useFakeTimers();
    hall = setUpHall();
    TestBed.inject(HallBoard);
    join = TestBed.inject(JoinDialog);
    dialogs = TestBed.inject(HallDialogs);
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  const decks = () => {
    hall.request('/deck/list', 'GET').flush('<decks><deck targetFormat="Fellowship Block">Elves</deck></decks>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
  };
  const titles = () => dialogs.open().map((d) => d.title);

  it('joins a waiting table with a deck, and reports HTTP failures in words', async () => {
    join.openTable('13');
    decks();
    expect(join.state()).toMatchObject({ kind: 'table', title: 'Join Table', buttonLabel: 'Join Table' });
    await join.submit(null);
    expect(join.result()).toEqual({ kind: 'error', message: MUST_SELECT_DECK });

    let joining = join.submit(elves);
    expect(join.result().kind).toBe('pending');
    const req = hall.request('/hall/13');
    expect(hall.body(req)).toEqual({ deckName: 'Elves', deckSource: 'player' });
    req.flush('<result>OK</result>');
    await joining;
    expect(join.state()).toBeNull();
    expect(titles()).toEqual([]);
    expect(JSON.parse(hall.cookies.read('join-table-decks')!)).toEqual({ _: ['Elves', 'player'] });

    join.openTable('13');
    decks();
    joining = join.submit(elves);
    hall.request('/hall/13').flush(JSON.stringify({ error: 'Your selected deck is not valid for this format' }), {
      status: 400,
      statusText: 'Bad Request',
    });
    await joining;
    expect(join.result().message).toBe('Your selected deck is not valid for this format');

    joining = join.submit(elves);
    hall.request('/hall/13').flush('', { status: 400, statusText: 'Bad Request', headers: { message: 'No deck' } });
    await joining;
    expect(join.result().message).toBe('No deck');

    joining = join.submit(elves);
    hall.request('/hall/13').flush('', { status: 401, statusText: 'Unauthorized' });
    await joining;
    expect(join.result()).toEqual({
      kind: 'error',
      message: 'You are not logged in. Log in again from the main page.',
    });
  });

  it('asks for a deck for a Constructed queue, and confirms the sign-up', async () => {
    hall.connect();
    const daily = TestBed.inject(HallBoard).events().queue('daily')!;
    await join.joinQueue(daily);
    decks();
    await settle();
    expect(join.state()).toMatchObject({
      title: 'Join Queue: Daily FotR',
      subtitle: 'Format: Fellowship Block · Starts: at 2026-11-20 19:00',
      memoryKey: 'Fellowship Block',
    });
    expect(join.initialDeck()).toEqual(elves);
    const joining = join.submit(elves);
    const req = hall.request('/hall/queue/daily');
    expect(hall.body(req)).toMatchObject({ deckName: 'Elves', deckSource: 'player' });
    req.flush('<result>OK</result>');
    await joining;
    expect(join.state()).toBeNull();
    expect(titles()).toEqual(['Joined Tournament']);
    expect(JSON.parse(hall.cookies.read('join-queue-decks')!)['Fellowship Block']).toEqual(['Elves', 'player']);
  });

  it('joins a limited queue at once, and shows a refusal in a dialog of its own', async () => {
    hall.connect();
    const draft = TestBed.inject(HallBoard).events().queue('pm1')!;
    const joining = join.joinQueue(draft);
    const req = hall.request('/hall/queue/pm1');
    expect(hall.body(req)).toMatchObject({ deckName: '', deckSource: '' });
    req.flush('<error message="Queue is full"/>');
    await joining;
    expect(titles()).toEqual(['Join Queue: Casual FotR Live Draft failed']);
    expect(dialogs.open()[0].body).toEqual([{ kind: 'text', text: 'Queue is full' }]);
  });

  it('registers a deck for a limited tournament', async () => {
    hall.connect();
    const t2 = TestBed.inject(HallBoard).events().tournament('t2')!;
    join.openRegisterDeck(t2);
    decks();
    expect(join.state()).toMatchObject({
      title: 'Register Deck: Casual FotR Solo Draft',
      subtitle: "Choose the deck you built from this tournament's cards.",
      memoryKey: 't2',
    });
    await join.submit(null);
    expect(join.result().message).toBe('You must select a deck.');
    const registering = join.submit(elves);
    hall.request('/hall/tournament/t2/registerdeck').flush('<response message="Deck registered."/>');
    await registering;
    expect(titles()).toEqual(['Deck Registered']);
  });

  it('joins a running Constructed tournament late with a deck, and says what the server answered', async () => {
    hall.connect();
    const late = TestBed.inject(HallBoard).events().tournament('t3')!;
    await join.joinLate(late);
    decks();
    await settle();
    expect(join.state()).toMatchObject({
      kind: 'late-join',
      title: 'Join Tournament: Casual Movie Block',
      subtitle: 'Format: Movie Block',
      buttonLabel: 'Join Tournament',
    });
    await join.submit(null);
    expect(join.result()).toEqual({ kind: 'error', message: MUST_SELECT_DECK });
    const joining = join.submit(elves);
    const req = hall.request('/hall/tournament/t3/join');
    expect(hall.body(req)).toMatchObject({ deckName: 'Elves', deckSource: 'player' });
    req.flush(JSON.stringify({ response: 'You joined Casual Movie Block.' }));
    await joining;
    expect(join.state()).toBeNull();
    expect(titles()).toEqual(['Joined Tournament']);
    expect(JSON.parse(hall.cookies.read('late-join-decks')!)['Movie Block']).toEqual(['Elves', 'player']);
  });

  it('joins a limited tournament late at once, with no deck', async () => {
    hall.connect();
    const draft = { ...TestBed.inject(HallBoard).events().tournament('t2')!, joinable: true };
    const joining = join.joinLate(draft);
    const req = hall.request('/hall/tournament/t2/join');
    expect(hall.body(req)).toMatchObject({ deckName: '', deckSource: '' });
    req.flush(JSON.stringify({ response: 'Joined.' }));
    await joining;
    expect(join.state()).toBeNull();
    expect(titles()).toEqual(['Joined Tournament']);
  });

  it('asks for a deck before joining a Constructed queue, and does nothing once closed', async () => {
    hall.connect();
    await join.joinQueue(TestBed.inject(HallBoard).events().queue('daily')!);
    decks();
    await join.submit(null);
    expect(join.result()).toEqual({ kind: 'error', message: MUST_SELECT_DECK });
    join.close();
    await join.submit(elves);
    expect(join.state()).toBeNull();
  });
});
