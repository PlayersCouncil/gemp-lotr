import { draftState } from '../../../testing/authoring';
import { insertAt, STOCK_TEMPLATES, templateCards, templateChoices, templateText } from './templates';

describe('numbers from the game in a message', () => {
  it('offers the totals, then the ones for a card, then the ones for a seat, each saying what it quotes', () => {
    const choices = templateChoices({ totals: ['twilight'], perCard: ['strength', 'odd'], perSeat: ['hand'] });
    expect(choices.map((choice) => [choice.name, choice.needs])).toEqual([
      ['twilight', 'none'],
      ['strength', 'card'],
      ['odd', 'card'],
      ['hand', 'seat'],
    ]);
    expect(choices[0]?.title).toBe('The twilight pool');
    expect(choices[1]?.title).toBe('A card’s strength now: choose the card');
    expect(choices[2]?.title).toBe('odd: choose the card');
    expect(choices[3]?.title).toBe('Cards in hand (the player’s; or a seat’s): choose the seat');
    // (a server that does not say: what it said when this was written)
    expect(templateChoices(undefined)).toHaveLength(
      STOCK_TEMPLATES.totals.length + STOCK_TEMPLATES.perCard.length + STOCK_TEMPLATES.perSeat.length,
    );
  });

  it('writes a template as the text has it, for a card or a seat', () => {
    expect(templateText('twilight')).toBe('{twilight}');
    expect(templateText('strength', 'aragorn')).toBe('{strength:aragorn}');
    expect(templateText('hand', null)).toBe('{hand}');
  });

  it("names the scenario's cards, the Free Peoples seat's first", () => {
    const draft = { ...draftState().draft, cards: { shadow: { orc: '1_1' }, freeps: { frodo: '1_2', sam: '1_3' } } };
    expect(templateCards(draft)).toEqual(['frodo', 'sam', 'orc']);
    expect(templateCards(draftState().draft)).toEqual([]);
    expect(templateCards(null)).toEqual([]);
  });

  it('puts a piece in at the caret, in place of what is selected, the caret after it', () => {
    expect(insertAt('Pool: now', 6, 6, '{twilight} ')).toEqual({ text: 'Pool: {twilight} now', caret: 17 });
    expect(insertAt('Pool: XX now', 6, 8, '{twilight}')).toEqual({ text: 'Pool: {twilight} now', caret: 16 });
    expect(insertAt('ab', 9, 12, 'c')).toEqual({ text: 'abc', caret: 3 });
    expect(insertAt('ab', -3, 1, 'c')).toEqual({ text: 'cb', caret: 1 });
  });
});
