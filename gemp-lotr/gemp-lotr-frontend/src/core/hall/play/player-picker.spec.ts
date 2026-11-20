import { TestBed } from '@angular/core/testing';
import { HallTestBed, setUpHall } from '../../../testing/hall';
import { ChatService } from '../../chat/chat.service';
import { hallPlayers, pickerItems, PlayerSearch, rankNames } from './player-picker';

describe('the invitee picker', () => {
  it('ranks names: exact, then starting with, then containing', () => {
    expect(rankNames(['Bobby', 'abob', 'bob', 'Carol'], 'bob')).toEqual(['bob', 'Bobby', 'abob']);
    expect(rankNames(['b', 'A'], '')).toEqual(['A', 'b']);
    expect(hallPlayers(['+ bob', 'Bob', '* alice', ''])).toEqual(['alice', 'bob']);
  });

  it('lists the players in the hall, then the others, never the viewer', () => {
    expect(pickerItems('', ['alice', 'bob'], ['zed'], 'alice', null)).toEqual([
      { kind: 'player', name: 'bob', heading: 'In the hall' },
      { kind: 'player', name: 'zed', heading: 'Other players' },
    ]);
    expect(pickerItems('', [], [], '', null)).toEqual([
      { kind: 'note', text: 'Nobody else is in the hall. Type a name to search all players.' },
    ]);
    expect(pickerItems('bo', ['bob'], [], 'alice', ['bob', 'boris', 'alice'])).toEqual([
      { kind: 'player', name: 'bob', heading: 'In the hall' },
      { kind: 'player', name: 'boris', heading: 'Other players' },
    ]);
    expect(pickerItems('qq', [], [], '', [])).toEqual([{ kind: 'note', text: 'No player\'s name starts with "qq".' }]);
    expect(pickerItems('z', [], ['zed'], '', null)).toEqual([
      { kind: 'player', name: 'zed', heading: 'Other players' },
    ]);
  });

  describe('PlayerSearch', () => {
    let hall: HallTestBed;
    beforeEach(() => {
      hall = setUpHall([
        {
          provide: ChatService,
          useValue: {
            users: () => [{ name: 'alice' }, { name: '+ bob' }],
            tournamentAnnouncements$: { subscribe: () => ({}) },
          },
        },
      ]);
    });
    afterEach(() => hall.finish());

    it('searches the server from 2 characters, once per prefix', async () => {
      const search = TestBed.inject(PlayerSearch);
      expect(search.online()).toEqual(['bob']);
      const first = search.suggest('er');
      const req = hall.request('/hall/players', 'GET');
      expect(req.request.params.get('prefix')).toBe('er');
      expect(req.request.params.get('limit')).toBe('10');
      req.flush({ players: ['erin'] });
      expect(await first).toEqual([{ kind: 'player', name: 'erin', heading: 'Other players' }]);
      expect(await search.suggest('ER')).toEqual([{ kind: 'player', name: 'erin', heading: 'Other players' }]);

      const failing = search.suggest('bo');
      hall.request('/hall/players', 'GET').flush('', { status: 500, statusText: 'Error' });
      expect(await failing).toEqual([{ kind: 'player', name: 'bob', heading: 'In the hall' }]);
      search.pin('zed');
      expect(await search.suggest('')).toContainEqual({ kind: 'player', name: 'zed', heading: 'Other players' });
    });
  });
});
