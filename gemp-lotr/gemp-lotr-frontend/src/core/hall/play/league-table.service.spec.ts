import { TestBed } from '@angular/core/testing';
import { readServerResponse } from '../../../testing/files';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { LEAGUE_COOKIE, LeagueTableFlow } from './league-table.service';
import { MUST_SELECT_LEAGUE, MUST_SELECT_LEAGUE_DECK, NO_LEAGUE_YET } from './play-messages';

describe('LeagueTableFlow', () => {
  let hall: HallTestBed;
  let league: LeagueTableFlow;

  beforeEach(() => {
    hall = setUpHall();
    league = TestBed.inject(LeagueTableFlow);
  });

  afterEach(() => hall.finish());

  it('offers the leagues the player is in, the remembered one first chosen', async () => {
    hall.cookies.write(LEAGUE_COOKIE, '1700000002');
    const showing = league.show();
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml'));
    await showing;
    expect(league.options().map((o) => o.label)).toEqual(['Constructed League', 'Race to Mount Doom']);
    expect(league.league()).toBe('1700000002');
  });

  it('says when the player is in no league, and checks the form', async () => {
    const showing = league.show();
    hall.request('/league', 'GET').flush('<leagues/>');
    await showing;
    expect(league.options()).toEqual([{ value: '', label: NO_LEAGUE_YET, disabled: true }]);
    expect(await league.submit(null)).toBe(false);
    expect(league.result().message).toBe(MUST_SELECT_LEAGUE);
    league.createTableIn('1700000001');
    expect(await league.submit(null)).toBe(false);
    expect(league.result().message).toBe(MUST_SELECT_LEAGUE_DECK);
  });

  it('creates a league table and remembers the league and deck', async () => {
    const showing = league.show();
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml'));
    await showing;
    const creating = league.submit({ name: 'Elves', source: 'player' });
    const req = hall.request('/hall');
    expect(hall.body(req)).toMatchObject({ format: '1700000001', timer: '', desc: '', isInviteOnly: 'false' });
    req.flush('<result>OK</result>');
    expect(await creating).toBe(true);
    expect(hall.cookies.read(LEAGUE_COOKIE)).toBe('1700000001');
    expect(JSON.parse(hall.cookies.read('league-table-decks')!)['1700000001']).toEqual(['Elves', 'player']);
  });

  it('offers the next steps after a join', async () => {
    const joined = league.leagueJoined('1700000003');
    hall.request('/league', 'GET').flush(readServerResponse('leagues.xml').replace('member="false"', 'member="true"'));
    await Promise.resolve();
    await Promise.resolve();
    hall.request('/league/1700000003', 'GET').flush('<league draftable="true"><serie limited="true"/></league>');
    await joined;
    expect(league.league()).toBe('1700000003');
    expect(league.nextSteps()).toEqual({
      message: 'You joined Another Race. Next:',
      isError: false,
      steps: [
        { kind: 'create-table', leagueCode: '1700000003' },
        { kind: 'go-to-draft', url: '/gemp-lotr/soloDraft.html?eventId=1700000003' },
        { kind: 'deckbuilder', url: '/gemp-lotr/ng/deckbuilder/' },
      ],
      hint: expect.stringContaining('This league issues its own cards') as unknown as string,
    });
    league.leagueJoinFailed('Not enough silver');
    expect(league.nextSteps()?.message).toBe('Could not join the league: Not enough silver');
  });
});
