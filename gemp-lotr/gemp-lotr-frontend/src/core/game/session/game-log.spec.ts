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

  it("starts again with the game's earlier messages in front after a reload, keeping the chat", () => {
    log.game([{ kind: 'message', text: 'latest' }]);
    chat.join('Game7');
    room().flush('<chat><message from="bob">hi</message></chat>');
    log.restart([
      { kind: 'message', text: 'earlier' },
      { kind: 'message', text: 'latest' },
    ]);
    expect(kinds()).toEqual(['message:earlier', 'message:latest', 'chat:hi']);
  });

  it("writes the room's problem and other warnings as warnings", () => {
    chat.join('Game7');
    room().flush('', { status: 403, statusText: 'x' });
    log.warn(['Lost']);
    expect(kinds()).toEqual(['warning:You have no permission to participate in this chat.', 'warning:Lost']);
  });

  it("keeps the server's thanks at the end of a game, wherever it was said", () => {
    expect(log.feedback()).toBeNull();
    log.game([{ kind: 'message', text: 'Thank you for playing! <a>form</a>' }]);
    expect(log.feedback()).toBe('Thank you for playing! <a>form</a>');
    log.feedback.set(null);
    chat.join('Game7');
    room().flush('<chat><message from="System">Thank you for playing! again</message></chat>');
    expect(log.feedback()).toBe('Thank you for playing! again');
  });
});
