import { DecisionView, NO_DECISION, decisionCard } from '../decision/decision-view';
import { emptyGame, GameModel } from '../state/game-model';
import { offTableChoices } from './hand-place';

const model = (extra: Partial<GameModel> = {}): GameModel => ({
  ...emptyGame(),
  decision: {
    decision: { id: '1', participantId: 'alice', type: 'ARBITRARY_CARDS', text: 'Choose', params: {} },
    arrivedAt: 12,
  },
  ...extra,
});

describe('offTableChoices', () => {
  it('shows nothing in the hand place without cards from off the table', () => {
    expect(offTableChoices(NO_DECISION, model())).toBeNull();
  });

  it("shows a decision's dialog of cards under its title, keyed by the decision", () => {
    const view: DecisionView = {
      ...NO_DECISION,
      dialog: {
        kind: 'cards',
        title: 'Choose a <b>possession</b>',
        cards: [{ id: 'temp1', blueprintId: '1_7', text: null, testingText: null }],
        buttons: ['Done'],
      },
    };
    const offer = offTableChoices(view, model());
    expect(offer?.label).toBe('Choose a <b>possession</b>');
    expect(offer?.labelIsHtml).toBe(true);
    expect(offer?.key).toBe('dialog:12');
    expect(offer?.cards.map((choice) => [choice.card.id, choice.card.zone])).toEqual([['temp1', 'SPECIAL']]);
  });

  it('names cards with an action after the pile they lie in, as the page knows it', () => {
    const site = { ...decisionCard('40', '1_319', null), zone: 'ADVENTURE_DECK', owner: 'alice' };
    const copy = decisionCard('extra40', '1_319', null);
    const view: DecisionView = { ...NO_DECISION, extraCards: [copy] };
    const offer = offTableChoices(view, model({ cards: { 40: site } }));
    expect(offer?.label).toBe('Your adventure deck');
    expect(offer?.labelIsHtml).toBe(false);
    // The decision's copy, which the decision marks
    expect(offer?.cards[0]?.card).toBe(copy);
  });

  it('calls cards from several places, or unknown ones, cards from elsewhere', () => {
    const view: DecisionView = { ...NO_DECISION, extraCards: [decisionCard('40', '1_7', null)] };
    expect(offTableChoices(view, model())?.label).toBe('Cards from elsewhere');
  });
});
