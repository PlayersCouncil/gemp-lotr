import { TestBed } from '@angular/core/testing';
import { Subject } from 'rxjs';
import { ChatService } from '../../chat/chat.service';
import { TournamentAnnouncement } from '../../chat/chat-models';
import { Notifier } from '../../notifier/notifier';
import { FakeNotifier, settle } from '../../../testing/fake-notifier';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { HallBoard } from '../board/hall-board.service';
import { HallDialogs } from '../hall-dialogs.service';
import { JoinDialog } from '../play/join-dialog.service';
import { HallReadyOffers } from '../ready/hall-ready-offers.service';
import { GAME_START_SOUND } from '../tables/hall-tables.service';
import { HallQueuesService, READY_CHECK_TICK_MS } from './hall-queues.service';

describe('HallQueuesService', () => {
  let hall: HallTestBed;
  let queues: HallQueuesService;
  let dialogs: HallDialogs;
  let announcements: Subject<TournamentAnnouncement>;
  const warnings: string[] = [];

  beforeEach(() => {
    vi.useFakeTimers();
    warnings.length = 0;
    announcements = new Subject();
    hall = setUpHall([
      {
        provide: ChatService,
        useValue: {
          tournamentAnnouncements$: announcements.asObservable(),
          appendWarning: (text: string) => warnings.push(text),
          users: () => [],
        },
      },
    ]);
    TestBed.inject(HallBoard);
    queues = TestBed.inject(HallQueuesService);
    dialogs = TestBed.inject(HallDialogs);
  });

  afterEach(() => {
    hall.finish();
    vi.useRealTimers();
  });

  const titles = () => dialogs.open().map((d) => d.title);
  const board = () => TestBed.inject(HallBoard);

  it('starts the ready check with its dialog and fanfare, counts down, and ends it', async () => {
    hall.connect();
    expect(queues.readyCheckDialogs()).toEqual([{ queueId: 'pm1', queueName: 'Casual FotR Live Draft', seconds: 45 }]);
    expect(hall.sounds).toContain(GAME_START_SOUND);
    expect(hall.navigation.opened).toContain('/gemp-lotr/soloDraft.html?eventId=t2');
    vi.advanceTimersByTime(READY_CHECK_TICK_MS * 5);
    expect(queues.readyCheckDialogs()[0].seconds).toBe(40);

    queues.closeReadyCheckDialog('pm1');
    expect(queues.readyCheckDialogs()).toEqual([]);
    // the next update does not show it again
    hall.poll(
      '<hall channelNumber="8"><queue action="update" id="pm1" signedUp="true" readyCheckSecsRemaining="30"/></hall>',
    );
    expect(queues.readyCheckDialogs()).toEqual([]);

    const confirming = queues.confirmReady('pm1');
    hall.request('/hall/queue/pm1/ready').flush('<result>OK</result>');
    await confirming;
    expect(titles()).toEqual([]);
    hall.poll('<hall channelNumber="8"><queue action="remove" id="pm1"/></hall>');
    expect(queues.readyCheckDialogs()).toEqual([]);
  });

  it('closes the dialog once the check is confirmed, and shows a refusal as an Error with a chat warning', async () => {
    hall.connect();
    hall.poll(
      '<hall channelNumber="8"><queue action="update" id="pm1" signedUp="true" readyCheckSecsRemaining="40" confirmedReadyCheck="true"/></hall>',
    );
    expect(queues.readyCheckDialogs()).toEqual([]);
    const starting = queues.startQueue('pm1');
    hall.request('/hall/queue/pm1/start').flush('<error message="Not &lt;b&gt;enough&lt;/b&gt; players"/>');
    await starting;
    expect(titles()).toEqual(['Error']);
    expect(dialogs.open()[0].body).toEqual([
      { kind: 'text', text: 'Not ' },
      { kind: 'element', tag: 'b', children: [{ kind: 'text', text: 'enough' }] },
      { kind: 'text', text: ' players' },
    ]);
    expect(warnings).toEqual(['Not <b>enough</b> players']);
  });

  it('leaves a queue and abandons a tournament after the question', async () => {
    hall.connect();
    expect(queues.inTournament()).toBe(true);
    const leaving = queues.leaveQueue('pm1');
    hall.request('/hall/queue/pm1/leave').flush('<result>OK</result>');
    await leaving;
    expect(queues.inTournament()).toBe(false);

    const notifier = TestBed.inject(Notifier) as FakeNotifier;
    const t1 = board().events().tournament('t1')!;
    await queues.abandon(t1);
    expect(notifier.dialogs).toEqual([
      'confirm: Are you sure you want to resign from the Casual Fellowship Block tournament? This cannot be undone.',
    ]);
    notifier.answers.push(true);
    const dropping = queues.abandon(t1);
    await settle();
    hall.request('/hall/tournament/t1/leave').flush('<response message="You dropped."/>');
    await dropping;
    expect(titles()).toEqual(['Response']);
  });

  it('shows the prizes, the competing players and the tournament announcements meant for the viewer', () => {
    hall.connect();
    queues.showPrizes(board().events().queue('daily')!.prizes);
    queues.showPrizes({ label: '', lines: [] });
    queues.showCompetingPlayers(board().events().tournament('t2')!);
    expect(titles()).toEqual(['Prizes details', 'Prizes details', 'Competing Players']);
    expect(dialogs.open()[0].body[0]).toMatchObject({ kind: 'element', tag: 'ul' });
    expect(dialogs.open()[1].body).toEqual([
      { kind: 'element', tag: 'p', children: [{ kind: 'text', text: 'No prize information available.' }] },
    ]);

    announcements.next({ message: 'Round <b>2</b>', recipients: null });
    announcements.next({ message: 'For bob', recipients: ['bob'] });
    announcements.next({ message: 'For alice', recipients: ['carol', 'alice'] });
    expect(titles().slice(3)).toEqual(['Tournament Update', 'Tournament Update']);
  });

  it('offers a blocked draft and closes its prompt when the draft is over', () => {
    hall.navigation.blockPopups = true;
    hall.connect();
    const offers = TestBed.inject(HallReadyOffers);
    expect(offers.offers().map((o) => [o.key, o.title, o.label])).toEqual([
      ['draft:t2', 'Your draft is ready', 'Open draft'],
    ]);
    hall.poll(
      '<hall channelNumber="8"><tournament action="update" id="t2" type="SOLODRAFT" stage="Playing Games" signedUp="true"/></hall>',
    );
    expect(offers.offers()).toEqual([]);
  });

  it('opens the Join dialog for a Constructed late join', () => {
    hall.connect();
    void queues.joinLate(board().events().tournament('t3')!);
    hall.request('/deck/list', 'GET').flush('<decks/>');
    hall.request('/deck/libraryList', 'GET').flush('<decks/>');
    expect(TestBed.inject(JoinDialog).state()).toMatchObject({
      kind: 'late-join',
      title: 'Join Tournament: Casual Movie Block',
      subtitle: 'Format: Movie Block',
    });
  });
});
