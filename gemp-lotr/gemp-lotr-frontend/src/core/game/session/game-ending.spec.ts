import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideFakeNotifier } from '../../../testing/fake-notifier';
import { draftGameXml } from '../../../testing/authoring';
import { provideGameState } from '../game-state';
import { GameEnding } from './game-ending';
import { GameSession } from './game-session.service';

const GAME = '/gemp-lotr-server/game/7';
// (a decision waits in both: the page asks for news once after conceding over it)
const ORDINARY =
  '<gameState cn="1"><ge type="P" participantId="alice" allParticipantIds="alice,bob" discardPublic="false"/>' +
  '<ge type="D" id="3" decisionType="MULTIPLE_CHOICE" participantId="alice" text="Go on?">' +
  '<parameter name="results" value="Yes"/><parameter name="results" value="No"/></ge></gameState>';
const ENDED = '<update cn="2"><ge type="EG"/></update>';

describe('GameEnding', () => {
  let ending: GameEnding;
  let session: GameSession;
  let backend: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        provideFakeNotifier(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    ending = TestBed.inject(GameEnding);
    session = TestBed.inject(GameSession);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.match(() => true));

  /** Opens game 7 as the server has it, then has the game end. */
  function playToTheEnd(game: string): void {
    session.open('7');
    backend.expectOne((r) => r.url === GAME && r.method === 'GET').flush(game);
    TestBed.tick();
    expect(ending.ended()).toBe(false);
    expect(ending.dialogOpen()).toBe(false);
    session.concede();
    backend.expectOne((r) => r.url === `${GAME}/concede`).flush('<ok/>');
    backend.expectOne((r) => r.url === GAME && r.method === 'POST').flush(ENDED);
    TestBed.tick();
    expect(ending.ended()).toBe(true);
  }

  it("shows an ordinary game's end as it ends, until it is closed", () => {
    playToTheEnd(ORDINARY);
    expect(ending.dialogOpen()).toBe(true);
    ending.close();
    expect(ending.dialogOpen()).toBe(false);
  });

  it("leaves a scripted game's end to the scenario's own panel, and shows the game's when asked", () => {
    playToTheEnd(draftGameXml({ author: false }));
    expect(session.model().scenario).not.toBeNull();
    expect(ending.dialogOpen()).toBe(false);
    ending.show();
    expect(ending.dialogOpen()).toBe(true);
  });

  it("finds the person's replay for a page flipped to the other seat (the scenario's human, not the page's player)", () => {
    const info = JSON.stringify({ type: 'info', id: 'sandbox', kind: 'sandbox', human: 'alice', seat: 'shadow' });
    const flipped =
      '<gameState cn="1"><ge type="P" participantId="bob" allParticipantIds="alice,bob" discardPublic="false"/>' +
      `<ge type="S" scenario="${info.replace(/"/g, '&quot;')}"/>` +
      '<ge type="D" id="3" decisionType="MULTIPLE_CHOICE" participantId="bob" text="Go on?">' +
      '<parameter name="results" value="Yes"/><parameter name="results" value="No"/></ge></gameState>';
    playToTheEnd(flipped);
    ending.showReplay();
    TestBed.tick();
    backend
      .expectOne((r) => r.url === '/gemp-lotr-server/gameHistory')
      .flush('<gameHistory><historyEntry opponent="bob" gameRecordingId="r1"/></gameHistory>');
    expect(ending.replay()).toEqual({ state: 'found', replayId: 'alice$r1' });
  });
});
