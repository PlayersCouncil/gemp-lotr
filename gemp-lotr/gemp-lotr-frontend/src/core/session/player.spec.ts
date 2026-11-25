import { readServerResponse } from '../../testing/files';
import { hasPlayerType, parsePlayer, PlayerType } from './player';
import { parseJson } from '../api/json';

describe('parsePlayer', () => {
  it('reads the answer of GET /player', () => {
    const player = parsePlayer(parseJson(readServerResponse('player.json')));
    expect(player).toEqual({ name: 'Visual tester', type: 'un', hasTrophies: true });
    expect(hasPlayerType(player, PlayerType.user)).toBe(true);
    expect(hasPlayerType(player, PlayerType.admin)).toBe(false);
  });

  it('is null for anything that names no player, and defaults the other fields', () => {
    expect(parsePlayer(null)).toBeNull();
    expect(parsePlayer('tester')).toBeNull();
    expect(parsePlayer({ type: 'u' })).toBeNull();
    expect(parsePlayer({ name: '' })).toBeNull();
    expect(parsePlayer({ name: 'bob' })).toEqual({ name: 'bob', type: '', hasTrophies: false });
    expect(hasPlayerType(null, PlayerType.user)).toBe(false);
  });
});
