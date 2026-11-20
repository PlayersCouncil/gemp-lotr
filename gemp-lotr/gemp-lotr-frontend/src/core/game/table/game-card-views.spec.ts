import { signal } from '@angular/core';
import { TestBed } from '@angular/core/testing';
import { asDrawn, cardDisplayData } from '../../cards/card-display';
import { CardImageService } from '../../cards/card-image.service';
import { CardViews } from '../../cards/card-views.service';
import { FoilSetting } from '../../platform/foil-setting';
import { GameSession } from '../session/game-session.service';
import { emptyGame, GameCard, GameModel } from '../state/game-model';
import { GameCardViews } from './game-card-views';

const card = (id: string, blueprintId: string, extra: Partial<GameCard> = {}): GameCard => ({
  id,
  blueprintId,
  zone: 'SUPPORT',
  owner: 'alice',
  hindered: false,
  numbersHidden: false,
  index: null,
  attachedTo: null,
  stackedOn: null,
  attachedAt: 0,
  tokens: {},
  inactive: false,
  assignedTo: null,
  inSkirmish: false,
  testingText: null,
  backSideTestingText: null,
  ...extra,
});

/** The game's pre-game setup naming these meta-sites ("player:visual|modifier;…,player:…"). */
const withMetaSites = (metaSites: string): GameModel => ({
  ...emptyGame(),
  preGame: { participantId: 'alice', players: ['alice', 'bob'], summary: '', notes: '', maps: '', metaSites },
});

describe('GameCardViews', () => {
  const model = signal<GameModel>(emptyGame());
  let views: GameCardViews;
  let images: CardImageService;

  beforeEach(() => {
    model.set(emptyGame());
    TestBed.configureTestingModule({
      providers: [
        GameCardViews,
        CardViews,
        { provide: FoilSetting, useValue: { overlayImage: () => 'foil.png' } },
        { provide: GameSession, useValue: { model } },
      ],
    });
    views = TestBed.inject(GameCardViews);
    images = TestBed.inject(CardImageService);
  });

  it('draws a card as its own picture, with no overlay', () => {
    const view = views.of(card('1', '1_7'));
    expect(view.imageUrl).toBe(images.imageUrl('1_7'));
    expect(view.overlayImage).toBeNull();
  });

  it("draws a meta-site the game names as game.html does: the visual card, the modifier's picture over it", () => {
    model.set(withMetaSites('alice:90_1|91_1;90_2|91_7,bob:90_3|92_4'));
    const alices = views.of(card('1', '91_1'));
    expect(alices.imageUrl).toBe(images.imageUrl('90_1'));
    expect(alices.overlayImage).toBe(images.imageUrl('91_1'));
    expect(alices.blueprintId).toBe('91_1');
    // The opponent's too, and a foil copy by its bare blueprint
    expect(views.of(card('2', '92_4', { owner: 'bob' })).imageUrl).toBe(images.imageUrl('90_3'));
    expect(views.of(card('3', '91_7*')).imageUrl).toBe(images.imageUrl('90_2'));
  });

  it('draws a modifier the game does not name as itself (no visual card to draw it on)', () => {
    model.set(withMetaSites('alice:90_1|91_1'));
    const view = views.of(card('1', '91_2'));
    expect(view.imageUrl).toBe(images.imageUrl('91_2'));
    expect(view.overlayImage).toBeNull();
  });

  it('draws a hindered meta-site face down, without the overlay', () => {
    model.set(withMetaSites('alice:90_1|91_1'));
    const view = views.of(card('1', '91_1', { hindered: true }));
    expect(view.imageUrl).toBe(images.resolve('91_1', 'SUPPORT').backSideImageUrl);
    expect(view.overlayImage).toBeNull();
  });

  it('draws a card again once the pre-game setup names it, the same view while nothing changes', () => {
    const before = views.of(card('1', '91_1'));
    expect(views.of(card('1', '91_1'))).toBe(before);
    model.set(withMetaSites('alice:90_1|91_1'));
    const after = views.of(card('1', '91_1'));
    expect(after).not.toBe(before);
    expect(after.imageUrl).toBe(images.imageUrl('90_1'));
  });

  it('draws a card named in a text as a meta-site too (asMetaSite), and leaves any other as it is', () => {
    model.set(withMetaSites('alice:90_1|91_1'));
    const plain = TestBed.inject(CardViews).create({ ref: null, blueprintId: '1_7', zone: 'SPECIAL' });
    expect(views.asMetaSite(plain)).toBe(plain);
    const named = views.asMetaSite(
      TestBed.inject(CardViews).create({ ref: null, blueprintId: '91_1', zone: 'SPECIAL' }),
    );
    expect(named.imageUrl).toBe(images.imageUrl('90_1'));
    expect(named.overlayImage).toBe(images.imageUrl('91_1'));
  });

  it('shows a meta-site large as it is drawn: the visual card with the overlay (asDrawn)', () => {
    model.set(withMetaSites('alice:90_1|91_1'));
    const view = views.of(card('1', '91_1'));
    const data = asDrawn(cardDisplayData(images.resolve(view.blueprintId, view.zone), view.overlayImage), view);
    expect(data.image).toBe(images.imageUrl('90_1'));
    expect(data.overlayImage).toBe(images.imageUrl('91_1'));
    // Any other card keeps its own picture
    const plain = views.of(card('2', '1_7'));
    const plainData = cardDisplayData(images.resolve('1_7', 'SUPPORT'), null);
    expect(asDrawn(plainData, plain)).toBe(plainData);
  });
});
