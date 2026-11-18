import { PreGameInfo } from '../state/game-model';
import { metaSitesOf, preGameView } from './pre-game';

const info = (extra: Partial<PreGameInfo> = {}): PreGameInfo => ({
  participantId: 'bob',
  players: ['alice', 'bob'],
  summary: 'Casual',
  notes: 'My notes',
  maps: '',
  metaSites: '',
  ...extra,
});

describe('preGameView', () => {
  it("puts the page's player on the left with their notes, the other player on the right", () => {
    const view = preGameView(info());
    expect(view.left).toEqual({ name: 'bob', cards: [], notes: 'My notes' });
    expect(view.right).toEqual({ name: 'alice', cards: [] });
  });

  it('adds the two reminders to the summary', () => {
    const { summary } = preGameView(info());
    expect(summary.startsWith('Casual<br><br>Please be courteous')).toBe(true);
    expect(summary).toContain("('Options' tab → 'Concede game' button)");
  });

  it('shows a spectator the players in their order', () => {
    const view = preGameView(info({ participantId: 'carol' }));
    expect([view.left.name, view.right.name]).toEqual(['alice', 'bob']);
    expect(preGameView(info({ participantId: null, players: [] })).left.name).toBe('');
  });

  it("lists each side's map and meta-sites, a meta-site's modifier drawn over its card", () => {
    const view = preGameView(info({ maps: 'bob:100_1,alice:100_2', metaSites: 'bob:91_1|92_1;91_2|92_2' }));
    expect(view.left.cards).toEqual([
      { blueprintId: '100_1', overlayBlueprintId: null, label: 'Map' },
      { blueprintId: '91_1', overlayBlueprintId: '92_1', label: 'Meta-Site' },
      { blueprintId: '91_2', overlayBlueprintId: '92_2', label: 'Meta-Site' },
    ]);
    expect(view.right.cards).toEqual([{ blueprintId: '100_2', overlayBlueprintId: null, label: 'Map' }]);
  });

  it('reads meta-sites per player, tolerating a pair with a part missing', () => {
    expect(metaSitesOf('')).toEqual({});
    expect(metaSitesOf('bob:91_1')).toEqual({ bob: [{ visual: '91_1', modifier: '' }] });
  });
});
