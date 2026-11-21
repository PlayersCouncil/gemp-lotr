import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { TestBed } from '@angular/core/testing';
import { provideGempHttp } from '../../api/gemp-interceptors';
import { ChatRoom } from '../../chat/chat-room.service';
import { PAGE_QUERY_STRING } from '../../platform/page-location';
import { provideGameState } from '../game-state';
import { GameLog } from './game-log';

describe('GameLog', () => {
  let log: GameLog;
  let chat: ChatRoom;
  let backend: HttpTestingController;

  beforeEach(() => {
    sessionStorage.clear();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    log = TestBed.inject(GameLog);
    chat = TestBed.inject(ChatRoom);
    backend = TestBed.inject(HttpTestingController);
  });

  afterEach(() => backend.match(() => true));

  const room = () => backend.expectOne((r) => r.url === '/gemp-lotr-server/chat/Game7');

  const kinds = () =>
    log
      .entries()
      .map((entry) => (entry.kind === 'chat' ? `chat:${entry.message.html}` : `${entry.kind}:${entry.html}`));

  it("lists the game's lines and the room's in the order they are handed over", () => {
    log.restart('7', []);
    log.game([{ kind: 'message', text: 'one' }]);
    chat.join('Game7');
    room().flush('<chat><message from="bob">hi</message></chat>');
    log.game([
      { kind: 'warning', text: 'two' },
      { kind: 'message', text: 'three' },
    ]);
    log.game([]);
    expect(kinds()).toEqual(['message:one', 'chat:hi', 'warning:two', 'message:three']);
  });

  it("puts the game's messages so far before the room's lines when the game opens", () => {
    chat.join('Game7');
    room().flush('<chat><message from="bob">hi</message></chat>');
    log.restart('7', [{ kind: 'message', text: 'earlier' }]);
    expect(kinds()).toEqual(['message:earlier', 'chat:hi']);
  });

  it('comes back after a reload as it was, without what the room says again on joining', () => {
    log.restart('7', [{ kind: 'message', text: 'one' }]);
    chat.join('Game7');
    room().flush(
      '<chat><message from="System" date="1">Welcome to room: Game7</message><message from="bob" date="2">hi</message></chat>',
    );
    log.game([{ kind: 'message', text: 'two' }]);
    const before = kinds();
    expect(before).toEqual(['message:one', 'chat:Welcome to room: Game7', 'chat:hi', 'message:two']);

    // The page loaded again: a new log, the game's messages (one more since), and the room's greeting again
    TestBed.resetTestingModule();
    TestBed.configureTestingModule({
      providers: [
        provideGempHttp(),
        provideHttpClientTesting(),
        { provide: PAGE_QUERY_STRING, useValue: '' },
        provideGameState(),
      ],
    });
    log = TestBed.inject(GameLog);
    chat = TestBed.inject(ChatRoom);
    backend = TestBed.inject(HttpTestingController);
    log.restart('7', [
      { kind: 'message', text: 'one' },
      { kind: 'message', text: 'two' },
      { kind: 'message', text: 'three' },
    ]);
    chat.join('Game7');
    room().flush(
      '<chat><message from="System" date="9">Welcome to room: Game7</message><message from="bob" date="2">hi</message>' +
        '<message from="bob" date="10">back?</message></chat>',
    );
    expect(kinds()).toEqual([...before, 'message:three', 'chat:back?']);
  });

  it("writes the room's problem and other warnings as warnings", () => {
    chat.join('Game7');
    room().flush('', { status: 403, statusText: 'x' });
    log.warn(['Lost']);
    expect(kinds()).toEqual(['warning:You have no permission to participate in this chat.', 'warning:Lost']);
  });

  it("keeps the server's thanks at the end of a game, wherever it was said", () => {
    expect(log.feedback()).toBeNull();
    log.restart('7', []);
    log.game([{ kind: 'message', text: 'Thank you for playing! <a>form</a>' }]);
    expect(log.feedback()).toBe('Thank you for playing! <a>form</a>');
    log.feedback.set(null);
    chat.join('Game7');
    room().flush('<chat><message from="System">Thank you for playing! again</message></chat>');
    expect(log.feedback()).toBe('Thank you for playing! again');
  });

  it("puts a scripted game's message in the opponent's mouth: one line per message, said again with the newer words", () => {
    log.restart('7', []);
    log.say('scenario 1.0:0', 'Basic Shadow', '<p>Welcome</p>');
    log.game([{ kind: 'message', text: 'played' }]);
    log.say('scenario 1.0:1', 'Basic Shadow', '<p>Next</p>');
    log.say('scenario 1.0:0', 'Basic Shadow', '<p>Welcome back</p>');
    expect(kinds()).toEqual(['chat:<p>Welcome back</p>', 'message:played', 'chat:<p>Next</p>']);
    expect(log.entries()[0]).toEqual({
      kind: 'chat',
      message: { from: 'Basic Shadow', html: '<p>Welcome back</p>', date: null, system: false },
      said: 'scenario 1.0:0',
    });
    // Kept with the game for a reload, the newer words too
    expect(JSON.stringify(sessionStorage.getItem('gemp.game.7.log'))).toContain('Welcome back');
  });

  it('takes no game lines before the game (or a replay) has come', () => {
    log.game([{ kind: 'message', text: 'too early' }]);
    expect(kinds()).toEqual([]);
    log.restart(null, []);
    log.game([{ kind: 'message', text: 'in the replay' }]);
    expect(kinds()).toEqual(['message:in the replay']);
  });
});
